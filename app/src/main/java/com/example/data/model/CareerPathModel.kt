package com.example.data.model

data class CareerOption(
    val id: String,
    val title: String,
    val description: String = "",
    val iconEmoji: String = "🎯"
)

data class CareerAssessmentQuestion(
    val id: Int,
    val questionNumber: Int,
    val title: String,
    val subtitle: String,
    val options: List<CareerOption>
)

data class CareerAssessmentResult(
    val fieldOfStudy: String = "Medicine & Healthcare",
    val specialization: String = "Pre-Med & General Medicine",
    val academicLevel: String = "Undergraduate",
    val weeklyStudyHours: String = "15-25 Hours/week",
    val learningStyle: String = "Visual & Video Lectures",
    val primaryChallenge: String = "Time Management & Heavy Workload",
    val timelineGoal: String = "2-4 Years (Degree / Board Exams)",
    val certificationGoal: String = "Medical Board Certification (USMLE / MCAT)",
    val resourcePreference: String = "Mix of Open-Source & Top MOOCs",
    val dailyStudyHabit: String = "Pomodoro Focus Sprints",
    val completedAt: Long = System.currentTimeMillis()
)

data class RoadmapMilestone(
    val id: String,
    val title: String,
    val description: String,
    val isCompleted: Boolean = false
)

data class CareerRoadmapPhase(
    val phaseNumber: Int,
    val title: String,
    val duration: String,
    val description: String,
    val milestones: List<RoadmapMilestone>,
    val coreCompetencies: List<String>
)

data class VideoRecommendation(
    val id: String,
    val title: String,
    val channel: String,
    val topic: String,
    val duration: String,
    val searchQuery: String,
    val youtubeUrl: String
)

data class OnlineClassRecommendation(
    val id: String,
    val title: String,
    val platform: String, // Coursera, edX, MIT OCW, Harvard Online, Khan Academy
    val institution: String,
    val level: String, // Beginner, Intermediate, Advanced
    val rating: Double,
    val courseUrl: String,
    val isFree: Boolean = true
)

data class FullCareerPath(
    val assessment: CareerAssessmentResult,
    val overallSummary: String,
    val phases: List<CareerRoadmapPhase>,
    val videoSuggestions: List<VideoRecommendation>,
    val classSuggestions: List<OnlineClassRecommendation>,
    val keySkillsToMaster: List<String>
)
