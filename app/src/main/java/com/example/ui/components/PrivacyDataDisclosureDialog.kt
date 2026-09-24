package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500

@Composable
fun PrivacyDataDisclosureDialog(
    onDismiss: () -> Unit,
    onOpenDataExport: () -> Unit = {}
) {
    var expandedSection by remember { mutableStateOf<Int?>(0) } // Default open first section

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("privacy_disclosure_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
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
                            color = Primary500.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = Primary500,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Privacy & Data Disclosures",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = "Complete transparency on data handling & privacy",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Privacy Trust Metrics Ribbon
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TrustBadge(icon = Icons.Default.Security, label = "Zero Trackers", subtext = "0% Ad Tracking", color = GreenSuccess)
                        VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        TrustBadge(icon = Icons.Default.Storage, label = "Local-First", subtext = "Encrypted DB", color = CyanAccent)
                        VerticalDivider(modifier = Modifier.height(30.dp), color = MaterialTheme.colorScheme.outlineVariant)
                        TrustBadge(icon = Icons.Default.Lock, label = "SHA-256 Auth", subtext = "Salted Hash", color = Primary500)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Disclosure Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Disclosure 1: Core Privacy Philosophy
                    DisclosureCard(
                        index = 0,
                        isExpanded = expandedSection == 0,
                        onToggle = { expandedSection = if (expandedSection == 0) null else 0 },
                        icon = Icons.Outlined.VerifiedUser,
                        title = "1. Student Privacy Commitment",
                        subtitle = "Your educational data belongs solely to you",
                        content = {
                            Text(
                                text = "Avora is designed with privacy by default. We do not sell, rent, monetize, or trade your personal data, study patterns, notes, or quiz results to third parties or advertising brokers. Your academic progression, career goals, and notes are private to you.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        }
                    )

                    // Disclosure 2: Local Data Storage & Persistence
                    DisclosureCard(
                        index = 1,
                        isExpanded = expandedSection == 1,
                        onToggle = { expandedSection = if (expandedSection == 1) null else 1 },
                        icon = Icons.Outlined.SdCard,
                        title = "2. On-Device Storage & Room DB",
                        subtitle = "Tasks, flashcards, and schedules stored on device",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "All study sessions, tasks, spaced repetition flashcards, quiz scores, and calendar schedules are written directly to an isolated, sandboxed SQLite database using Android Jetpack Room on your local device.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                BulletPoint(text = "Sandbox Protection: Stored within the Android OS protected application container.")
                                BulletPoint(text = "Offline Accessible: 100% functional without internet connectivity.")
                            }
                        }
                    )

                    // Disclosure 3: AI & Gemini Cloud Processing
                    DisclosureCard(
                        index = 2,
                        isExpanded = expandedSection == 2,
                        onToggle = { expandedSection = if (expandedSection == 2) null else 2 },
                        icon = Icons.Outlined.AutoAwesome,
                        title = "3. AI & Google Gemini Processing",
                        subtitle = "How AI questions, quizzes & summaries work",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "When you interact with the AI Tech Tutor, analyze a document, debug code, or generate custom quizzes, your specific prompt text is transmitted via secure HTTPS (TLS 1.3 encryption) to Google Gemini API servers to formulate intelligent responses.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                BulletPoint(text = "Transmission Security: Encrypted end-to-end in transit using modern TLS.")
                                BulletPoint(text = "Ephemeral Processing: Prompts are used exclusively to generate your response.")
                                BulletPoint(text = "User Controls: You can clear tutor history anytime with the trash icon.")
                            }
                        }
                    )

                    // Disclosure 4: PDF & Document Import Handling
                    DisclosureCard(
                        index = 3,
                        isExpanded = expandedSection == 3,
                        onToggle = { expandedSection = if (expandedSection == 3) null else 3 },
                        icon = Icons.Outlined.Description,
                        title = "4. Document & PDF Parsing Disclosures",
                        subtitle = "Real PDF/DOCX reading occurs locally",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "When you select a document (.pdf, .docx, .txt, .md), the file is accessed via Android's secure Storage Access Framework (SAF). Parsing and text stream extraction happen entirely on your device inside memory.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                BulletPoint(text = "No File Uploads: We never upload your raw files to external file hosting servers.")
                                BulletPoint(text = "Temporary Cache: Parsed text is retained in app memory only during your active analysis session.")
                            }
                        }
                    )

                    // Disclosure 5: Audio & Microphone Disclosures
                    DisclosureCard(
                        index = 4,
                        isExpanded = expandedSection == 4,
                        onToggle = { expandedSection = if (expandedSection == 4) null else 4 },
                        icon = Icons.Outlined.Mic,
                        title = "5. Voice Tutor & Audio Privacy",
                        subtitle = "Speech recognition and audio permissions",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "The AI Voice Tutor uses the Android System SpeechRecognizer. Microphone access is requested only when you tap the mic icon.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                BulletPoint(text = "No Background Listening: Audio recording stops immediately upon finishing speaking.")
                                BulletPoint(text = "No Audio Archiving: Raw voice recordings are never saved to disk or transmitted.")
                            }
                        }
                    )

                    // Disclosure 6: Passwords & Authentication Security
                    DisclosureCard(
                        index = 5,
                        isExpanded = expandedSection == 5,
                        onToggle = { expandedSection = if (expandedSection == 5) null else 5 },
                        icon = Icons.Outlined.Lock,
                        title = "6. Cryptographic Password Security",
                        subtitle = "Salted SHA-256 digest hashing",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Your account passwords are protected using cryptographic SHA-256 digest hashing combined with a unique per-user cryptographically random UUID salt. Plaintext passwords are never stored in storage or logs.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    )

                    // Disclosure 7: User Data Rights, Export & Deletion
                    DisclosureCard(
                        index = 6,
                        isExpanded = expandedSection == 6,
                        onToggle = { expandedSection = if (expandedSection == 6) null else 6 },
                        icon = Icons.Outlined.Download,
                        title = "7. Full Data Portability & Erasure",
                        subtitle = "Export in JSON/CSV or delete anytime",
                        content = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "You maintain complete ownership of your data. You can export all your tasks, flashcards, sessions, and career milestones as standard JSON or CSV spreadsheets at any time.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 20.sp
                                )
                                OutlinedButton(
                                    onClick = {
                                        onDismiss()
                                        onOpenDataExport()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanAccent)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open Data Export & Backup Tool")
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Footer Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenDataExport()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Data", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("I Understand & Agree", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TrustBadge(
    icon: ImageVector,
    label: String,
    subtext: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DisclosureCard(
    index: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isExpanded) Primary500.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isExpanded) Primary500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isExpanded) Primary500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    content()
                }
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = Primary500,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 18.sp
        )
    }
}
