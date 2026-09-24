package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ThemeMode
import kotlin.math.*

/**
 * High-performance, multi-layered Tech & AI Atmosphere Background for Avora.
 * 
 * Provides:
 * - Deep navy obsidian canvas with radial ambient glows
 * - Subtle digital precision coordinate grid & circuit traces
 * - Faint neural network nodes & synaptic link connections
 * - Floating data points and minimalist code/logic symbols
 * - Ultra-low opacity elements to maximize content contrast and legibility
 * - Smooth, lightweight motion with reduced-motion static fallback
 */
@Composable
fun TechAtmosphereBackground(
    modifier: Modifier = Modifier,
    isDark: Boolean = true,
    enableAnimation: Boolean = true,
    content: @Composable () -> Unit
) {
    val isPreview = LocalInspectionMode.current
    val shouldAnimate = enableAnimation && !isPreview

    // Infinite transition for serene, non-distracting continuous tech atmosphere motion
    val infiniteTransition = rememberInfiniteTransition(label = "tech_atmosphere_motion")

    val driftPhase by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 24000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "drift_phase"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val pulsePhase by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_phase"
        )
    } else {
        remember { mutableFloatStateOf(0.5f) }
    }

    // Pre-configured neural nodes coordinates (normalized 0..1)
    val neuralNodes = remember {
        listOf(
            AtmosphereNode(0.12f, 0.18f, 0.04f, 0.03f, 3.2f, Color(0xFF00CEC9)),
            AtmosphereNode(0.28f, 0.12f, -0.03f, 0.04f, 2.5f, Color(0xFF6C5CE7)),
            AtmosphereNode(0.82f, 0.22f, 0.03f, -0.03f, 3.0f, Color(0xFF00CEC9)),
            AtmosphereNode(0.68f, 0.35f, -0.04f, 0.02f, 2.8f, Color(0xFF00B894)),
            AtmosphereNode(0.20f, 0.45f, 0.03f, 0.04f, 3.5f, Color(0xFF6C5CE7)),
            AtmosphereNode(0.42f, 0.58f, -0.02f, -0.03f, 2.4f, Color(0xFF00CEC9)),
            AtmosphereNode(0.85f, 0.52f, 0.04f, 0.03f, 3.0f, Color(0xFF6C5CE7)),
            AtmosphereNode(0.15f, 0.78f, -0.03f, 0.04f, 3.2f, Color(0xFF00B894)),
            AtmosphereNode(0.72f, 0.82f, 0.04f, -0.03f, 2.6f, Color(0xFF00CEC9)),
            AtmosphereNode(0.45f, 0.88f, -0.04f, 0.02f, 3.0f, Color(0xFF6C5CE7))
        )
    }

    // Pre-configured circuit path segments (normalized coordinates)
    val circuitSegments = remember {
        listOf(
            CircuitSegment(0.05f, 0.28f, 0.25f, 0.28f, 0.32f, 0.35f),
            CircuitSegment(0.70f, 0.15f, 0.88f, 0.15f, 0.94f, 0.21f),
            CircuitSegment(0.10f, 0.65f, 0.18f, 0.65f, 0.24f, 0.71f),
            CircuitSegment(0.75f, 0.68f, 0.85f, 0.68f, 0.90f, 0.73f)
        )
    }

    // Pre-configured floating data points
    val dataPoints = remember {
        listOf(
            DataParticle(0.08f, 0.40f, 1.8f, 0.4f),
            DataParticle(0.35f, 0.25f, 1.5f, 0.6f),
            DataParticle(0.55f, 0.18f, 2.0f, 0.8f),
            DataParticle(0.92f, 0.38f, 1.6f, 0.5f),
            DataParticle(0.28f, 0.72f, 2.2f, 0.7f),
            DataParticle(0.62f, 0.65f, 1.7f, 0.4f),
            DataParticle(0.80f, 0.92f, 2.0f, 0.6f),
            DataParticle(0.48f, 0.42f, 1.4f, 0.9f)
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw Deep Tech Base Canvas
            drawTechBackgroundBase(isDark, width, height, pulsePhase)

            // 2. Draw Digital Precision Matrix Grid
            drawDigitalMatrixGrid(isDark, width, height)

            // 3. Draw Fine Circuit Geometry Traces
            drawCircuitTraces(circuitSegments, isDark, width, height, pulsePhase)

            // 4. Draw Floating Neural Network Nodes & Synaptic Connections
            drawNeuralNetwork(neuralNodes, isDark, width, height, driftPhase, pulsePhase)

            // 5. Draw Drifting Data Particles & Subtle Tech Glyphs
            drawDataParticles(dataPoints, isDark, width, height, driftPhase)

            // 6. Draw Subtle Chip / Module Framing Geometry
            drawChipFraming(isDark, width, height)
        }

        // Foreground content with transparent layered depth
        content()
    }
}

