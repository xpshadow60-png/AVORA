package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ScheduleEntity
import com.example.data.local.TaskEntity
import com.example.data.model.GeneratedStudyTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerScreen(
    tasks: List<TaskEntity>,
    schedules: List<ScheduleEntity>,
    generatedPlan: List<GeneratedStudyTask>,
    isGeneratingPlan: Boolean,
    onGeneratePlan: (List<String>, String, Double, List<String>, String, String) -> Unit,
    onApplyPlan: () -> Unit,
    onAddTask: (String, String, String, Int, String, String, Long) -> Unit,
    onTaskToggle: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddSchedule: (String, String, String, String, String) -> Unit,
    onPrioritizeWithAi: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: AI Plan Generator, 1: Tasks & Schedule

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("📅 AI Study Planner", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.AutoAwesome, contentDescription = null) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("✅ Active Tasks (${tasks.count { !it.isCompleted }})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.Checklist, contentDescription = null) }
            )
        }

        when (selectedTab) {
            0 -> AiStudyPlanGeneratorView(
                generatedPlan = generatedPlan,
                isGenerating = isGeneratingPlan,
                onGeneratePlan = onGeneratePlan,
                onApplyPlan = onApplyPlan,
                onViewActiveTasks = { selectedTab = 1 }
            )
            1 -> TasksScreen(
                tasks = tasks,
                schedules = schedules,
                onAddTask = onAddTask,
                onTaskToggle = onTaskToggle,
                onDeleteTask = onDeleteTask,
                onAddSchedule = onAddSchedule,
                onPrioritizeWithAi = onPrioritizeWithAi
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiStudyPlanGeneratorView(
    generatedPlan: List<GeneratedStudyTask>,
    isGenerating: Boolean,
    onGeneratePlan: (List<String>, String, Double, List<String>, String, String) -> Unit,
    onApplyPlan: () -> Unit,
    onViewActiveTasks: () -> Unit
) {
    var selectedSubjects by remember { mutableStateOf(setOf("Mathematics", "Physics", "Computer Science")) }
    var topicsText by remember { mutableStateOf("Calculus Integrals, Newton Dynamics, Sorting Algorithms & Recursion") }
    var dailyHours by remember { mutableStateOf(2.5) }
    var selectedDays by remember { mutableStateOf(setOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")) }
    var examGoal by remember { mutableStateOf("Ace Midterm Exams with 90%+ Score") }
    var examDate by remember { mutableStateOf("In 3 Weeks") }

    val allSubjects = listOf("Mathematics", "Physics", "Computer Science", "Chemistry", "Biology", "History", "Literature")
    val allDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        text = "Personalized AI Study Schedule Generator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Build a balanced, exam-ready weekly study timetable tailored to your subjects and available hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subjects multi-select
                    Text("Select Subjects to Study:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allSubjects) { subj ->
                            val isSelected = selectedSubjects.contains(subj)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSubjects = if (isSelected) {
                                        if (selectedSubjects.size > 1) selectedSubjects - subj else selectedSubjects
                                    } else {
                                        selectedSubjects + subj
                                    }
                                },
                                label = { Text(subj, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Topics
                    OutlinedTextField(
                        value = topicsText,
                        onValueChange = { topicsText = it },
                        label = { Text("Key Topics & Chapters to Cover") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Target Goal & Exam Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = examGoal,
                            onValueChange = { examGoal = it },
                            label = { Text("Study Goal") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = examDate,
                            onValueChange = { examDate = it },
                            label = { Text("Exam Date / Timeline") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Available Days Multi-select
                    Text("Available Study Days:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(allDays) { day ->
                            val isSelected = selectedDays.contains(day)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedDays = if (isSelected) {
                                        if (selectedDays.size > 1) selectedDays - day else selectedDays
                                    } else {
                                        selectedDays + day
                                    }
                                },
                                label = { Text(day.take(3), fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            onGeneratePlan(
                                selectedSubjects.toList(),
                                topicsText,
                                dailyHours,
                                selectedDays.toList(),
                                examGoal,
                                examDate
                            )
                        },
                        enabled = !isGenerating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("generate_study_plan_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Formulating Balanced Schedule...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate AI Study Schedule")
                        }
                    }
                }
            }
        }

        // Generated Plan Results
        if (generatedPlan.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("📅 Generated Timetable (${generatedPlan.size} Sessions)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Ready to add to your task manager", style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = {
                                onApplyPlan()
                                onViewActiveTasks()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add All to Planner")
                        }
                    }
                }
            }

            items(generatedPlan) { taskItem ->
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = taskItem.dayOfWeek,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = taskItem.subject,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(taskItem.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text("Topic: ${taskItem.topic} • ${taskItem.taskType}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "${taskItem.estimatedMinutes}m",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
