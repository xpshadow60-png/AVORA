package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.backup.DataBackupManager
import com.example.data.backup.RestoreSummary
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500

enum class BackupExportTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    FULL_BACKUP("JSON Backup", Icons.Default.CloudSync),
    CSV_EXPORT("CSV Sheets", Icons.Default.TableChart),
    STUDY_REPORT("Summary", Icons.Default.Description),
    RESTORE_DATA("Restore", Icons.Default.Restore)
}

@Composable
fun DataBackupExportDialog(
    onDismiss: () -> Unit,
    onExportJson: () -> String,
    onExportTasksCsv: () -> String,
    onExportFlashcardsCsv: () -> String,
    onExportSessionsCsv: () -> String,
    onExportReportMarkdown: () -> String,
    onRestoreJson: (String, (RestoreSummary) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(BackupExportTab.FULL_BACKUP) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var restoreStatus by remember { mutableStateOf<RestoreSummary?>(null) }
    var isRestoring by remember { mutableStateOf(false) }

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
                .fillMaxHeight(0.85f)
                .testTag("data_backup_export_dialog")
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
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SaveAlt,
                                    contentDescription = null,
                                    tint = Primary500,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Data Export & Backup",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                            )
                            Text(
                                text = "Export your progress or restore study data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = Primary500,
                    divider = {}
                ) {
                    BackupExportTab.entries.forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Tab Content Body
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        BackupExportTab.FULL_BACKUP -> {
                            val jsonContent = remember { onExportJson() }
                            JsonBackupSection(
                                jsonContent = jsonContent,
                                onCopy = {
                                    DataBackupManager.copyToClipboard(context, "Avora Full Backup (JSON)", jsonContent)
                                },
                                onShare = {
                                    DataBackupManager.shareText(context, "Avora Backup JSON", jsonContent, "application/json")
                                }
                            )
                        }

                        BackupExportTab.CSV_EXPORT -> {
                            CsvExportSection(
                                onExportTasks = {
                                    val csv = onExportTasksCsv()
                                    DataBackupManager.shareText(context, "Avora Tasks CSV", csv, "text/csv")
                                },
                                onExportFlashcards = {
                                    val csv = onExportFlashcardsCsv()
                                    DataBackupManager.shareText(context, "Avora Flashcards CSV", csv, "text/csv")
                                },
                                onExportSessions = {
                                    val csv = onExportSessionsCsv()
                                    DataBackupManager.shareText(context, "Avora Focus Sessions CSV", csv, "text/csv")
                                },
                                onCopyTasks = {
                                    val csv = onExportTasksCsv()
                                    DataBackupManager.copyToClipboard(context, "Tasks (CSV)", csv)
                                },
                                onCopyFlashcards = {
                                    val csv = onExportFlashcardsCsv()
                                    DataBackupManager.copyToClipboard(context, "Flashcards (CSV)", csv)
                                }
                            )
                        }

                        BackupExportTab.STUDY_REPORT -> {
                            val report = remember { onExportReportMarkdown() }
                            StudyReportSection(
                                reportContent = report,
                                onCopy = {
                                    DataBackupManager.copyToClipboard(context, "Study Summary Report", report)
                                },
                                onShare = {
                                    DataBackupManager.shareText(context, "Avora Study Summary Report", report, "text/plain")
                                }
                            )
                        }

                        BackupExportTab.RESTORE_DATA -> {
                            RestoreDataSection(
                                jsonInput = restoreJsonInput,
                                onJsonInputChange = { restoreJsonInput = it },
                                isRestoring = isRestoring,
                                restoreStatus = restoreStatus,
                                onTriggerRestore = {
                                    if (restoreJsonInput.isNotBlank()) {
                                        isRestoring = true
                                        onRestoreJson(restoreJsonInput) { summary ->
                                            isRestoring = false
                                            restoreStatus = summary
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonBackupSection(
    jsonContent: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(18.dp))
                    Text(
                        text = "100% Offline & Private Backup",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Includes all tasks, flashcards, SRS interval history, study focus session logs, schedules, and career roadmaps in structured JSON format.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Action Buttons: Share & Copy
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Save File", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy JSON", fontWeight = FontWeight.Bold)
            }
        }

        // JSON Code Preview Block
        Text(
            text = "Backup Preview (${jsonContent.length} bytes):",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (jsonContent.length > 800) jsonContent.take(800) + "\n... (truncated for preview)" else jsonContent,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}

@Composable
private fun CsvExportSection(
    onExportTasks: () -> Unit,
    onExportFlashcards: () -> Unit,
    onExportSessions: () -> Unit,
    onCopyTasks: () -> Unit,
    onCopyFlashcards: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Export Spreadsheets (CSV)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Compatible with Google Sheets, Microsoft Excel, Notion, and Anki.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // 1. Tasks Sheet Card
        CsvExportItemCard(
            title = "Tasks & Assignments",
            subtitle = "Titles, subjects, priorities, categories, due dates, & completion logs",
            icon = Icons.Default.CheckCircle,
            accentColor = Primary500,
            onShare = onExportTasks,
            onCopy = onCopyTasks
        )

        // 2. Flashcards SRS Deck Card
        CsvExportItemCard(
            title = "Flashcard Decks (Anki/Quizlet compatible)",
            subtitle = "Questions, answers, topics, SM-2 ease factors, & review intervals",
            icon = Icons.Default.Style,
            accentColor = CyanAccent,
            onShare = onExportFlashcards,
            onCopy = onCopyFlashcards
        )

        // 3. Study Sessions Card
        CsvExportItemCard(
            title = "Focus Time & Study Logs",
            subtitle = "Subjects studied, duration minutes, focus scores, & distraction counts",
            icon = Icons.Default.Timer,
            accentColor = GreenSuccess,
            onShare = onExportSessions,
            onCopy = null
        )
    }
}

@Composable
private fun CsvExportItemCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onShare: () -> Unit,
    onCopy: (() -> Unit)?
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontWeight = FontWeight.Bold, color = Color.Black)
                }

                if (onCopy != null) {
                    OutlinedButton(
                        onClick = onCopy,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyReportSection(
    reportContent: String,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onShare,
                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share Summary", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Report", fontWeight = FontWeight.Bold)
            }
        }

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = reportContent,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun RestoreDataSection(
    jsonInput: String,
    onJsonInputChange: (String) -> Unit,
    isRestoring: Boolean,
    restoreStatus: RestoreSummary?,
    onTriggerRestore: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Restore, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                    Text(
                        text = "Restore Study Data from Backup",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "Paste a previously exported JSON backup below. Existing data will be safely merged and updated in your offline database.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        OutlinedTextField(
            value = jsonInput,
            onValueChange = onJsonInputChange,
            label = { Text("Paste JSON Backup Data") },
            placeholder = { Text("{\n  \"app\": \"Avora\",\n  \"tasks\": [...]\n}") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = onTriggerRestore,
            enabled = jsonInput.isNotBlank() && !isRestoring,
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isRestoring) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Restoring Data...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Validate & Restore Database", fontWeight = FontWeight.Bold)
            }
        }

        // Restore Result Feedback Banner
        AnimatedVisibility(visible = restoreStatus != null) {
            restoreStatus?.let { status ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (status.success) GreenSuccess.copy(alpha = 0.15f) else CoralPriority.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (status.success) GreenSuccess else CoralPriority
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (status.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (status.success) GreenSuccess else CoralPriority,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (status.success) "Backup Restored Successfully! 🎉" else "Restore Failed",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (status.success) GreenSuccess else CoralPriority
                            )
                        }

                        if (status.success) {
                            Text(
                                text = "• Restored ${status.tasksRestored} tasks\n• Restored ${status.flashcardsRestored} flashcards\n• Restored ${status.schedulesRestored} schedule blocks\n• Restored ${status.sessionsRestored} focus sessions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = status.errorMessage ?: "Unknown error while parsing JSON.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoralPriority
                            )
                        }
                    }
                }
            }
        }
    }
}