private fun DrawScope.drawTechBackgroundBase(
    isDark: Boolean,
    width: Float,
    height: Float,
    pulsePhase: Float
) {
    if (isDark) {
        // Deep obsidian navy canvas
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF070A13),
                    Color(0xFF0B1020),
                    Color(0xFF060911)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Ambient top-right Cyan/Indigo Tech Glow Pool
        val glowPulse = 0.08f + (pulsePhase * 0.04f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00CEC9).copy(alpha = glowPulse),
                    Color(0xFF6C5CE7).copy(alpha = glowPulse * 0.5f),
                    Color.Transparent
                ),
                center = Offset(width * 0.88f, height * 0.12f),
                radius = width * 0.75f
            ),
            center = Offset(width * 0.88f, height * 0.12f),
            radius = width * 0.75f
        )

        // Ambient bottom-left Electric Blue Glow Pool
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF4F46E5).copy(alpha = glowPulse * 0.6f),
                    Color(0xFF0984E3).copy(alpha = glowPulse * 0.3f),
                    Color.Transparent
                ),
                center = Offset(width * 0.15f, height * 0.85f),
                radius = width * 0.85f
            ),
            center = Offset(width * 0.15f, height * 0.85f),
            radius = width * 0.85f
        )
    } else {
        // Light tech canvas: Crisp high-tech slate/ice white
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF6F8FD),
                    Color(0xFFEDF2FA),
                    Color(0xFFF9FAFD)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Soft pastel blue ambient glow for light mode
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF6C5CE7).copy(alpha = 0.04f),
                    Color(0xFF00CEC9).copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = Offset(width * 0.85f, height * 0.15f),
                radius = width * 0.7f
            ),
            center = Offset(width * 0.85f, height * 0.15f),
            radius = width * 0.7f
        )
    }
}

