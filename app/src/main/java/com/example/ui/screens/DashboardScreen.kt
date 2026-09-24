package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.FlashcardEntity
import com.example.data.local.ScheduleEntity
import com.example.data.local.StudySessionEntity
import com.example.data.local.TaskEntity
import com.example.data.model.FullCareerPath
import com.example.focus.FocusTimerState
import com.example.ui.components.DailyStudyGoalCard
import com.example.ui.components.computeTodayStudyMinutes
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500

@Composable
fun DashboardScreen(
    tasks: List<TaskEntity>,
    schedules: List<ScheduleEntity>,
    studySessions: List<StudySessionEntity> = emptyList(),
    flashcards: List<FlashcardEntity> = emptyList(),
    careerPath: FullCareerPath? = null,
    dailyGoalHours: Float = 3.0f,
    onUpdateDailyGoal: (Float) -> Unit = {},
    focusTimerState: FocusTimerState,
    onStartQuickFocus: (Int, String) -> Unit,
    onTaskToggle: (TaskEntity) -> Unit,
    onNavigateToTutor: () -> Unit,
    onNavigateToHomework: () -> Unit,
    onNavigateToCoding: () -> Unit,
    onNavigateToQuiz: () -> Unit,
    onNavigateToNotes: () -> Unit,
    onNavigateToCards: () -> Unit,
    onNavigateToPlanner: () -> Unit,
    onNavigateToFocus: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToCareer: () -> Unit = {},
    onOpenPrivacyDisclosures: () -> Unit = {}
) {
    var quickPromptText by remember { mutableStateOf("") }

    val pendingTasks = tasks.filter { !it.isCompleted }
    val todayCompletedMinutes = remember(studySessions) {
        computeTodayStudyMinutes(studySessions)
    }

    val activeSessionExtraMinutes = if (focusTimerState.isActive) {
        maxOf(0, (focusTimerState.totalSeconds - focusTimerState.remainingSeconds) / 60)
    } else {
        0
    }

    val dueCardsCount = remember(flashcards) {
        val now = System.currentTimeMillis()
        flashcards.count { it.nextReviewAt <= now }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 12.dp)
    ) {
        // Hero Visual Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_study_hero_1786458240847),
                        contentDescription = "Avora Student Hero",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary500,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = "AVORA AI LEARNING PLATFORM",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = "Learn • Build • Ship Technology",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White
                        )
                        Text(
                            text = "Your 24/7 AI technology tutor, code mentor, challenge engine, and software roadmap.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE0E3F5)
                        )
                    }
                }
            }
        }

        // "Ask Avora Anything" Quick Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "✨ What would you like to master today?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { onNavigateToTutor() }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search / Ask",
                            tint = Primary500,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Ask any tech, code, AI, API, or algorithm question...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Primary500.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowForward,
                                    contentDescription = null,
                                    tint = Primary500,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Quick Action Chips Row (All Core Modules)
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            SuggestionChip(
                                onClick = onNavigateToTutor,
                                label = { Text("🤖 AI Tutor", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToHomework,
                                label = { Text("✍️ Solve Problem", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToCoding,
                                label = { Text("🐍 Python Playground", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToCareer,
                                label = { Text("🚀 Projects & Roadmaps", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToQuiz,
                                label = { Text("🎯 AI Quiz", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToNotes,
                                label = { Text("📑 Tech Notes", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToCards,
                                label = { Text("🃏 Flashcards", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToPlanner,
                                label = { Text("📅 Planner", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onNavigateToFocus,
                                label = { Text("⏱️ Focus Timer", fontSize = 11.sp) }
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = onOpenPrivacyDisclosures,
                                label = { Text("🛡️ Privacy & Data Disclosures", fontSize = 11.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Daily Study Goal Card
        item {
            DailyStudyGoalCard(
                todayStudyMinutes = todayCompletedMinutes,
                activeSessionExtraMinutes = activeSessionExtraMinutes,
                goalHours = dailyGoalHours,
                onUpdateGoal = onUpdateDailyGoal,
                onStartFocusClick = onNavigateToFocus
            )
        }

        // Complete AI Learning & Engineering Suite Grid (All Features)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🛠️ Complete AI Learning & Builder Suite",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Row 1: AI Tutor & Tech Problem Solver
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearningToolCard(
                        title = "Avora AI Tech Tutor",
                        subtitle = "5-step learning, Socratic hints, voice explanations",
                        icon = Icons.Filled.AutoAwesome,
                        iconColor = Primary500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToTutor
                    )
                    LearningToolCard(
                        title = "Tech Problem Solver",
                        subtitle = "Hint-first code solutions with architectural steps",
                        icon = Icons.Filled.EditNote,
                        iconColor = CyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToHomework
                    )
                }

                // Row 2: Code Lab & Project Builder
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearningToolCard(
                        title = "Code Lab & Mentor",
                        subtitle = "Live syntax sandbox, instant debugging & challenges",
                        icon = Icons.Filled.Terminal,
                        iconColor = CoralPriority,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCoding
                    )
                    LearningToolCard(
                        title = "Project Builder",
                        subtitle = "Real-world portfolio apps with step-by-step milestones",
                        icon = Icons.Filled.Architecture,
                        iconColor = GreenSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCareer() }
                    )
                }

                // Row 3: Learning Roadmaps & AI Quiz Labs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearningToolCard(
                        title = "Learning Roadmaps",
                        subtitle = "Structured career paths (AI, Web, Mobile, Cloud)",
                        icon = Icons.Filled.Map,
                        iconColor = Primary500,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToCareer() }
                    )
                    LearningToolCard(
                        title = "AI Tech Quiz & Labs",
                        subtitle = "Custom technical practice tests & timers",
                        icon = Icons.Filled.Quiz,
                        iconColor = CyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToQuiz
                    )
                }

                // Row 4: Notes & Docs & Flashcard Decks
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearningToolCard(
                        title = "Tech Notes & Docs",
                        subtitle = "API docs, cheat sheets, 1-tap concept flashcards",
                        icon = Icons.Filled.Description,
                        iconColor = CoralPriority,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToNotes
                    )
                    LearningToolCard(
                        title = "Flashcard Decks",
                        subtitle = if (dueCardsCount > 0) "$dueCardsCount cards due for SuperMemo recall" else "${flashcards.size} cards in active rotation",
                        icon = Icons.Filled.Style,
                        iconColor = GreenSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCards
                    )
                }

                // Row 5: Study Planner & Focus Timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearningToolCard(
                        title = "Study Planner",
                        subtitle = "${pendingTasks.size} pending tasks and AI schedule generation",
                        icon = Icons.Filled.EventNote,
                        iconColor = Primary500,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPlanner
                    )
                    LearningToolCard(
                        title = "Focus Timer",
                        subtitle = if (focusTimerState.isActive) "Session in progress" else "Pomodoro timer & ambient sounds",
                        icon = Icons.Filled.Timer,
                        iconColor = CyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToFocus
                    )
                }

                // Row 6: Skill Mastery Matrix & Verified Portfolio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LearningToolCard(
                        title = "Skill Matrix",
                        subtitle = "Track competency across 14+ technical domains",
                        icon = Icons.Filled.BarChart,
                        iconColor = CoralPriority,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAnalytics
                    )
                    LearningToolCard(
                        title = "Verified Portfolio",
                        subtitle = "Showcase built projects, certificates & achievements",
                        icon = Icons.Filled.VerifiedUser,
                        iconColor = GreenSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToAnalytics
                    )
                }

                // Privacy & Data Governance Banner Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onOpenPrivacyDisclosures() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GreenSuccess.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = null,
                                        tint = GreenSuccess,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "Privacy & Data Handling Transparency",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "0% Ad Trackers • Local-First Room DB • Ephemeral AI Processing",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Disclosures",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Active Tasks Preview Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Today's Study Tasks",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${pendingTasks.size} tasks pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        TextButton(onClick = onNavigateToPlanner) {
                            Text("Open Planner", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (tasks.isEmpty()) {
                        Text(
                            text = "No study tasks scheduled for today. Generate an AI study plan!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        tasks.take(4).forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { onTaskToggle(task) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (!task.isCompleted) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${task.subject} • ${task.estimatedMinutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
private fun LearningToolCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
