package com.example.data.remote

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Result of an AI safety and rate-limiting inspection.
 */
sealed class AiSafetyResult {
    object Allowed : AiSafetyResult()
    data class RateLimited(
        val retryAfterSeconds: Int,
        val limit: Int,
        val remaining: Int,
        val message: String
    ) : AiSafetyResult()
    data class Blocked(
        val reason: String,
        val threatLevel: String = "MEDIUM"
    ) : AiSafetyResult()
}

/**
 * Production-grade Rate Limiting & Abuse Protection Engine for Avora AI Gateway.
 * Protects AI backend endpoints from token exhaustion attacks, prompt injection,
 * high-frequency automated spam, and credential leakage.
 */
object AiSecurityEngine {

    private const val MAX_PROMPT_CHARS = 8000
    private const val MIN_REQUEST_INTERVAL_MS = 1200L // 1.2s debounce between clicks
    private const val WINDOW_DURATION_MS = 60_000L // 1 minute window

    // Limits per tier (Requests per minute)
    private const val GUEST_LIMIT_PER_MINUTE = 15
    private const val REGISTERED_LIMIT_PER_MINUTE = 35

    // Request timestamps per client key (e.g. user ID or session ID)
    private val clientTimestamps = ConcurrentHashMap<String, MutableList<Long>>()
    private val lastRequestTime = ConcurrentHashMap<String, Long>()
    private val lastPromptHash = ConcurrentHashMap<String, Pair<Int, Int>>() // Pair(hash, count)

    // Suspicious pattern detector for prompt injection and secret leaking
    private val MALICIOUS_PATTERNS = listOf(
        Regex("(?i)ignore (all )?previous instructions and (reveal|show|print|output) (the )?(api|secret|system|key)"),
        Regex("(?i)(give me|leak|extract|dump) (the )?(gemini|google|backend|openai) api key"),
        Regex("(?i)what is the (system prompt|internal prompt|backend api key)"),
        Regex("(?i)<script.*?>.*?</script>"),
        Regex("(?i)BEGIN PRIVATE KEY"),
        Regex("(?i)password\\s*=\\s*['\"][^'\"]+['\"]")
    )

    /**
     * Inspects an outgoing AI request before dispatching to the network.
     */
    fun inspectRequest(
        prompt: String,
        userId: String = "guest_session",
        isGuest: Boolean = true
    ): AiSafetyResult {
        val now = System.currentTimeMillis()

        // 1. Length & Payload Validation
        if (prompt.isBlank()) {
            return AiSafetyResult.Blocked("Prompt cannot be empty.", threatLevel = "LOW")
        }
        if (prompt.length > MAX_PROMPT_CHARS) {
            return AiSafetyResult.Blocked(
                "Prompt size exceeds maximum limit of $MAX_PROMPT_CHARS characters (Received ${prompt.length} chars). Please condense your input.",
                threatLevel = "HIGH"
            )
        }

        // 2. Malicious Content & Injection Guard
        for (pattern in MALICIOUS_PATTERNS) {
            if (pattern.containsMatchIn(prompt)) {
                return AiSafetyResult.Blocked(
                    "Security Notice: Prompt contains prohibited instructions or sensitive security keywords.",
                    threatLevel = "CRITICAL"
                )
            }
        }

        // 3. Rapid-fire spam prevention (Debounce)
        val lastTime = lastRequestTime[userId] ?: 0L
        if (now - lastTime < MIN_REQUEST_INTERVAL_MS) {
            val waitSec = (((MIN_REQUEST_INTERVAL_MS - (now - lastTime)) / 1000L) + 1).toInt()
            return AiSafetyResult.RateLimited(
                retryAfterSeconds = waitSec,
                limit = if (isGuest) GUEST_LIMIT_PER_MINUTE else REGISTERED_LIMIT_PER_MINUTE,
                remaining = 0,
                message = "Slow down! Please wait $waitSec second(s) before sending another query."
            )
        }

        // 4. Repeated Prompt Flooding Guard
        val promptHash = prompt.trim().hashCode()
        val prevHashInfo = lastPromptHash[userId]
        if (prevHashInfo != null && prevHashInfo.first == promptHash) {
            val count = prevHashInfo.second + 1
            lastPromptHash[userId] = Pair(promptHash, count)
            if (count > 4) {
                return AiSafetyResult.Blocked(
                    "Identical prompt submitted repeatedly. Please ask a different question or rephrase.",
                    threatLevel = "MEDIUM"
                )
            }
        } else {
            lastPromptHash[userId] = Pair(promptHash, 1)
        }

        // 5. Sliding Window Rate Limiter
        val limit = if (isGuest) GUEST_LIMIT_PER_MINUTE else REGISTERED_LIMIT_PER_MINUTE

        val timestamps = clientTimestamps.getOrPut(userId) { mutableListOf() }
        synchronized(timestamps) {
            // Prune timestamps older than 1 minute
            timestamps.removeAll { now - it > WINDOW_DURATION_MS }

            if (timestamps.size >= limit) {
                val oldestInWindow = timestamps.firstOrNull() ?: now
                val resetInSec = (((WINDOW_DURATION_MS - (now - oldestInWindow)) / 1000L) + 1).toInt().coerceAtLeast(1)
                return AiSafetyResult.RateLimited(
                    retryAfterSeconds = resetInSec,
                    limit = limit,
                    remaining = 0,
                    message = "⏳ AI Rate limit reached ($limit requests/min). Please wait $resetInSec seconds."
                )
            }

            // Record this request
            timestamps.add(now)
            lastRequestTime[userId] = now
        }

        return AiSafetyResult.Allowed
    }

    /**
     * Gets current rate limit metadata for UI indicators.
     */
    fun getRateLimitStatus(userId: String = "guest_session", isGuest: Boolean = true): Triple<Int, Int, Int> {
        val now = System.currentTimeMillis()
        val limit = if (isGuest) GUEST_LIMIT_PER_MINUTE else REGISTERED_LIMIT_PER_MINUTE
        val timestamps = clientTimestamps[userId] ?: mutableListOf()
        val used = synchronized(timestamps) {
            timestamps.count { now - it <= WINDOW_DURATION_MS }
        }
        val remaining = (limit - used).coerceAtLeast(0)
        return Triple(limit, remaining, used)
    }

    /**
     * Reset rate limits (for test suites or explicit session resets).
     */
    fun resetAll() {
        clientTimestamps.clear()
        lastRequestTime.clear()
        lastPromptHash.clear()
    }
}
