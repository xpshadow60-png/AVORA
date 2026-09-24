package com.example.data.model

data class RoadmapCheckpoint(
    val id: String,
    val title: String,
    val description: String,
    val estimatedMinutes: Int = 30,
    val checkpointType: String = "LESSON", // "LESSON", "CODE_CHALLENGE", "PROJECT_STEP", "QUIZ"
    val isCompleted: Boolean = false
)

data class LearningTrackModule(
    val id: String,
    val moduleNumber: Int,
    val title: String,
    val summary: String,
    val topics: List<String>,
    val checkpoints: List<RoadmapCheckpoint> = emptyList(),
    val practiceGoal: String,
    val projectMilestone: String,
    val isCompleted: Boolean = false
)

data class LearningTrack(
    val id: String,
    val title: String,
    val targetRole: String,
    val iconEmoji: String,
    val description: String,
    val difficultyLevel: String, // "Beginner to Pro", "Intermediate", "Advanced"
    val estimatedWeeks: Int,
    val isIndiaFocused: Boolean = true,
    val keySkills: List<String>,
    val modules: List<LearningTrackModule>
)
