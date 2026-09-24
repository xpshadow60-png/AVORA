package com.example.data.model

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val questionType: String = "MULTIPLE_CHOICE", // MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    val topic: String = ""
)

data class GeneratedQuiz(
    val subject: String,
    val topic: String,
    val difficulty: String,
    val questions: List<QuizQuestion>
)

data class DocumentAnalysisResult(
    val summary: String,
    val keyPoints: List<String>,
    val definitions: List<Pair<String, String>>, // Term to definition
    val formulasOrFacts: List<String>,
    val sampleQuestions: List<String>,
    val generatedFlashcards: List<Pair<String, String>> // Question to Answer
)

data class HomeworkSolution(
    val subject: String,
    val understoodQuestion: String,
    val whatIsBeingAsked: String,
    val hint: String,
    val steps: List<String>,
    val finalAnswer: String,
    val whyCorrect: String,
    val similarPracticeQuestion: String
)

data class CodeExplanationResult(
    val language: String,
    val summary: String,
    val bugsOrIssues: List<String>,
    val stepByStepExplanation: String,
    val improvedCode: String,
    val practiceChallenge: String
)

data class StudyPlanRequest(
    val subjects: List<String>,
    val topics: String,
    val dailyHours: Double,
    val studyDays: List<String>,
    val goal: String,
    val examDate: String
)

data class GeneratedStudyTask(
    val title: String,
    val subject: String,
    val topic: String,
    val estimatedMinutes: Int,
    val taskType: String, // "Concept Review", "Practice Problems", "Flashcards Review", "Exam Prep", "Coding Lab"
    val priority: String, // "HIGH", "MEDIUM", "LOW"
    val dayOfWeek: String
)
