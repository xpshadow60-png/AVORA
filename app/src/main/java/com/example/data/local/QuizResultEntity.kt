package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_results",
    indices = [Index(value = ["userId"])]
)
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String = "guest_user",
    val subject: String,
    val topic: String,
    val difficulty: String, // Easy, Medium, Hard
    val score: Int, // e.g. 4
    val totalQuestions: Int, // e.g. 5
    val percentage: Int, // e.g. 80
    val weakTopicsJson: String = "", // Comma separated or JSON of topics missed
    val completedAt: Long = System.currentTimeMillis()
)
