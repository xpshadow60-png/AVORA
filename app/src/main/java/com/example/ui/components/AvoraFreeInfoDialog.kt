package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * Information dialog highlighting Avora's commitment to free, accessible education for all students.
 * Replaces any previous subscription/paywall areas with a clear message:
 * "Avora is Free — Learning should be accessible to everyone."
 */
@Composable
fun AvoraFreeInfoDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        GreenSuccess.copy(alpha = 0.8f),
                        CyanAccent.copy(alpha = 0.7f),
                        Primary500.copy(alpha = 0.8f)
                    )
                )
            ),
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("avora_free_info_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Close Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = GreenSuccess.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolunteerActivism,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "100% FREE FOREVER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GreenSuccess
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hero Badge Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    GreenSuccess.copy(alpha = 0.25f),
                                    CyanAccent.copy(alpha = 0.10f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = GreenSuccess,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title: Avora is Free
                Text(
                    text = "Avora is Free",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                // Subtitle: Learning should be accessible to everyone.
                Text(
                    text = "Learning should be accessible to everyone.",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    color = CyanAccent,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Text(
                    text = "Avora was created to empower students from every background with modern AI education tools. There are no subscriptions, no paywalls, and no hidden fees.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Feature Highlights List (All Free)
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FreeFeatureRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = "AI Tech Tutor & Problem Solver",
                        description = "Unlimited interactive tutoring, code explanations, and homework solutions."
                    )
                    FreeFeatureRow(
                        icon = Icons.Filled.Terminal,
                        title = "Code Lab & Compiler Simulator",
                        description = "Real-time Python runner, starter challenges, and AI Code Doctor."
                    )
                    FreeFeatureRow(
                        icon = Icons.Filled.Map,
                        title = "Career Guidance & Roadmaps",
                        description = "Full tech career path generator with milestone tracking and curated projects."
                    )
                    FreeFeatureRow(
                        icon = Icons.Filled.Description,
                        title = "Notes & Document Analysis",
                        description = "Extract key concepts, study guides, and instant quizzes from your notes."
                    )
                    FreeFeatureRow(
                        icon = Icons.Filled.Timer,
                        title = "Unlimited Study & Focus Timer",
                        description = "Ambient soundscapes, pomodoro timers, and zero daily time cutoffs."
                    )
                    FreeFeatureRow(
                        icon = Icons.Filled.Shield,
                        title = "Privacy-First Architecture",
                        description = "0% ad trackers, local Room database persistence, and secure AI gateway."
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Action Button: Got it / Let's Learn
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSuccess
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Learning Free 🚀",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FreeFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GreenSuccess.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GreenSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
