package com.example.data.model

data class TestCase(
    val inputDescription: String,
    val expectedOutput: String,
    val isHidden: Boolean = false
)

data class ExecutionResult(
    val output: String,
    val isSuccess: Boolean,
    val errorMsg: String? = null,
    val executionTimeMs: Long = 0,
    val passedTests: Int = 0,
    val totalTests: Int = 0
)

data class CodingChallenge(
    val id: String,
    val title: String,
    val difficulty: String, // "Beginner", "Intermediate", "Advanced"
    val language: String = "Python",
    val topic: String,
    val promptDescription: String,
    val starterCode: String,
    val testCases: List<TestCase>,
    val hints: List<String>, // Level 1 (Gentle), Level 2 (Specific), Level 3 (Logic)
    val solutionCode: String,
    val explanation: String,
    val isSolved: Boolean = false
)
