package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId
        ORDER BY isCompleted ASC, 
        CASE priority 
            WHEN 'URGENT' THEN 1 
            WHEN 'HIGH' THEN 2 
            WHEN 'MEDIUM' THEN 3 
            WHEN 'LOW' THEN 4 
            ELSE 5 
        END ASC, 
        dueDate ASC
    """)
    fun getAllTasks(userId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND isCompleted = 0 
        ORDER BY 
        CASE priority 
            WHEN 'URGENT' THEN 1 
            WHEN 'HIGH' THEN 2 
            WHEN 'MEDIUM' THEN 3 
            WHEN 'LOW' THEN 4 
            ELSE 5 
        END ASC, 
        dueDate ASC
    """)
    fun getPendingTasks(userId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND isCompleted = 1 ORDER BY completedAt DESC, dueDate DESC")
    fun getCompletedTasks(userId: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND priority = :priority 
        ORDER BY isCompleted ASC, dueDate ASC
    """)
    fun getTasksByPriority(userId: String, priority: String): Flow<List<TaskEntity>>

    @Query("""
        SELECT * FROM tasks 
        WHERE userId = :userId AND dueDate BETWEEN :startOfDay AND :endOfDay 
        ORDER BY isCompleted ASC, 
        CASE priority 
            WHEN 'URGENT' THEN 1 
            WHEN 'HIGH' THEN 2 
            WHEN 'MEDIUM' THEN 3 
            WHEN 'LOW' THEN 4 
            ELSE 5 
        END ASC
    """)
    fun getDailyTasks(userId: String, startOfDay: Long, endOfDay: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND subject = :subject ORDER BY isCompleted ASC, dueDate ASC")
    fun getTasksBySubject(userId: String, subject: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND id = :id")
    fun getTaskById(userId: String, id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getTaskByIdDirect(userId: String, id: Long): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET priority = :priority WHERE userId = :userId AND id = :id")
    suspend fun updateTaskPriority(userId: String, id: Long, priority: String)

    @Query("UPDATE tasks SET isCompleted = :isCompleted, completedAt = :completedAt WHERE userId = :userId AND id = :id")
    suspend fun updateTaskCompletion(userId: String, id: Long, isCompleted: Boolean, completedAt: Long?)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE userId = :userId AND id = :id")
    suspend fun deleteTaskById(userId: String, id: Long)

    @Query("DELETE FROM tasks WHERE userId = :userId AND isCompleted = 1")
    suspend fun deleteCompletedTasks(userId: String)

    @Query("DELETE FROM tasks WHERE userId = :userId")
    suspend fun deleteUserData(userId: String)

    @Query("UPDATE tasks SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignUser(fromUserId: String, toUserId: String)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedule_blocks WHERE userId = :userId ORDER BY startTime ASC")
    fun getAllSchedules(userId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedule_blocks WHERE userId = :userId ORDER BY startTime ASC")
    suspend fun getAllSchedulesList(userId: String): List<ScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>): List<Long>

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM schedule_blocks WHERE userId = :userId")
    suspend fun deleteUserData(userId: String)

    @Query("UPDATE schedule_blocks SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignUser(fromUserId: String, toUserId: String)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getAllMessages(userId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun clearHistory(userId: String)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun deleteUserData(userId: String)

    @Query("UPDATE chat_messages SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignUser(fromUserId: String, toUserId: String)
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions WHERE userId = :userId ORDER BY completedAt DESC")
    fun getAllSessions(userId: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("DELETE FROM study_sessions WHERE userId = :userId")
    suspend fun deleteUserData(userId: String)

    @Query("UPDATE study_sessions SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignUser(fromUserId: String, toUserId: String)
}

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE userId = :userId ORDER BY nextReviewAt ASC, subject ASC")
    fun getAllFlashcards(userId: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND nextReviewAt <= :cutoffTime ORDER BY nextReviewAt ASC")
    fun getDueFlashcards(userId: String, cutoffTime: Long): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND subject = :subject ORDER BY nextReviewAt ASC")
    fun getFlashcardsBySubject(userId: String, subject: String): Flow<List<FlashcardEntity>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId AND id = :id")
    fun getFlashcardById(userId: String, id: Long): Flow<FlashcardEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(card: FlashcardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(cards: List<FlashcardEntity>): List<Long>

    @Update
    suspend fun updateFlashcard(card: FlashcardEntity)

    @Delete
    suspend fun deleteFlashcard(card: FlashcardEntity)

    @Query("DELETE FROM flashcards WHERE userId = :userId AND id = :id")
    suspend fun deleteFlashcardById(userId: String, id: Long)

    @Query("DELETE FROM flashcards WHERE userId = :userId")
    suspend fun deleteUserData(userId: String)

    @Query("UPDATE flashcards SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignUser(fromUserId: String, toUserId: String)
}

@Dao
interface QuizResultDao {
    @Query("SELECT * FROM quiz_results WHERE userId = :userId ORDER BY completedAt DESC")
    fun getAllQuizResults(userId: String): Flow<List<QuizResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    @Query("DELETE FROM quiz_results WHERE userId = :userId")
    suspend fun clearQuizResults(userId: String)

    @Query("DELETE FROM quiz_results WHERE userId = :userId")
    suspend fun deleteUserData(userId: String)

    @Query("UPDATE quiz_results SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignUser(fromUserId: String, toUserId: String)
}
