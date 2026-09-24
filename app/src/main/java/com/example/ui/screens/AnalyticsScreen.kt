package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.FlashcardEntity
import com.example.data.local.QuizResultEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.ui.components.DailyStudyGoalCard
import com.example.ui.components.WeeklyStudyHoursSummaryChart
import com.example.ui.components.WeeklyTaskProgressChart
import com.example.ui.components.computeTodayStudyMinutes
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AnalyticsScreen(
    tasks: List<TaskEntity>,
    sessions: List<StudySessionEntity>,
    flashcards: List<FlashcardEntity> = emptyList(),
    quizResults: List<QuizResultEntity> = emptyList(),
    dailyGoalHours: Float = 4.0f,
    onUpdateDailyGoal: (Float) -> Unit = {},
    onOpenExportBackup: () -> Unit = {}
) {
    val todayMinutes = computeTodayStudyMinutes(sessions)
    val totalMinutes = sessions.sumOf { it.durationMinutes }
    val totalHours = totalMinutes / 60
    val remainingMins = totalMinutes % 60
    val completedTasksCount = tasks.count { it.isCompleted }
    val totalTasksCount = tasks.size
    
    // Quiz Performance Stats
    val averageQuizScore = if (quizResults.isNotEmpty()) {
        quizResults.map { it.percentage }.average().toInt()
    } else 0
    val totalQuizzesTaken = quizResults.size
    val allWeakTopics = quizResults
        .map { it.weakTopicsJson }
        .filter { it.isNotBlank() }
        .flatMap { it.split(",") }
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .take(5)
    
    // SRS Flashcard Retention Stats
    val totalReviewsLifetime = flashcards.sumOf { it.totalReviews }
    val totalSuccessfulLifetime = flashcards.sumOf { it.successfulReviews }
    val retentionRatePercent = if (totalReviewsLifetime > 0) {
        ((totalSuccessfulLifetime.toDouble() / totalReviewsLifetime) * 100).toInt()
    } else {
        100
    }
    val dueTodayCount = flashcards.count { it.nextReviewAt <= System.currentTimeMillis() }
    val masteredCardsCount = flashcards.count { it.intervalDays >= 7 }

    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Study Productivity Analytics",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Track daily goal progress, total focus hours, task execution, and session consistency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenExportBackup,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                ) {
                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Daily Study Goal Progress Card
        item {
            DailyStudyGoalCard(
                todayStudyMinutes = todayMinutes,
                goalHours = dailyGoalHours,
                onUpdateGoal = onUpdateDailyGoal
            )
        }

        // Quiz Mastery & Weak Topics Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "QUIZ MASTERY & ACCURACY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = Primary500
                            )
                            Text(
                                text = if (totalQuizzesTaken > 0) "$averageQuizScore% Average Score ($totalQuizzesTaken Tests)" else "No quizzes completed yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (averageQuizScore >= 75) GreenSuccess.copy(alpha = 0.15f) else Primary500.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = "Quiz Mastery",
                                    tint = if (averageQuizScore >= 75) GreenSuccess else Primary500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    if (allWeakTopics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("🎯 Identified Weak Areas for Review:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = CoralPriority)
                        Spacer(modifier = Modifier.height(4.dp))
                        allWeakTopics.forEach { weakTopic ->
                            Surface(
                                color = CoralPriority.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "• $weakTopic",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoralPriority,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Gauge Row
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TOTAL FOCUS TIME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = Primary500
                    )
                    Text(
                        text = "${totalHours}h ${remainingMins}m",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Tasks Finished", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "$completedTasksCount / $totalTasksCount", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Sessions Done", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "${sessions.size} sessions", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Primary500)
                        }
                        Column {
                            Text(text = "Focus Streak", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "🔥 4 Days", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = CyanAccent)
                        }
                    }
                }
            }
        }

        // Spaced Repetition Mastery & Retention Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SPACED REPETITION RETENTION",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = CyanAccent
                            )
                            Text(
                                text = "$retentionRatePercent% Recall",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = CyanAccent.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { retentionRatePercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (retentionRatePercent >= 80) GreenSuccess else CoralPriority,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total Deck Cards", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "${flashcards.size} cards", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Column {
                            Text(text = "Cards Due Today", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "$dueTodayCount due", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = if (dueTodayCount > 0) CoralPriority else GreenSuccess)
                        }
                        Column {
                            Text(text = "Mastered (7d+)", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = "$masteredCardsCount cards", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Primary500)
                        }
                    }
                }
            }
        }

        // Weekly Study Hours Summary Chart (D3 & Recharts Visualizer)
        item {
            WeeklyStudyHoursSummaryChart(
                sessions = sessions,
                dailyGoalHours = dailyGoalHours
            )
        }

        // Weekly Task Execution Progress
        item {
            WeeklyTaskProgressChart(tasks = tasks)
        }

        // Session Logs Header
        item {
            Text(
                text = "Recent Study Sessions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (sessions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(text = "No study sessions recorded yet. Start a session in 'Focus Timer'!", color = Color.Gray)
                    }
                }
            }
        } else {
            items(sessions, key = { it.id }) { session ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Primary500.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = "Session",
                                    tint = Primary500
                                )
                            }

                            Column {
                                Text(
                                    text = session.subject,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = dateFormat.format(Date(session.completedAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${session.durationMinutes} mins",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = CyanAccent
                            )
                            Text(
                                text = "Focus Score: ${session.focusScore}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun StudySessionEntity.distractionCount(): Int {
    return this.distractionsBlockedCount
}
