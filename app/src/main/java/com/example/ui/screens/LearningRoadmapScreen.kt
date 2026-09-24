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
import com.example.data.model.LearningTrack
import com.example.data.remote.AvoraLearningDataEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningRoadmapScreen(
    onNavigateToLesson: (String, String) -> Unit = { _, _ -> },
    onNavigateToProject: () -> Unit = {},
    onNavigateToCode: () -> Unit = {}
) {
    val tracks = remember { AvoraLearningDataEngine.getLearningTracks() }
    var selectedTrackIndex by remember { mutableStateOf(0) }
    val activeTrack = tracks.getOrNull(selectedTrackIndex) ?: tracks.first()

    // Map of completed modules: moduleId -> Boolean
    var completedModules by remember { mutableStateOf(mutableStateMapOf<String, Boolean>()) }
    val context = LocalContext.current

    val completedCount = completedModules.values.count { it }
    val progressRatio = (completedCount.toFloat() / activeTrack.modules.size.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
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
                                text = "🧭 Personalized Learning Paths",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Structured curricula designed for Indian tech students to become future-ready AI builders.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Text(activeTrack.iconEmoji, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$completedCount of ${activeTrack.modules.size} Modules Completed (${(progressRatio * 100).toInt()}%)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Track Selector Chips
        item {
            Text(
                text = "🎯 Select Your Learning Track",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(tracks.indices.toList()) { idx ->
                    val trk = tracks[idx]
                    val isSelected = selectedTrackIndex == idx
                    Card(
                        onClick = {
                            selectedTrackIndex = idx
                            completedModules.clear()
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(220.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(trk.iconEmoji, fontSize = 20.sp)
                                Text(
                                    trk.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                trk.targetRole,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "${trk.estimatedWeeks} Weeks • ${trk.difficultyLevel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Key Skills In Track
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🚀 Key Competencies To Master",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeTrack.keySkills) { skill ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text(skill, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            }
        }

        // Sequential Modules
        item {
            Text(
                text = "📚 Sequential Curriculum Modules",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(activeTrack.modules) { module ->
            val isDone = completedModules[module.id] == true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDone) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
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
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isDone) Color(0xFF10B981) else MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isDone) {
                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("${module.moduleNumber}", color = Color.White, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                            Column {
                                Text(
                                    text = module.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = module.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Checkbox(
                            checked = isDone,
                            onCheckedChange = { checked ->
                                completedModules[module.id] = checked
                                if (checked) {
                                    Toast.makeText(context, "Module ${module.moduleNumber} Completed! +40 XP", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Topics Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(module.topics) { topic ->
                            AssistChip(
                                onClick = { onNavigateToLesson(activeTrack.title, topic) },
                                label = { Text(topic, style = MaterialTheme.typography.labelSmall) },
                                leadingIcon = { Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛠️ Milestone: ${module.projectMilestone}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onNavigateToCode) {
                            Text("Code Challenge", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
