package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        TaskEntity::class,
        ScheduleEntity::class,
        ChatMessageEntity::class,
        StudySessionEntity::class,
        FlashcardEntity::class,
        QuizResultEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun chatDao(): ChatDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun flashcardDao(): FlashcardDao
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_sessions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `durationMinutes` INTEGER NOT NULL,
                        `completedAt` INTEGER NOT NULL,
                        `focusScore` INTEGER NOT NULL,
                        `distractionsBlockedCount` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `flashcards` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `question` TEXT NOT NULL,
                        `answer` TEXT NOT NULL,
                        `difficulty` TEXT NOT NULL,
                        `reviewCount` INTEGER NOT NULL,
                        `lastReviewedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `quiz_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `topic` TEXT NOT NULL,
                        `scorePercent` INTEGER NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `completedAt` INTEGER NOT NULL,
                        `summaryAdvice` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE `chat_messages` ADD COLUMN `isPriorityAdvice` INTEGER NOT NULL DEFAULT 0")
                } catch (ignored: Exception) {
                    // Column already exists
                }
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Safely add userId column with default 'guest_user' to all 6 entities to preserve all existing data
                try {
                    db.execSQL("ALTER TABLE `tasks` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'guest_user'")
                } catch (ignored: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `schedule_blocks` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'guest_user'")
                } catch (ignored: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `chat_messages` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'guest_user'")
                } catch (ignored: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `study_sessions` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'guest_user'")
                } catch (ignored: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `flashcards` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'guest_user'")
                } catch (ignored: Exception) {}
                try {
                    db.execSQL("ALTER TABLE `quiz_results` ADD COLUMN `userId` TEXT NOT NULL DEFAULT 'guest_user'")
                } catch (ignored: Exception) {}

                // Create indices on userId for performant user-scoped lookups
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_userId` ON `tasks` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedule_blocks_userId` ON `schedule_blocks` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_chat_messages_userId` ON `chat_messages` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_sessions_userId` ON `study_sessions` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flashcards_userId` ON `flashcards` (`userId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_quiz_results_userId` ON `quiz_results` (`userId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "studypulse_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

