package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudySessionEntity
import com.example.ui.model.SubjectCategoryManager
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary400
import com.example.ui.theme.Primary500
import com.example.ui.theme.Primary600
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

data class DayStudyData(
    val dayName: String,         // "Mon", "Tue"
    val dateLabel: String,       // "Aug 18"
    val isToday: Boolean,
    val totalMinutes: Int,
    val hours: Float,            // e.g. 3.5f
    val sessions: List<StudySessionEntity>,
    val averageFocusScore: Int,
    val distractionsBlocked: Int,
    val subjectBreakdown: Map<String, Int> // Subject -> minutes
)

enum class ChartDisplayMode {
    BAR_TREND,      // D3 Bar + Spline Trendline
    SUBJECT_STACK   // Recharts Stacked Subject distribution
}

@Composable
fun WeeklyStudyHoursSummaryChart(
    sessions: List<StudySessionEntity>,
    dailyGoalHours: Float = 4.0f,
    modifier: Modifier = Modifier
) {
    val weeklyData = remember(sessions) {
        computeWeeklyStudyData(sessions)
    }

    var selectedDayIndex by remember { mutableStateOf<Int?>(null) }
    var displayMode by remember { mutableStateOf(ChartDisplayMode.BAR_TREND) }

    val totalWeekMinutes = weeklyData.sumOf { it.totalMinutes }
    val totalWeekHours = totalWeekMinutes / 60f
    val dailyAverageHours = totalWeekHours / 7f
    val peakDay = weeklyData.maxByOrNull { it.totalMinutes }?.takeIf { it.totalMinutes > 0 }
    val daysMeetingGoal = weeklyData.count { it.hours >= dailyGoalHours }

    // Dynamic Max Y Scale (at least goal + 1h or peak + 1h)
    val maxRecordedHours = weeklyData.maxOfOrNull { it.hours } ?: 0f
    val yAxisMaxHours = max(dailyGoalHours + 1.5f, max(maxRecordedHours + 1f, 6.0f))

    // Previous week comparison trend calculation (first 3 days vs last 4 days or sample baseline)
    val earlierHalf = weeklyData.take(3).sumOf { it.totalMinutes }
    val laterHalf = weeklyData.takeLast(4).sumOf { it.totalMinutes }
    val isTrendingUp = laterHalf >= earlierHalf

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_study_summary_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Icon, Title, and Trend Badge
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
                        color = Primary500.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Study Chart",
                                tint = Primary500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "WEEKLY PRODUCTIVITY TRENDS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = Primary500
                        )
                        Text(
                            text = "Study Hours Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Trend Pill Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isTrendingUp) GreenSuccess.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isTrendingUp) Icons.Default.TrendingUp else Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = if (isTrendingUp) GreenSuccess else CyanAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isTrendingUp) "Trending Up ↗" else "Steady Pace 🎯",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isTrendingUp) GreenSuccess else CyanAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Metric Row + Quick Mode Toggle
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
                            text = String.format(Locale.getDefault(), "%.1f", totalWeekHours),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "hrs this week",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        text = "Avg ${String.format(Locale.getDefault(), "%.1f", dailyAverageHours)}h / day • $daysMeetingGoal/7 days met target",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chart mode chips (Bar + Spline vs Stacked Subjects)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (displayMode == ChartDisplayMode.BAR_TREND) Primary500 else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { displayMode = ChartDisplayMode.BAR_TREND }
                    ) {
                        Text(
                            text = "Trend",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (displayMode == ChartDisplayMode.BAR_TREND) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (displayMode == ChartDisplayMode.SUBJECT_STACK) Primary500 else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.clickable { displayMode = ChartDisplayMode.SUBJECT_STACK }
                    ) {
                        Text(
                            text = "Subjects",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (displayMode == ChartDisplayMode.SUBJECT_STACK) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main D3/Recharts Visualization Canvas & Columns
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                // Background Grid Lines & Daily Target Guideline
                val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                val targetGuidelineColor = CoralPriority.copy(alpha = 0.7f)
                val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.DarkBg

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 32.dp, top = 16.dp, start = 8.dp, end = 8.dp)
                ) {
                    val chartHeight = size.height
                    val chartWidth = size.width

                    // 4 Horizontal Grid Lines (0%, 33%, 66%, 100% of height)
                    val gridSteps = 4
                    for (i in 0..gridSteps) {
                        val y = chartHeight * (i.toFloat() / gridSteps)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(chartWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    // Daily Goal Target Guideline (Dashed Line)
                    val goalFraction = (dailyGoalHours / yAxisMaxHours).coerceIn(0f, 1f)
                    val targetY = chartHeight * (1f - goalFraction)

                    drawLine(
                        color = targetGuidelineColor,
                        start = Offset(0f, targetY),
                        end = Offset(chartWidth, targetY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                    )
                }

                // Target Benchmark Label Tag
                val goalFraction = (dailyGoalHours / yAxisMaxHours).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = (160.dp * (1f - goalFraction)).coerceAtLeast(0.dp))
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CoralPriority.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "Goal: ${dailyGoalHours}h",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = CoralPriority,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // D3 Smooth Spline Trendline (Rendered when in BAR_TREND mode)
                if (displayMode == ChartDisplayMode.BAR_TREND) {
                    val splineColor = CyanAccent
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 32.dp, top = 16.dp, start = 20.dp, end = 20.dp)
                    ) {
                        if (weeklyData.isNotEmpty()) {
                            val count = weeklyData.size
                            val stepX = size.width / (count - 1).coerceAtLeast(1)
                            val points = weeklyData.mapIndexed { idx, day ->
                                val x = idx * stepX
                                val fraction = (day.hours / yAxisMaxHours).coerceIn(0f, 1f)
                                val y = size.height * (1f - fraction)
                                Offset(x, y)
                            }

                            val path = Path()
                            val fillPath = Path()

                            if (points.isNotEmpty()) {
                                path.moveTo(points.first().x, points.first().y)
                                fillPath.moveTo(points.first().x, size.height)
                                fillPath.lineTo(points.first().x, points.first().y)

                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val controlX1 = p0.x + (p1.x - p0.x) / 2f
                                    val controlY1 = p0.y
                                    val controlX2 = p0.x + (p1.x - p0.x) / 2f
                                    val controlY2 = p1.y

                                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                                }

                                fillPath.lineTo(points.last().x, size.height)
                                fillPath.close()

                                // Soft area gradient underneath spline
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            CyanAccent.copy(alpha = 0.25f),
                                            CyanAccent.copy(alpha = 0.02f)
                                        )
                                    )
                                )

                                // Main Trendline Stroke
                                drawPath(
                                    path = path,
                                    color = splineColor,
                                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                // Point Node dots
                                points.forEachIndexed { idx, pt ->
                                    val isSelected = selectedDayIndex == idx
                                    drawCircle(
                                        color = if (isSelected) Color.White else CyanAccent,
                                        radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                        center = pt
                                    )
                                    if (isSelected) {
                                        drawCircle(
                                            color = Primary500,
                                            radius = 7.dp.toPx(),
                                            center = pt,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive 7-Day Bar Columns
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEachIndexed { index, day ->
                        val isSelected = selectedDayIndex == index
                        val fraction = (day.hours / yAxisMaxHours).coerceIn(0.04f, 1f)
                        val animatedFraction by animateFloatAsState(
                            targetValue = fraction,
                            animationSpec = tween(durationMillis = 650, delayMillis = index * 50, easing = FastOutSlowInEasing),
                            label = "chart_bar_height_$index"
                        )

                        val goalAchievedToday = day.hours >= dailyGoalHours

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    selectedDayIndex = if (selectedDayIndex == index) null else index
                                }
                        ) {
                            // Top Hours Value Label
                            Box(
                                modifier = Modifier
                                    .height(26.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.hours > 0f) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            isSelected -> CyanAccent
                                            goalAchievedToday -> GreenSuccess.copy(alpha = 0.2f)
                                            day.isToday -> Primary500.copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        }
                                    ) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.1fh", day.hours),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (day.isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 10.sp
                                            ),
                                            color = when {
                                                isSelected -> Color.Black
                                                goalAchievedToday -> GreenSuccess
                                                day.isToday -> Primary500
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            // Column Bar Container
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .width(26.dp)
                                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)
                                    ),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (displayMode == ChartDisplayMode.SUBJECT_STACK && day.subjectBreakdown.isNotEmpty()) {
                                    // Stacked Subject Distribution
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animatedFraction)
                                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                    ) {
                                        day.subjectBreakdown.entries.forEach { entry ->
                                            val subjectInfo = SubjectCategoryManager.getSubjectInfo(entry.key)
                                            val segFraction = (entry.value.toFloat() / day.totalMinutes.toFloat()).coerceIn(0f, 1f)
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(segFraction)
                                                    .background(subjectInfo.color)
                                            )
                                        }
                                    }
                                } else {
                                    // Gradient Cylinder Bar
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight(animatedFraction)
                                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                            .background(
                                                if (day.hours == 0f) {
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                                                        )
                                                    )
                                                } else if (goalAchievedToday) {
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            GreenSuccess,
                                                            CyanAccent
                                                        )
                                                    )
                                                } else if (isSelected || day.isToday) {
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            CyanAccent,
                                                            Primary500
                                                        )
                                                    )
                                                } else {
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Primary400,
                                                            Primary600
                                                        )
                                                    )
                                                }
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Day of week label (e.g. "Mon")
                            Text(
                                text = day.dayName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (day.isToday || isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 11.sp
                                ),
                                color = when {
                                    isSelected -> CyanAccent
                                    day.isToday -> Primary500
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                textAlign = TextAlign.Center
                            )

                            // Today Indicator Pill Dot
                            if (day.isToday) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(Primary500)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(7.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // D3-style Interactive Tooltip / Breakdown Card
            AnimatedVisibility(
                visible = selectedDayIndex != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val selectedDay = selectedDayIndex?.let { weeklyData.getOrNull(it) }
                if (selectedDay != null) {
                    val goalPercent = ((selectedDay.hours / dailyGoalHours) * 100).toInt()
                    val isGoalMet = selectedDay.hours >= dailyGoalHours

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("selected_day_study_tooltip")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${selectedDay.dayName}, ${selectedDay.dateLabel}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (selectedDay.isToday) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Primary500.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Today",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Primary500,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isGoalMet) GreenSuccess.copy(alpha = 0.15f) else CyanAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = if (isGoalMet) "🎯 Target Met ($goalPercent%)" else "$goalPercent% of Goal",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isGoalMet) GreenSuccess else CyanAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Stats breakdown chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Studied", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    val hours = selectedDay.totalMinutes / 60
                                    val mins = selectedDay.totalMinutes % 60
                                    Text(
                                        text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column {
                                    Text("Avg Focus", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = if (selectedDay.averageFocusScore > 0) "${selectedDay.averageFocusScore}%" else "-",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CyanAccent
                                    )
                                }

                                Column {
                                    Text("Sessions", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    Text(
                                        text = "${selectedDay.sessions.size} completed",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Primary500
                                    )
                                }
                            }

                            // Subject distribution chips
                            if (selectedDay.subjectBreakdown.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    selectedDay.subjectBreakdown.entries.forEach { (sub, minutes) ->
                                        val info = SubjectCategoryManager.getSubjectInfo(sub)
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = info.color.copy(alpha = 0.15f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "${info.emoji} $sub: ${minutes / 60}h ${minutes % 60}m",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = info.color
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

            if (selectedDayIndex == null) {
                // Bottom Summary Bar: Peak Day & Insights
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = peakDay?.let { "Peak: ${it.dayName} (${String.format(Locale.getDefault(), "%.1f", it.hours)}h)" } ?: "No sessions yet",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Tap any day to inspect breakdown",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Computes study duration aggregation for the last 7 consecutive days.
 */
private fun computeWeeklyStudyData(sessions: List<StudySessionEntity>): List<DayStudyData> {
    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    val todayCalendar = Calendar.getInstance()
    val todayYear = todayCalendar.get(Calendar.YEAR)
    val todayDayOfYear = todayCalendar.get(Calendar.DAY_OF_YEAR)

    val result = mutableListOf<DayStudyData>()

    // Generate 7 days ending today (6 days ago -> today)
    for (i in 6 downTo 0) {
        val dayCal = Calendar.getInstance()
        dayCal.add(Calendar.DAY_OF_YEAR, -i)

        val dayYear = dayCal.get(Calendar.YEAR)
        val dayOfYear = dayCal.get(Calendar.DAY_OF_YEAR)
        val isToday = (dayYear == todayYear && dayOfYear == todayDayOfYear)

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

        val daySessions = sessions.filter { it.completedAt in startOfDay..endOfDay }
        val totalMinutes = daySessions.sumOf { it.durationMinutes }
        val hours = totalMinutes / 60f
        val avgFocus = if (daySessions.isNotEmpty()) daySessions.map { it.focusScore }.average().toInt() else 0
        val totalDistractions = daySessions.sumOf { it.distractionsBlockedCount }

        val subjectMap = mutableMapOf<String, Int>()
        daySessions.forEach { session ->
            subjectMap[session.subject] = (subjectMap[session.subject] ?: 0) + session.durationMinutes
        }

        result.add(
            DayStudyData(
                dayName = dayFormat.format(dayCal.time),
                dateLabel = dateFormat.format(dayCal.time),
                isToday = isToday,
                totalMinutes = totalMinutes,
                hours = hours,
                sessions = daySessions,
                averageFocusScore = avgFocus,
                distractionsBlocked = totalDistractions,
                subjectBreakdown = subjectMap
            )
        )
    }

    return result
}
