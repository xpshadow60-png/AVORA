package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focus.FocusTimerState
import com.example.focus.audio.AmbientSoundType
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500

@Composable
fun FocusAlertScreen(
    timerState: FocusTimerState,
    onStartFocus: (Int, String) -> Unit,
    onPauseFocus: () -> Unit,
    onResumeFocus: () -> Unit,
    onStopFocus: () -> Unit,
    onSetAmbientSound: (String) -> Unit,
    onToggleAmbientSound: (String) -> Unit = onSetAmbientSound,
    onSetAmbientVolume: (Float) -> Unit = {}
) {
    var selectedDurationMinutes by remember { mutableStateOf(25) }
    var selectedSubject by remember { mutableStateOf("Mathematics Focus") }

    val presetDurations = listOf(
        Pair(15, "Quick Sprint"),
        Pair(25, "Pomodoro"),
        Pair(45, "Deep Study"),
        Pair(60, "Intensive Block"),
        Pair(90, "Mastery Session")
    )

    val quickSubjects = listOf(
        "Mathematics", "Physics", "Computer Science", "Biology", "Literature", "Chemistry", "History", "General Study"
    )

    val progressFraction = if (timerState.totalSeconds > 0) {
        (timerState.remainingSeconds.toFloat() / timerState.totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progressFraction,
        animationSpec = tween(durationMillis = 500),
        label = "timer_progress"
    )

    val activeSoundEnum = AmbientSoundType.fromId(timerState.activeAmbientSound)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp, top = 12.dp)
    ) {
        // Study Timer & Status Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (timerState.isActive) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth().testTag("focus_timer_main_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Active Status Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (timerState.isActive) {
                            if (timerState.isPaused) Color(0xFFFAB1A0).copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)
                        } else {
                            Primary500.copy(alpha = 0.15f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (timerState.isActive) Icons.Default.Timer else Icons.Default.SelfImprovement,
                                contentDescription = "Focus",
                                tint = if (timerState.isActive) {
                                    if (timerState.isPaused) Color(0xFFFAB1A0) else CyanAccent
                                } else Primary500,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = when {
                                    !timerState.isActive -> "READY TO FOCUS"
                                    timerState.isPaused -> "PAUSED"
                                    else -> "DEEP WORK IN PROGRESS"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (timerState.isActive) {
                                    if (timerState.isPaused) Color(0xFFFAB1A0) else CyanAccent
                                } else Primary500
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Circular Countdown Timer Gauge
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(200.dp)
                    ) {
                        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 12.dp.toPx()
                            val arcSize = size.width - strokeWidth

                            // Background Track Arc
                            drawArc(
                                color = trackColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                size = Size(arcSize, arcSize)
                            )

                            // Animated Progress Arc
                            val sweep = if (timerState.isActive) animatedProgress * 360f else 360f
                            drawArc(
                                brush = Brush.sweepGradient(
                                    listOf(Primary500, CyanAccent, Primary500)
                                ),
                                startAngle = -90f,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                size = Size(arcSize, arcSize)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (timerState.isActive) {
                                    formatTime(timerState.remainingSeconds)
                                } else {
                                    formatTime(selectedDurationMinutes * 60)
                                },
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 38.sp
                                ),
                                color = if (timerState.isActive) CyanAccent else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (timerState.isActive) timerState.currentSubject else "Focus Session",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Active Soundscape Indicator in Gauge
                            if (activeSoundEnum != AmbientSoundType.OFF) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (timerState.isAmbientSoundPlaying) {
                                        SoundWaveVisualizer(color = CyanAccent, modifier = Modifier.height(10.dp))
                                    }
                                    Text(
                                        text = activeSoundEnum.displayName,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = if (timerState.isAmbientSoundPlaying) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!timerState.isActive) {
                        // Duration Preset Selection
                        Text(
                            text = "Choose Session Duration",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(presetDurations) { (mins, label) ->
                                FilterChip(
                                    selected = selectedDurationMinutes == mins,
                                    onClick = { selectedDurationMinutes = mins },
                                    label = { Text("${mins}m • $label") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary500,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Subject Selection Chips
                        Text(
                            text = "Target Subject",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(quickSubjects) { subject ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedSubject.startsWith(subject)) Primary500 else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { selectedSubject = "$subject Focus" }
                                ) {
                                    Text(
                                        text = subject,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (selectedSubject.startsWith(subject)) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = { selectedSubject = it },
                            label = { Text("Custom Study Topic / Goal") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { onStartFocus(selectedDurationMinutes, selectedSubject) },
                            colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("start_focus_session_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Begin Focus Session (${selectedDurationMinutes} mins)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        // Timer Control Buttons (Pause / Resume / End)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (timerState.isPaused) {
                                Button(
                                    onClick = onResumeFocus,
                                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Resume", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = onPauseFocus,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFAB1A0), contentColor = Color.Black),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pause", fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = onStopFocus,
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPriority, contentColor = Color.White),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Stop, contentDescription = "End")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("End Session", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Ambient Soundscapes Media Player Card
        item {
            AmbientSoundscapePlayerCard(
                timerState = timerState,
                onSetAmbientSound = onSetAmbientSound,
                onToggleAmbientSound = onToggleAmbientSound,
                onSetAmbientVolume = onSetAmbientVolume
            )
        }

        // Science of Deep Work Guidelines Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "💡 Best Practices for High Retention",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("1.", fontWeight = FontWeight.Bold, color = CyanAccent)
                        Text(
                            "Single-Task Principle: Work exclusively on one subject per timer cycle without switching contexts.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("2.", fontWeight = FontWeight.Bold, color = CyanAccent)
                        Text(
                            "Active Recall: Write summaries or solve problems rather than passively re-reading notes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("3.", fontWeight = FontWeight.Bold, color = CyanAccent)
                        Text(
                            "Deliberate Breaks: Take 5-minute movement or water breaks between completed cycles.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AmbientSoundscapePlayerCard(
    timerState: FocusTimerState,
    onSetAmbientSound: (String) -> Unit,
    onToggleAmbientSound: (String) -> Unit,
    onSetAmbientVolume: (Float) -> Unit
) {
    val activeSoundEnum = AmbientSoundType.fromId(timerState.activeAmbientSound)
    val isPlaying = timerState.isAmbientSoundPlaying

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().testTag("ambient_soundscapes_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header with Playback Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isPlaying) CyanAccent.copy(alpha = 0.2f) else Primary500.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.Headphones,
                                contentDescription = null,
                                tint = if (isPlaying) CyanAccent else Primary500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Ambient Soundscapes",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (isPlaying) "Playing: ${activeSoundEnum.displayName}" else "Toggle background audio for focus",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isPlaying) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Mini Play / Pause toggle chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isPlaying) CyanAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clickable {
                            if (activeSoundEnum == AmbientSoundType.OFF) {
                                onSetAmbientSound(AmbientSoundType.RAIN.id)
                            } else {
                                onToggleAmbientSound(activeSoundEnum.id)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isPlaying) {
                            SoundWaveVisualizer(color = CyanAccent, modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = if (isPlaying) "PAUSE" else "PLAY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isPlaying) CyanAccent else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Soundscape Selection List (Horizontal Chips & Cards)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AmbientSoundType.entries) { sound ->
                    val isSelected = timerState.activeAmbientSound.equals(sound.id, ignoreCase = true)
                    val soundActive = isSelected && isPlaying

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = when {
                            soundActive -> Primary500
                            isSelected -> MaterialTheme.colorScheme.surface
                            else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        },
                        border = if (isSelected) {
                            BorderStroke(1.5.dp, if (soundActive) CyanAccent else Primary500)
                        } else null,
                        modifier = Modifier
                            .clickable {
                                onSetAmbientSound(sound.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Sound Icon
                            Icon(
                                imageVector = when (sound) {
                                    AmbientSoundType.RAIN -> Icons.Default.WaterDrop
                                    AmbientSoundType.WHITE_NOISE -> Icons.Default.Air
                                    AmbientSoundType.OCEAN_WAVES -> Icons.Default.Waves
                                    AmbientSoundType.CAMPFIRE -> Icons.Default.LocalFireDepartment
                                    AmbientSoundType.BINAURAL_ALPHA -> Icons.Default.GraphicEq
                                    AmbientSoundType.FOREST_BREEZE -> Icons.Default.Park
                                    AmbientSoundType.COFFEE_SHOP -> Icons.Default.Coffee
                                    AmbientSoundType.OFF -> Icons.Default.VolumeOff
                                },
                                contentDescription = sound.displayName,
                                tint = when {
                                    soundActive -> CyanAccent
                                    isSelected -> Primary500
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(18.dp)
                            )

                            Column {
                                Text(
                                    text = sound.displayName,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (soundActive) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = sound.description,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = if (soundActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Volume Slider Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = when {
                        timerState.ambientVolume <= 0.05f -> Icons.Default.VolumeMute
                        timerState.ambientVolume < 0.5f -> Icons.Default.VolumeDown
                        else -> Icons.Default.VolumeUp
                    },
                    contentDescription = "Volume",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Slider(
                    value = timerState.ambientVolume,
                    onValueChange = onSetAmbientVolume,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                    )
                )

                Text(
                    text = "${(timerState.ambientVolume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(36.dp)
                )
            }
        }
    }
}

@Composable
fun SoundWaveVisualizer(color: Color, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "soundwaves")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.1f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h1)
                .background(color, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h2)
                .background(color, RoundedCornerShape(1.dp))
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .fillMaxHeight(h3)
                .background(color, RoundedCornerShape(1.dp))
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
