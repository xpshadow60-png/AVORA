package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// --- Backend Request & Response Models ---

data class BackendChatMessage(
    val role: String, // "user", "model", "system"
    val content: String
)

data class BackendAiRequest(
    val prompt: String,
    val history: List<BackendChatMessage> = emptyList(),
    val systemPrompt: String? = null,
    val taskType: String = "GENERAL_REASONING", // "PROBLEM_SOLVER", "CODING_MENTOR", "QUIZ_GEN", "STUDY_PLANNER", "PRODUCTIVITY"
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val clientAppVersion: String = "1.0",
    val platform: String = "android",
    val imageBase64: String? = null,
    val imageMimeType: String? = "image/jpeg"
)

data class BackendAiResponse(
    val success: Boolean,
    val reply: String?,
    val modelUsed: String? = "gemini-2.5-flash",
    val tokensUsed: Int? = null,
    val errorMessage: String? = null
)

/**
 * Backend API interface for Avora AI Proxy Gateway.
 * All client requests route to the secure backend server rather than exposing API keys directly in the APK.
 */
interface AvoraBackendAiApi {
    @POST("api/ai/generate")
    suspend fun generateAiCompletion(
        @Header("Authorization") authToken: String?,
        @Header("X-Avora-Client-Version") clientVersion: String = "1.0",
        @Body request: BackendAiRequest
    ): BackendAiResponse
}

/**
 * AvoraBackendClient routes all AI requests through the secure Avora backend gateway.
 * The backend securely manages authentication, rate limiting, system instructions, and LLM model endpoints.
 * No Gemini API keys are bundled or shipped inside the client APK.
 */
object AvoraBackendClient {

    private const val DEFAULT_BACKEND_URL = "https://api.avora.tech/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("User-Agent", "Avora-Android-App/1.0")
                .header("Accept", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backendRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(DEFAULT_BACKEND_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val backendApi: AvoraBackendAiApi by lazy {
        backendRetrofit.create(AvoraBackendAiApi::class.java)
    }

    /**
     * Sends prompt through the secure Avora backend AI gateway.
     * Evaluates rate limiting, token size, prompt injection guards, and abuse protection
     * before dispatching to the secure backend server.
     */
    suspend fun getAiResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(), // Pair(sender, text)
        systemInstructionText: String? = null,
        taskType: String = "GENERAL_REASONING",
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user"),
        imageBase64: String? = null,
        imageMimeType: String? = "image/jpeg"
    ): Result<String> {
        // Pre-flight security & rate limit check
        when (val safety = AiSecurityEngine.inspectRequest(prompt, userId, isGuest)) {
            is AiSafetyResult.RateLimited -> {
                return Result.failure(Exception(safety.message))
            }
            is AiSafetyResult.Blocked -> {
                return Result.failure(Exception("🛡️ Request Blocked: ${safety.reason}"))
            }
            is AiSafetyResult.Allowed -> {
                // Proceed with AI generation via backend
            }
        }

        val historyList = conversationHistory.map { (sender, text) ->
            BackendChatMessage(
                role = if (sender.lowercase() == "user") "user" else "model",
                content = text
            )
        }

        val backendRequest = BackendAiRequest(
            prompt = prompt,
            history = historyList,
            systemPrompt = systemInstructionText,
            taskType = taskType,
            temperature = 0.7f,
            imageBase64 = imageBase64,
            imageMimeType = imageMimeType
        )

        // Execute via Avora Backend Proxy Gateway
        return try {
            val response = backendApi.generateAiCompletion(
                authToken = authToken?.let { "Bearer $it" },
                request = backendRequest
            )
            if (response.success && !response.reply.isNullOrBlank()) {
                Result.success(response.reply)
            } else if (!response.errorMessage.isNullOrBlank()) {
                Result.failure(Exception(response.errorMessage))
            } else {
                Result.failure(Exception("Empty response from Avora AI service."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Avora AI service unavailable: ${e.localizedMessage ?: "Connection error"}"))
        }
    }
}

/**
 * Backward compatibility alias for GeminiClient pointing to the secure AvoraBackendClient.
 */
object GeminiClient {
    suspend fun getAiResponse(
        prompt: String,
        conversationHistory: List<Pair<String, String>> = emptyList(),
        systemInstructionText: String? = null,
        taskType: String = "GENERAL_REASONING",
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user"),
        imageBase64: String? = null,
        imageMimeType: String? = "image/jpeg"
    ): Result<String> {
        return AvoraBackendClient.getAiResponse(
            prompt = prompt,
            conversationHistory = conversationHistory,
            systemInstructionText = systemInstructionText,
            taskType = taskType,
            authToken = authToken,
            userId = userId,
            isGuest = isGuest,
            imageBase64 = imageBase64,
            imageMimeType = imageMimeType
        )
    }
}


