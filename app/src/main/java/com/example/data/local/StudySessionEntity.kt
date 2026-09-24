package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "study_sessions",
    indices = [Index(value = ["userId"])]
)
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "guest_user",
    val subject: String,
    val durationMinutes: Int,
    val completedAt: Long = System.currentTimeMillis(),
    val focusScore: Int = 100, // percentage 0..100
    val distractionsBlockedCount: Int = 0
)
