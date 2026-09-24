package com.example.data.model

data class ProjectStage(
    val stageNumber: Int,
    val title: String,
    val description: String,
    val actionItems: List<String>,
    val codeSnippet: String? = null,
    val checkpointQuestion: String? = null,
    val isCompleted: Boolean = false
)

data class AiProjectGuide(
    val id: String,
    val title: String,
    val subtitle: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val estimatedHours: String,
    val targetCategory: String, // "AI & Chatbots", "Python & Data", "Web Development", "AI Agents & RAG", "Mobile & Tools"
    val iconEmoji: String,
    val overview: String,
    val prerequisites: List<String>,
    val conceptsToLearn: List<String>,
    val stages: List<ProjectStage>,
    val starterCodeTemplate: String,
    val finalSolutionSnippet: String,
    val readmeTemplate: String
)
