package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "flashcards",
    indices = [Index(value = ["userId"])]
)
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "guest_user",
    val question: String,
    val answer: String,
    val subject: String,
    val topic: String = "General",
    val intervalDays: Int = 1,
    val easeFactor: Double = 2.5,
    val repetitions: Int = 0,
    val nextReviewAt: Long = System.currentTimeMillis(),
    val lastReviewedAt: Long? = null,
    val totalReviews: Int = 0,
    val successfulReviews: Int = 0,
    val lastRating: String = "NEW", // "NEW", "AGAIN", "HARD", "GOOD", "EASY"
    val createdAt: Long = System.currentTimeMillis()
)