private fun DrawScope.drawDigitalMatrixGrid(
    isDark: Boolean,
    width: Float,
    height: Float
) {
    val dotColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.055f) else Color(0xFF6366F1).copy(alpha = 0.04f)
    val crossColor = if (isDark) Color(0xFF00CEC9).copy(alpha = 0.07f) else Color(0xFF3B82F6).copy(alpha = 0.05f)

    val step = 44.dp.toPx()
    val crossStep = step * 4 // Every 4 grid units, draw a subtle tech crosshair

    var y = step * 0.5f
    while (y < height) {
        var x = step * 0.5f
        while (x < width) {
            // Check if this is a major intersection (draw crosshair)
            val isMajorX = (x % crossStep) < step
            val isMajorY = (y % crossStep) < step

            if (isMajorX && isMajorY) {
                val crossArm = 3.5.dp.toPx()
                drawLine(
                    color = crossColor,
                    start = Offset(x - crossArm, y),
                    end = Offset(x + crossArm, y),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = crossColor,
                    start = Offset(x, y - crossArm),
                    end = Offset(x, y + crossArm),
                    strokeWidth = 1.dp.toPx()
                )
            } else {
                // Subtle matrix dot
                drawCircle(
                    color = dotColor,
                    radius = 1.1.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            x += step
        }
        y += step
    }
}

private fun DrawScope.drawCircuitTraces(
    segments: List<CircuitSegment>,
    isDark: Boolean,
    width: Float,
    height: Float,
    pulsePhase: Float
) {
    val traceColor = if (isDark) Color(0xFF00CEC9).copy(alpha = 0.07f) else Color(0xFF0284C7).copy(alpha = 0.05f)
    val padColor = if (isDark) Color(0xFF6C5CE7).copy(alpha = 0.09f) else Color(0xFF6366F1).copy(alpha = 0.06f)
    val strokeWidth = 1.2.dp.toPx()

    segments.forEach { seg ->
        val x1 = seg.normX1 * width
        val y1 = seg.normY1 * height
        val x2 = seg.normX2 * width
        val y2 = seg.normY2 * height
        val x3 = seg.normX3 * width
        val y3 = seg.normY3 * height

        // First segment (horizontal or vertical)
        drawLine(
            color = traceColor,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = strokeWidth
        )

        // 45-degree angle segment
        drawLine(
            color = traceColor,
            start = Offset(x2, y2),
            end = Offset(x3, y3),
            strokeWidth = strokeWidth
        )

        // Circuit Via / Pad at endpoints
        drawCircle(
            color = padColor,
            radius = 2.5.dp.toPx(),
            center = Offset(x1, y1),
            style = Stroke(width = 1.dp.toPx())
        )

        drawCircle(
            color = traceColor,
            radius = 2.0.dp.toPx(),
            center = Offset(x3, y3)
        )
    }
}

private fun DrawScope.drawNeuralNetwork(
    nodes: List<AtmosphereNode>,
    isDark: Boolean,
    width: Float,
    height: Float,
    driftPhase: Float,
    pulsePhase: Float
) {
    val calculatedPositions = nodes.mapIndexed { index, node ->
        // Drift offsets using trigonometric oscillations
        val localOffset = (index * 0.7f)
        val dx = sin(driftPhase + localOffset) * node.orbitRadiusX * width
        val dy = cos(driftPhase * 0.8f + localOffset) * node.orbitRadiusY * height

        Offset(
            x = (node.normX * width + dx).coerceIn(0f, width),
            y = (node.normY * height + dy).coerceIn(0f, height)
        )
    }

    val maxConnectDistSq = (width * 0.38f) * (width * 0.38f)
    val baseLineColor = if (isDark) Color(0xFF00CEC9) else Color(0xFF4F46E5)

    // Draw neural synaptic connection lines between nearby nodes
    for (i in calculatedPositions.indices) {
        for (j in (i + 1) until calculatedPositions.size) {
            val p1 = calculatedPositions[i]
            val p2 = calculatedPositions[j]

            val distSq = (p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y)
            if (distSq < maxConnectDistSq) {
                val proximity = 1f - (distSq / maxConnectDistSq)
                val lineAlpha = (proximity * 0.09f * (if (isDark) 1f else 0.6f)).coerceIn(0f, 0.12f)

                drawLine(
                    color = baseLineColor.copy(alpha = lineAlpha),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.dp.toPx()
                )

                // Occasional moving data packet across active connection
                if ((i + j) % 3 == 0) {
                    val packetProgress = ((pulsePhase + (i * 0.2f)) % 1f)
                    val packetX = p1.x + (p2.x - p1.x) * packetProgress
                    val packetY = p1.y + (p2.y - p1.y) * packetProgress

                    drawCircle(
                        color = Color(0xFF00F5D4).copy(alpha = lineAlpha * 1.5f),
                        radius = 1.5.dp.toPx(),
                        center = Offset(packetX, packetY)
                    )
                }
            }
        }
    }

    // Draw Neural Node Points
    calculatedPositions.forEachIndexed { idx, pos ->
        val nodeDef = nodes[idx]
        val nodeColor = if (isDark) nodeDef.color else Color(0xFF4F46E5)
        val alpha = if (isDark) 0.15f else 0.10f

        // Outer faint halo
        drawCircle(
            color = nodeColor.copy(alpha = alpha * 0.4f),
            radius = nodeDef.baseRadius.dp.toPx() * 1.8f,
            center = pos
        )

        // Core node dot
        drawCircle(
            color = nodeColor.copy(alpha = alpha),
            radius = nodeDef.baseRadius.dp.toPx(),
            center = pos
        )
    }
}

private fun DrawScope.drawDataParticles(
    particles: List<DataParticle>,
    isDark: Boolean,
    width: Float,
    height: Float,
    driftPhase: Float
) {
    val particleColor = if (isDark) Color(0xFF00F5D4) else Color(0xFF0284C7)

    particles.forEachIndexed { i, p ->
        val phaseOffset = i * 0.85f
        val currentY = ((p.normY + sin(driftPhase * 0.5f + phaseOffset) * 0.04f) * height) % height
        val currentX = ((p.normX + cos(driftPhase * 0.4f + phaseOffset) * 0.03f) * width) % width

        val alpha = (p.opacity * (if (isDark) 0.10f else 0.06f)).coerceIn(0.02f, 0.12f)

        // Gentle floating data point
        drawCircle(
            color = particleColor.copy(alpha = alpha),
            radius = p.radius.dp.toPx(),
            center = Offset(currentX, currentY)
        )
    }
}

private fun DrawScope.drawChipFraming(
    isDark: Boolean,
    width: Float,
    height: Float
) {
    val frameColor = if (isDark) Color(0xFF334155).copy(alpha = 0.06f) else Color(0xFFCBD5E1).copy(alpha = 0.04f)
    val cornerLen = 16.dp.toPx()
    val margin = 8.dp.toPx()

    // Top-Left corner bracket
    drawLine(frameColor, Offset(margin, margin), Offset(margin + cornerLen, margin), 1.dp.toPx())
    drawLine(frameColor, Offset(margin, margin), Offset(margin, margin + cornerLen), 1.dp.toPx())

    // Top-Right corner bracket
    drawLine(frameColor, Offset(width - margin, margin), Offset(width - margin - cornerLen, margin), 1.dp.toPx())
    drawLine(frameColor, Offset(width - margin, margin), Offset(width - margin, margin + cornerLen), 1.dp.toPx())

    // Bottom-Left corner bracket
    drawLine(frameColor, Offset(margin, height - margin), Offset(margin + cornerLen, height - margin), 1.dp.toPx())
    drawLine(frameColor, Offset(margin, height - margin), Offset(margin, height - margin - cornerLen), 1.dp.toPx())

    // Bottom-Right corner bracket
    drawLine(frameColor, Offset(width - margin, height - margin), Offset(width - margin - cornerLen, height - margin), 1.dp.toPx())
    drawLine(frameColor, Offset(width - margin, height - margin), Offset(width - margin, height - margin - cornerLen), 1.dp.toPx())
}

// Data structures for atmosphere geometry
private data class AtmosphereNode(
    val normX: Float,
    val normY: Float,
    val orbitRadiusX: Float,
    val orbitRadiusY: Float,
    val baseRadius: Float,
    val color: Color
)

private data class CircuitSegment(
    val normX1: Float,
    val normY1: Float,
    val normX2: Float,
    val normY2: Float,
    val normX3: Float,
    val normY3: Float
)

private data class DataParticle(
    val normX: Float,
    val normY: Float,
    val radius: Float,
    val opacity: Float
)
