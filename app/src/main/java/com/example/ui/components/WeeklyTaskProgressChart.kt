package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
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
import com.example.data.local.TaskEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500
import com.example.ui.theme.Primary600
import java.text.SimpleDateFormat
import java.util.*

data class DayTaskSummary(
    val dayName: String,         // "Mon", "Tue"
    val dateLabel: String,       // "Aug 15"
    val isToday: Boolean,
    val completedCount: Int,
    val totalCount: Int,
    val dateTimestamp: Long,
    val completedTasks: List<TaskEntity>
)

@Composable
fun WeeklyTaskProgressChart(
    tasks: List<TaskEntity>,
    modifier: Modifier = Modifier
) {
    val weeklyData = remember(tasks) {
        computeWeeklyTaskProgress(tasks)
    }

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    val totalWeekCompleted = weeklyData.sumOf { it.completedCount }
    val maxDayCount = (weeklyData.maxOfOrNull { it.completedCount } ?: 0).coerceAtLeast(1)
    val bestDay = weeklyData.maxByOrNull { it.completedCount }?.takeIf { it.completedCount > 0 }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_progress_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Progress",
                            tint = CyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "WEEKLY TASK PROGRESS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = Primary500
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalWeekCompleted Tasks Completed",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = GreenSuccess.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GreenSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = GreenSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bar Chart Columns (7 Days)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEachIndexed { index, day ->
                    val isSelected = selectedDayIndex == index
                    val targetFraction = (day.completedCount.toFloat() / maxDayCount.toFloat()).coerceIn(0.08f, 1f)
                    val animatedHeightFraction by animateFloatAsState(
                        targetValue = if (day.completedCount == 0) 0.08f else targetFraction,
                        animationSpec = tween(durationMillis = 600, delayMillis = index * 60, easing = FastOutSlowInEasing),
                        label = "bar_height_$index"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp)
                            .clickable {
                                selectedDayIndex = if (selectedDayIndex == index) null else index
                            }
                    ) {
                        // Count Badge on top of bar
                        Text(
                            text = if (day.completedCount > 0) "${day.completedCount}" else "-",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (day.isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ),
                            color = when {
                                isSelected -> CyanAccent
                                day.isToday -> Primary500
                                day.completedCount > 0 -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Column Bar
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(105.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.surface
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                ),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedHeightFraction)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (day.completedCount == 0) {
                                            Brush.verticalGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f)
                                                )
                                            )
                                        } else if (isSelected || day.isToday) {
                                            Brush.verticalGradient(
                                                listOf(
                                                    CyanAccent,
                                                    Primary600
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Primary500,
                                                    Primary600.copy(alpha = 0.8f)
                                                )
                                            )
                                        }
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Day Name Label
                        Text(
                            text = day.dayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (day.isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            ),
                            color = when {
                                isSelected -> CyanAccent
                                day.isToday -> Primary500
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.Center
                        )

                        // Today Indicator Dot
                        if (day.isToday) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Primary500)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selected Day Breakdown or Weekly Summary
            val currentSelected = selectedDayIndex?.let { weeklyData.getOrNull(it) }
            if (currentSelected != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${currentSelected.dayName} (${currentSelected.dateLabel})",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentSelected.completedCount} completed",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (currentSelected.completedCount > 0) GreenSuccess else Color.Gray
                            )
                        }

                        if (currentSelected.completedTasks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            currentSelected.completedTasks.take(3).forEach { task ->
                                Text(
                                    text = "• ${task.title} (${task.subject})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                // Summary Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Daily Average",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val avg = String.format(Locale.getDefault(), "%.1f", totalWeekCompleted / 7.0)
                        Text(
                            text = "$avg tasks/day",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Peak Day",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = bestDay?.let { "${it.dayName} (${it.completedCount})" } ?: "None yet",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                    }
                }
            }
        }
    }
}

/**
 * Computes task completion counts for each of the past 7 days.
 */
private fun computeWeeklyTaskProgress(tasks: List<TaskEntity>): List<DayTaskSummary> {
    val calendar = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    // Normalize to end of today
    calendar.set(Calendar.HOUR_OF_DAY, 23)
    calendar.set(Calendar.MINUTE, 59)
    calendar.set(Calendar.SECOND, 59)
    calendar.set(Calendar.MILLISECOND, 999)

    val todayCalendar = Calendar.getInstance()
    val todayYear = todayCalendar.get(Calendar.YEAR)
    val todayDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR)

    val daysList = mutableListOf<DayTaskSummary>()

    // Generate 7 days ending today (6 days ago -> today)
    for (i in 6 downTo 0) {
        val dayCal = Calendar.getInstance()
        dayCal.add(Calendar.DAY_OF_YEAR, -i)

        val dayYear = dayCal.get(Calendar.YEAR)
        val dayOfYear = dayCal.get(Calendar.DAY_OF_YEAR)
        val isToday = (dayYear == todayYear && dayOfYear == todayDayOfYear)

        // Day boundary timestamps
        dayCal.set(Calendar.HOUR_OF_DAY, 0)
        dayCal.set(Calendar.MINUTE, 0)
        dayCal.set(Calendar.SECOND, 0)
        dayCal.set(Calendar.MILLISECOND, 0)
        val startOfDay = dayCal.timeInMillis

        dayCal.set(Calendar.HOUR_OF_DAY, 23)
        dayCal.set(Calendar.MINUTE, 59)
        dayCal.set(Calendar.SECOND, 59)
        dayCal.set(Calendar.MILLISECOND, 999)
        val endOfDay = dayCal.timeInMillis

        // Tasks completed on this specific day
        val completedOnDay = tasks.filter { task ->
            if (!task.isCompleted) return@filter false
            val completionTime = task.completedAt ?: task.dueDate
            completionTime in startOfDay..endOfDay
        }

        // Total tasks due/scheduled on this day
        val totalOnDay = tasks.count { task ->
            task.dueDate in startOfDay..endOfDay
        }

        daysList.add(
            DayTaskSummary(
                dayName = dayFormat.format(dayCal.time),
                dateLabel = dateFormat.format(dayCal.time),
                isToday = isToday,
                completedCount = completedOnDay.size,
                totalCount = totalOnDay,
                dateTimestamp = startOfDay,
                completedTasks = completedOnDay
            )
        )
    }

    return daysList
}
