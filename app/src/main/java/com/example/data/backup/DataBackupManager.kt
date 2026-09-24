package com.example.data.backup

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.local.*
import com.example.data.model.CareerAssessmentResult
import com.example.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

data class RestoreSummary(
    val success: Boolean,
    val tasksRestored: Int = 0,
    val flashcardsRestored: Int = 0,
    val schedulesRestored: Int = 0,
    val sessionsRestored: Int = 0,
    val errorMessage: String? = null
)

object DataBackupManager {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Creates a complete JSON backup payload of all user data.
     */
    fun exportAllToJson(
        tasks: List<TaskEntity>,
        flashcards: List<FlashcardEntity>,
        sessions: List<StudySessionEntity>,
        schedules: List<ScheduleEntity>,
        userProfile: UserProfile? = null,
        careerAssessment: CareerAssessmentResult? = null
    ): String {
        val root = JSONObject()
        root.put("app", "Avora")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("exportedAtFormatted", dateFormat.format(Date()))

        // User Profile
        userProfile?.let { user ->
            val userObj = JSONObject().apply {
                put("name", user.name)
                put("email", user.email)
                put("majorOrField", user.majorOrField)
                put("isGuest", user.isGuest)
            }
            root.put("userProfile", userObj)
        }

        // Career Assessment
        careerAssessment?.let { assessment ->
            val assessObj = JSONObject().apply {
                put("fieldOfStudy", assessment.fieldOfStudy)
                put("specialization", assessment.specialization)
                put("academicLevel", assessment.academicLevel)
                put("weeklyStudyHours", assessment.weeklyStudyHours)
                put("learningStyle", assessment.learningStyle)
                put("primaryChallenge", assessment.primaryChallenge)
                put("timelineGoal", assessment.timelineGoal)
                put("certificationGoal", assessment.certificationGoal)
                put("resourcePreference", assessment.resourcePreference)
                put("dailyStudyHabit", assessment.dailyStudyHabit)
            }
            root.put("careerAssessment", assessObj)
        }

        // Tasks
        val tasksArray = JSONArray()
        tasks.forEach { task ->
            val taskObj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("subject", task.subject)
                put("priority", task.priority)
                put("dueDate", task.dueDate)
                put("estimatedMinutes", task.estimatedMinutes)
                put("isCompleted", task.isCompleted)
                put("completedAt", task.completedAt ?: JSONObject.NULL)
                put("category", task.category)
                put("notes", task.notes)
                put("createdAt", task.createdAt)
            }
            tasksArray.put(taskObj)
        }
        root.put("tasks", tasksArray)

        // Flashcards
        val flashcardsArray = JSONArray()
        flashcards.forEach { card ->
            val cardObj = JSONObject().apply {
                put("id", card.id)
                put("question", card.question)
                put("answer", card.answer)
                put("subject", card.subject)
                put("topic", card.topic)
                put("intervalDays", card.intervalDays)
                put("easeFactor", card.easeFactor)
                put("repetitions", card.repetitions)
                put("nextReviewAt", card.nextReviewAt)
                put("lastRating", card.lastRating)
                put("totalReviews", card.totalReviews)
                put("successfulReviews", card.successfulReviews)
                put("createdAt", card.createdAt)
            }
            flashcardsArray.put(cardObj)
        }
        root.put("flashcards", flashcardsArray)

        // Study Sessions
        val sessionsArray = JSONArray()
        sessions.forEach { session ->
            val sessionObj = JSONObject().apply {
                put("id", session.id)
                put("subject", session.subject)
                put("durationMinutes", session.durationMinutes)
                put("completedAt", session.completedAt)
                put("focusScore", session.focusScore)
                put("distractionsBlockedCount", session.distractionsBlockedCount)
            }
            sessionsArray.put(sessionObj)
        }
        root.put("studySessions", sessionsArray)

        // Schedule Blocks
        val schedulesArray = JSONArray()
        schedules.forEach { block ->
            val blockObj = JSONObject().apply {
                put("id", block.id)
                put("title", block.title)
                put("subject", block.subject)
                put("startTime", block.startTime)
                put("endTime", block.endTime)
                put("dayOfWeek", block.dayOfWeek)
                put("colorHex", block.colorHex)
            }
            schedulesArray.put(blockObj)
        }
        root.put("schedules", schedulesArray)

