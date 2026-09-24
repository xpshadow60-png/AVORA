package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.QuizResultEntity
import com.example.data.model.GeneratedQuiz
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    activeQuiz: GeneratedQuiz?,
    userAnswers: Map<Int, Int>,
    isQuizSubmitted: Boolean,
    isGenerating: Boolean,
    quizHistory: List<QuizResultEntity>,
    onGenerateQuiz: (String, String, String, Int) -> Unit,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onClearHistory: () -> Unit,
    onNavigateToFlashcards: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Active Quiz / Generator, 1: History

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🎯 AI Quiz Engine", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.Quiz, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("📊 Quiz History (${quizHistory.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.History, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> ActiveQuizOrGeneratorView(
                quiz = activeQuiz,
                userAnswers = userAnswers,
                isSubmitted = isQuizSubmitted,
                isGenerating = isGenerating,
                onGenerateQuiz = onGenerateQuiz,
                onSelectAnswer = onSelectAnswer,
                onSubmitQuiz = onSubmitQuiz,
                onRetakeQuiz = onRetakeQuiz,
                onNavigateToFlashcards = onNavigateToFlashcards
            )
            1 -> QuizHistoryView(
                history = quizHistory,
                onClearHistory = onClearHistory
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveQuizOrGeneratorView(
    quiz: GeneratedQuiz?,
    userAnswers: Map<Int, Int>,
    isSubmitted: Boolean,
    isGenerating: Boolean,
    onGenerateQuiz: (String, String, String, Int) -> Unit,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmitQuiz: () -> Unit,
    onRetakeQuiz: () -> Unit,
    onNavigateToFlashcards: () -> Unit
) {
    var subject by remember { mutableStateOf("Mathematics") }
    var topic by remember { mutableStateOf("Calculus Integration & Derivatives") }
    var difficulty by remember { mutableStateOf("Medium") }
    var questionCount by remember { mutableStateOf(5) }
    val context = LocalContext.current

    val subjects = listOf("Mathematics", "Computer Science", "Physics", "Chemistry", "Biology", "History", "Literature")
    val difficulties = listOf("Easy", "Medium", "Hard")
    val counts = listOf(3, 5, 10)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quiz Generator Card (Shown when no active quiz or when user wants to configure new quiz)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI Quiz Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate instant practice quizzes tailored to your academic level",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subject Chips
                    Text("Subject:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects) { subj ->
                            FilterChip(
                                selected = subject == subj,
                                onClick = { subject = subj },
                                label = { Text(subj, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Topic Input
                    OutlinedTextField(
                        value = topic,
                        onValueChange = { topic = it },
                        label = { Text("Topic or Chapter Name") },
                        placeholder = { Text("e.g. Data Structures, Newton's Laws, Organic Reactions") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Difficulty & Count Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Difficulty:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                difficulties.forEach { diff ->
                                    FilterChip(
                                        selected = difficulty == diff,
                                        onClick = { difficulty = diff },
                                        label = { Text(diff, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Questions:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                counts.forEach { cnt ->
                                    FilterChip(
                                        selected = questionCount == cnt,
                                        onClick = { questionCount = cnt },
                                        label = { Text("$cnt Qs", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (topic.isNotBlank()) {
                                onGenerateQuiz(subject, topic, difficulty, questionCount)
                            } else {
                                Toast.makeText(context, "Please enter a topic", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_quiz_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating Personalized Quiz...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Quiz")
                        }
                    }
                }
            }
        }

        // Active Quiz Player
        if (quiz != null && quiz.questions.isNotEmpty()) {
            val total = quiz.questions.size
            val answeredCount = userAnswers.size
            val progress = if (total > 0) answeredCount.toFloat() / total else 0f

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(quiz.topic, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("${quiz.subject} • ${quiz.difficulty} Level", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            AssistChip(
                                onClick = {},
                                label = { Text("$answeredCount / $total Answered") },
                                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Questions List
            items(quiz.questions) { question ->
                val selectedOption = userAnswers[question.id]
                val isCorrect = selectedOption == question.correctOptionIndex

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSubmitted) {
                            if (isCorrect) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        } else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Question ${question.id}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (isSubmitted) {
                                if (isCorrect) {
                                    Text("✓ Correct", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                } else {
                                    Text("✗ Incorrect", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(question.question, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Options
                        question.options.forEachIndexed { optIndex, optionText ->
                            val isThisSelected = selectedOption == optIndex
                            val isThisCorrectAnswer = isSubmitted && optIndex == question.correctOptionIndex

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    isSubmitted && isThisCorrectAnswer -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    isSubmitted && isThisSelected && !isCorrect -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                    isThisSelected -> MaterialTheme.colorScheme.primaryContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable(enabled = !isSubmitted) {
                                        onSelectAnswer(question.id, optIndex)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isThisSelected,
                                        onClick = { if (!isSubmitted) onSelectAnswer(question.id, optIndex) },
                                        enabled = !isSubmitted
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isThisSelected || isThisCorrectAnswer) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }

                        // Explanation after submit
                        if (isSubmitted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("📖 Explanation:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Text(question.explanation, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Submit / Results Actions
            item {
                if (!isSubmitted) {
                    Button(
                        onClick = onSubmitQuiz,
                        enabled = answeredCount > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_quiz_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Quiz & Check Score")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var correct = 0
                            quiz.questions.forEach {
                                if (userAnswers[it.id] == it.correctOptionIndex) correct++
                            }
                            val pct = if (total > 0) ((correct.toFloat() / total) * 100).toInt() else 0

                            Text("🏆 Quiz Completed!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Score: $correct / $total ($pct%)", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onRetakeQuiz,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🔄 Retake Quiz")
                                }
                                Button(
                                    onClick = onNavigateToFlashcards,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("🎴 Practice Flashcards")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizHistoryView(
    history: List<QuizResultEntity>,
    onClearHistory: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Past Quiz Attempts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (history.isNotEmpty()) {
                    TextButton(onClick = onClearHistory) {
                        Text("Clear History", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Quiz, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No quiz results recorded yet.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Text("Take your first AI practice quiz to track your mastery.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(history) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.topic, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("${item.subject} • ${item.difficulty}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(dateFormat.format(Date(item.completedAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))

                            if (item.weakTopicsJson.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Needs review: ${item.weakTopicsJson}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }

                        // Score Badge
                        Surface(
                            shape = CircleShape,
                            color = if (item.percentage >= 80) MaterialTheme.colorScheme.primaryContainer
                            else if (item.percentage >= 50) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${item.percentage}%",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (item.percentage >= 80) MaterialTheme.colorScheme.primary
                                    else if (item.percentage >= 50) MaterialTheme.colorScheme.tertiary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
