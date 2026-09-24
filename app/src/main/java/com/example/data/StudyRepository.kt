package com.example.data

import com.example.data.local.*
import com.example.data.remote.GeminiProductivityRecommendationService
import com.example.data.remote.ProductivityRecommendationService
import com.example.data.remote.StudyProductivityReport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class StudyRepository(
    private val db: AppDatabase,
    private val recommendationService: ProductivityRecommendationService = GeminiProductivityRecommendationService()
) {
    private val _activeUserId = MutableStateFlow("guest_user")
    val activeUserId: Flow<String> = _activeUserId.asStateFlow()

    fun setActiveUserId(userId: String) {
        val sanitized = userId.trim().ifEmpty { "guest_user" }
        _activeUserId.value = sanitized
    }

    fun getActiveUserId(): String = _activeUserId.value

    val tasks: Flow<List<TaskEntity>> = _activeUserId.flatMapLatest { uid ->
        db.taskDao().getAllTasks(uid)
    }

    val schedule: Flow<List<ScheduleEntity>> = _activeUserId.flatMapLatest { uid ->
        db.scheduleDao().getAllSchedules(uid)
    }

    val chatMessages: Flow<List<ChatMessageEntity>> = _activeUserId.flatMapLatest { uid ->
        db.chatDao().getAllMessages(uid)
    }

    val studySessions: Flow<List<StudySessionEntity>> = _activeUserId.flatMapLatest { uid ->
        db.studySessionDao().getAllSessions(uid)
    }

    val flashcards: Flow<List<FlashcardEntity>> = _activeUserId.flatMapLatest { uid ->
        db.flashcardDao().getAllFlashcards(uid)
    }

    val quizResults: Flow<List<QuizResultEntity>> = _activeUserId.flatMapLatest { uid ->
        db.quizResultDao().getAllQuizResults(uid)
    }

    suspend fun seedDefaultDataIfEmpty(userId: String = getActiveUserId()) {
        val existingChat = db.chatDao().getAllMessages(userId).first()
        if (existingChat.isEmpty()) {
            db.chatDao().insertMessage(
                ChatMessageEntity(
                    userId = userId,
                    sender = "AI",
                    text = "👋 Welcome to Avora! I'm your AI Academic & Technology Mentor, created by Sir Barie Bilal.\n\nI can help you understand complex concepts, solve homework problems step-by-step, explain science & biology topics (like cells or genetics), and master coding in Python and Kotlin.\n\nHow can I help you today?"
                )
            )
        }
    }

    // Task operations
    suspend fun addTask(task: TaskEntity): Long {
        val resolvedTask = if (task.userId.isBlank() || (task.userId == "guest_user" && getActiveUserId() != "guest_user")) {
            task.copy(userId = getActiveUserId())
        } else {
            task
        }
        return db.taskDao().insertTask(resolvedTask)
    }

    suspend fun addTasks(tasks: List<TaskEntity>): List<Long> {
        val currentUid = getActiveUserId()
        val resolvedTasks = tasks.map { task ->
            if (task.userId.isBlank() || (task.userId == "guest_user" && currentUid != "guest_user")) {
                task.copy(userId = currentUid)
            } else {
                task
            }
        }
        return db.taskDao().insertTasks(resolvedTasks)
    }

    suspend fun updateTask(task: TaskEntity) {
        val currentUid = getActiveUserId()
        val resolved = if (task.userId.isBlank()) task.copy(userId = currentUid) else task
        db.taskDao().updateTask(resolved)
    }

    suspend fun updateTaskPriority(id: Long, priority: String) =
        db.taskDao().updateTaskPriority(getActiveUserId(), id, priority)

    suspend fun toggleTaskCompletion(task: TaskEntity) {
        val nextCompleted = !task.isCompleted
        val completedAt = if (nextCompleted) System.currentTimeMillis() else null
        db.taskDao().updateTaskCompletion(task.userId, task.id, nextCompleted, completedAt)
    }

    suspend fun deleteTask(task: TaskEntity) = db.taskDao().deleteTask(task)

    suspend fun deleteTaskById(id: Long) = db.taskDao().deleteTaskById(getActiveUserId(), id)

    suspend fun deleteCompletedTasks() = db.taskDao().deleteCompletedTasks(getActiveUserId())

    fun getTasksByPriority(priority: String): Flow<List<TaskEntity>> =
        db.taskDao().getTasksByPriority(getActiveUserId(), priority)

    fun getDailyTasks(startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>> =
        db.taskDao().getDailyTasks(getActiveUserId(), startOfDay, endOfDay)

    fun getPendingTasks(): Flow<List<TaskEntity>> =
        db.taskDao().getPendingTasks(getActiveUserId())

    fun getCompletedTasks(): Flow<List<TaskEntity>> =
        db.taskDao().getCompletedTasks(getActiveUserId())

    // Flashcard operations
    suspend fun addFlashcard(card: FlashcardEntity): Long {
        val resolved = if (card.userId.isBlank() || (card.userId == "guest_user" && getActiveUserId() != "guest_user")) {
            card.copy(userId = getActiveUserId())
        } else {
            card
        }
        return db.flashcardDao().insertFlashcard(resolved)
    }

    suspend fun addFlashcards(cards: List<FlashcardEntity>): List<Long> {
        val currentUid = getActiveUserId()
        val resolved = cards.map { card ->
            if (card.userId.isBlank() || (card.userId == "guest_user" && currentUid != "guest_user")) {
                card.copy(userId = currentUid)
            } else {
                card
            }
        }
        return db.flashcardDao().insertFlashcards(resolved)
    }

    suspend fun updateFlashcard(card: FlashcardEntity) {
        val resolved = if (card.userId.isBlank()) card.copy(userId = getActiveUserId()) else card
        db.flashcardDao().updateFlashcard(resolved)
    }

    suspend fun deleteFlashcard(card: FlashcardEntity) = db.flashcardDao().deleteFlashcard(card)

    suspend fun deleteFlashcardById(id: Long) = db.flashcardDao().deleteFlashcardById(getActiveUserId(), id)

    fun getDueFlashcards(cutoff: Long = System.currentTimeMillis()): Flow<List<FlashcardEntity>> =
        db.flashcardDao().getDueFlashcards(getActiveUserId(), cutoff)

    fun getFlashcardsBySubject(subject: String): Flow<List<FlashcardEntity>> =
        db.flashcardDao().getFlashcardsBySubject(getActiveUserId(), subject)

    // Schedule operations
    suspend fun addSchedule(schedule: ScheduleEntity): Long {
        val resolved = if (schedule.userId.isBlank() || (schedule.userId == "guest_user" && getActiveUserId() != "guest_user")) {
            schedule.copy(userId = getActiveUserId())
        } else {
            schedule
        }
        return db.scheduleDao().insertSchedule(resolved)
    }

    suspend fun insertSchedules(schedules: List<ScheduleEntity>): List<Long> {
        val currentUid = getActiveUserId()
        val resolved = schedules.map { sched ->
            if (sched.userId.isBlank() || (sched.userId == "guest_user" && currentUid != "guest_user")) {
                sched.copy(userId = currentUid)
            } else {
                sched
            }
        }
        return db.scheduleDao().insertSchedules(resolved)
    }

    suspend fun getAllSchedulesSync(userId: String = getActiveUserId()): List<ScheduleEntity> =
        db.scheduleDao().getAllSchedulesList(userId)

    suspend fun deleteSchedule(schedule: ScheduleEntity) = db.scheduleDao().deleteSchedule(schedule)

    // Chat operations
    suspend fun addChatMessage(message: ChatMessageEntity): Long {
        val resolved = if (message.userId.isBlank() || (message.userId == "guest_user" && getActiveUserId() != "guest_user")) {
            message.copy(userId = getActiveUserId())
        } else {
            message
        }
        return db.chatDao().insertMessage(resolved)
    }

    suspend fun clearChat() = db.chatDao().clearHistory(getActiveUserId())

    // Study session operations
    suspend fun logStudySession(session: StudySessionEntity): Long {
        val resolved = if (session.userId.isBlank() || (session.userId == "guest_user" && getActiveUserId() != "guest_user")) {
            session.copy(userId = getActiveUserId())
        } else {
            session
        }
        return db.studySessionDao().insertSession(resolved)
    }

    // Quiz Result operations
    suspend fun addQuizResult(result: QuizResultEntity): Long {
        val resolved = if (result.userId.isBlank() || (result.userId == "guest_user" && getActiveUserId() != "guest_user")) {
            result.copy(userId = getActiveUserId())
        } else {
            result
        }
        return db.quizResultDao().insertQuizResult(resolved)
    }

    suspend fun clearQuizResults() = db.quizResultDao().clearQuizResults(getActiveUserId())

    // Deletes all locally stored data for a user (Account deletion / GDPR compliance)
    suspend fun deleteUserData(userId: String) {
        val targetUser = userId.trim().ifEmpty { getActiveUserId() }
        db.taskDao().deleteUserData(targetUser)
        db.scheduleDao().deleteUserData(targetUser)
        db.chatDao().deleteUserData(targetUser)
        db.studySessionDao().deleteUserData(targetUser)
        db.flashcardDao().deleteUserData(targetUser)
        db.quizResultDao().deleteUserData(targetUser)
    }

    // Data Backup and Restore operations
    suspend fun restoreBackupData(
        tasks: List<TaskEntity>,
        flashcards: List<FlashcardEntity>,
        sessions: List<StudySessionEntity>,
        schedules: List<ScheduleEntity>,
        targetUserId: String? = null
    ) {
        // Enforce account isolation: Restored items are strictly scoped to the active session user
        val assignedUserId = getActiveUserId()
        if (tasks.isNotEmpty()) {
            val userTasks = tasks.map { it.copy(id = 0L, userId = assignedUserId) }
            db.taskDao().insertTasks(userTasks)
        }
        if (flashcards.isNotEmpty()) {
            val userCards = flashcards.map { it.copy(id = 0L, userId = assignedUserId) }
            db.flashcardDao().insertFlashcards(userCards)
        }
        sessions.forEach {
            db.studySessionDao().insertSession(it.copy(id = 0L, userId = assignedUserId))
        }
        schedules.forEach {
            db.scheduleDao().insertSchedule(it.copy(id = 0L, userId = assignedUserId))
        }
    }

    /**
     * Seamlessly reclaims data created offline as a guest when user logs in or creates an account.
     */
    suspend fun claimGuestDataForUser(newUserId: String) {
        val target = newUserId.trim()
        if (target.isBlank() || target == "guest_user") return
        db.taskDao().reassignUser("guest_user", target)
        db.scheduleDao().reassignUser("guest_user", target)
        db.chatDao().reassignUser("guest_user", target)
        db.studySessionDao().reassignUser("guest_user", target)
        db.flashcardDao().reassignUser("guest_user", target)
        db.quizResultDao().reassignUser("guest_user", target)
    }

    // Productivity Recommendation Service
    suspend fun getTaskProductivityRecommendations(
        tasks: List<TaskEntity>,
        availableStudyHours: Double = 3.0,
        energyLevel: String = "Medium",
        studyStyle: String = "Pomodoro (25/5)",
        authToken: String? = null,
        userId: String = getActiveUserId(),
        isGuest: Boolean = (userId == "guest_user")
    ): Result<StudyProductivityReport> {
        return recommendationService.getRecommendationsForTasks(
            tasks = tasks,
            availableStudyHours = availableStudyHours,
            energyLevel = energyLevel,
            studyStyle = studyStyle,
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )
    }
}
