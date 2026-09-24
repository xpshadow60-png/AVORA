package com.example.data.auth

import android.content.Context
import android.util.Log
import com.example.data.model.UserProfile
import com.example.data.sync.CloudSyncManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class AuthResult {
    data class Success(val user: UserProfile) : AuthResult()
    data class Error(val message: String, val code: String? = null) : AuthResult()
    data class PasswordResetSent(val email: String) : AuthResult()
    data class VerificationSent(val message: String) : AuthResult()
}

/**
 * Production Authentication & Identity Engine for Avora.
 * Powered by Firebase Authentication and Cloud Firestore.
 * Passwords are encrypted and handled exclusively by Firebase Authentication servers.
 * No passwords, hashes, or plain text credentials are ever stored locally.
 */
class AuthManager(
    private val context: Context,
    private val cloudSyncManager: CloudSyncManager = CloudSyncManager()
) {
    private val firebaseAuth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth is not initialized: ${e.message}")
            null
        }

    companion object {
        private const val TAG = "AuthManager"
    }

    val googleSignInHelper = GoogleSignInHelper(context)

    /**
     * Get the currently authenticated Firebase user as an Avora UserProfile
     */
    fun getCurrentUser(): UserProfile? {
        val auth = firebaseAuth ?: return null
        val firebaseUser = auth.currentUser ?: return null
        return mapFirebaseUser(firebaseUser)
    }

    /**
     * Check if a valid session currently exists
     */
    fun isUserSignedIn(): Boolean = firebaseAuth?.currentUser != null

    /**
     * Real-time stream of Firebase Authentication state
     */
    fun observeAuthState(): Flow<UserProfile?> = callbackFlow {
        val auth = firebaseAuth
        if (auth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { fa ->
            val user = fa.currentUser?.let { mapFirebaseUser(it) }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Sign in an existing user with Email and Password using Firebase Auth
     */
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Email and password cannot be empty.")
        }
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")

        try {
            val authResult = auth.signInWithEmailAndPassword(cleanEmail, password).await()
            val firebaseUser = authResult.user ?: return@withContext AuthResult.Error("Failed to obtain authenticated session.")

            // Fetch stored profile from Firestore or construct from FirebaseUser
            var profile = cloudSyncManager.fetchUserProfile(firebaseUser.uid)
            if (profile == null) {
                profile = mapFirebaseUser(firebaseUser)
                cloudSyncManager.saveUserProfile(profile)
            } else {
                // Update email verification status if changed
                if (profile.isEmailVerified != firebaseUser.isEmailVerified) {
                    profile = profile.copy(isEmailVerified = firebaseUser.isEmailVerified)
                    cloudSyncManager.saveUserProfile(profile)
                }
            }

            AuthResult.Success(profile)
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email address. Please create an account.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Incorrect password or invalid email format. Please check your credentials.")
        } catch (e: FirebaseNetworkException) {
            AuthResult.Error("Network unavailable. Please check your internet connection and try again.")
        } catch (e: Exception) {
            Log.e(TAG, "Sign in error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Authentication failed. Please try again.")
        }
    }

    /**
     * Create a new user account with Email and Password using Firebase Auth
     */
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        major: String = "Computer Science",
        classLevel: String = "Undergraduate",
        learningPreferences: String = "Visual & Interactive Code Labs"
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        val cleanName = name.trim().ifBlank { "Future Leader" }

        if (cleanEmail.isBlank() || password.isBlank()) {
            return@withContext AuthResult.Error("Please enter both email and password.")
        }

        if (password.length < 6) {
            return@withContext AuthResult.Error("Password must be at least 6 characters.")
        }

        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")

        try {
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, password).await()
            val firebaseUser = authResult.user ?: return@withContext AuthResult.Error("Account creation failed.")

            // Set display name in Firebase Auth
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            // Send email verification link
            try {
                firebaseUser.sendEmailVerification().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to send initial verification email: ${e.localizedMessage}")
            }

            val userProfile = UserProfile(
                id = firebaseUser.uid,
                name = cleanName,
                email = cleanEmail,
                photoUrl = firebaseUser.photoUrl?.toString(),
                majorOrField = major.ifBlank { "Computer Science" },
                classLevel = classLevel.ifBlank { "Undergraduate" },
                learningPreferences = learningPreferences,
                isEmailVerified = firebaseUser.isEmailVerified,
                isGuest = false,
                memberSince = System.currentTimeMillis()
            )

            // Save to Cloud Firestore
            cloudSyncManager.saveUserProfile(userProfile)

            AuthResult.Success(userProfile)
        } catch (e: FirebaseAuthUserCollisionException) {
            AuthResult.Error("An account with $cleanEmail already exists. Please sign in or use Google.")
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Password is too weak. Please use at least 6 characters including numbers and letters.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Invalid email address format. Please enter a valid email.")
        } catch (e: FirebaseNetworkException) {
            AuthResult.Error("Network unavailable. Please check your internet connection.")
        } catch (e: Exception) {
            Log.e(TAG, "Sign up error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to create account.")
        }
    }

    /**
     * Sign in or register with verified Google ID Token from Credential Manager
     */
    suspend fun signInWithGoogleCredential(idToken: String): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return@withContext AuthResult.Error("Google Sign-In failed.")

            var profile = cloudSyncManager.fetchUserProfile(firebaseUser.uid)
            if (profile == null) {
                val displayName = firebaseUser.displayName?.ifBlank { "Future Leader" } ?: "Future Leader"
                profile = UserProfile(
                    id = firebaseUser.uid,
                    name = displayName,
                    email = firebaseUser.email ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    majorOrField = "Computer Science",
                    classLevel = "Undergraduate",
                    learningPreferences = "Visual & Interactive Code Labs",
                    isEmailVerified = firebaseUser.isEmailVerified,
                    isGuest = false,
                    memberSince = System.currentTimeMillis()
                )
                cloudSyncManager.saveUserProfile(profile)
            } else {
                // Update photo and email if updated in Google account
                val updated = profile.copy(
                    photoUrl = firebaseUser.photoUrl?.toString() ?: profile.photoUrl,
                    isEmailVerified = firebaseUser.isEmailVerified
                )
                cloudSyncManager.saveUserProfile(updated)
                profile = updated
            }

            AuthResult.Success(profile)
        } catch (e: FirebaseNetworkException) {
            AuthResult.Error("Network unavailable. Please check your connection.")
        } catch (e: Exception) {
            Log.e(TAG, "Google auth sign in error: ${e.localizedMessage}", e)
            AuthResult.Error("Google authentication failed: ${e.localizedMessage}")
        }
    }

    /**
     * Send real password reset email via Firebase Auth
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            return@withContext AuthResult.Error("Please enter your registered email address.")
        }
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")

        try {
            auth.sendPasswordResetEmail(cleanEmail).await()
            AuthResult.PasswordResetSent(cleanEmail)
        } catch (e: FirebaseAuthInvalidUserException) {
            AuthResult.Error("No account found with this email address.")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            AuthResult.Error("Please enter a valid email address.")
        } catch (e: FirebaseNetworkException) {
            AuthResult.Error("Network error. Please check your internet connection.")
        } catch (e: Exception) {
            Log.e(TAG, "Password reset error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to send password reset email.")
        }
    }

    /**
     * Change password for the currently logged in user
     */
    suspend fun updatePassword(newPassword: String): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")
        val currentUser = auth.currentUser ?: return@withContext AuthResult.Error("No authenticated session.")
        if (newPassword.length < 6) {
            return@withContext AuthResult.Error("New password must be at least 6 characters.")
        }

        try {
            currentUser.updatePassword(newPassword).await()
            AuthResult.Success(mapFirebaseUser(currentUser))
        } catch (e: FirebaseAuthRecentLoginRequiredException) {
            AuthResult.Error("For security reasons, please log out and log in again before changing your password.")
        } catch (e: FirebaseAuthWeakPasswordException) {
            AuthResult.Error("Password is too weak. Please use a stronger password.")
        } catch (e: Exception) {
            Log.e(TAG, "Update password error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to update password.")
        }
    }

    /**
     * Send email verification link to current user's email
     */
    suspend fun sendEmailVerification(): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")
        val currentUser = auth.currentUser ?: return@withContext AuthResult.Error("No active user.")
        try {
            currentUser.sendEmailVerification().await()
            AuthResult.VerificationSent("Verification email sent to ${currentUser.email}. Please check your inbox.")
        } catch (e: Exception) {
            Log.e(TAG, "Send email verification error: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to send verification email.")
        }
    }

    /**
     * Reload user profile and refresh email verification state
     */
    suspend fun reloadUser(): UserProfile? = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext null
        val user = auth.currentUser ?: return@withContext null
        try {
            user.reload().await()
            val profile = cloudSyncManager.fetchUserProfile(user.uid) ?: mapFirebaseUser(user)
            val updated = profile.copy(isEmailVerified = user.isEmailVerified)
            cloudSyncManager.saveUserProfile(updated)
            updated
        } catch (e: Exception) {
            Log.w(TAG, "Failed to reload user: ${e.localizedMessage}")
            mapFirebaseUser(user)
        }
    }

    /**
     * Update user profile information in Firestore
     */
    suspend fun updateProfile(
        name: String,
        major: String,
        classLevel: String = "Undergraduate",
        learningPreferences: String = "Visual & Interactive Code Labs"
    ): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")
        val user = auth.currentUser ?: return@withContext AuthResult.Error("No active session.")
        try {
            val cleanName = name.trim().ifBlank { "Future Leader" }
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
            user.updateProfile(profileUpdates).await()

            val existing = cloudSyncManager.fetchUserProfile(user.uid) ?: mapFirebaseUser(user)
            val updated = existing.copy(
                name = cleanName,
                majorOrField = major.ifBlank { "Computer Science" },
                classLevel = classLevel,
                learningPreferences = learningPreferences,
                isEmailVerified = user.isEmailVerified
            )
            cloudSyncManager.saveUserProfile(updated)
            AuthResult.Success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update profile: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to update profile.")
        }
    }

    /**
     * Delete user account permanently from Firebase Auth
     */
    suspend fun deleteAccount(): AuthResult = withContext(Dispatchers.IO) {
        val auth = firebaseAuth ?: return@withContext AuthResult.Error("Authentication service unavailable.")
        val user = auth.currentUser ?: return@withContext AuthResult.Error("No active user session to delete.")
        try {
            user.delete().await()
            AuthResult.Success(UserProfile(id = "", name = "", email = "", isGuest = true))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete Firebase account: ${e.localizedMessage}", e)
            AuthResult.Error(e.localizedMessage ?: "Failed to delete remote account.")
        }
    }

    /**
     * Fetch the current Firebase Auth ID token for authenticating backend requests.
     */
    suspend fun getIdToken(forceRefresh: Boolean = false): String? = withContext(Dispatchers.IO) {
        try {
            firebaseAuth?.currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get Firebase Auth ID token: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Sign out the user and clear Firebase Auth token session
     */
    fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error during signOut: ${e.localizedMessage}")
        }
    }

    private fun mapFirebaseUser(firebaseUser: FirebaseUser): UserProfile {
        val name = firebaseUser.displayName?.ifBlank { "Future Leader" } ?: "Future Leader"
        return UserProfile(
            id = firebaseUser.uid,
            name = name,
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            majorOrField = "Computer Science",
            classLevel = "Undergraduate",
            learningPreferences = "Visual & Interactive Code Labs",
            isEmailVerified = firebaseUser.isEmailVerified,
            isGuest = false,
            memberSince = firebaseUser.metadata?.creationTimestamp ?: System.currentTimeMillis()
        )
    }
}
