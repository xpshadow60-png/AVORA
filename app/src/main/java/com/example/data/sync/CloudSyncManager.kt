package com.example.data.sync

import android.util.Log
import com.example.data.StudyRepository
import com.example.data.local.*
import com.example.data.model.CareerAssessmentResult
import com.example.data.model.UserProfile
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Production Cloud Synchronization Engine for Avora.
 * Synchronizes local Room Database records with Cloud Firestore under the authenticated user's ID (`users/{userId}/...`).
 * Provides an offline-first architecture where the local database functions instantly offline,
 * and data is synchronized bidirectionally with the cloud when connected.
 */
class CloudSyncManager {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore is not initialized: ${e.message}")
            null
        }

    companion object {
        private const val TAG = "CloudSyncManager"
        private const val USERS_COLLECTION = "users"
        private const val TASKS_COLLECTION = "tasks"
        private const val FLASHCARDS_COLLECTION = "flashcards"
        private const val SESSIONS_COLLECTION = "study_sessions"
        private const val QUIZ_COLLECTION = "quiz_results"
        private const val SCHEDULES_COLLECTION = "schedules"
        private const val ROADMAP_DOC = "career_roadmap"
    }

    /**
     * Save or update the authenticated user's profile document in Firestore
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        if (profile.id.isBlank() || profile.isGuest) return@withContext Result.success(Unit)
        val fs = firestore ?: return@withContext Result.success(Unit)
        try {
            val userMap = hashMapOf(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "photoUrl" to (profile.photoUrl ?: ""),
                "majorOrField" to profile.majorOrField,
                "classLevel" to profile.classLevel,
                "learningPreferences" to profile.learningPreferences,
                "isEmailVerified" to profile.isEmailVerified,
                "memberSince" to profile.memberSince,
                "lastActiveAt" to System.currentTimeMillis()
            )
            fs.collection(USERS_COLLECTION)
                .document(profile.id)
                .set(userMap, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save user profile to Firestore: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    /**
     * Retrieve user profile from Firestore by user ID
     */
    suspend fun fetchUserProfile(userId: String): UserProfile? = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext null
        val fs = firestore ?: return@withContext null
        try {
            val snapshot = fs.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (snapshot.exists()) {
                UserProfile(
                    id = snapshot.getString("id") ?: userId,
                    name = snapshot.getString("name") ?: "Future Leader",
                    email = snapshot.getString("email") ?: "",
                    photoUrl = snapshot.getString("photoUrl"),
                    majorOrField = snapshot.getString("majorOrField") ?: "Computer Science",
                    classLevel = snapshot.getString("classLevel") ?: "Undergraduate",
                    learningPreferences = snapshot.getString("learningPreferences") ?: "Visual & Interactive Code Labs",
                    isEmailVerified = snapshot.getBoolean("isEmailVerified") ?: false,
                    memberSince = snapshot.getLong("memberSince") ?: System.currentTimeMillis(),
                    isGuest = false
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch user profile: ${e.localizedMessage}")
            null
        }
    }

    /**
     * Perform full bidirectional synchronization for the user upon login or reconnect
     */
    suspend fun syncAll(userId: String, repository: StudyRepository): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Result.success(Unit)
        val fs = firestore ?: return@withContext Result.success(Unit)
        try {
            // 1. Sync Tasks
            syncTasks(userId, repository)

            // 2. Sync Flashcards
            syncFlashcards(userId, repository)

            // 3. Sync Study Sessions
            syncSessions(userId, repository)

            // 4. Sync Quiz Results
            syncQuizResults(userId, repository)

            // 5. Sync Schedules
            syncSchedules(userId, repository)

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error performing full sync for $userId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }

    private suspend fun syncTasks(userId: String, repository: StudyRepository) {
        val fs = firestore ?: return
        try {
            val tasksRef = fs.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION)
            val snapshot = tasksRef.get().await()

            val cloudTasks = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id") ?: return@mapNotNull null
                val title = doc.getString("title") ?: ""
                val subject = doc.getString("subject") ?: "General"
                val priority = doc.getString("priority") ?: "MEDIUM"
                val dueDate = doc.getLong("dueDate") ?: System.currentTimeMillis()
                val estimatedMinutes = doc.getLong("estimatedMinutes")?.toInt() ?: 30
                val category = doc.getString("category") ?: "Assignment"
                val notes = doc.getString("notes") ?: ""
                val isCompleted = doc.getBoolean("isCompleted") ?: false
                val completedAt = doc.getLong("completedAt")
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                TaskEntity(
                    id = id,
                    userId = userId,
                    title = title,
                    subject = subject,
                    priority = priority,
                    dueDate = dueDate,
                    estimatedMinutes = estimatedMinutes,
                    category = category,
                    notes = notes,
                    isCompleted = isCompleted,
                    completedAt = completedAt,
                    createdAt = createdAt
                )
            }

            // Insert cloud tasks into local Room DB if not present
            val localTasks = repository.tasks.first()
            val localTaskIds = localTasks.map { it.id }.toSet()

            for (cloudTask in cloudTasks) {
                if (cloudTask.id !in localTaskIds) {
                    repository.addTask(cloudTask)
                }
            }

            // Push local tasks to cloud if not in cloud
            val cloudTaskIds = cloudTasks.map { it.id }.toSet()
            for (localTask in localTasks) {
                if (localTask.id !in cloudTaskIds) {
                    pushTask(userId, localTask)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Task sync error: ${e.localizedMessage}")
        }
    }

    suspend fun pushTask(userId: String, task: TaskEntity) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            val taskMap = hashMapOf(
                "id" to task.id,
                "title" to task.title,
                "subject" to task.subject,
                "priority" to task.priority,
                "dueDate" to task.dueDate,
                "estimatedMinutes" to task.estimatedMinutes,
                "category" to task.category,
                "notes" to task.notes,
                "isCompleted" to task.isCompleted,
                "completedAt" to task.completedAt,
                "createdAt" to task.createdAt
            )
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(TASKS_COLLECTION)
                .document(task.id.toString())
                .set(taskMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push task ${task.id} to cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteCloudTask(userId: String, taskId: Long) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(TASKS_COLLECTION)
                .document(taskId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete task $taskId from cloud: ${e.localizedMessage}")
        }
    }

    private suspend fun syncFlashcards(userId: String, repository: StudyRepository) {
        val fs = firestore ?: return
        try {
            val cardsRef = fs.collection(USERS_COLLECTION).document(userId).collection(FLASHCARDS_COLLECTION)
            val snapshot = cardsRef.get().await()

            val cloudCards = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id") ?: return@mapNotNull null
                val question = doc.getString("question") ?: ""
                val answer = doc.getString("answer") ?: ""
                val subject = doc.getString("subject") ?: "General"
                val topic = doc.getString("topic") ?: ""
                val intervalDays = doc.getLong("intervalDays")?.toInt() ?: 1
                val easeFactor = doc.getDouble("easeFactor") ?: 2.5
                val repetitions = doc.getLong("repetitions")?.toInt() ?: 0
                val nextReviewAt = doc.getLong("nextReviewAt") ?: System.currentTimeMillis()
                val totalReviews = doc.getLong("totalReviews")?.toInt() ?: 0
                val successfulReviews = doc.getLong("successfulReviews")?.toInt() ?: 0
                val lastRating = doc.getString("lastRating") ?: "NEW"
                val createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()

                FlashcardEntity(
                    id = id,
                    userId = userId,
                    question = question,
                    answer = answer,
                    subject = subject,
                    topic = topic,
                    intervalDays = intervalDays,
                    easeFactor = easeFactor,
                    repetitions = repetitions,
                    nextReviewAt = nextReviewAt,
                    totalReviews = totalReviews,
                    successfulReviews = successfulReviews,
                    lastRating = lastRating,
                    createdAt = createdAt
                )
            }

            val localCards = repository.flashcards.first()
            val localCardIds = localCards.map { it.id }.toSet()

            for (cloudCard in cloudCards) {
                if (cloudCard.id !in localCardIds) {
                    repository.addFlashcard(cloudCard)
                }
            }

            val cloudCardIds = cloudCards.map { it.id }.toSet()
            for (localCard in localCards) {
                if (localCard.id !in cloudCardIds) {
                    pushFlashcard(userId, localCard)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Flashcard sync error: ${e.localizedMessage}")
        }
    }

    suspend fun pushFlashcard(userId: String, card: FlashcardEntity) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            val cardMap = hashMapOf(
                "id" to card.id,
                "question" to card.question,
                "answer" to card.answer,
                "subject" to card.subject,
                "topic" to card.topic,
                "intervalDays" to card.intervalDays,
                "easeFactor" to card.easeFactor,
                "repetitions" to card.repetitions,
                "nextReviewAt" to card.nextReviewAt,
                "totalReviews" to card.totalReviews,
                "successfulReviews" to card.successfulReviews,
                "lastRating" to card.lastRating,
                "createdAt" to card.createdAt
            )
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(FLASHCARDS_COLLECTION)
                .document(card.id.toString())
                .set(cardMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push card ${card.id} to cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteCloudFlashcard(userId: String, cardId: Long) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(FLASHCARDS_COLLECTION)
                .document(cardId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete card $cardId from cloud: ${e.localizedMessage}")
        }
    }

    private suspend fun syncSessions(userId: String, repository: StudyRepository) {
        val fs = firestore ?: return
        try {
            val sessionsRef = fs.collection(USERS_COLLECTION).document(userId).collection(SESSIONS_COLLECTION)
            val snapshot = sessionsRef.get().await()

            val cloudSessions = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id") ?: return@mapNotNull null
                val subject = doc.getString("subject") ?: "General"
                val durationMinutes = doc.getLong("durationMinutes")?.toInt() ?: 25
                val completedAt = doc.getLong("completedAt") ?: System.currentTimeMillis()
                val focusScore = doc.getLong("focusScore")?.toInt() ?: 100
                val distractionsBlockedCount = doc.getLong("distractionsBlockedCount")?.toInt() ?: 0

                StudySessionEntity(
                    id = id,
                    userId = userId,
                    subject = subject,
                    durationMinutes = durationMinutes,
                    completedAt = completedAt,
                    focusScore = focusScore,
                    distractionsBlockedCount = distractionsBlockedCount
                )
            }

            val localSessions = repository.studySessions.first()
            val localSessionIds = localSessions.map { it.id }.toSet()

            for (cloudSession in cloudSessions) {
                if (cloudSession.id !in localSessionIds) {
                    repository.logStudySession(cloudSession)
                }
            }

            val cloudSessionIds = cloudSessions.map { it.id }.toSet()
            for (localSession in localSessions) {
                if (localSession.id !in cloudSessionIds) {
                    pushSession(userId, localSession)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Study session sync error: ${e.localizedMessage}")
        }
    }

    suspend fun pushSession(userId: String, session: StudySessionEntity) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            val sessionMap = hashMapOf(
                "id" to session.id,
                "subject" to session.subject,
                "durationMinutes" to session.durationMinutes,
                "completedAt" to session.completedAt,
                "focusScore" to session.focusScore,
                "distractionsBlockedCount" to session.distractionsBlockedCount
            )
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SESSIONS_COLLECTION)
                .document(session.id.toString())
                .set(sessionMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push session ${session.id} to cloud: ${e.localizedMessage}")
        }
    }

    private suspend fun syncQuizResults(userId: String, repository: StudyRepository) {
        val fs = firestore ?: return
        try {
            val quizRef = fs.collection(USERS_COLLECTION).document(userId).collection(QUIZ_COLLECTION)
            val snapshot = quizRef.get().await()

            val cloudQuizzes = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id") ?: return@mapNotNull null
                val subject = doc.getString("subject") ?: "General"
                val topic = doc.getString("topic") ?: ""
                val difficulty = doc.getString("difficulty") ?: "Medium"
                val score = doc.getLong("score")?.toInt() ?: 0
                val totalQuestions = doc.getLong("totalQuestions")?.toInt() ?: 5
                val percentage = doc.getLong("percentage")?.toInt() ?: 0
                val weakTopicsJson = doc.getString("weakTopicsJson") ?: ""
                val completedAt = doc.getLong("completedAt") ?: System.currentTimeMillis()

                QuizResultEntity(
                    id = id,
                    userId = userId,
                    subject = subject,
                    topic = topic,
                    difficulty = difficulty,
                    score = score,
                    totalQuestions = totalQuestions,
                    percentage = percentage,
                    weakTopicsJson = weakTopicsJson,
                    completedAt = completedAt
                )
            }

            val localQuizzes = repository.quizResults.first()
            val localQuizIds = localQuizzes.map { it.id }.toSet()

            for (cloudQuiz in cloudQuizzes) {
                if (cloudQuiz.id !in localQuizIds) {
                    repository.addQuizResult(cloudQuiz)
                }
            }

            val cloudQuizIds = cloudQuizzes.map { it.id }.toSet()
            for (localQuiz in localQuizzes) {
                if (localQuiz.id !in cloudQuizIds) {
                    pushQuizResult(userId, localQuiz)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Quiz results sync error: ${e.localizedMessage}")
        }
    }

    suspend fun pushQuizResult(userId: String, result: QuizResultEntity) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            val quizMap = hashMapOf(
                "id" to result.id,
                "subject" to result.subject,
                "topic" to result.topic,
                "difficulty" to result.difficulty,
                "score" to result.score,
                "totalQuestions" to result.totalQuestions,
                "percentage" to result.percentage,
                "weakTopicsJson" to result.weakTopicsJson,
                "completedAt" to result.completedAt
            )
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(QUIZ_COLLECTION)
                .document(result.id.toString())
                .set(quizMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push quiz result ${result.id} to cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Sync Career Path Assessment & Completed Milestones
     */
    suspend fun syncCareerProgress(
        userId: String,
        assessment: CareerAssessmentResult,
        completedMilestones: Set<String>
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext
        val fs = firestore ?: return@withContext
        try {
            val roadmapMap = hashMapOf(
                "fieldOfStudy" to assessment.fieldOfStudy,
                "specialization" to assessment.specialization,
                "academicLevel" to assessment.academicLevel,
                "weeklyStudyHours" to assessment.weeklyStudyHours,
                "learningStyle" to assessment.learningStyle,
                "primaryChallenge" to assessment.primaryChallenge,
                "timelineGoal" to assessment.timelineGoal,
                "certificationGoal" to assessment.certificationGoal,
                "resourcePreference" to assessment.resourcePreference,
                "dailyStudyHabit" to assessment.dailyStudyHabit,
                "completedMilestones" to completedMilestones.toList(),
                "updatedAt" to System.currentTimeMillis()
            )

            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection("roadmap")
                .document("progress")
                .set(roadmapMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync career progress: ${e.localizedMessage}")
        }
    }

    suspend fun fetchCareerProgress(userId: String): Pair<CareerAssessmentResult?, Set<String>> = withContext(Dispatchers.IO) {
        if (userId.isBlank()) return@withContext Pair(null, emptySet())
        val fs = firestore ?: return@withContext Pair(null, emptySet())
        try {
            val doc = fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection("roadmap")
                .document("progress")
                .get()
                .await()

            if (doc.exists()) {
                val field = doc.getString("fieldOfStudy") ?: return@withContext Pair(null, emptySet())
                val assessment = CareerAssessmentResult(
                    fieldOfStudy = field,
                    specialization = doc.getString("specialization") ?: "AI Systems & Full-Stack Builder",
                    academicLevel = doc.getString("academicLevel") ?: "Practicing Developer",
                    weeklyStudyHours = doc.getString("weeklyStudyHours") ?: "10 - 20 Hours / week",
                    learningStyle = doc.getString("learningStyle") ?: "Interactive Code & Project Sprints",
                    primaryChallenge = doc.getString("primaryChallenge") ?: "Building Real Production Systems",
                    timelineGoal = doc.getString("timelineGoal") ?: "6 - 12 Months",
                    certificationGoal = doc.getString("certificationGoal") ?: "Portfolio Proof of Work",
                    resourcePreference = doc.getString("resourcePreference") ?: "Avora Interactive Code Labs",
                    dailyStudyHabit = doc.getString("dailyStudyHabit") ?: "Daily Code Challenge + Milestone"
                )
                @Suppress("UNCHECKED_CAST")
                val milestones = (doc.get("completedMilestones") as? List<String>)?.toSet() ?: emptySet()
                Pair(assessment, milestones)
            } else {
                Pair(null, emptySet())
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch career progress: ${e.localizedMessage}")
            Pair(null, emptySet())
        }
    }

    private suspend fun syncSchedules(userId: String, repository: StudyRepository) {
        val fs = firestore ?: return
        try {
            val schedRef = fs.collection(USERS_COLLECTION).document(userId).collection(SCHEDULES_COLLECTION)
            val snapshot = schedRef.get().await()

            val cloudSchedules = snapshot.documents.mapNotNull { doc ->
                val id = doc.getLong("id") ?: return@mapNotNull null
                val title = doc.getString("title") ?: ""
                val subject = doc.getString("subject") ?: "General"
                val startTime = doc.getString("startTime") ?: "09:00"
                val endTime = doc.getString("endTime") ?: "10:00"
                val dayOfWeek = doc.getString("dayOfWeek") ?: "Monday"
                val isStudyBlock = doc.getBoolean("isStudyBlock") ?: true
                val colorHex = doc.getString("colorHex") ?: "#6C5CE7"

                ScheduleEntity(
                    id = id,
                    userId = userId,
                    title = title,
                    subject = subject,
                    startTime = startTime,
                    endTime = endTime,
                    dayOfWeek = dayOfWeek,
                    isStudyBlock = isStudyBlock,
                    colorHex = colorHex
                )
            }

            if (cloudSchedules.isNotEmpty()) {
                repository.insertSchedules(cloudSchedules)
            }

            val localSchedules = repository.getAllSchedulesSync(userId)
            val cloudIds = cloudSchedules.map { it.id }.toSet()
            for (localSchedule in localSchedules) {
                if (localSchedule.id !in cloudIds) {
                    pushSchedule(userId, localSchedule)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Schedule sync error: ${e.localizedMessage}")
        }
    }

    suspend fun pushSchedule(userId: String, schedule: ScheduleEntity) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val fs = firestore ?: return@withContext
        try {
            val scheduleMap = hashMapOf(
                "id" to schedule.id,
                "title" to schedule.title,
                "subject" to schedule.subject,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "dayOfWeek" to schedule.dayOfWeek,
                "isStudyBlock" to schedule.isStudyBlock,
                "colorHex" to schedule.colorHex
            )
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SCHEDULES_COLLECTION)
                .document(schedule.id.toString())
                .set(scheduleMap, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push schedule ${schedule.id} to cloud: ${e.localizedMessage}")
        }
    }

    suspend fun deleteCloudSchedule(userId: String, scheduleId: Long) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val fs = firestore ?: return@withContext
        try {
            fs.collection(USERS_COLLECTION)
                .document(userId)
                .collection(SCHEDULES_COLLECTION)
                .document(scheduleId.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete schedule $scheduleId from cloud: ${e.localizedMessage}")
        }
    }

    /**
     * Delete all user documents and subcollections in Cloud Firestore.
     * Must be invoked prior to Firebase Auth account deletion while request.auth.uid is valid.
     */
    suspend fun deleteUserCloudData(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext Result.success(Unit)
        val fs = firestore ?: return@withContext Result.success(Unit)
        try {
            val userDocRef = fs.collection(USERS_COLLECTION).document(userId)
            val subcollections = listOf(
                TASKS_COLLECTION,
                FLASHCARDS_COLLECTION,
                SESSIONS_COLLECTION,
                QUIZ_COLLECTION,
                SCHEDULES_COLLECTION,
                "roadmap"
            )

            for (colName in subcollections) {
                try {
                    val snapshot = userDocRef.collection(colName).get().await()
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                } catch (subErr: Exception) {
                    Log.w(TAG, "Error cleaning up cloud subcollection $colName for $userId: ${subErr.localizedMessage}")
                }
            }

            // Delete root user profile document
            userDocRef.delete().await()
            Log.d(TAG, "Successfully purged all cloud Firestore data for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error during deleteUserCloudData for $userId: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }
}
