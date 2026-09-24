package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun CareerHubScreen(
    careerPath: FullCareerPath,
    onRetakeAssessment: () -> Unit,
    onToggleMilestone: (milestoneId: String) -> Unit = {},
    completedMilestoneIds: Set<String> = emptySet(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSectionTab by remember { mutableStateOf(0) } // 0: Roadmap, 1: YouTube Videos, 2: Online Classes, 3: Strategy

    val allMilestones = careerPath.phases.flatMap { it.milestones }
    val totalMilestones = allMilestones.size
    val completedCount = allMilestones.count { completedMilestoneIds.contains(it.id) || it.isCompleted }
    val progressPct = if (totalMilestones > 0) (completedCount.toFloat() / totalMilestones.toFloat()) else 0f

    fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("CareerHubScreen", "Failed to open link: $url", e)
            android.widget.Toast.makeText(context, "Could not open link: ${e.localizedMessage ?: "No browser app found"}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Primary500),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = "Career",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = careerPath.assessment.fieldOfStudy,
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Target: ${careerPath.assessment.specialization}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = CyanAccent
                                    )
                                }
                            }

                            // Retake button
                            OutlinedButton(
                                onClick = onRetakeAssessment,
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Retake (10Q)", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Text(
                            text = careerPath.overallSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Milestone Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Career Journey Milestones",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$completedCount of $totalMilestones (${(progressPct * 100).toInt()}%)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Primary500
                                )
                            }

                            LinearProgressIndicator(
                                progress = progressPct,
                                color = Primary500,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }

            // Section Tabs Row (Roadmap / YouTube / Online Classes / Strategy)
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabs = listOf("🗺️ Roadmap", "📺 YouTube", "🎓 Classes", "💡 Strategy")
                        tabs.forEachIndexed { index, title ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedSectionTab == index) Primary500 else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedSectionTab = index }
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (selectedSectionTab == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Tab 0: Roadmap Phases
            if (selectedSectionTab == 0) {
                items(careerPath.phases) { phase ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Phase Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Primary500,
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${phase.phaseNumber}",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Text(
                                        text = phase.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CyanAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = phase.duration,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = CyanAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = phase.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Milestones List
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                phase.milestones.forEach { milestone ->
                                    val isDone = completedMilestoneIds.contains(milestone.id) || milestone.isCompleted

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isDone) GreenSuccess.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (isDone) GreenSuccess.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onToggleMilestone(milestone.id) }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Checkbox(
                                                checked = isDone,
                                                onCheckedChange = { onToggleMilestone(milestone.id) },
                                                colors = CheckboxDefaults.colors(checkedColor = GreenSuccess),
                                                modifier = Modifier.size(20.dp)
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = milestone.title,
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        textDecoration = if (isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                    ),
                                                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = milestone.description,
                                                    style = MaterialTheme.typography.bodySmall,
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

            // Tab 1: Curated YouTube Video Series
            if (selectedSectionTab == 1) {
                item {
                    Text(
                        text = "Curated YouTube Video Masterclasses",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(careerPath.videoSuggestions) { video ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CoralPriority),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "YouTube",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = video.channel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CoralPriority
                                        )
                                        Text(
                                            text = video.topic,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = video.duration,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                text = video.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { openUrl(video.youtubeUrl) },
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPriority),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Watch on YouTube", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Tab 2: Online Classes & MOOC Suggestions
            if (selectedSectionTab == 2) {
                item {
                    Text(
                        text = "Accredited Online Classes & MOOCs",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(careerPath.classSuggestions) { course ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Primary500.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = course.platform,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Primary500,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = GoldReview, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "${course.rating}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Offered by: ${course.institution}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (course.isFree) GreenSuccess.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (course.isFree) "Free Audit" else "Certificate",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (course.isFree) GreenSuccess else CyanAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = { openUrl(course.courseUrl) },
                                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enroll / View Course", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Tab 3: Study Strategy & Key Skills
            if (selectedSectionTab == 3) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "💡 Personalized Study Strategy",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            StrategyItem("Target Field", careerPath.assessment.fieldOfStudy)
                            StrategyItem("Specialization", careerPath.assessment.specialization)
                            StrategyItem("Weekly Commitment", careerPath.assessment.weeklyStudyHours)
                            StrategyItem("Learning Style", careerPath.assessment.learningStyle)
                            StrategyItem("Primary Challenge", careerPath.assessment.primaryChallenge)
                            StrategyItem("Target Credential", careerPath.assessment.certificationGoal)
                            StrategyItem("Daily Focus Rhythm", careerPath.assessment.dailyStudyHabit)
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "⚡ Core Competencies to Master",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            careerPath.keySkillsToMaster.forEach { skill ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
                                    Text(text = skill, style = MaterialTheme.typography.bodyMedium)
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
private fun StrategyItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
