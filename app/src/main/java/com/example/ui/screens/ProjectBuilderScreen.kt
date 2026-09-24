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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiProjectGuide
import com.example.data.remote.AvoraLearningDataEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectBuilderScreen(
    onGenerateCustomProject: (String, String, String) -> Unit = { _, _, _ -> },
    onNavigateToCode: () -> Unit = {},
    onProjectCompleted: (String) -> Unit = {}
) {
    val curatedProjects = remember { AvoraLearningDataEngine.getCuratedProjects() }
    var selectedProjectIndex by remember { mutableStateOf(0) }
    val activeProject = curatedProjects.getOrNull(selectedProjectIndex) ?: curatedProjects.first()

    // Map of stage completion: stageNumber -> isCompleted
    var completedStages by remember { mutableStateOf(mutableStateMapOf<Int, Boolean>()) }
    var showCustomProjectDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val completedCount = completedStages.values.count { it }
    val progressPercent = (completedCount.toFloat() / activeProject.stages.size.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header & Custom Project Trigger
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🛠️ AI Project Builder",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Learn by building real-world AI and software projects step-by-step.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = { showCustomProjectDialog = true },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("btn_add_custom_project")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "New Project", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$completedCount of ${activeProject.stages.size} Stages Completed (${(progressPercent * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Project Selection Carousel
        item {
            Text(
                text = "📚 Choose Project Track",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(curatedProjects.indices.toList()) { idx ->
                    val proj = curatedProjects[idx]
                    val isSelected = selectedProjectIndex == idx
                    Card(
                        onClick = {
                            selectedProjectIndex = idx
                            completedStages.clear()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(240.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(proj.iconEmoji, fontSize = 20.sp)
                                Text(
                                    proj.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                proj.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(proj.difficulty, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text(proj.estimatedHours, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }
        }

        // Active Project Overview & Prerequisites
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎯 Project Blueprint: ${activeProject.title}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeProject.overview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Prerequisites: " + activeProject.prerequisites.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Key Concepts: " + activeProject.conceptsToLearn.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // 9-Stage Guided Roadmap Cards
        item {
            Text(
                text = "📍 9-Stage Step-by-Step Roadmap",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(activeProject.stages) { stage ->
            val isDone = completedStages[stage.stageNumber] == true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDone) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("${stage.stageNumber}", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                            Text(
                                text = stage.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { checked ->
                                completedStages[stage.stageNumber] = checked
                                if (completedStages.values.count { it } == activeProject.stages.size) {
                                    onProjectCompleted(activeProject.id)
                                    Toast.makeText(context, "🎉 Full Project Completed! Added to Portfolio!", Toast.LENGTH_LONG).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stage.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    stage.actionItems.forEach { action ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(action, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    stage.codeSnippet?.let { snippet ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Code Checkpoint", color = Color(0xFFCDD6F4), style = MaterialTheme.typography.labelSmall)
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(snippet))
                                            Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color(0xFFBAC2DE), modifier = Modifier.size(14.dp))
                                    }
                                }
                                SelectionContainer {
                                    Text(
                                        text = snippet,
                                        color = Color(0xFFA6E3A1),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action: Copy README & Open Code Playground
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(activeProject.readmeTemplate))
                        Toast.makeText(context, "Portfolio README template copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy README")
                }

                Button(
                    onClick = onNavigateToCode,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Terminal, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Playground")
                }
            }
        }
    }

    if (showCustomProjectDialog) {
        var goalText by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("AI & Machine Learning") }
        var selectedDifficulty by remember { mutableStateOf("Intermediate") }

        AlertDialog(
            onDismissRequest = { showCustomProjectDialog = false },
            title = { Text("Generate Custom AI Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Enter what you want to build (e.g. 'Build an AI Hindi Voice Tutor' or 'Cryptocurrency Price Predictor').", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { goalText = it },
                        label = { Text("Project Goal / Idea") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (goalText.isNotBlank()) {
                            onGenerateCustomProject(goalText, selectedCategory, selectedDifficulty)
                            showCustomProjectDialog = false
                            Toast.makeText(context, "Generating dynamic 9-stage roadmap with Avora AI...", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Generate Roadmap")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
