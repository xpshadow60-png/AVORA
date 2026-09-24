package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TaskPriority(val level: String, val weight: Int) {
    URGENT("URGENT", 4),
    HIGH("HIGH", 3),
    MEDIUM("MEDIUM", 2),
    LOW("LOW", 1);

    companion object {
        fun fromString(value: String): TaskPriority {
            return entries.find { it.level.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["userId"])]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "guest_user",
    val title: String,
    val subject: String,
    val priority: String = "HIGH", // "URGENT", "HIGH", "MEDIUM", "LOW"
    val dueDate: Long = System.currentTimeMillis(), // timestamp in millis
    val estimatedMinutes: Int = 30,
    val isCompleted: Boolean = false,
    val category: String = "Assignment", // "Assignment", "Exam Prep", "Homework", "Project", "Reading"
    val notes: String = "",
    val subtasksJson: String = "[]", // JSON array of string subtasks
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