        return root.toString(2)
    }

    /**
     * Parses a JSON backup string into individual entity lists for database restoration.
     */
    fun parseJsonBackup(jsonString: String): ParsedBackupData {
        val root = JSONObject(jsonString)
        
        val tasks = mutableListOf<TaskEntity>()
        val flashcards = mutableListOf<FlashcardEntity>()
        val sessions = mutableListOf<StudySessionEntity>()
        val schedules = mutableListOf<ScheduleEntity>()

        // Tasks
        if (root.has("tasks")) {
            val tasksArray = root.getJSONArray("tasks")
            for (i in 0 until tasksArray.length()) {
                val obj = tasksArray.getJSONObject(i)
                tasks.add(
                    TaskEntity(
                        id = if (obj.has("id")) obj.getLong("id") else 0L,
                        title = obj.optString("title", "Untitled Task"),
                        subject = obj.optString("subject", "General Study"),
                        priority = obj.optString("priority", "MEDIUM"),
                        dueDate = obj.optLong("dueDate", System.currentTimeMillis()),
                        estimatedMinutes = obj.optInt("estimatedMinutes", 30),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        completedAt = if (obj.isNull("completedAt")) null else obj.optLong("completedAt"),
                        category = obj.optString("category", "General"),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Flashcards
        if (root.has("flashcards")) {
            val flashcardsArray = root.getJSONArray("flashcards")
            for (i in 0 until flashcardsArray.length()) {
                val obj = flashcardsArray.getJSONObject(i)
                flashcards.add(
                    FlashcardEntity(
                        id = if (obj.has("id")) obj.getLong("id") else 0L,
                        question = obj.optString("question", ""),
                        answer = obj.optString("answer", ""),
                        subject = obj.optString("subject", "General"),
                        topic = obj.optString("topic", "General"),
                        intervalDays = obj.optInt("intervalDays", 1),
                        easeFactor = obj.optDouble("easeFactor", 2.5),
                        repetitions = obj.optInt("repetitions", 0),
                        nextReviewAt = obj.optLong("nextReviewAt", System.currentTimeMillis()),
                        lastRating = obj.optString("lastRating", "NEW"),
                        totalReviews = obj.optInt("totalReviews", 0),
                        successfulReviews = obj.optInt("successfulReviews", 0),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
        }

        // Study Sessions
        if (root.has("studySessions")) {
            val sessionsArray = root.getJSONArray("studySessions")
            for (i in 0 until sessionsArray.length()) {
                val obj = sessionsArray.getJSONObject(i)
                sessions.add(
                    StudySessionEntity(
                        id = if (obj.has("id")) obj.getLong("id") else 0L,
                        subject = obj.optString("subject", "General Study"),
                        durationMinutes = obj.optInt("durationMinutes", 25),
                        completedAt = obj.optLong("completedAt", System.currentTimeMillis()),
                        focusScore = obj.optInt("focusScore", 100),
                        distractionsBlockedCount = obj.optInt("distractionsBlockedCount", 0)
                    )
                )
            }
        }

        // Schedules
        if (root.has("schedules")) {
            val schedulesArray = root.getJSONArray("schedules")
            for (i in 0 until schedulesArray.length()) {
                val obj = schedulesArray.getJSONObject(i)
                schedules.add(
                    ScheduleEntity(
                        id = if (obj.has("id")) obj.getLong("id") else 0L,
                        title = obj.optString("title", "Study Block"),
                        subject = obj.optString("subject", "General"),
                        startTime = obj.optString("startTime", "09:00"),
                        endTime = obj.optString("endTime", "10:00"),
                        dayOfWeek = obj.optString("dayOfWeek", "Monday"),
                        colorHex = obj.optString("colorHex", "#6C5CE7")
                    )
                )
            }
        }

        return ParsedBackupData(
            tasks = tasks,
            flashcards = flashcards,
            sessions = sessions,
            schedules = schedules
        )
    }

    /**
     * Exports Tasks to CSV.
     */
    fun exportTasksToCsv(tasks: List<TaskEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Title,Subject,Priority,Category,Status,Estimated Minutes,Due Date,Completed Date,Notes\n")
        tasks.forEach { task ->
            val dueDateStr = dateFormat.format(Date(task.dueDate))
            val completedDateStr = task.completedAt?.let { dateFormat.format(Date(it)) } ?: "N/A"
            val status = if (task.isCompleted) "Completed" else "Pending"

            sb.append(escapeCsv(task.id.toString())).append(",")
            sb.append(escapeCsv(task.title)).append(",")
            sb.append(escapeCsv(task.subject)).append(",")
            sb.append(escapeCsv(task.priority)).append(",")
            sb.append(escapeCsv(task.category)).append(",")
            sb.append(escapeCsv(status)).append(",")
            sb.append(task.estimatedMinutes).append(",")
            sb.append(escapeCsv(dueDateStr)).append(",")
            sb.append(escapeCsv(completedDateStr)).append(",")
            sb.append(escapeCsv(task.notes)).append("\n")
        }
        return sb.toString()
    }

    /**
     * Exports Flashcards to CSV.
     */
    fun exportFlashcardsToCsv(flashcards: List<FlashcardEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Question,Answer,Subject,Topic,Interval (Days),Ease Factor,Repetitions,Next Review Date,Last Rating,Total Reviews,Success Rate (%)\n")
        flashcards.forEach { card ->
            val nextReviewStr = dateFormat.format(Date(card.nextReviewAt))
            val successRate = if (card.totalReviews > 0) {
                ((card.successfulReviews.toDouble() / card.totalReviews.toDouble()) * 100).toInt()
            } else 0

            sb.append(escapeCsv(card.id.toString())).append(",")
            sb.append(escapeCsv(card.question)).append(",")
            sb.append(escapeCsv(card.answer)).append(",")
            sb.append(escapeCsv(card.subject)).append(",")
            sb.append(escapeCsv(card.topic)).append(",")
            sb.append(card.intervalDays).append(",")
            sb.append(String.format(Locale.US, "%.2f", card.easeFactor)).append(",")
            sb.append(card.repetitions).append(",")
            sb.append(escapeCsv(nextReviewStr)).append(",")
            sb.append(escapeCsv(card.lastRating)).append(",")
            sb.append(card.totalReviews).append(",")
            sb.append(successRate).append("\n")
        }
        return sb.toString()
    }

    /**
     * Exports Study Focus Sessions / Time Log to CSV.
     */
    fun exportStudySessionsToCsv(sessions: List<StudySessionEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Subject,Duration (Minutes),Completed At,Focus Score,Distractions Blocked\n")
        sessions.forEach { s ->
            val completedDateStr = dateFormat.format(Date(s.completedAt))
            sb.append(escapeCsv(s.id.toString())).append(",")
            sb.append(escapeCsv(s.subject)).append(",")
            sb.append(s.durationMinutes).append(",")
            sb.append(escapeCsv(completedDateStr)).append(",")
            sb.append(s.focusScore).append(",")
            sb.append(s.distractionsBlockedCount).append("\n")
        }
        return sb.toString()
    }

    /**
     * Generates a comprehensive Markdown Study Summary Report.
     */
    fun generateStudySummaryReport(
        tasks: List<TaskEntity>,
        flashcards: List<FlashcardEntity>,
        sessions: List<StudySessionEntity>,
        userProfile: UserProfile?
    ): String {
        val totalStudyMinutes = sessions.sumOf { it.durationMinutes }
        val totalStudyHours = totalStudyMinutes / 60.0
        val avgFocusScore = if (sessions.isNotEmpty()) sessions.map { it.focusScore }.average().toInt() else 100
        val completedTasks = tasks.count { it.isCompleted }
        val pendingTasks = tasks.count { !it.isCompleted }
        val dueCards = flashcards.count { it.nextReviewAt <= System.currentTimeMillis() }
        val masteredCards = flashcards.count { it.intervalDays >= 21 }

        val sb = StringBuilder()
        sb.append("# 📊 Avora Study & Productivity Report\n\n")
        sb.append("**Generated:** ${dateFormat.format(Date())}\n")
        if (userProfile != null) {
            sb.append("**Student:** ${userProfile.name} (${userProfile.majorOrField})\n")
        }
        sb.append("\n---\n\n")

        sb.append("## ⏱️ Study Time & Focus Metrics\n")
        sb.append("- **Total Study Hours Logged:** ${String.format(Locale.US, "%.1f", totalStudyHours)} hrs (${totalStudyMinutes} mins)\n")
        sb.append("- **Completed Focus Sessions:** ${sessions.size}\n")
        sb.append("- **Average Focus & Concentration Score:** $avgFocusScore / 100\n\n")

        sb.append("## 📝 Tasks Breakdown\n")
        sb.append("- **Completed Tasks:** $completedTasks\n")
        sb.append("- **Pending Tasks:** $pendingTasks\n")
        sb.append("- **Completion Rate:** ${if (tasks.isNotEmpty()) ((completedTasks * 100) / tasks.size) else 0}%\n\n")

        sb.append("### High Priority Tasks\n")
        val urgentAndHigh = tasks.filter { !it.isCompleted && (it.priority == "URGENT" || it.priority == "HIGH") }
        if (urgentAndHigh.isNotEmpty()) {
            urgentAndHigh.forEach { t ->
                sb.append("- [ ] **[${t.priority}]** ${t.title} (${t.subject}, ~${t.estimatedMinutes}m)\n")
            }
        } else {
            sb.append("_No urgent pending tasks._\n")
        }
        sb.append("\n")

        sb.append("## 🃏 Spaced Repetition Flashcards\n")
        sb.append("- **Total Flashcards in Decks:** ${flashcards.size}\n")
        sb.append("- **Cards Due for Review:** $dueCards\n")
        sb.append("- **Mastered Cards (Interval ≥ 21 days):** $masteredCards\n\n")

        sb.append("---\n")
        sb.append("_Generated by Avora AI Study Companion_\n")
        return sb.toString()
    }

    /**
     * Copies text to the Android Clipboard.
     */
    fun copyToClipboard(context: Context, label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied $label to clipboard! 📋", Toast.LENGTH_SHORT).show()
    }

    /**
     * Opens Android System Share Sheet to share or save text / CSV / JSON files.
     */
    fun shareText(context: Context, title: String, content: String, mimeType: String = "text/plain") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, content)
        }
        val chooser = Intent.createChooser(intent, "Share or Save $title")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\r") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }
}

data class ParsedBackupData(
    val tasks: List<TaskEntity>,
    val flashcards: List<FlashcardEntity>,
    val sessions: List<StudySessionEntity>,
    val schedules: List<ScheduleEntity>
)
