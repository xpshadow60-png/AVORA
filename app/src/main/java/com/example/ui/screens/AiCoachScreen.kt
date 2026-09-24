package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Primary500

@Composable
fun AiCoachScreen(
    messages: List<ChatMessageEntity>,
    isAiThinking: Boolean,
    onSendMessage: (String) -> Unit,
    onPrioritizeTasks: () -> Unit,
    onClearChat: () -> Unit = {},
    onRegenerate: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestedPrompts = listOf(
        "⚡ Prioritize my tasks",
        "💻 Explain Kotlin Coroutines & Flow",
        "🐍 Explain Object Oriented Programming in Python",
        "🚀 Binary Search Tree implementation",
        "📐 Solve Calculus Integration problem",
        "⏱️ Create 2-hour Pomodoro study plan",
        "📝 Tips for writing a research paper outline"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // ChatGPT Style Model Header & New Chat Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Primary500),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "GPT Model",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Avora AI Mentor",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CyanAccent.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "FAST ⚡",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                                    color = Primary500,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Smart Study, CS & Code Counselor",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { showClearDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "New Chat",
                            tint = CoralPriority,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New Chat",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = CoralPriority
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // AI Task Prioritizer Banner Button
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Prioritizer",
                        tint = Primary500,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "AI Task Prioritizer",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Auto-ranks homework & creates study plan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Button(
                    onClick = onPrioritizeTasks,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Prioritize ⚡", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Prompt Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 2.dp)
        ) {
            items(suggestedPrompts) { prompt ->
                SuggestionChip(
                    onClick = {
                        if (prompt.contains("Prioritize")) {
                            onPrioritizeTasks()
                        } else {
                            onSendMessage(prompt)
                        }
                    },
                    label = { Text(prompt, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Messages Timeline
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    msg = msg,
                    onRegenerate = onRegenerate,
                    context = context
                )
            }

            if (isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Primary500,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Avora AI is thinking & writing...",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Primary500
                        )
                    }
                }
            }
        }

        // Input Bar
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, top = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask study question or coding help...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f),
                    maxLines = 4
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isAiThinking) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank() && !isAiThinking
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isAiThinking) Primary500 else Color.Gray),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Clear Chat Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Start New Chat Session?") },
            text = { Text("This will clear your current conversation history and start a fresh Avora AI session.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearChat()
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPriority)
                ) {
                    Text("Clear & New Chat")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChatMessageBubble(
    msg: ChatMessageEntity,
    onRegenerate: () -> Unit,
    context: Context
) {
    val isUser = msg.sender.equals("USER", ignoreCase = true)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Primary500),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 300.dp)) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isUser) 18.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 18.dp
                ),
                color = if (isUser) Primary500 else if (msg.isPriorityAdvice) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (msg.isPriorityAdvice) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CyanAccent.copy(alpha = 0.25f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = "⚡ AI PRIORITIZED STUDY SCHEDULE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Primary500,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Render text with dark code block components if present
                    FormattedChatContent(
                        text = msg.text,
                        isUser = isUser,
                        context = context
                    )
                }
            }

            // ChatGPT Action Buttons under AI messages
            if (!isUser) {
                Row(
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("AI Response", msg.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    TextButton(
                        onClick = onRegenerate,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Regenerate", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun FormattedChatContent(
    text: String,
    isUser: Boolean,
    context: Context
) {
    val codeBlockRegex = Regex("```(?:(\\w+)?\\n)?([\\s\\S]*?)```")
    val matches = codeBlockRegex.findAll(text).toList()

    if (matches.isEmpty()) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        )
    } else {
        var lastIndex = 0
        matches.forEach { match ->
            // Print preceding normal text
            val precedingText = text.substring(lastIndex, match.range.first).trim()
            if (precedingText.isNotEmpty()) {
                Text(
                    text = precedingText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            val lang = match.groupValues.getOrNull(1)?.ifBlank { "code" } ?: "code"
            val codeSnippet = match.groupValues.getOrNull(2) ?: ""

            // Render ChatGPT Code Block Card
            CodeBlockCard(
                language = lang,
                code = codeSnippet,
                context = context
            )

            lastIndex = match.range.last + 1
        }

        // Print remaining text after last code block
        if (lastIndex < text.length) {
            val remainingText = text.substring(lastIndex).trim()
            if (remainingText.isNotEmpty()) {
                Text(
                    text = remainingText,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    ),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    context: Context
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column {
            // Code Block Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF2D2D2D))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.lowercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFB0BEC5)
                )

                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Code Snippet", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Code",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy Code",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = Color.White
                    )
                }
            }

            // Code Text Snippet
            Text(
                text = code.trim(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = Color(0xFF80D8FF),
                    lineHeight = 18.sp
                ),
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
