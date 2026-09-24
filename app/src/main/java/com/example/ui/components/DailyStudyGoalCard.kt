package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.StudySessionEntity
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500
import com.example.ui.theme.Primary600
import java.util.*
import kotlin.math.roundToInt

@Composable
fun DailyStudyGoalCard(
    todayStudyMinutes: Int,
    goalHours: Float,
    onUpdateGoal: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeSessionExtraMinutes: Int = 0,
    onStartFocusClick: (() -> Unit)? = null
) {
    var showGoalDialog by remember { mutableStateOf(false) }

    val totalEffectiveMinutes = todayStudyMinutes + activeSessionExtraMinutes
    val goalMinutes = (goalHours * 60).toInt().coerceAtLeast(1)
    val progressFraction = (totalEffectiveMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f)
    val percentage = ((totalEffectiveMinutes.toFloat() / goalMinutes.toFloat()) * 100).toInt()
    val isGoalAchieved = totalEffectiveMinutes >= goalMinutes

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "goal_progress_animation"
    )

    val completedHours = totalEffectiveMinutes / 60
    val completedRemainingMinutes = totalEffectiveMinutes % 60

    val remainingMinutes = (goalMinutes - totalEffectiveMinutes).coerceAtLeast(0)
    val remainingHours = remainingMinutes / 60
    val remainingSubMinutes = remainingMinutes % 60

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_study_goal_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Badge, Title, and Edit Goal Button
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
                        color = if (isGoalAchieved) GreenSuccess.copy(alpha = 0.2f) else Primary500.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isGoalAchieved) Icons.Default.EmojiEvents else Icons.Default.TrackChanges,
                                contentDescription = "Goal",
                                tint = if (isGoalAchieved) GreenSuccess else Primary500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "DAILY STUDY TARGET",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = if (isGoalAchieved) GreenSuccess else Primary500
                        )
                        Text(
                            text = if (isGoalAchieved) "Target Smashed! 🎉" else "Today's Study Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Edit Goal Chip / Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clickable { showGoalDialog = true }
                        .testTag("edit_daily_goal_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Adjust Goal",
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Set ${formatHours(goalHours)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Stat & Percentage Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (completedHours > 0) "${completedHours}h ${completedRemainingMinutes}m" else "${completedRemainingMinutes}m",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "/ ${formatHours(goalHours)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    if (activeSessionExtraMinutes > 0) {
                        Text(
                            text = "⚡ +$activeSessionExtraMinutes min in active session",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                    }
                }

                // Percentage Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        isGoalAchieved -> GreenSuccess
                        percentage >= 75 -> Primary500
                        percentage >= 40 -> CyanAccent.copy(alpha = 0.85f)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ) {
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (percentage >= 40) Color.White else MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Modern Rounded Animated Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .testTag("daily_study_goal_progress_bar")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(7.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isGoalAchieved) {
                                    listOf(GreenSuccess, Color(0xFF00CEC9))
                                } else {
                                    listOf(Primary500, CyanAccent)
                                }
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Info: Time Left & Motivational Tip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isGoalAchieved) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isGoalAchieved) GreenSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = when {
                            isGoalAchieved -> {
                                val overtime = totalEffectiveMinutes - goalMinutes
                                if (overtime > 0) "+${overtime / 60}h ${overtime % 60}m beyond target!" else "Goal completed today!"
                            }
                            remainingHours > 0 -> "${remainingHours}h ${remainingSubMinutes}m remaining"
                            else -> "${remainingSubMinutes}m remaining"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isGoalAchieved) GreenSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isGoalAchieved && onStartFocusClick != null) {
                    Text(
                        text = "Start Focus →",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Primary500,
                        modifier = Modifier.clickable { onStartFocusClick() }
                    )
                } else {
                    Text(
                        text = when {
                            isGoalAchieved -> "🔥 Excellent consistency!"
                            percentage >= 75 -> "💪 Almost there!"
                            percentage >= 40 -> "✨ Halfway through!"
                            else -> "🚀 Keep building momentum"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Set Daily Goal Dialog
    if (showGoalDialog) {
        SetDailyGoalDialog(
            currentGoal = goalHours,
            onDismiss = { showGoalDialog = false },
            onSave = { newGoal ->
                onUpdateGoal(newGoal)
                showGoalDialog = false
            }
        )
    }
}

@Composable
fun SetDailyGoalDialog(
    currentGoal: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var selectedGoal by remember { mutableStateOf(currentGoal) }
    val presetOptions = listOf(1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 8.0f)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("set_daily_goal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary500.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = Primary500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Daily Study Goal",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Set your target study hours per day",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Target Hours Stepper & Display
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Minus 0.5h
                        IconButton(
                            onClick = {
                                selectedGoal = (selectedGoal - 0.5f).coerceAtLeast(0.5f)
                            },
                            enabled = selectedGoal > 0.5f
                        ) {
                            Icon(
                                imageVector = Icons.Default.RemoveCircleOutline,
                                contentDescription = "Decrease Goal",
                                tint = if (selectedGoal > 0.5f) Primary500 else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatHours(selectedGoal),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                color = Primary500
                            )
                            Text(
                                text = "${(selectedGoal * 60).toInt()} minutes daily",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Plus 0.5h
                        IconButton(
                            onClick = {
                                selectedGoal = (selectedGoal + 0.5f).coerceAtMost(12.0f)
                            },
                            enabled = selectedGoal < 12.0f
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = "Increase Goal",
                                tint = if (selectedGoal < 12.0f) CyanAccent else Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Slider Control
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.5h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("12.0h", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Slider(
                        value = selectedGoal,
                        onValueChange = { selectedGoal = (it * 2).roundToInt() / 2f },
                        valueRange = 0.5f..12.0f,
                        steps = 22,
                        colors = SliderDefaults.colors(
                            thumbColor = Primary500,
                            activeTrackColor = Primary500,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // Preset Quick Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Quick Presets",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(presetOptions) { preset ->
                            val isSelected = selectedGoal == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedGoal = preset },
                                label = { Text("${formatHours(preset)}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary500,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSave(selectedGoal) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Goal")
                    }
                }
            }
        }
    }
}

/**
 * Calculates total minutes completed today from list of sessions.
 */
fun computeTodayStudyMinutes(sessions: List<StudySessionEntity>): Int {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfDay = cal.timeInMillis

    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val endOfDay = cal.timeInMillis

    return sessions
        .filter { it.completedAt in startOfDay..endOfDay }
        .sumOf { it.durationMinutes }
}

private fun formatHours(hours: Float): String {
    return if (hours % 1f == 0f) {
        "${hours.toInt()}h"
    } else {
        "${hours}h"
    }
}
