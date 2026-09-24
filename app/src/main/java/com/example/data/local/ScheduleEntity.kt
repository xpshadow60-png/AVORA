package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_blocks",
    indices = [Index(value = ["userId"])]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String = "guest_user",
    val title: String,
    val subject: String,
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:30"
    val dayOfWeek: String, // "Monday", "Tuesday", etc.
    val isStudyBlock: Boolean = true,
    val colorHex: String = "#6C5CE7"
)
