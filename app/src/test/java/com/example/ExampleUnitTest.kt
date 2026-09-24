package com.example

import com.example.data.career.CareerPathEngine
import com.example.data.local.FlashcardEntity
import com.example.data.local.ScheduleEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.data.model.CareerAssessmentResult
import com.example.data.model.UserProfile
import com.example.data.srs.ReviewRating
import com.example.data.srs.SpacedRepetitionEngine
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCareerAssessmentQuestions_exactlyTenQuestions() {
        val questions = CareerPathEngine.assessmentQuestions
        assertEquals(10, questions.size)
        questions.forEachIndexed { index, question ->
            assertEquals(index + 1, question.questionNumber)
            assertTrue(question.title.isNotBlank())
            assertTrue(question.options.isNotEmpty())
        }
    }

    @Test
    fun testCareerPathEngine_medicalFieldGeneration() {
        val assessment = CareerAssessmentResult(
            fieldOfStudy = "Medicine & Healthcare",
            specialization = "Physician / Specialist"
        )
        val path = CareerPathEngine.generateCareerPath(assessment)

        assertEquals(4, path.phases.size)
        assertTrue(path.videoSuggestions.isNotEmpty())
        assertTrue(path.classSuggestions.isNotEmpty())
        assertTrue(path.videoSuggestions.any { it.channel.contains("Ninja Nerd") || it.channel.contains("AnatomyZone") })
        assertTrue(path.classSuggestions.any { it.institution.contains("Harvard") || it.institution.contains("Stanford") })
    }

    @Test
    fun testCareerPathEngine_computerScienceFieldGeneration() {
        val assessment = CareerAssessmentResult(
            fieldOfStudy = "Computer Science & AI",
            specialization = "Software Engineer"
        )
        val path = CareerPathEngine.generateCareerPath(assessment)

        assertEquals(4, path.phases.size)
        assertTrue(path.videoSuggestions.any { it.channel.contains("freeCodeCamp") || it.channel.contains("MIT") })
        assertTrue(path.classSuggestions.any { it.institution.contains("Harvard") || it.platform.contains("Coursera") })
    }

    @Test
    fun testUserProfile_initialsGeneration() {
        val user = UserProfile(
            id = "test-123",
            name = "Barie Bilal",
            email = "barie@example.com",
            majorOrField = "Computer Science"
        )
        assertEquals("BB", user.avatarInitials)
        assertEquals("Barie Bilal", user.name)
        assertFalse(user.isGuest)
    }

    @Test
    fun testSpacedRepetitionEngine_goodRating_increasesInterval() {
        val initialCard = FlashcardEntity(
            id = 1L,
            question = "What is a coroutine?",
            answer = "A lightweight thread.",
            subject = "Computer Science",
            topic = "Concurrency",
            intervalDays = 1,
            easeFactor = 2.5,
            repetitions = 0,
            nextReviewAt = 1000L
        )

        val updatedCard = SpacedRepetitionEngine.calculateNextReview(
            card = initialCard,
            rating = ReviewRating.GOOD,
            now = 2000L
        )

        assertEquals(1, updatedCard.repetitions)
        assertEquals(1, updatedCard.intervalDays)
        assertEquals(1, updatedCard.totalReviews)
        assertEquals(1, updatedCard.successfulReviews)

        val secondReview = SpacedRepetitionEngine.calculateNextReview(
            card = updatedCard,
            rating = ReviewRating.GOOD,
            now = 3000L
        )

        assertEquals(2, secondReview.repetitions)
        assertEquals(3, secondReview.intervalDays)
    }

    @Test
    fun testSpacedRepetitionEngine_againRating_resetsInterval() {
        val masteredCard = FlashcardEntity(
            id = 2L,
            question = "What is Kotlin?",
            answer = "Modern static typed language.",
            subject = "Computer Science",
            topic = "Languages",
            intervalDays = 15,
            easeFactor = 2.6,
            repetitions = 4,
            nextReviewAt = 1000L
        )

        val updatedCard = SpacedRepetitionEngine.calculateNextReview(
            card = masteredCard,
            rating = ReviewRating.AGAIN,
            now = 2000L
        )

        assertEquals(0, updatedCard.repetitions)
        assertEquals(1, updatedCard.intervalDays)
        assertTrue(updatedCard.easeFactor < 2.6)
    }

    @Test
    fun testSpacedRepetitionEngine_easyRating_boostsIntervalAndEase() {
        val card = FlashcardEntity(
            id = 3L,
            question = "Speed of light in vacuum?",
            answer = "3x10^8 m/s",
            subject = "Physics",
            topic = "Optics",
            intervalDays = 1,
            easeFactor = 2.5,
            repetitions = 0,
            nextReviewAt = 1000L
        )

        val updatedCard = SpacedRepetitionEngine.calculateNextReview(
            card = card,
            rating = ReviewRating.EASY,
            now = 2000L
        )

        assertEquals(4, updatedCard.intervalDays)
        assertTrue(updatedCard.easeFactor > 2.5)
        assertEquals("EASY", updatedCard.lastRating)
    }

    @Test
    fun testAmbientSoundType_resolutionAndDefaults() {
        val rain = com.example.focus.audio.AmbientSoundType.fromId("RAIN")
        assertEquals(com.example.focus.audio.AmbientSoundType.RAIN, rain)
        assertEquals("Rainfall", rain.displayName)

        val whiteNoise = com.example.focus.audio.AmbientSoundType.fromId("WHITE_NOISE")
        assertEquals(com.example.focus.audio.AmbientSoundType.WHITE_NOISE, whiteNoise)
        assertEquals("White Noise", whiteNoise.displayName)

        val oceanWaves = com.example.focus.audio.AmbientSoundType.fromId("OCEAN_WAVES")
        assertEquals(com.example.focus.audio.AmbientSoundType.OCEAN_WAVES, oceanWaves)

        val unknownSound = com.example.focus.audio.AmbientSoundType.fromId("UNKNOWN_SOUND_ABC")
        assertEquals(com.example.focus.audio.AmbientSoundType.OFF, unknownSound)
    }

    @Test
    fun testDataBackupManager_exportAndParseJson() {
        val tasks = listOf(
            TaskEntity(id = 1L, title = "Study Algorithms", subject = "Computer Science", priority = "HIGH", dueDate = 10000L)
        )
        val flashcards = listOf(
            FlashcardEntity(id = 10L, question = "What is Big O?", answer = "Asymptotic notation", subject = "CS", topic = "Algorithms")
        )
        val sessions = listOf(
            StudySessionEntity(id = 100L, subject = "Computer Science", durationMinutes = 45, completedAt = 20000L, focusScore = 95)
        )
        val schedules = listOf(
            ScheduleEntity(id = 50L, title = "Morning Study", subject = "CS", startTime = "08:00", endTime = "09:00", dayOfWeek = "Monday")
        )

        val json = com.example.data.backup.DataBackupManager.exportAllToJson(
            tasks = tasks,
            flashcards = flashcards,
            sessions = sessions,
            schedules = schedules
        )

        assertTrue(json.contains("Avora"))
        assertTrue(json.contains("Study Algorithms"))
        assertTrue(json.contains("What is Big O?"))

        val parsed = com.example.data.backup.DataBackupManager.parseJsonBackup(json)
        assertEquals(1, parsed.tasks.size)
        assertEquals("Study Algorithms", parsed.tasks[0].title)
        assertEquals(1, parsed.flashcards.size)
        assertEquals("What is Big O?", parsed.flashcards[0].question)
        assertEquals(1, parsed.sessions.size)
        assertEquals(45, parsed.sessions[0].durationMinutes)
        assertEquals(1, parsed.schedules.size)
    }

    @Test
    fun testDataBackupManager_exportCsv() {
        val tasks = listOf(
            TaskEntity(id = 1L, title = "Math Homework, Part 1", subject = "Math", priority = "HIGH", dueDate = 10000L, notes = "Notes with \"quotes\"")
        )
        val csv = com.example.data.backup.DataBackupManager.exportTasksToCsv(tasks)
        assertTrue(csv.startsWith("ID,Title,Subject"))
        assertTrue(csv.contains("\"Math Homework, Part 1\""))
    }

    @Test
    fun testChatbot_creatorAndComplimentKeywords() {
        val originQueries = listOf(
            "where is from sir barie bilal",
            "where is sir barie bilal from",
            "where is sir barie from",
            "where is barie bilal from",
            "from where is sir barie bilal",
            "where does sir barie bilal live"
        )
        val bilalQueries = listOf(
            "who is bilal",
            "who is bilal?",
            "who's bilal",
            "who is Mr Bilal",
            "tell me about bilal"
        )
        val creatorQueries = listOf(
            "who made you",
            "who created you",
            "who created Avora?",
            "who is your maker?",
            "who developed this app?",
            "who is Sir Barie Bilal",
            "is this made by google ai studio",
            "are you from google ai studio"
        )
        val complimentQueries = listOf(
            "you are the best app i have ever tried",
            "you are the best app I've ever tried",
            "you are the best app",
            "best app I have ever tried"
        )

        originQueries.forEach { query ->
            val lower = query.lowercase().trim()
            val isOrigin = lower.contains("where") && (lower.contains("barie") || lower.contains("bilal"))
            assertTrue("Expected query '$query' to match origin check", isOrigin)
        }

        bilalQueries.forEach { query ->
            val lower = query.lowercase().trim()
            val isBilal = lower.contains("bilal") && (lower.contains("who") || lower.contains("tell"))
            assertTrue("Expected query '$query' to match bilal check", isBilal)
        }

        creatorQueries.forEach { query ->
            val lower = query.lowercase().trim()
            val isCreator = ((lower.contains("who") || lower.contains("wo") || lower.contains("is") || lower.contains("did") || lower.contains("are")) &&
                    (lower.contains("create") || lower.contains("created") || lower.contains("make") || lower.contains("made") || lower.contains("built") || lower.contains("develop") || lower.contains("sir barie bilal") || lower.contains("studio") || lower.contains("google")))
            assertTrue("Expected query '$query' to match creator check", isCreator)
        }

        complimentQueries.forEach { query ->
            val lower = query.lowercase().trim()
            val isCompliment = lower.contains("best app") && (lower.contains("ever") || lower.contains("tried") || lower.contains("used") || lower.contains("the best app"))
            assertTrue("Expected query '$query' to match compliment check", isCompliment)
        }
    }
}


