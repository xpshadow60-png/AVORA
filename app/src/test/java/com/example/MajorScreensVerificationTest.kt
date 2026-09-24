package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.career.CareerPathEngine
import com.example.data.local.FlashcardEntity
import com.example.data.local.ScheduleEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.data.model.CareerAssessmentResult
import com.example.focus.FocusTimerState
import com.example.ui.components.PrivacyDataDisclosureDialog
import com.example.ui.screens.*
import com.example.ui.theme.StudyPulseTheme
import com.example.ui.tutor.VoiceState
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class MajorScreensVerificationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testLoginScreen_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                LoginScreen(
                    onSignInWithPassword = { _, _ -> null },
                    onSignUpWithPassword = { _, _, _, _ -> null },
                    onResetPassword = { _ -> null },
                    onGoogleSignIn = {},
                    onContinueAsGuest = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testDashboardScreen_rendersProperly() {
        val dummyTasks = listOf(
            TaskEntity(id = 1L, title = "Data Structures Lab", subject = "Computer Science", priority = "HIGH", isCompleted = false),
            TaskEntity(id = 2L, title = "Operating Systems Quiz", subject = "Computer Science", priority = "MEDIUM", isCompleted = true)
        )
        val dummySessions = listOf(
            StudySessionEntity(id = 1L, subject = "Computer Science", durationMinutes = 45, completedAt = System.currentTimeMillis(), focusScore = 92)
        )
        val dummySchedules = listOf(
            ScheduleEntity(id = 1L, title = "CS Lecture", subject = "Computer Science", startTime = "10:00", endTime = "11:30", dayOfWeek = "Monday")
        )

        composeTestRule.setContent {
            StudyPulseTheme {
                DashboardScreen(
                    tasks = dummyTasks,
                    schedules = dummySchedules,
                    studySessions = dummySessions,
                    focusTimerState = FocusTimerState(
                        remainingSeconds = 1500,
                        totalSeconds = 1500,
                        isActive = false,
                        currentSubject = "Computer Science"
                    ),
                    onStartQuickFocus = { _, _ -> },
                    onTaskToggle = {},
                    onNavigateToTutor = {},
                    onNavigateToHomework = {},
                    onNavigateToCoding = {},
                    onNavigateToQuiz = {},
                    onNavigateToNotes = {},
                    onNavigateToCards = {},
                    onNavigateToPlanner = {},
                    onNavigateToFocus = {},
                    onNavigateToAnalytics = {},
                    onNavigateToCareer = {},
                    onOpenPrivacyDisclosures = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testAiTutorScreen_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                AiTutorScreen(
                    messages = emptyList(),
                    isAiThinking = false,
                    isResearchMode = false,
                    voiceState = VoiceState.IDLE,
                    spokenText = "",
                    onSendMessage = {},
                    onSendMessageWithModifier = { _, _ -> },
                    onToggleResearchMode = {},
                    onStartVoiceInput = {},
                    onStopVoiceInput = {},
                    onSpeakText = {},
                    onStopSpeaking = {},
                    onPrioritizeTasks = {},
                    onClearChat = {},
                    onRegenerate = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testCodePlaygroundScreen_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                CodePlaygroundScreen(
                    codingResult = null,
                    isAnalyzingCode = false,
                    onAnalyzeCode = { _, _, _, _ -> },
                    onClearCodingResult = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testProjectBuilderScreen_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                ProjectBuilderScreen(
                    onGenerateCustomProject = { _, _, _ -> },
                    onNavigateToCode = {},
                    onProjectCompleted = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testCareerHubScreen_rendersProperly() {
        val careerPath = CareerPathEngine.generateCareerPath(
            CareerAssessmentResult(
                fieldOfStudy = "Computer Science & AI",
                specialization = "Software Engineer"
            )
        )
        composeTestRule.setContent {
            StudyPulseTheme {
                CareerHubScreen(
                    careerPath = careerPath,
                    onRetakeAssessment = {},
                    onToggleMilestone = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testNotesAnalyzerScreen_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                NotesAnalyzerScreen(
                    analysisResult = null,
                    isAnalyzing = false,
                    onAnalyzeDocument = {},
                    onSaveFlashcards = {},
                    onStartDocumentQuiz = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testAnalyticsScreen_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                AnalyticsScreen(
                    tasks = emptyList(),
                    sessions = emptyList(),
                    flashcards = emptyList(),
                    quizResults = emptyList()
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun testPrivacyDataDisclosureDialog_rendersProperly() {
        composeTestRule.setContent {
            StudyPulseTheme {
                PrivacyDataDisclosureDialog(
                    onDismiss = {},
                    onOpenDataExport = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}
