package com.example.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import java.security.MessageDigest
import java.util.UUID

sealed class GoogleAuthResult {
    data class Success(val idToken: String, val email: String?, val displayName: String?, val photoUrl: String?) : GoogleAuthResult()
    data object Cancelled : GoogleAuthResult()
    data class Error(val message: String) : GoogleAuthResult()
}

/**
 * Official Google Sign-In Helper powered by Jetpack CredentialManager and Google Identity.
 * Launches the native Google account selector bottom sheet, securely fetches the verified
 * Google ID Token, and returns the identity for Firebase Authentication.
 */
class GoogleSignInHelper(private val context: Context) {

    companion object {
        private const val TAG = "GoogleSignInHelper"
    }

    private val credentialManager = CredentialManager.create(context)

    private fun getWebClientId(): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) {
            context.getString(resId)
        } else {
            // Fallback to standard cloud project OAuth client ID or placeholder
            "593778062268-apps.googleusercontent.com"
        }
    }

    suspend fun startGoogleSignIn(activityContext: Context): GoogleAuthResult {
        return try {
            val serverClientId = getWebClientId()
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                GoogleAuthResult.Success(
                    idToken = googleIdTokenCredential.idToken,
                    email = googleIdTokenCredential.id,
                    displayName = googleIdTokenCredential.displayName,
                    photoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                )
            } else {
                GoogleAuthResult.Error("Unexpected credential type returned.")
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "Google Sign-In cancelled by user.")
            GoogleAuthResult.Cancelled
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Google Sign-In CredentialManager exception: ${e.message}")
            GoogleAuthResult.Error(mapCredentialError(e))
        } catch (e: Exception) {
            Log.e(TAG, "General Google Sign-In error: ${e.localizedMessage}", e)
            GoogleAuthResult.Error(e.localizedMessage ?: "Failed to sign in with Google.")
        }
    }

    private fun mapCredentialError(e: GetCredentialException): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("no credential", ignoreCase = true) || msg.contains("16", ignoreCase = true) ->
                "No Google accounts found or sign-in was dismissed."
            msg.contains("network", ignoreCase = true) || msg.contains("7", ignoreCase = true) ->
                "Network error during Google Sign-In. Please check your connection."
            else -> "Google sign-in could not be completed: ${e.localizedMessage}"
        }
    }
}
