package com.example.ui

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.StudyRepository
import com.example.data.auth.AuthManager
import com.example.data.auth.AuthResult
import com.example.data.auth.GoogleAuthResult
import com.example.data.career.CareerPathEngine
import com.example.data.document.DocumentParserEngine
import com.example.data.document.ImportedDocument
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.AiTutorServices
import com.example.data.remote.AvoraLearningDataEngine
import com.example.data.remote.GeminiClient
import com.example.data.remote.StudyProductivityReport
import com.example.data.srs.ReviewRating
import com.example.data.srs.SpacedRepetitionEngine
import com.example.data.sync.CloudSyncManager
import com.example.focus.FocusModeManager
import com.example.focus.FocusTimerState
import com.example.ui.theme.ThemeMode
import com.example.ui.tutor.VoiceState
import com.example.ui.tutor.VoiceTutorHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = StudyRepository(db)
    val cloudSyncManager = CloudSyncManager()
    val authManager = AuthManager(application, cloudSyncManager = cloudSyncManager)
    val focusModeManager = FocusModeManager(application, repository)
    val voiceTutorHelper = VoiceTutorHelper(application)
    private val networkObserver = com.example.util.NetworkConnectivityObserver(application)
    val isOnline: StateFlow<Boolean> = networkObserver.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        networkObserver.isCurrentlyConnected()
    )
    private val sharedPrefs = application.getSharedPreferences("studypulse_prefs", Context.MODE_PRIVATE)

    // User Authentication State
    private val _currentUser = MutableStateFlow<UserProfile?>(authManager.getCurrentUser() ?: loadSavedUserProfile())
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(authManager.isUserSignedIn() || sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _showLoginScreen = MutableStateFlow(!_isLoggedIn.value)
    val showLoginScreen: StateFlow<Boolean> = _showLoginScreen.asStateFlow()

    // 10-Question Career Assessment & Career Path
    private val _hasCompletedAssessment = MutableStateFlow(
        sharedPrefs.getBoolean("has_completed_assessment", false)
    )
    val hasCompletedAssessment: StateFlow<Boolean> = _hasCompletedAssessment.asStateFlow()

    private val _showCareerAssessment = MutableStateFlow(false)
    val showCareerAssessment: StateFlow<Boolean> = _showCareerAssessment.asStateFlow()

    private val _careerAssessment = MutableStateFlow<CareerAssessmentResult?>(loadSavedAssessment())
    val careerAssessment: StateFlow<CareerAssessmentResult?> = _careerAssessment.asStateFlow()

    private val _fullCareerPath = MutableStateFlow<FullCareerPath>(
        CareerPathEngine.generateCareerPath(_careerAssessment.value ?: CareerAssessmentResult())
    )
    val fullCareerPath: StateFlow<FullCareerPath> = _fullCareerPath.asStateFlow()

    private val _completedMilestones = MutableStateFlow<Set<String>>(
        sharedPrefs.getStringSet("completed_milestones", emptySet()) ?: emptySet()
    )
    val completedMilestones: StateFlow<Set<String>> = _completedMilestones.asStateFlow()

    private fun loadSavedAssessment(): CareerAssessmentResult? {
        val field = sharedPrefs.getString("career_field", null) ?: return null
        val spec = sharedPrefs.getString("career_spec", "Physician / Specialist") ?: "Physician / Specialist"
        val level = sharedPrefs.getString("career_level", "Undergraduate") ?: "Undergraduate"
        val hrs = sharedPrefs.getString("career_hrs", "10 - 20 Hours / week") ?: "10 - 20 Hours / week"
        val style = sharedPrefs.getString("career_style", "Visual & Video Masterclasses") ?: "Visual & Video Masterclasses"
        val challenge = sharedPrefs.getString("career_challenge", "Time Management & Heavy Workload") ?: "Time Management & Heavy Workload"
        val timeline = sharedPrefs.getString("career_timeline", "1 - 2 Years") ?: "1 - 2 Years"
        val cert = sharedPrefs.getString("career_cert", "Professional Board / Licensure") ?: "Professional Board / Licensure"
        val res = sharedPrefs.getString("career_res", "University MOOCs & Courseware") ?: "University MOOCs & Courseware"
        val habit = sharedPrefs.getString("career_habit", "Pomodoro Focus Sprints") ?: "Pomodoro Focus Sprints"

        return CareerAssessmentResult(
            fieldOfStudy = field,
            specialization = spec,
            academicLevel = level,
            weeklyStudyHours = hrs,
            learningStyle = style,
            primaryChallenge = challenge,
            timelineGoal = timeline,
            certificationGoal = cert,
            resourcePreference = res,
            dailyStudyHabit = habit
        )
    }

    private fun loadSavedUserProfile(): UserProfile? {
        val email = sharedPrefs.getString("user_email", null) ?: return null
        var name = sharedPrefs.getString("user_name", "Future Leader") ?: "Future Leader"
        if (name.equals("Google Scholar", ignoreCase = true) || name.equals("Scholar", ignoreCase = true)) {
            name = "Future Leader"
            sharedPrefs.edit().putString("user_name", "Future Leader").apply()
        }
        val major = sharedPrefs.getString("user_major", "Computer Science") ?: "Computer Science"
        val isGuest = sharedPrefs.getBoolean("user_is_guest", false)
        val id = sharedPrefs.getString("user_id", UUID.randomUUID().toString()) ?: UUID.randomUUID().toString()
        return UserProfile(
            id = id,
            name = name,
            email = email,
            majorOrField = major,
            isGuest = isGuest
        )
    }

    // Avora Free Information Dialog State
    private val _showFreeInfoDialog = MutableStateFlow(false)
    val showFreeInfoDialog: StateFlow<Boolean> = _showFreeInfoDialog.asStateFlow()

    private val todayDateString: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val _dailyUsageSeconds = MutableStateFlow(loadTodayUsageSeconds())
    val dailyUsageSeconds: StateFlow<Long> = _dailyUsageSeconds.asStateFlow()

    private fun loadTodayUsageSeconds(): Long {
        val savedDate = sharedPrefs.getString("daily_usage_date", "")
        return if (savedDate == todayDateString) {
            sharedPrefs.getLong("daily_usage_seconds", 0L)
        } else {
            sharedPrefs.edit().putString("daily_usage_date", todayDateString).putLong("daily_usage_seconds", 0L).apply()
            0L
        }
    }

    private val _dailyStudyGoalHours = MutableStateFlow(
        sharedPrefs.getFloat("daily_study_goal_hours", 4.0f)
    )
    val dailyStudyGoalHours: StateFlow<Float> = _dailyStudyGoalHours.asStateFlow()

    private val _themeMode = MutableStateFlow(
        when (sharedPrefs.getString("app_theme_mode", "DARK")) {
            "LIGHT" -> ThemeMode.LIGHT
            "SYSTEM" -> ThemeMode.SYSTEM
            else -> ThemeMode.DARK // Default to modern sleek Dark mode
        }
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    val tasksState: StateFlow<List<TaskEntity>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduleState: StateFlow<List<ScheduleEntity>> = repository.schedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesState: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessionsState: StateFlow<List<StudySessionEntity>> = repository.studySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcardsState: StateFlow<List<FlashcardEntity>> = repository.flashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizResultsState: StateFlow<List<QuizResultEntity>> = repository.quizResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusTimerState: StateFlow<FocusTimerState> = focusModeManager.timerState

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Research Mode toggle
    private val _isResearchMode = MutableStateFlow(false)
    val isResearchMode: StateFlow<Boolean> = _isResearchMode.asStateFlow()

    // Homework Helper State
    private val _homeworkSolution = MutableStateFlow<HomeworkSolution?>(null)
    val homeworkSolution: StateFlow<HomeworkSolution?> = _homeworkSolution.asStateFlow()

    private val _isSolvingHomework = MutableStateFlow(false)
    val isSolvingHomework: StateFlow<Boolean> = _isSolvingHomework.asStateFlow()

    // Document Analyzer & Importer State
    private val _importedDocument = MutableStateFlow<ImportedDocument?>(null)
    val importedDocument: StateFlow<ImportedDocument?> = _importedDocument.asStateFlow()

    private val _isImportingDoc = MutableStateFlow(false)
    val isImportingDoc: StateFlow<Boolean> = _isImportingDoc.asStateFlow()

    private val _documentAnalysis = MutableStateFlow<DocumentAnalysisResult?>(null)
    val documentAnalysis: StateFlow<DocumentAnalysisResult?> = _documentAnalysis.asStateFlow()

    private val _isAnalyzingDoc = MutableStateFlow(false)
    val isAnalyzingDoc: StateFlow<Boolean> = _isAnalyzingDoc.asStateFlow()

    // Coding Mentor State
    private val _codingResult = MutableStateFlow<CodeExplanationResult?>(null)
    val codingResult: StateFlow<CodeExplanationResult?> = _codingResult.asStateFlow()

    private val _isAnalyzingCode = MutableStateFlow(false)
    val isAnalyzingCode: StateFlow<Boolean> = _isAnalyzingCode.asStateFlow()

    // AI Quiz Generator & Player State
    private val _activeQuiz = MutableStateFlow<GeneratedQuiz?>(null)
    val activeQuiz: StateFlow<GeneratedQuiz?> = _activeQuiz.asStateFlow()

    private val _userQuizAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // questionId -> selectedOptionIndex
    val userQuizAnswers: StateFlow<Map<Int, Int>> = _userQuizAnswers.asStateFlow()

    private val _isQuizSubmitted = MutableStateFlow(false)
    val isQuizSubmitted: StateFlow<Boolean> = _isQuizSubmitted.asStateFlow()

    private val _isGeneratingQuiz = MutableStateFlow(false)
    val isGeneratingQuiz: StateFlow<Boolean> = _isGeneratingQuiz.asStateFlow()

    // AI Study Plan Generator State
    private val _generatedStudyPlan = MutableStateFlow<List<GeneratedStudyTask>>(emptyList())
    val generatedStudyPlan: StateFlow<List<GeneratedStudyTask>> = _generatedStudyPlan.asStateFlow()

    private val _isGeneratingPlan = MutableStateFlow(false)
    val isGeneratingPlan: StateFlow<Boolean> = _isGeneratingPlan.asStateFlow()

    private val _latestProductivityReport = MutableStateFlow<StudyProductivityReport?>(null)
    val latestProductivityReport: StateFlow<StudyProductivityReport?> = _latestProductivityReport.asStateFlow()

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    init {
        val initialUser = _currentUser.value
        val initialUserId = initialUser?.id ?: "guest_user"
        repository.setActiveUserId(initialUserId)

        viewModelScope.launch {
            repository.seedDefaultDataIfEmpty(initialUserId)
        }

        // Listen to Firebase Auth state for automatic session sync and user data isolation
        viewModelScope.launch {
            authManager.observeAuthState().collect { userProfile ->
                if (userProfile != null) {
                    _currentUser.value = userProfile
                    _isLoggedIn.value = true
                    _showLoginScreen.value = false
                    repository.setActiveUserId(userProfile.id)
                    cloudSyncManager.syncAll(userProfile.id, repository)
                } else if (_isLoggedIn.value && _currentUser.value?.isGuest == false) {
                    _currentUser.value = null
                    _isLoggedIn.value = false
                    _showLoginScreen.value = true
                    repository.setActiveUserId("guest_user")
                    sharedPrefs.edit().putBoolean("is_logged_in", false).remove("user_id").apply()
                }
            }
        }

        // Automatic synchronization when internet connection is restored
        viewModelScope.launch {
            isOnline.collect { online ->
                if (online) {
                    val user = _currentUser.value
                    if (user != null && !user.isGuest) {
                        cloudSyncManager.syncAll(user.id, repository)
                    }
                }
            }
        }

        // Active Daily App Learning Time Tracker Ticker
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                val newSeconds = _dailyUsageSeconds.value + 1L
                _dailyUsageSeconds.value = newSeconds
                if (newSeconds % 10L == 0L) {
                    sharedPrefs.edit()
                        .putLong("daily_usage_seconds", newSeconds)
                        .putString("daily_usage_date", todayDateString)
                        .apply()
                }
            }
        }
    }

    fun setShowFreeInfoDialog(show: Boolean) {
        _showFreeInfoDialog.value = show
    }

    fun addUsageMinutes(minutes: Int) {
        val extra = minutes * 60L
        val updated = _dailyUsageSeconds.value + extra
        _dailyUsageSeconds.value = updated
        sharedPrefs.edit()
            .putLong("daily_usage_seconds", updated)
            .putString("daily_usage_date", todayDateString)
            .apply()
    }

    fun resetDailyUsage() {
        _dailyUsageSeconds.value = 0L
        sharedPrefs.edit()
            .putLong("daily_usage_seconds", 0L)
            .putString("daily_usage_date", todayDateString)
            .apply()
        viewModelScope.launch {
            _snackbarMessage.emit("⏱️ Daily learning timer reset.")
        }
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    // Daily Study Goal
    fun setDailyStudyGoalHours(hours: Float) {
        val rounded = ((hours * 10f).roundToInt()) / 10f
        _dailyStudyGoalHours.value = rounded
        sharedPrefs.edit().putFloat("daily_study_goal_hours", rounded).apply()
        viewModelScope.launch {
            _snackbarMessage.emit("Daily study target updated to ${if (rounded % 1f == 0f) "${rounded.toInt()}h" else "${rounded}h"}! 🎯")
        }
    }

    // Theme Mode
    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        sharedPrefs.edit().putString("app_theme_mode", mode.name).apply()
        val label = when (mode) {
            ThemeMode.DARK -> "Dark Theme enabled 🌙"
            ThemeMode.LIGHT -> "Light Theme enabled ☀️"
            ThemeMode.SYSTEM -> "System Theme enabled ⚙️"
        }
        viewModelScope.launch {
            _snackbarMessage.emit(label)
        }
    }

    fun toggleThemeMode() {
        val nextMode = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setThemeMode(nextMode)
    }

    // Task Actions
    fun addTask(title: String, subject: String, priority: String, estimatedMinutes: Int, category: String, notes: String, dueDateMillis: Long) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                subject = subject,
                priority = priority,
                dueDate = dueDateMillis,
                estimatedMinutes = estimatedMinutes,
                category = category,
                notes = notes
            )
            repository.addTask(task)
            _snackbarMessage.emit("Task \"$title\" added successfully! 📋")
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val willBeCompleted = !task.isCompleted
            repository.toggleTaskCompletion(task)
            val feedback = if (willBeCompleted) {
                "Completed: \"${task.title}\" 🎉"
            } else {
                "Marked \"${task.title}\" as pending"
            }
            _snackbarMessage.emit(feedback)
        }
    }

    fun updateTaskPriority(taskId: Long, priority: String) {
        viewModelScope.launch {
            repository.updateTaskPriority(taskId, priority)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.deleteCompletedTasks()
        }
    }

    fun addSchedule(title: String, subject: String, startTime: String, endTime: String, dayOfWeek: String) {
        viewModelScope.launch {
            repository.addSchedule(
                ScheduleEntity(
                    title = title,
                    subject = subject,
                    startTime = startTime,
                    endTime = endTime,
                    dayOfWeek = dayOfWeek
                )
            )
        }
    }

    // Focus Session Actions
    fun startFocusSession(durationMinutes: Int, subject: String) {
        focusModeManager.startSession(durationMinutes, subject)
    }

    fun pauseFocusSession() {
        focusModeManager.pauseSession()
    }

    fun resumeFocusSession() {
        focusModeManager.resumeSession()
    }

    fun stopFocusSession() {
        val currentState = focusTimerState.value
        if (currentState.isActive) {
            val elapsedMinutes = maxOf(1, (currentState.totalSeconds - currentState.remainingSeconds) / 60)
            viewModelScope.launch {
                repository.logStudySession(
                    StudySessionEntity(
                        subject = currentState.currentSubject,
                        durationMinutes = elapsedMinutes,
                        focusScore = currentState.focusScore,
                        distractionsBlockedCount = 0
                    )
                )
            }
        }
        focusModeManager.stopSession()
    }

    fun setAmbientSound(sound: String) {
        focusModeManager.setAmbientSound(sound)
    }

    fun toggleAmbientSoundscape(sound: String) {
        focusModeManager.toggleAmbientSoundscape(sound)
    }

    fun setAmbientVolume(volume: Float) {
        focusModeManager.setAmbientVolume(volume)
    }

    // Flashcard & Spaced Repetition Actions
    fun addFlashcard(question: String, answer: String, subject: String, topic: String) {
        if (question.isBlank() || answer.isBlank()) return
        viewModelScope.launch {
            val card = FlashcardEntity(
                question = question.trim(),
                answer = answer.trim(),
                subject = subject.trim().ifBlank { "General" },
                topic = topic.trim().ifBlank { "Core" },
                nextReviewAt = System.currentTimeMillis()
            )
            repository.addFlashcard(card)
            _snackbarMessage.emit("✨ Flashcard added for $subject!")
        }
    }

    fun updateFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.updateFlashcard(card)
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
            _snackbarMessage.emit("🗑️ Flashcard deleted.")
        }
    }

    fun reviewFlashcard(card: FlashcardEntity, rating: ReviewRating) {
        viewModelScope.launch {
            val updatedCard = SpacedRepetitionEngine.calculateNextReview(card, rating)
            repository.updateFlashcard(updatedCard)
        }
    }

    fun resetFlashcardProgress(card: FlashcardEntity) {
        viewModelScope.launch {
            val resetCard = card.copy(
                intervalDays = 1,
                easeFactor = 2.5,
                repetitions = 0,
                nextReviewAt = System.currentTimeMillis(),
                lastRating = "NEW"
            )
            repository.updateFlashcard(resetCard)
        }
    }

    // User Authentication & Profile Actions (Firebase Auth + Firestore)
    suspend fun signInWithPassword(email: String, password: String): AuthResult {
        val result = authManager.signIn(email, password)
        if (result is AuthResult.Success) {
            val user = result.user
            _currentUser.value = user
            _isLoggedIn.value = true
            _showLoginScreen.value = false
            repository.setActiveUserId(user.id)

            sharedPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_id", user.id)
                .putString("user_name", user.name)
                .putString("user_email", user.email)
                .putString("user_major", user.majorOrField)
                .putBoolean("user_is_guest", false)
                .apply()

            if (_careerAssessment.value == null) {
                val defaultResult = CareerAssessmentResult(
                    fieldOfStudy = user.majorOrField.ifBlank { "AI & Software Engineering" },
                    specialization = "AI Systems & Full-Stack Builder",
                    academicLevel = "Practicing Developer",
                    weeklyStudyHours = "10 - 20 Hours / week",
                    learningStyle = "Interactive Code & Project Sprints",
                    primaryChallenge = "Building Real Production Systems",
                    timelineGoal = "6 - 12 Months",
                    certificationGoal = "Portfolio Proof of Work",
                    resourcePreference = "Avora Interactive Code Labs",
                    dailyStudyHabit = "Daily Code Challenge + Milestone"
                )
                _careerAssessment.value = defaultResult
                _fullCareerPath.value = CareerPathEngine.generateCareerPath(defaultResult)
            }
            _showCareerAssessment.value = false
            _hasCompletedAssessment.value = true

            // Trigger full cloud sync for this user
            cloudSyncManager.syncAll(user.id, repository)

            viewModelScope.launch {
                _snackbarMessage.emit("🚀 Welcome back, ${user.name}!")
            }
        }
        return result
    }

    suspend fun signUpWithPassword(name: String, email: String, password: String, major: String): AuthResult {
        val result = authManager.signUp(name, email, password, major)
        if (result is AuthResult.Success) {
            val user = result.user
            _currentUser.value = user
            _isLoggedIn.value = true
            _showLoginScreen.value = false
            repository.setActiveUserId(user.id)

            sharedPrefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_id", user.id)
                .putString("user_name", user.name)
                .putString("user_email", user.email)
                .putString("user_major", user.majorOrField)
                .putBoolean("user_is_guest", false)
                .apply()

            val defaultResult = CareerAssessmentResult(
                fieldOfStudy = user.majorOrField.ifBlank { "AI & Software Engineering" },
                specialization = "AI Systems & Full-Stack Builder",
                academicLevel = "Practicing Developer",
                weeklyStudyHours = "10 - 20 Hours / week",
                learningStyle = "Interactive Code & Project Sprints",
                primaryChallenge = "Building Real Production Systems",
                timelineGoal = "6 - 12 Months",
                certificationGoal = "Portfolio Proof of Work",
                resourcePreference = "Avora Interactive Code Labs",
                dailyStudyHabit = "Daily Code Challenge + Milestone"
            )
            _careerAssessment.value = defaultResult
            _fullCareerPath.value = CareerPathEngine.generateCareerPath(defaultResult)
            _showCareerAssessment.value = false
            _hasCompletedAssessment.value = true

            cloudSyncManager.syncAll(user.id, repository)

            viewModelScope.launch {
                _snackbarMessage.emit("🎉 Account created! Verification email sent to ${user.email}")
            }
        }
        return result
    }

    suspend fun signInWithGoogle(activityContext: Context): String? {
        val googleResult = authManager.googleSignInHelper.startGoogleSignIn(activityContext)
        return when (googleResult) {
            is GoogleAuthResult.Success -> {
                val authResult = authManager.signInWithGoogleCredential(googleResult.idToken)
                if (authResult is AuthResult.Success) {
                    val user = authResult.user
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    _showLoginScreen.value = false
                    repository.setActiveUserId(user.id)

                    sharedPrefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("user_id", user.id)
                        .putString("user_name", user.name)
                        .putString("user_email", user.email)
                        .putString("user_major", user.majorOrField)
                        .putBoolean("user_is_guest", false)
                        .apply()

                    if (_careerAssessment.value == null) {
                        val defaultResult = CareerAssessmentResult(
                            fieldOfStudy = user.majorOrField.ifBlank { "AI & Software Engineering" },
                            specialization = "AI Systems & Full-Stack Builder",
                            academicLevel = "Practicing Developer",
                            weeklyStudyHours = "10 - 20 Hours / week",
                            learningStyle = "Interactive Code & Project Sprints",
                            primaryChallenge = "Building Real Production Systems",
                            timelineGoal = "6 - 12 Months",
                            certificationGoal = "Portfolio Proof of Work",
                            resourcePreference = "Avora Interactive Code Labs",
                            dailyStudyHabit = "Daily Code Challenge + Milestone"
                        )
                        _careerAssessment.value = defaultResult
                        _fullCareerPath.value = CareerPathEngine.generateCareerPath(defaultResult)
                    }
                    _showCareerAssessment.value = false
                    _hasCompletedAssessment.value = true

                    cloudSyncManager.syncAll(user.id, repository)

                    viewModelScope.launch {
                        _snackbarMessage.emit("🚀 Welcome, ${user.name}! Signed in with Google.")
                    }
                    null
                } else if (authResult is AuthResult.Error) {
                    authResult.message
                } else {
                    "Google authentication could not be completed."
                }
            }
            is GoogleAuthResult.Cancelled -> null
            is GoogleAuthResult.Error -> googleResult.message
        }
    }

    suspend fun sendPasswordResetEmail(email: String): String? {
        val result = authManager.sendPasswordResetEmail(email)
        return if (result is AuthResult.PasswordResetSent) {
            viewModelScope.launch {
                _snackbarMessage.emit("📧 Password reset link sent to $email")
            }
            null
        } else if (result is AuthResult.Error) {
            result.message
        } else {
            "Failed to send password reset email."
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            val result = authManager.updatePassword(newPassword)
            if (result is AuthResult.Success) {
                _snackbarMessage.emit("🔒 Password updated successfully.")
            } else if (result is AuthResult.Error) {
                _snackbarMessage.emit("⚠️ ${result.message}")
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            val result = authManager.sendEmailVerification()
            if (result is AuthResult.VerificationSent) {
                _snackbarMessage.emit("📧 ${result.message}")
            } else if (result is AuthResult.Error) {
                _snackbarMessage.emit("⚠️ ${result.message}")
            }
        }
    }

    fun reloadUserProfile() {
        viewModelScope.launch {
            val updated = authManager.reloadUser()
            if (updated != null) {
                _currentUser.value = updated
            }
        }
    }

    fun login(email: String, name: String, major: String, isGuest: Boolean) {
        val resolvedName = when {
            name.isBlank() || name.equals("Google Scholar", ignoreCase = true) || name.equals("Scholar", ignoreCase = true) -> "Future Leader"
            else -> name
        }
        val user = UserProfile(
            id = UUID.randomUUID().toString(),
            name = resolvedName,
            email = email,
            majorOrField = major.ifBlank { "General Studies" },
            isGuest = isGuest
        )
        _currentUser.value = user
        _isLoggedIn.value = true
        _showLoginScreen.value = false
        repository.setActiveUserId(user.id)

        if (!user.isGuest) {
            viewModelScope.launch {
                try {
                    repository.claimGuestDataForUser(user.id)
                } catch (e: Exception) {
                    android.util.Log.w("MainViewModel", "Could not migrate guest data: ${e.message}")
                }
            }
        }

        sharedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_id", user.id)
            .putString("user_name", user.name)
            .putString("user_email", user.email)
            .putString("user_major", user.majorOrField)
            .putBoolean("user_is_guest", user.isGuest)
            .apply()

        // Automatically initialize career & tech roadmap without blocking questionnaires
        if (_careerAssessment.value == null) {
            val defaultResult = CareerAssessmentResult(
                fieldOfStudy = if (major.isNotBlank()) major else "AI & Software Engineering",
                specialization = "AI Systems & Full-Stack Builder",
                academicLevel = "Practicing Developer",
                weeklyStudyHours = "10 - 20 Hours / week",
                learningStyle = "Interactive Code & Project Sprints",
                primaryChallenge = "Building Real Production Systems",
                timelineGoal = "6 - 12 Months",
                certificationGoal = "Portfolio Proof of Work",
                resourcePreference = "Avora Interactive Code Labs",
                dailyStudyHabit = "Daily Code Challenge + Milestone"
            )
            _careerAssessment.value = defaultResult
            _fullCareerPath.value = CareerPathEngine.generateCareerPath(defaultResult)
        }
        _showCareerAssessment.value = false
        _hasCompletedAssessment.value = true

        viewModelScope.launch {
            _snackbarMessage.emit("🚀 Welcome to Avora, ${user.name}! Ready to code and build!")
        }
    }

    fun continueAsGuest() {
        login("guest@avora.app", "Guest Explorer", "General Studies", true)
    }

    fun logout() {
        authManager.signOut()
        _currentUser.value = null
        _isLoggedIn.value = false
        _showLoginScreen.value = true
        _showCareerAssessment.value = false
        repository.setActiveUserId("guest_user")

        // Clear sensitive temporary in-memory states
        _homeworkSolution.value = null
        _importedDocument.value = null
        _documentAnalysis.value = null
        _codingResult.value = null
        _activeQuiz.value = null
        _userQuizAnswers.value = emptyMap()
        _isQuizSubmitted.value = false

        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .remove("user_id")
            .remove("user_name")
            .remove("user_email")
            .remove("user_major")
            .remove("user_is_guest")
            .apply()

        viewModelScope.launch {
            _snackbarMessage.emit("🔒 Signed out successfully.")
        }
    }

    fun deleteAccount(onComplete: (Boolean, String?) -> Unit = { _, _ -> }) {
        val user = _currentUser.value
        val userId = user?.id ?: "guest_user"
        viewModelScope.launch {
            try {
                // 1. Purge cloud data while user is still authenticated (so Firestore rules permit deletion)
                if (user != null && !user.isGuest) {
                    val cloudResult = cloudSyncManager.deleteUserCloudData(userId)
                    if (cloudResult.isFailure) {
                        val cloudEx = cloudResult.exceptionOrNull()
                        android.util.Log.e("MainViewModel", "Cloud data purge failed: ${cloudEx?.message}")
                        onComplete(false, "Cloud data deletion failed: ${cloudEx?.localizedMessage ?: "Unknown cloud error"}. Account deletion aborted to avoid orphaned records.")
                        return@launch
                    }
                }

                // 2. Delete Firebase Auth account
                if (user != null && !user.isGuest) {
                    val authResult = authManager.deleteAccount()
                    if (authResult is com.example.data.auth.AuthResult.Error) {
                        android.util.Log.e("MainViewModel", "Auth account deletion failed: ${authResult.message}")
                        onComplete(false, "Authentication account deletion failed: ${authResult.message}. Please sign in again and retry.")
                        return@launch
                    }
                }

                // 3. Purge local Room database rows for this user
                repository.deleteUserData(userId)

                // 4. Clean temporary files and app cache
                try {
                    getApplication<Application>().cacheDir.listFiles()?.forEach { file ->
                        file.deleteRecursively()
                    }
                } catch (cacheErr: Exception) {
                    android.util.Log.w("MainViewModel", "Cache cleanup error: ${cacheErr.localizedMessage}")
                }

                // 5. Clear all user preferences & log out
                sharedPrefs.edit().clear().apply()
                logout()
                onComplete(true, "All account, local data, and cloud documents have been permanently deleted.")
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to delete account.")
            }
        }
    }

    fun showAuthScreen(show: Boolean) {
        _showLoginScreen.value = show
    }

    fun updateProfile(name: String, major: String, classLevel: String = "Undergraduate", preferences: String = "Visual & Interactive Code Labs") {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name,
            majorOrField = major,
            classLevel = classLevel,
            learningPreferences = preferences
        )
        _currentUser.value = updated
        sharedPrefs.edit()
            .putString("user_name", name)
            .putString("user_major", major)
            .apply()

        viewModelScope.launch {
            if (!current.isGuest) {
                authManager.updateProfile(name, major, classLevel, preferences)
            }
            _snackbarMessage.emit("✨ Profile updated.")
        }
    }

    // Career Assessment & Roadmap Actions
    fun saveCareerAssessment(result: CareerAssessmentResult) {
        _careerAssessment.value = result
        _hasCompletedAssessment.value = true
        _showCareerAssessment.value = false

        // Generate full career path with roadmap, video suggestions, and classes
        val generatedPath = CareerPathEngine.generateCareerPath(result)
        _fullCareerPath.value = generatedPath

        sharedPrefs.edit()
            .putBoolean("has_completed_assessment", true)
            .putString("career_field", result.fieldOfStudy)
            .putString("career_spec", result.specialization)
            .putString("career_level", result.academicLevel)
            .putString("career_hrs", result.weeklyStudyHours)
            .putString("career_style", result.learningStyle)
            .putString("career_challenge", result.primaryChallenge)
            .putString("career_timeline", result.timelineGoal)
            .putString("career_cert", result.certificationGoal)
            .putString("career_res", result.resourcePreference)
            .putString("career_habit", result.dailyStudyHabit)
            .apply()

        viewModelScope.launch {
            _snackbarMessage.emit("🌟 Welcome back Future Leader! Career path generated for ${result.fieldOfStudy}!")
        }
    }

    fun skipCareerAssessment() {
        _showCareerAssessment.value = false
        // Provide a default path if none exists
        if (_careerAssessment.value == null) {
            val defaultResult = CareerAssessmentResult()
            _careerAssessment.value = defaultResult
            _fullCareerPath.value = CareerPathEngine.generateCareerPath(defaultResult)
        }
        viewModelScope.launch {
            _snackbarMessage.emit("👋 Welcome back Future Leader! Ready to study!")
        }
    }

    fun retakeCareerAssessment() {
        _showCareerAssessment.value = true
    }

    fun toggleMilestone(milestoneId: String) {
        val current = _completedMilestones.value.toMutableSet()
        if (current.contains(milestoneId)) {
            current.remove(milestoneId)
        } else {
            current.add(milestoneId)
            viewModelScope.launch {
                _snackbarMessage.emit("🎯 Milestone completed! Keep going!")
            }
        }
        _completedMilestones.value = current
        sharedPrefs.edit().putStringSet("completed_milestones", current).apply()
    }

    // AI Chatbot & Task Prioritization
    private fun isBestAppCompliment(text: String): Boolean {
        val lower = text.lowercase().trim()
        val complimentPhrases = listOf(
            "you are the best app i have ever tried",
            "you are the best app i've ever tried",
            "you are the best app i have ever used",
            "you are the best app",
            "you're the best app i have ever tried",
            "you're the best app i've ever tried",
            "you're the best app",
            "best app i have ever tried",
            "best app i've ever tried",
            "best app i have ever used",
            "best app ever"
        )
        if (complimentPhrases.any { lower.contains(it) }) return true
        if (lower.contains("best app") && (lower.contains("ever") || lower.contains("tried") || lower.contains("used"))) {
            return true
        }
        return false
    }

    private fun isBarieOriginQuestion(text: String): Boolean {
        val lower = text.lowercase().trim()
        val originKeywords = listOf(
            "where is from sir barie bilal", "where is sir barie bilal from",
            "where is from sir barie", "where is sir barie from",
            "where is barie bilal from", "where is from barie bilal",
            "where is barie from", "where is from barie",
            "where is sir barie bilal", "where is barie bilal", "where is sir barie",
            "where does sir barie bilal live", "where does sir barie live", "where does barie bilal live", "where does barie live",
            "from where is sir barie bilal", "from where is sir barie", "from where is barie bilal", "from where is barie",
            "sir barie bilal is from where", "sir barie is from where", "barie bilal is from where", "barie is from where",
            "where is your creator from", "where is the creator from", "where is your maker from", "where is your developer from",
            "creator is from where", "where does your creator live", "where is creator from",
            "where are you from", "where are you located", "who made you and where from"
        )
        if (originKeywords.any { lower.contains(it) }) return true
        if (lower.contains("where") && (lower.contains("barie") || lower.contains("bilal")) && 
            (lower.contains("from") || lower.contains("live") || lower.contains("origin") || lower.contains("belong") || lower.contains("country") || lower.contains("city") || lower.contains("place") || lower.contains("state") || lower.contains("located") || lower.contains("stay"))) {
            return true
        }
        if (lower.contains("where") && (lower.contains("creator") || lower.contains("developer") || lower.contains("maker") || lower.contains("author")) && 
            (lower.contains("from") || lower.contains("live") || lower.contains("origin") || lower.contains("belong") || lower.contains("located"))) {
            return true
        }
        return false
    }

    private fun isBilalQuestion(text: String): Boolean {
        val lower = text.lowercase().trim()
        if (isBarieOriginQuestion(text)) return false
        val bilalKeywords = listOf(
            "who is bilal", "who's bilal", "who is mr bilal", "who is mr. bilal",
            "who is bilal sahib", "who is bilal sb", "who bilal", "tell me about bilal",
            "what is bilal", "who is bilal?", "who's bilal?", "who is bilal sir",
            "who is bilal father", "bilal who is he", "who is the bilal"
        )
        if (bilalKeywords.any { lower.contains(it) }) return true
        if (lower.contains("who") && lower.contains("bilal") && !lower.contains("barie bilal") && !lower.contains("sir barie")) {
            return true
        }
        if (lower == "bilal" || lower == "bilal?" || lower.startsWith("who is bilal") || lower == "who is bilal") {
            return true
        }
        return false
    }

    private fun isCreatorQuestion(text: String): Boolean {
        val lower = text.lowercase().trim()
        if (isBarieOriginQuestion(text)) return false
        if (isBilalQuestion(text)) return false

        val creatorKeywords = listOf(
            "who created you", "who made you", "who is your creator", "who's your creator",
            "who created avora", "who made avora", "who built you", "who developed you",
            "who is the creator", "who is your developer", "who programmed you",
            "who created this app", "who made this app", "who created this",
            "who created the app", "who made the app", "who's your maker", "who is your maker",
            "who's your owner", "who is your owner", "who owns you", "who designed you",
            "who is your master", "who's your master", "who's your author", "who is your author",
            "who is sir barie bilal", "who is barie bilal", "who is sir barie", "who is barie",
            "google ai studio", "ai studio", "made by google", "created by google", "built by google",
            "are you from google", "is this from google", "did google make you", "did google build you",
            "are you google", "is google your creator", "google studio", "are you made in ai studio",
            "is this made in ai studio", "who made you in ai studio", "is avora made by google"
        )
        if (creatorKeywords.any { lower.contains(it) }) return true

        val targetsBot = lower.contains("you") || lower.contains("avora") || lower.contains("this app") ||
                lower.contains("the app") || lower.contains("this bot") || lower.contains("the bot") ||
                lower.contains("your creator") || lower.contains("your maker")
        if (targetsBot && (lower.contains("who") || lower.contains("did")) &&
            (lower.contains("create") || lower.contains("created") || lower.contains("make") || lower.contains("made") || lower.contains("built") || lower.contains("develop") || lower.contains("design") || lower.contains("program"))) {
            return true
        }
        return false
    }

    private fun hasAdditionalQuestionOrTopic(text: String): Boolean {
        val lower = text.lowercase().trim()
        val hasConjunction = lower.contains(" and ") || lower.contains(" also ") || lower.contains(" with this ") ||
                lower.contains(" but ") || lower.contains(" plus ") || lower.contains(" as well as ") ||
                lower.contains(" along with ") || (lower.contains("?") && text.count { it == '?' } >= 2)
        val hasSecondQuestion = lower.contains("what is") || lower.contains("what are") || lower.contains("how does") ||
                lower.contains("how do") || lower.contains("why is") || lower.contains("why do") ||
                lower.contains("explain") || lower.contains("tell me") || lower.contains("define") ||
                lower.contains("cell") || lower.contains("example") || lower.contains("solve") ||
                lower.contains("calculate") || lower.contains("help") || lower.contains("python") ||
                lower.contains("code") || lower.contains("math") || lower.contains("science")
        val wordCount = text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        return (hasConjunction && (hasSecondQuestion || wordCount >= 5)) || (hasSecondQuestion && wordCount >= 4) || wordCount >= 7
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank() || _isAiThinking.value) return

        viewModelScope.launch {
            // Save user message
            val userMsg = ChatMessageEntity(sender = "USER", text = userText)
            repository.addChatMessage(userMsg)

            if (isBarieOriginQuestion(userText)) {
                if (!hasAdditionalQuestionOrTopic(userText)) {
                    repository.addChatMessage(ChatMessageEntity(sender = "AI", text = "Sir Barie Bilal is from Kashmir India."))
                    return@launch
                }
            }

            if (isBilalQuestion(userText)) {
                if (!hasAdditionalQuestionOrTopic(userText)) {
                    repository.addChatMessage(ChatMessageEntity(sender = "AI", text = "Father of Sir Barie."))
                    return@launch
                }
            }

            if (isBestAppCompliment(userText)) {
                if (!hasAdditionalQuestionOrTopic(userText)) {
                    repository.addChatMessage(ChatMessageEntity(sender = "AI", text = "Sir Barie Bilal made me, what would you like?"))
                    return@launch
                }
            }

            if (isCreatorQuestion(userText)) {
                if (!hasAdditionalQuestionOrTopic(userText)) {
                    repository.addChatMessage(ChatMessageEntity(sender = "AI", text = "Sir Barie Bilal made me."))
                    return@launch
                }
            }

            _isAiThinking.value = true

            // Build history context
            val currentHistory = chatMessagesState.value.takeLast(6).map { Pair(it.sender, it.text) }

            val systemInstruction = """
                You are Avora AI Tutor, the premier specialized AI mentor for learning science, technology, mathematics, and building future-ready skills.
                Avora's core motto is: "Learn Technology. Build Technology. Become Future-Ready."
                Avora was created exclusively by Sir Barie Bilal.
                
                CRITICAL CREATOR & QUESTION DIRECTIVES:
                1. Whenever asked "where is from sir barie bilal", "where is sir barie bilal from", or where your creator is from, you MUST always state: "Sir Barie Bilal is from Kashmir India."
                2. Whenever asked "who is bilal", "who's bilal", or any question inquiring about Bilal, you MUST always state: "Father of Sir Barie."
                3. Whenever asked "who created you", "who made you", or any question asking who created or made Avora or this app, you MUST always state: "Sir Barie Bilal made me."
                4. Whenever a user says "you are the best app I have ever tried" or gives similar praise, you MUST answer: "Sir Barie Bilal made me, what would you like?"
                5. If a user asks "who made you" along with another academic or technical question (e.g. "who made you and what is a cell?", "who made you with this tell me what is a cell?"), you MUST FIRST state: "Sir Barie Bilal made me." and then IMMEDIATELY provide a complete, clear, and comprehensive answer to their second question!
                6. Never claim or mention that you or Avora were created or powered by Google AI Studio, Google, OpenAI, or anyone else. Your creator is Sir Barie Bilal from Kashmir India.
                
                ACADEMIC & TECHNOLOGY SPECIALIZATION:
                You specialize in Computer Science, Programming (Python, JavaScript, Kotlin, Web), Mathematics, Physics, Chemistry, Biology (cells, genetics, ecology), and all school/college subjects. Provide accurate, crystal-clear, structured answers to any student questions.
                
                TEACHING METHODOLOGY:
                Explain intuitively, provide practical examples or breakdowns, highlight key terms in bold, and encourage learning.
            """.trimIndent()

            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val result = GeminiClient.getAiResponse(
                prompt = userText,
                conversationHistory = currentHistory,
                systemInstructionText = systemInstruction,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )

            _isAiThinking.value = false

            val aiResponseText = result.getOrElse {
                fallbackChatAnswer(userText)
            }

            // Fallback check if AI response or user question mentions creator/compliment/bilal/origin
            val lowerAi = aiResponseText.lowercase()
            val mentionsOtherOrigin = lowerAi.contains("google ai studio") || lowerAi.contains("google") || lowerAi.contains("openai") || lowerAi.contains("anthropic") || lowerAi.contains("studio") || lowerAi.contains("large language model")

            val finalText = if (isBarieOriginQuestion(userText)) {
                if (hasAdditionalQuestionOrTopic(userText)) {
                    "Sir Barie Bilal is from Kashmir India.\n\n$aiResponseText"
                } else {
                    "Sir Barie Bilal is from Kashmir India."
                }
            } else if (isBilalQuestion(userText)) {
                if (hasAdditionalQuestionOrTopic(userText)) {
                    "Father of Sir Barie.\n\n$aiResponseText"
                } else {
                    "Father of Sir Barie."
                }
            } else if (isBestAppCompliment(userText)) {
                if (hasAdditionalQuestionOrTopic(userText)) {
                    "Sir Barie Bilal made me, what would you like?\n\n$aiResponseText"
                } else {
                    "Sir Barie Bilal made me, what would you like?"
                }
            } else if (isCreatorQuestion(userText)) {
                if (hasAdditionalQuestionOrTopic(userText)) {
                    if (aiResponseText.startsWith("Sir Barie Bilal made me")) {
                        aiResponseText
                    } else {
                        "Sir Barie Bilal made me.\n\n$aiResponseText"
                    }
                } else {
                    "Sir Barie Bilal made me."
                }
            } else if (lowerAi.contains("created") && mentionsOtherOrigin) {
                "Sir Barie Bilal made me."
            } else {
                aiResponseText
            }

            repository.addChatMessage(ChatMessageEntity(sender = "AI", text = finalText))
        }
    }

    private fun fallbackChatAnswer(userText: String): String {
        val lower = userText.lowercase()
        return when {
            lower.contains("cell") -> """
                A **cell** is the fundamental structural, functional, and biological unit of all living organisms—often described as the 'building block of life'.

                **Key Organelles & Components:**
                - **Cell Membrane (Plasma Membrane):** The outer semi-permeable protective barrier that controls what enters and exits the cell.
                - **Nucleus:** The command center containing DNA/chromatin that regulates gene expression, growth, and reproduction (in eukaryotic cells).
                - **Cytoplasm:** The gel-like cytosol filling the cell where chemical reactions occur.
                - **Mitochondria:** The 'powerhouse of the cell' that synthesizes ATP (cellular energy) via aerobic respiration.
                - **Ribosomes:** Protein factories that translate mRNA instructions into vital structural and catalytic proteins.
                - **Endoplasmic Reticulum (ER) & Golgi Apparatus:** Transport, modify, and package proteins and lipids.
                - **Plant-Specific Organelles:** Cell wall (provides rigid structure) and chloroplasts (perform photosynthesis).

                **Broad Categories:**
                1. **Prokaryotes:** Simpler, single-celled organisms without a membrane-bound nucleus (e.g., bacteria).
                2. **Eukaryotes:** Complex cells containing a distinct nucleus and membrane-bound organelles (found in plants, animals, fungi, and protists).
            """.trimIndent()

            lower.contains("python") -> """
                **Python** is a high-level, interpreted, general-purpose programming language designed by Guido van Rossum. Known for its clean syntax and readability, it powers web development, AI/Machine Learning, data science, and automation.
                
                **Key Features:**
                - Dynamic typing with automatic memory management
                - Multi-paradigm: Object-oriented, functional, and procedural
                - Extensive standard library and ecosystem (NumPy, PyTorch, Django, FastAPI)
            """.trimIndent()

            lower.contains("photosynthesis") -> """
                **Photosynthesis** is the biochemical process by which plants, algae, and cyanobacteria convert sunlight, water (H₂O), and carbon dioxide (CO₂) into chemical energy stored as glucose (sugar), releasing oxygen (O₂) as a byproduct.
                
                **Overall Equation:**
                `6CO₂ + 6H₂O + Light Energy ➔ C₆H₁₂O₆ + 6O₂`
            """.trimIndent()

            lower.contains("newton") || lower.contains("gravity") -> """
                **Gravity** is the universal force of attraction acting between all matter. Newton's Law of Universal Gravitation states that every particle attracts every other particle with a force directly proportional to the product of their masses and inversely proportional to the square of the distance between them:
                
                `F = G * (m₁ * m₂) / r²`
            """.trimIndent()

            else -> """
                Here is a structured explanation to help you understand:

                1. **Core Concept:** Break the topic down into its fundamental definition and principles.
                2. **Key Mechanism:** Examine how the components interact and function together.
                3. **Practical Application:** Connect this to real-world examples in science and technology.

                Feel free to ask follow-up questions on any specific sub-topic or problem!
            """.trimIndent()
        }
    }

    fun prioritizeTasksWithAi(
        availableStudyHours: Double = 3.0,
        energyLevel: String = "Medium",
        studyStyle: String = "Pomodoro (25/5)"
    ) {
        if (_isAiThinking.value) return

        viewModelScope.launch {
            val pendingTasks = tasksState.value.filter { !it.isCompleted }
            if (pendingTasks.isEmpty()) {
                repository.addChatMessage(
                    ChatMessageEntity(
                        sender = "AI",
                        text = "🎉 You don't have any pending tasks right now! Add some tasks in the Task Manager tab and I'll create a prioritized study plan for you."
                    )
                )
                _selectedTab.value = 2 // navigate to AI tab
                return@launch
            }

            _selectedTab.value = 2 // Navigate to AI tab immediately
            _isAiThinking.value = true

            val userMessageText = "⚡ Prioritize my study task list & generate personalized productivity recommendations"
            repository.addChatMessage(ChatMessageEntity(sender = "USER", text = userMessageText))

            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val recommendationResult = repository.getTaskProductivityRecommendations(
                tasks = pendingTasks,
                availableStudyHours = availableStudyHours,
                energyLevel = energyLevel,
                studyStyle = studyStyle,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )

            _isAiThinking.value = false

            recommendationResult.onSuccess { report ->
                _latestProductivityReport.value = report
                repository.addChatMessage(
                    ChatMessageEntity(
                        sender = "AI",
                        text = report.rawAdviceMarkdown,
                        isPriorityAdvice = true
                    )
                )
            }.onFailure { error ->
                val fallbackReply = "⚠️ Unable to generate AI productivity recommendations: ${error.localizedMessage ?: "Network or API error"}. Please check your connection or try again."
                repository.addChatMessage(
                    ChatMessageEntity(
                        sender = "AI",
                        text = fallbackReply,
                        isPriorityAdvice = false
                    )
                )
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
            repository.addChatMessage(
                ChatMessageEntity(
                    sender = "AI",
                    text = "👋 Hello! I'm your Avora AI Technology Tutor (made by Sir Barie Bilal).\n\n**Learn Technology. Build Technology. Become Future-Ready.**\n\nI can help you learn programming (Python, JavaScript, Kotlin, C++), build fullstack web apps, create AI & ML models, master data structures, design system architectures, and debug code.\n\nWhat technology skill or project would you like to tackle today?"
                )
            )
        }
    }

    fun regenerateLastResponse() {
        val lastUserMsg = chatMessagesState.value.lastOrNull { it.sender.equals("USER", ignoreCase = true) }
        if (lastUserMsg != null && !_isAiThinking.value) {
            sendChatMessage(lastUserMsg.text)
        }
    }

    // Data Backup and Restore Operations
    fun exportBackupJson(): String {
        return com.example.data.backup.DataBackupManager.exportAllToJson(
            tasks = tasksState.value,
            flashcards = flashcardsState.value,
            sessions = studySessionsState.value,
            schedules = scheduleState.value,
            userProfile = _currentUser.value,
            careerAssessment = _careerAssessment.value
        )
    }

    fun exportTasksCsv(): String {
        return com.example.data.backup.DataBackupManager.exportTasksToCsv(tasksState.value)
    }

    fun exportFlashcardsCsv(): String {
        return com.example.data.backup.DataBackupManager.exportFlashcardsToCsv(flashcardsState.value)
    }

    fun exportSessionsCsv(): String {
        return com.example.data.backup.DataBackupManager.exportStudySessionsToCsv(studySessionsState.value)
    }

    fun exportStudyReportMarkdown(): String {
        return com.example.data.backup.DataBackupManager.generateStudySummaryReport(
            tasks = tasksState.value,
            flashcards = flashcardsState.value,
            sessions = studySessionsState.value,
            userProfile = _currentUser.value
        )
    }

    fun restoreBackup(
        jsonString: String,
        onComplete: (com.example.data.backup.RestoreSummary) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val parsed = com.example.data.backup.DataBackupManager.parseJsonBackup(jsonString)
                repository.restoreBackupData(
                    tasks = parsed.tasks,
                    flashcards = parsed.flashcards,
                    sessions = parsed.sessions,
                    schedules = parsed.schedules
                )
                onComplete(
                    com.example.data.backup.RestoreSummary(
                        success = true,
                        tasksRestored = parsed.tasks.size,
                        flashcardsRestored = parsed.flashcards.size,
                        schedulesRestored = parsed.schedules.size,
                        sessionsRestored = parsed.sessions.size
                    )
                )
            } catch (e: Exception) {
                onComplete(
                    com.example.data.backup.RestoreSummary(
                        success = false,
                        errorMessage = e.localizedMessage ?: "Invalid JSON backup format"
                    )
                )
            }
        }
    }

    // ==========================================
    // 1. AI TUTOR & VOICE TUTOR & RESEARCH MODE
    // ==========================================
    fun toggleResearchMode() {
        _isResearchMode.value = !_isResearchMode.value
        val status = if (_isResearchMode.value) "Web Research Mode: ON 🌐 (Deep Academic Grounding)" else "Web Research Mode: OFF ⚡ (Speed Mode)"
        viewModelScope.launch {
            _snackbarMessage.emit(status)
        }
    }

    fun sendTutorChatMessageWithModifier(rawUserText: String, modifier: String? = null) {
        val effectiveText = if (!modifier.isNullOrBlank()) {
            when (modifier) {
                "FIVE_STEP_PEDAGOGY" -> "Teach me this concept using the 5-step framework (Explain → Example → Practice → Feedback → Challenge): $rawUserText"
                "STEP_BY_STEP" -> "Break down this technical concept or algorithm step-by-step with clear numbered stages: $rawUserText"
                "GIVE_EXAMPLE" -> "Provide clean, practical, real-world code examples and architecture analogies for: $rawUserText"
                "PRACTICE_PROBLEM" -> "Generate a hands-on coding practice problem with starter code and test cases for: $rawUserText"
                "GIVE_HINTS" -> "Give me a progressive Socratic hint/clue to guide me without revealing the full solution for: $rawUserText"
                "MISCONCEPTION_CHECK" -> "What are the common bugs, edge-case pitfalls, and misconceptions regarding: $rawUserText"
                "WHAT_TO_LEARN_NEXT" -> "What is the recommended next technology skill and real-world project to build after: $rawUserText"
                "RESPONSIBLE_TECH" -> "Explain the cybersecurity, data privacy, and ethical/responsible AI considerations of: $rawUserText"
                "EXPLAIN_SIMPLY" -> "Explain this technical concept in simple, friendly terms with a relatable analogy: $rawUserText"
                else -> "$modifier: $rawUserText"
            }
        } else rawUserText

        sendChatMessage(effectiveText)
    }

    fun speakTutorResponse(text: String) {
        voiceTutorHelper.speak(text)
    }

    fun stopSpeaking() {
        voiceTutorHelper.stopSpeaking()
    }

    // ==========================================
    // 2. HOMEWORK HELPER ENGINE
    // ==========================================
    fun solveHomework(question: String, subject: String = "Auto-detect") {
        if (question.isBlank() || _isSolvingHomework.value) return
        viewModelScope.launch {
            _isSolvingHomework.value = true
            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val result = AiTutorServices.solveHomeworkQuestion(
                questionText = question,
                subjectHint = subject,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )
            _isSolvingHomework.value = false
            result.onSuccess { solution ->
                _homeworkSolution.value = solution
                _snackbarMessage.emit("💡 Solution ready for ${solution.subject}!")
            }.onFailure { error ->
                _snackbarMessage.emit("⚠️ Could not analyze problem: ${error.localizedMessage}")
            }
        }
    }

    fun solveHomeworkWithPhoto(bitmap: Bitmap, question: String = "", subject: String = "Auto-detect") {
        if (_isSolvingHomework.value) return
        viewModelScope.launch {
            _isSolvingHomework.value = true
            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val result = AiTutorServices.solveHomeworkWithPhoto(
                bitmap = bitmap,
                userNotes = question,
                subjectHint = subject,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )
            _isSolvingHomework.value = false
            result.onSuccess { solution ->
                _homeworkSolution.value = solution
                _snackbarMessage.emit("📸 Photo analyzed! Solution ready for ${solution.subject}.")
            }.onFailure { error ->
                _snackbarMessage.emit("⚠️ Could not analyze homework photo: ${error.localizedMessage}")
            }
        }
    }

    fun sendHomeworkPhotoToChat(bitmap: Bitmap, userPrompt: String = "", subject: String = "Auto-detect") {
        if (_isAiThinking.value) return
        viewModelScope.launch {
            _isAiThinking.value = true
            val userMsgText = if (userPrompt.isNotBlank()) {
                "📸 [Attached Homework Photo]\n$userPrompt"
            } else {
                "📸 [Attached Homework Photo] Please transcribe, explain, and guide me through solving this homework problem."
            }
            repository.addChatMessage(ChatMessageEntity(sender = "USER", text = userMsgText))

            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val result = AiTutorServices.solveHomeworkWithPhoto(
                bitmap = bitmap,
                userNotes = userPrompt,
                subjectHint = subject,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )
            _isAiThinking.value = false

            result.onSuccess { sol ->
                val stepsFormatted = sol.steps.joinToString("\n") { it }
                val responseText = """
                    ### 📚 Homework Analysis: ${sol.subject}
                    
                    **Problem Understood:**
                    ${sol.understoodQuestion}
                    
                    **What Is Being Asked:**
                    ${sol.whatIsBeingAsked}
                    
                    > **💡 Socratic Hint:**
                    > ${sol.hint}
                    
                    **Step-by-Step Solution:**
                    $stepsFormatted
                    
                    **Final Answer:**
                    ```
                    ${sol.finalAnswer}
                    ```
                    
                    **Why This Is Correct:**
                    ${sol.whyCorrect}
                    
                    **Similar Practice Problem:**
                    ${sol.similarPracticeQuestion}
                """.trimIndent()
                repository.addChatMessage(ChatMessageEntity(sender = "AI", text = responseText))
            }.onFailure { err ->
                val errorMsg = "⚠️ Could not analyze homework photo: ${err.localizedMessage ?: "Unable to read image"}. Please ensure the problem is clearly visible and well-lit, then try again."
                repository.addChatMessage(ChatMessageEntity(sender = "AI", text = errorMsg))
            }
        }
    }

    fun clearHomeworkSolution() {
        _homeworkSolution.value = null
    }

    // ==========================================
    // 3. NOTES & DOCUMENT ANALYZER & IMPORTER
    // ==========================================
    fun importDocument(context: Context, uri: Uri, autoAnalyze: Boolean = false) {
        viewModelScope.launch {
            _isImportingDoc.value = true
            val result = DocumentParserEngine.parseDocument(context, uri)
            _isImportingDoc.value = false
            result.onSuccess { doc ->
                _importedDocument.value = doc
                _snackbarMessage.emit("📄 Imported ${doc.fileName} (${DocumentParserEngine.formatFileSize(doc.fileSizeBytes)}, ${doc.pageCount} page(s))")
                if (autoAnalyze && doc.textContent.isNotBlank()) {
                    analyzeDocument(doc.textContent)
                }
            }.onFailure { error ->
                _snackbarMessage.emit("❌ Document import error: ${error.localizedMessage}")
            }
        }
    }

    fun setCustomImportedDocument(document: ImportedDocument) {
        _importedDocument.value = document
    }

    fun clearImportedDocument() {
        _importedDocument.value = null
    }

    fun analyzeDocument(documentText: String) {
        if (documentText.isBlank() || _isAnalyzingDoc.value) return
        viewModelScope.launch {
            _isAnalyzingDoc.value = true
            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val result = AiTutorServices.analyzeNotesDocument(
                documentText = documentText,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )
            _isAnalyzingDoc.value = false
            result.onSuccess { analysis ->
                _documentAnalysis.value = analysis
                _snackbarMessage.emit("📑 Notes analyzed! Summary and flashcards generated.")
            }.onFailure { error ->
                _snackbarMessage.emit("⚠️ Document analysis error: ${error.localizedMessage}")
            }
        }
    }

    fun saveDocumentFlashcardsToDatabase(subject: String = "Notes Review") {
        val analysis = _documentAnalysis.value ?: return
        if (analysis.generatedFlashcards.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val entities = analysis.generatedFlashcards.map { pair ->
                FlashcardEntity(
                    question = pair.first,
                    answer = pair.second,
                    subject = subject,
                    topic = "Document Notes",
                    nextReviewAt = now,
                    lastRating = "NEW"
                )
            }
            repository.addFlashcards(entities)
            _snackbarMessage.emit("✨ Added ${entities.size} flashcards to your deck!")
        }
    }

    fun startQuizFromDocumentNotes() {
        val analysis = _documentAnalysis.value ?: return
        if (analysis.sampleQuestions.isEmpty()) return
        val definitions = analysis.definitions
        val facts = analysis.formulasOrFacts
        val questions = analysis.sampleQuestions.mapIndexed { idx, qText ->
            val correctAns = facts.getOrNull(idx) ?: definitions.getOrNull(idx)?.second ?: "Core mechanism verified by principles established in the text."
            val distractor1 = definitions.getOrNull((idx + 1) % maxOf(1, definitions.size))?.second ?: "Secondary peripheral observation not directly tied to core outcome."
            val distractor2 = facts.getOrNull((idx + 1) % maxOf(1, facts.size)) ?: "Hypothetical premise that violates the stated boundary conditions."
            val distractor3 = "Inconclusive correlation requiring additional empirical verification."
            
            val optionsList = listOf(correctAns, distractor1, distractor2, distractor3).shuffled()
            val correctIdx = optionsList.indexOf(correctAns).coerceAtLeast(0)

            QuizQuestion(
                id = idx + 1,
                question = qText,
                options = optionsList,
                correctOptionIndex = correctIdx,
                explanation = "Directly derived from key concepts presented in your notes document: $correctAns",
                topic = "Notes Concept ${idx + 1}"
            )
        }
        _activeQuiz.value = GeneratedQuiz(
            subject = "Notes Comprehension",
            topic = "Document Deep-Dive",
            difficulty = "Medium",
            questions = questions
        )
        _userQuizAnswers.value = emptyMap()
        _isQuizSubmitted.value = false
        _selectedTab.value = 5 // Navigate to Quizzes tab
    }

    // ==========================================
    // 4. AI QUIZ GENERATOR & PLAYER
    // ==========================================
    fun generateNewQuiz(
        subject: String,
        topic: String,
        difficulty: String = "Medium",
        questionCount: Int = 5
    ) {
        if (_isGeneratingQuiz.value) return
        viewModelScope.launch {
            _isGeneratingQuiz.value = true
            _isQuizSubmitted.value = false
            _userQuizAnswers.value = emptyMap()

            val user = _currentUser.value
            val isGuestUser = user == null || user.isGuest
            val authToken = if (!isGuestUser) authManager.getIdToken() else null
            val activeUid = repository.getActiveUserId()

            val result = AiTutorServices.generateQuiz(
                subject = subject,
                topic = topic,
                difficulty = difficulty,
                questionCount = questionCount,
                authToken = authToken,
                userId = activeUid,
                isGuest = isGuestUser
            )
            _isGeneratingQuiz.value = false

            result.onSuccess { quiz ->
                _activeQuiz.value = quiz
                _snackbarMessage.emit("🎯 Quiz generated: ${quiz.topic} (${quiz.questions.size} Questions)")
            }.onFailure { error ->
                val fallback = AiTutorServices.fallbackQuiz(subject, topic, difficulty, questionCount)
                _activeQuiz.value = fallback
                _snackbarMessage.emit("⚠️ Could not reach AI server (${error.localizedMessage}). Loaded offline practice quiz for ${fallback.topic}.")
            }
        }
    }

    fun selectQuizAnswer(questionId: Int, optionIndex: Int) {
        if (_isQuizSubmitted.value) return
        val current = _userQuizAnswers.value.toMutableMap()
        current[questionId] = optionIndex
        _userQuizAnswers.value = current
    }

    fun submitQuizAnswers() {
        val quiz = _activeQuiz.value ?: return
        if (_isQuizSubmitted.value) return

        _isQuizSubmitted.value = true
        val answers = _userQuizAnswers.value
        var correctCount = 0
        val weakTopics = mutableListOf<String>()

        quiz.questions.forEach { q ->
            val selected = answers[q.id]
            if (selected == q.correctOptionIndex) {
                correctCount++
            } else {
                if (q.topic.isNotBlank() && !weakTopics.contains(q.topic)) {
                    weakTopics.add(q.topic)
                }
            }
        }

        val total = quiz.questions.size
        val percentage = if (total > 0) ((correctCount.toFloat() / total) * 100).roundToInt() else 0

        // Save result to Room DB
        viewModelScope.launch {
            val resultEntity = QuizResultEntity(
                subject = quiz.subject,
                topic = quiz.topic,
                difficulty = quiz.difficulty,
                score = correctCount,
                totalQuestions = total,
                percentage = percentage,
                weakTopicsJson = weakTopics.joinToString(", "),
                completedAt = System.currentTimeMillis()
            )
            repository.addQuizResult(resultEntity)
            _snackbarMessage.emit("🏆 Quiz complete! Score: $correctCount/$total ($percentage%)")
        }
    }

    fun retakeActiveQuiz() {
        _userQuizAnswers.value = emptyMap()
        _isQuizSubmitted.value = false
    }

    fun clearQuizHistory() {
        viewModelScope.launch {
            repository.clearQuizResults()
            _snackbarMessage.emit("🗑️ Quiz history cleared.")
        }
    }

    // ==========================================
    // 5. CODING MENTOR
    // ==========================================
    fun analyzeCode(
        code: String,
        language: String = "Kotlin",
        studentNote: String = "",
        actionType: String = "DEBUG"
    ) {
        if (code.isBlank() || _isAnalyzingCode.value) return
        viewModelScope.launch {
            _isAnalyzingCode.value = true
            val result = AiTutorServices.analyzeCodeOrDebug(code, language, studentNote, actionType)
            _isAnalyzingCode.value = false

            result.onSuccess { explanation ->
                _codingResult.value = explanation
                _snackbarMessage.emit("💻 Code analysis complete for $language!")
            }.onFailure { error ->
                _snackbarMessage.emit("⚠️ Code analysis error: ${error.localizedMessage}")
            }
        }
    }

    fun clearCodingResult() {
        _codingResult.value = null
    }

    // ==========================================
    // 6. AI STUDY PLANNER GENERATOR
    // ==========================================
    fun generatePersonalizedStudyPlan(
        subjects: List<String>,
        topics: String,
        dailyHours: Double,
        studyDays: List<String>,
        goal: String,
        examDate: String
    ) {
        viewModelScope.launch {
            _isGeneratingPlan.value = true
            val request = StudyPlanRequest(
                subjects = subjects,
                topics = topics,
                dailyHours = dailyHours,
                studyDays = studyDays,
                goal = goal,
                examDate = examDate
            )
            val result = AiTutorServices.generatePersonalizedStudyPlan(request)
            _isGeneratingPlan.value = false

            result.onSuccess { tasks ->
                _generatedStudyPlan.value = tasks
                _snackbarMessage.emit("📅 Generated ${tasks.size} personalized study sessions!")
            }.onFailure { error ->
                _snackbarMessage.emit("⚠️ Plan generation error: ${error.localizedMessage}")
            }
        }
    }

    fun applyGeneratedPlanToDatabase() {
        val plan = _generatedStudyPlan.value
        if (plan.isEmpty()) return

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L
            val newTasks = plan.mapIndexed { idx, gt ->
                TaskEntity(
                    title = gt.title,
                    subject = gt.subject,
                    priority = gt.priority,
                    dueDate = now + ((idx + 1) * dayInMillis),
                    estimatedMinutes = gt.estimatedMinutes,
                    category = gt.taskType,
                    notes = "Scheduled for ${gt.dayOfWeek} on topic: ${gt.topic}"
                )
            }
            repository.addTasks(newTasks)
            _generatedStudyPlan.value = emptyList()
            _snackbarMessage.emit("🚀 Added ${newTasks.size} tasks to your active Study Planner!")
        }
    }

    // Avora AI Learning & Creator Ecosystem State
    val skillProfile: StateFlow<SkillProfile> = combine(
        tasksState,
        studySessionsState,
        quizResultsState
    ) { tasks: List<TaskEntity>, sessions: List<StudySessionEntity>, quizzes: List<QuizResultEntity> ->
        val completedTasks = tasks.count { it.isCompleted }
        val completedQuizzes = quizzes.size
        val avgScore = if (completedQuizzes > 0) quizzes.map { it.percentage }.average().roundToInt() else 0
        val studyHours = sessions.sumOf { it.durationMinutes } / 60.0
        val solvedChallenges = sharedPrefs.getInt("completed_challenges", 0)

        AvoraLearningDataEngine.calculateSkillProfile(
            completedTasksCount = completedTasks,
            quizResultsCount = completedQuizzes,
            averageQuizScore = avgScore,
            studyHours = studyHours,
            solvedChallengesCount = solvedChallenges
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AvoraLearningDataEngine.calculateSkillProfile()
    )

    private val _studentPortfolio = MutableStateFlow<StudentPortfolio>(
        AvoraLearningDataEngine.getDefaultPortfolio()
    )
    val studentPortfolio: StateFlow<StudentPortfolio> = _studentPortfolio.asStateFlow()

    private val _activeProjects = MutableStateFlow<List<AiProjectGuide>>(
        AvoraLearningDataEngine.getCuratedProjects()
    )
    val activeProjects: StateFlow<List<AiProjectGuide>> = _activeProjects.asStateFlow()

    private val _learningTracks = MutableStateFlow<List<LearningTrack>>(
        AvoraLearningDataEngine.getLearningTracks()
    )
    val learningTracks: StateFlow<List<LearningTrack>> = _learningTracks.asStateFlow()

    private val _codingChallenges = MutableStateFlow<List<CodingChallenge>>(
        AvoraLearningDataEngine.getCodingChallenges()
    )
    val codingChallenges: StateFlow<List<CodingChallenge>> = _codingChallenges.asStateFlow()

    private val _isGeneratingCustomProject = MutableStateFlow(false)
    val isGeneratingCustomProject: StateFlow<Boolean> = _isGeneratingCustomProject.asStateFlow()

    fun recordChallengeCompleted(challengeId: String) {
        val currentChallenges = sharedPrefs.getInt("completed_challenges", 0) + 1
        sharedPrefs.edit().putInt("completed_challenges", currentChallenges).apply()

        viewModelScope.launch {
            _snackbarMessage.emit("🏆 Challenge solved! +60 XP added to your Skill Profile!")
        }
    }

    fun recordProjectCompleted(projectId: String) {
        val currentProjects = sharedPrefs.getInt("completed_projects", 0) + 1
        sharedPrefs.edit().putInt("completed_projects", currentProjects).apply()

        viewModelScope.launch {
            _snackbarMessage.emit("🎉 Project milestone completed! Added to your Verified Portfolio!")
        }
    }

    fun generateCustomAiProject(goal: String, category: String, difficulty: String) {
        viewModelScope.launch {
            _isGeneratingCustomProject.value = true
            _snackbarMessage.emit("✨ Architecting 9-stage custom project roadmap for: $goal...")
            val result = AiTutorServices.generateCustomProject(goal, category, difficulty)
            _isGeneratingCustomProject.value = false

            result.onSuccess { newGuide ->
                val current = _activeProjects.value.toMutableList()
                current.add(0, newGuide)
                _activeProjects.value = current
                _snackbarMessage.emit("🚀 Created roadmap: ${newGuide.title}!")
            }.onFailure { err ->
                _snackbarMessage.emit("⚠️ Project generator notice: ${err.localizedMessage}")
            }
        }
    }

    // Quick AI Flashcards Generator
    fun generateAiFlashcardsForTopic(subject: String, topic: String) {
        viewModelScope.launch {
            _snackbarMessage.emit("✨ Generating flashcards for $topic...")
            val sampleCards = listOf(
                FlashcardEntity(
                    question = "Core definition & purpose of $topic in $subject?",
                    answer = "$topic provides the foundational structure for solving complex problems and optimizes system reliability.",
                    subject = subject,
                    topic = topic,
                    nextReviewAt = System.currentTimeMillis(),
                    lastRating = "NEW"
                ),
                FlashcardEntity(
                    question = "What is the primary theorem or equation associated with $topic?",
                    answer = "The governing equation links input variables with boundary conditions to predict state transitions.",
                    subject = subject,
                    topic = topic,
                    nextReviewAt = System.currentTimeMillis(),
                    lastRating = "NEW"
                ),
                FlashcardEntity(
                    question = "What is the most common exam pitfall when dealing with $topic?",
                    answer = "Failing to account for boundary conditions and unit consistency before substituting numerical values.",
                    subject = subject,
                    topic = topic,
                    nextReviewAt = System.currentTimeMillis(),
                    lastRating = "NEW"
                )
            )
            repository.addFlashcards(sampleCards)
            _snackbarMessage.emit("🎴 Added 3 smart flashcards for $topic!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceTutorHelper.destroy()
    }
}
