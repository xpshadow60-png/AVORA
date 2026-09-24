package com.example.data.model

data class UserProfile(
    val id: String, // Unique Auth Provider UID (e.g. Firebase Auth UID)
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val majorOrField: String = "Computer Science",
    val classLevel: String = "Undergraduate",
    val learningPreferences: String = "Visual & Interactive Code Labs",
    val isEmailVerified: Boolean = false,
    val avatarInitials: String = if (name.isNotBlank()) {
        val parts = name.trim().split("\\s+".toRegex())
        if (parts.size >= 2) {
            "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
        } else {
            name.take(2).uppercase()
        }
    } else "FL",
    val isGuest: Boolean = false,
    val memberSince: Long = System.currentTimeMillis()
)
