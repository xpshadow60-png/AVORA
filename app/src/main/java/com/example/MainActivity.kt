package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ScheduleEntity
import com.example.ui.MainViewModel
import com.example.ui.components.BottomNav
import com.example.ui.components.TechAtmosphereBackground
import com.example.ui.components.TopHeader
import com.example.ui.screens.*
import com.example.ui.theme.StudyPulseTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            StudyPulseTheme(themeMode = themeMode) {
                StudyPulseApp(viewModel)
            }
        }
    }
}

@Composable
fun StudyPulseApp(viewModel: MainViewModel) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val tasks by viewModel.tasksState.collectAsStateWithLifecycle()
    val schedules by viewModel.scheduleState.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcardsState.collectAsStateWithLifecycle()
    val quizResults by viewModel.quizResultsState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessagesState.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessionsState.collectAsStateWithLifecycle()
    val timerState by viewModel.focusTimerState.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val isResearchMode by viewModel.isResearchMode.collectAsStateWithLifecycle()
    val homeworkSolution by viewModel.homeworkSolution.collectAsStateWithLifecycle()
    val isSolvingHomework by viewModel.isSolvingHomework.collectAsStateWithLifecycle()
    val documentAnalysis by viewModel.documentAnalysis.collectAsStateWithLifecycle()
    val isAnalyzingDoc by viewModel.isAnalyzingDoc.collectAsStateWithLifecycle()
    val importedDocument by viewModel.importedDocument.collectAsStateWithLifecycle()
    val isImportingDoc by viewModel.isImportingDoc.collectAsStateWithLifecycle()
    val codingResult by viewModel.codingResult.collectAsStateWithLifecycle()
    val isAnalyzingCode by viewModel.isAnalyzingCode.collectAsStateWithLifecycle()
    val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
    val userQuizAnswers by viewModel.userQuizAnswers.collectAsStateWithLifecycle()
    val isQuizSubmitted by viewModel.isQuizSubmitted.collectAsStateWithLifecycle()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsStateWithLifecycle()
    val generatedStudyPlan by viewModel.generatedStudyPlan.collectAsStateWithLifecycle()
    val isGeneratingPlan by viewModel.isGeneratingPlan.collectAsStateWithLifecycle()
    val voiceState by viewModel.voiceTutorHelper.voiceState.collectAsStateWithLifecycle()
    val spokenText by viewModel.voiceTutorHelper.spokenText.collectAsStateWithLifecycle()

    val dailyGoalHours by viewModel.dailyStudyGoalHours.collectAsStateWithLifecycle()
    val dailyUsageSeconds by viewModel.dailyUsageSeconds.collectAsStateWithLifecycle()
    val showFreeInfoDialog by viewModel.showFreeInfoDialog.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val showLoginScreen by viewModel.showLoginScreen.collectAsStateWithLifecycle()
    val showCareerAssessment by viewModel.showCareerAssessment.collectAsStateWithLifecycle()
    val fullCareerPath by viewModel.fullCareerPath.collectAsStateWithLifecycle()
    val completedMilestones by viewModel.completedMilestones.collectAsStateWithLifecycle()

    val skillProfile by viewModel.skillProfile.collectAsStateWithLifecycle()
    val studentPortfolio by viewModel.studentPortfolio.collectAsStateWithLifecycle()
    val activeProjects by viewModel.activeProjects.collectAsStateWithLifecycle()
    val learningTracks by viewModel.learningTracks.collectAsStateWithLifecycle()
    val codingChallenges by viewModel.codingChallenges.collectAsStateWithLifecycle()
    val isGeneratingCustomProject by viewModel.isGeneratingCustomProject.collectAsStateWithLifecycle()

    var showBackupExportDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    if (showLoginScreen) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        LoginScreen(
            onSignInWithPassword = { email, password ->
                val result = viewModel.signInWithPassword(email, password)
                if (result is com.example.data.auth.AuthResult.Error) result.message else null
            },
            onSignUpWithPassword = { name, email, password, major ->
                val result = viewModel.signUpWithPassword(name, email, password, major)
                if (result is com.example.data.auth.AuthResult.Error) result.message else null
            },
            onResetPassword = { email ->
                viewModel.sendPasswordResetEmail(email)
            },
            onGoogleSignIn = {
                coroutineScope.launch {
                    viewModel.signInWithGoogle(context)
                }
            },
            onContinueAsGuest = {
                viewModel.continueAsGuest()
            }
        )
    } else if (showCareerAssessment) {
        CareerAssessmentScreen(
            onAssessmentCompleted = { result ->
                viewModel.saveCareerAssessment(result)
            },
            onSkip = {
                viewModel.skipCareerAssessment()
            }
        )
    } else {
        TechAtmosphereBackground(
            isDark = isDark,
            modifier = Modifier.fillMaxSize()
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState)
                },
                topBar = {
                    TopHeader(
                        timerState = timerState,
                        themeMode = themeMode,
                        currentUser = currentUser,
                        dailyUsageSeconds = dailyUsageSeconds,
                        selectedTab = selectedTab,
                        onSelectTab = { viewModel.selectTab(it) },
                        onToggleTheme = { viewModel.toggleThemeMode() },
                        onSetThemeMode = { viewModel.setThemeMode(it) },
                        onOpenFreeInfo = { viewModel.setShowFreeInfoDialog(true) },
                        onPrioritizeClick = { viewModel.prioritizeTasksWithAi() },
                        onFocusTabClick = { viewModel.selectTab(8) },
                        onOpenCareerHub = { viewModel.selectTab(4) },
                        onOpenBackupExport = { showBackupExportDialog = true },
                        onOpenPrivacyDisclosures = { showPrivacyDialog = true },
                        onOpenAuth = { viewModel.showAuthScreen(true) },
                        onResendVerification = { viewModel.sendEmailVerification() },
                        onChangePassword = { newPass -> viewModel.changePassword(newPass) },
                        onLogout = { viewModel.logout() }
                    )
                },
                bottomBar = {
                    BottomNav(
                        selectedTab = selectedTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                BackHandler(enabled = selectedTab != 0) {
                    viewModel.selectTab(0)
                }

                when (selectedTab) {
                    0 -> DashboardScreen(
                        tasks = tasks,
                        schedules = schedules,
                        studySessions = studySessions,
                        flashcards = flashcards,
                        careerPath = fullCareerPath,
                        dailyGoalHours = dailyGoalHours,
                        onUpdateDailyGoal = { viewModel.setDailyStudyGoalHours(it) },
                        focusTimerState = timerState,
                        onStartQuickFocus = { duration, subject ->
                            viewModel.startFocusSession(duration, subject)
                            viewModel.selectTab(8)
                        },
                        onTaskToggle = { viewModel.toggleTaskCompletion(it) },
                        onNavigateToTutor = { viewModel.selectTab(1) },
                        onNavigateToHomework = { viewModel.selectTab(11) },
                        onNavigateToCoding = { viewModel.selectTab(2) },
                        onNavigateToQuiz = { viewModel.selectTab(5) },
                        onNavigateToNotes = { viewModel.selectTab(6) },
                        onNavigateToCards = { viewModel.selectTab(12) },
                        onNavigateToPlanner = { viewModel.selectTab(7) },
                        onNavigateToFocus = { viewModel.selectTab(8) },
                        onNavigateToAnalytics = { viewModel.selectTab(13) },
                        onNavigateToCareer = { viewModel.selectTab(14) },
                        onOpenPrivacyDisclosures = { showPrivacyDialog = true }
                    )
                    1 -> AiTutorScreen(
                        messages = chatMessages,
                        isAiThinking = isAiThinking,
                        isResearchMode = isResearchMode,
                        voiceState = voiceState,
                        spokenText = spokenText,
                        onSendMessage = { viewModel.sendChatMessage(it) },
                        onSendMessageWithModifier = { text, mod -> viewModel.sendTutorChatMessageWithModifier(text, mod) },
                        onSendPhotoMessage = { bitmap, prompt, subj -> viewModel.sendHomeworkPhotoToChat(bitmap, prompt, subj) },
                        onToggleResearchMode = { viewModel.toggleResearchMode() },
                        onStartVoiceInput = {
                            viewModel.voiceTutorHelper.startListening { text ->
                                if (text.isNotBlank()) {
                                    viewModel.sendChatMessage(text)
                                }
                            }
                        },
                        onStopVoiceInput = { viewModel.voiceTutorHelper.stopListening() },
                        onSpeakText = { viewModel.speakTutorResponse(it) },
                        onStopSpeaking = { viewModel.stopSpeaking() },
                        onPrioritizeTasks = { viewModel.prioritizeTasksWithAi() },
                        onClearChat = { viewModel.clearChatHistory() },
                        onRegenerate = { viewModel.regenerateLastResponse() }
                    )
                    2 -> CodePlaygroundScreen(
                        codingResult = codingResult,
                        isAnalyzingCode = isAnalyzingCode,
                        onAnalyzeCode = { code, lang, note, type -> viewModel.analyzeCode(code, lang, note, type) },
                        onClearCodingResult = { viewModel.clearCodingResult() },
                        onChallengeSolved = { viewModel.recordChallengeCompleted(it) }
                    )
                    3 -> ProjectBuilderScreen(
                        onGenerateCustomProject = { goal, cat, diff -> viewModel.generateCustomAiProject(goal, cat, diff) },
                        onNavigateToCode = { viewModel.selectTab(2) },
                        onProjectCompleted = { viewModel.recordProjectCompleted(it) }
                    )
                    4 -> LearningRoadmapScreen(
                        onNavigateToLesson = { subj, topic ->
                            viewModel.sendChatMessage("Let's learn $topic in $subj step by step.")
                            viewModel.selectTab(1)
                        },
                        onNavigateToProject = { viewModel.selectTab(3) },
                        onNavigateToCode = { viewModel.selectTab(2) }
                    )
                    5 -> QuizScreen(
                        activeQuiz = activeQuiz,
                        userAnswers = userQuizAnswers,
                        isQuizSubmitted = isQuizSubmitted,
                        isGenerating = isGeneratingQuiz,
                        quizHistory = quizResults,
                        onGenerateQuiz = { subj, top, diff, count -> viewModel.generateNewQuiz(subj, top, diff, count) },
                        onSelectAnswer = { qId, optIdx -> viewModel.selectQuizAnswer(qId, optIdx) },
                        onSubmitQuiz = { viewModel.submitQuizAnswers() },
                        onRetakeQuiz = { viewModel.retakeActiveQuiz() },
                        onClearHistory = { viewModel.clearQuizHistory() },
                        onNavigateToFlashcards = { viewModel.selectTab(12) }
                    )
                    6 -> {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        NotesAnalyzerScreen(
                            analysisResult = documentAnalysis,
                            isAnalyzing = isAnalyzingDoc,
                            importedDocument = importedDocument,
                            isImportingDocument = isImportingDoc,
                            onImportDocument = { uri -> viewModel.importDocument(context, uri) },
                            onClearImportedDocument = { viewModel.clearImportedDocument() },
                            onAnalyzeDocument = { docText -> viewModel.analyzeDocument(docText) },
                            onSaveFlashcards = { subj -> viewModel.saveDocumentFlashcardsToDatabase(subj) },
                            onStartDocumentQuiz = { viewModel.startQuizFromDocumentNotes() }
                        )
                    }
                    7 -> StudyPlannerScreen(
                        tasks = tasks,
                        schedules = schedules,
                        generatedPlan = generatedStudyPlan,
                        isGeneratingPlan = isGeneratingPlan,
                        onGeneratePlan = { subjs, tops, hrs, days, goal, date ->
                            viewModel.generatePersonalizedStudyPlan(subjs, tops, hrs, days, goal, date)
                        },
                        onApplyPlan = { viewModel.applyGeneratedPlanToDatabase() },
                        onAddTask = { title, subject, priority, estimated, category, notes, dueDate ->
                            viewModel.addTask(title, subject, priority, estimated, category, notes, dueDate)
                        },
                        onTaskToggle = { viewModel.toggleTaskCompletion(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onAddSchedule = { title, subject, startTime, endTime, day ->
                            viewModel.addSchedule(title, subject, startTime, endTime, day)
                        },
                        onPrioritizeWithAi = { viewModel.prioritizeTasksWithAi() }
                    )
                    8 -> FocusAlertScreen(
                        timerState = timerState,
                        onStartFocus = { duration, subject -> viewModel.startFocusSession(duration, subject) },
                        onPauseFocus = { viewModel.pauseFocusSession() },
                        onResumeFocus = { viewModel.resumeFocusSession() },
                        onStopFocus = { viewModel.stopFocusSession() },
                        onSetAmbientSound = { viewModel.setAmbientSound(it) },
                        onToggleAmbientSound = { viewModel.toggleAmbientSoundscape(it) },
                        onSetAmbientVolume = { viewModel.setAmbientVolume(it) }
                    )
                    9 -> SkillProfileScreen(
                        skillProfile = skillProfile,
                        onNavigateToLearn = { viewModel.selectTab(4) },
                        onNavigateToChallenges = { viewModel.selectTab(2) }
                    )
                    10 -> PortfolioScreen(
                        portfolio = studentPortfolio,
                        onNavigateToProjectBuilder = { viewModel.selectTab(3) }
                    )
                    11 -> HomeworkAndCodingScreen(
                        homeworkSolution = homeworkSolution,
                        isSolvingHomework = isSolvingHomework,
                        codingResult = codingResult,
                        isAnalyzingCode = isAnalyzingCode,
                        onSolveHomework = { problem, subj -> viewModel.solveHomework(problem, subj) },
                        onSolveHomeworkWithPhoto = { photo, problem, subj -> viewModel.solveHomeworkWithPhoto(photo, problem, subj) },
                        onClearHomework = { viewModel.clearHomeworkSolution() },
                        onAnalyzeCode = { code, lang, note, type -> viewModel.analyzeCode(code, lang, note, type) },
                        onClearCodingResult = { viewModel.clearCodingResult() },
                        onNavigateToQuiz = { subj, topic ->
                            viewModel.generateNewQuiz(subj, topic, "Medium", 5)
                            viewModel.selectTab(5)
                        }
                    )
                    12 -> FlashcardsScreen(
                        flashcards = flashcards,
                        onAddFlashcard = { question, answer, subject, topic ->
                            viewModel.addFlashcard(question, answer, subject, topic)
                        },
                        onUpdateFlashcard = { viewModel.updateFlashcard(it) },
                        onDeleteFlashcard = { viewModel.deleteFlashcard(it) },
                        onReviewFlashcard = { card, rating -> viewModel.reviewFlashcard(card, rating) }
                    )
                    13 -> AnalyticsScreen(
                        tasks = tasks,
                        sessions = studySessions,
                        flashcards = flashcards,
                        quizResults = quizResults,
                        dailyGoalHours = dailyGoalHours,
                        onUpdateDailyGoal = { viewModel.setDailyStudyGoalHours(it) },
                        onOpenExportBackup = { showBackupExportDialog = true }
                    )
                    14 -> CareerHubScreen(
                        careerPath = fullCareerPath,
                        onRetakeAssessment = { viewModel.retakeCareerAssessment() },
                        onToggleMilestone = { viewModel.toggleMilestone(it) },
                        completedMilestoneIds = completedMilestones
                    )
                }
            }
        }

        if (showBackupExportDialog) {
            com.example.ui.components.DataBackupExportDialog(
                onDismiss = { showBackupExportDialog = false },
                onExportJson = { viewModel.exportBackupJson() },
                onExportTasksCsv = { viewModel.exportTasksCsv() },
                onExportFlashcardsCsv = { viewModel.exportFlashcardsCsv() },
                onExportSessionsCsv = { viewModel.exportSessionsCsv() },
                onExportReportMarkdown = { viewModel.exportStudyReportMarkdown() },
                onRestoreJson = { jsonStr, onResult ->
                    viewModel.restoreBackup(jsonStr, onResult)
                }
            )
        }

        if (showPrivacyDialog) {
            com.example.ui.components.PrivacyDataDisclosureDialog(
                onDismiss = { showPrivacyDialog = false },
                onOpenDataExport = {
                    showPrivacyDialog = false
                    showBackupExportDialog = true
                }
            )
        }

        if (showFreeInfoDialog) {
            com.example.ui.components.AvoraFreeInfoDialog(
                onDismiss = { viewModel.setShowFreeInfoDialog(false) }
            )
        }
        }
    }
}
