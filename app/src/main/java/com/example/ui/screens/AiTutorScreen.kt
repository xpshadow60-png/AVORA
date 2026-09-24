package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ChatMessageEntity
import com.example.ui.components.StructuredAiContent
import com.example.ui.theme.*
import com.example.ui.tutor.VoiceState
import com.example.util.HomeworkImageHelper
import kotlinx.coroutines.launch

/**
 * Avora AI Tutor Screen - Redesigned professional AI assistant experience.
 * Features:
 * - Clean, modern top bar with Avora identity, online status indicator, and quick actions
 * - Socratic starter prompt cards when chat is fresh/empty
 * - Natural homework photo capture & attachment preview in composer
 * - Markdown & code block rendering with copy & syntax styling
 * - Progressive Socratic learning & prompt guidance chips
 * - Voice speech input and text-to-speech reading
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiTutorScreen(
    messages: List<ChatMessageEntity>,
    isAiThinking: Boolean,
    isResearchMode: Boolean,
    voiceState: VoiceState,
    spokenText: String,
    onSendMessage: (String) -> Unit,
    onSendMessageWithModifier: (String, String?) -> Unit,
    onSendPhotoMessage: ((Bitmap, String, String) -> Unit)? = null,
    onToggleResearchMode: () -> Unit,
    onStartVoiceInput: () -> Unit,
    onStopVoiceInput: () -> Unit,
    onSpeakText: (String) -> Unit,
    onStopSpeaking: () -> Unit,
    onPrioritizeTasks: () -> Unit,
    onClearChat: () -> Unit,
    onRegenerate: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All Subjects") }
    var attachedDocSummary by remember { mutableStateOf<String?>(null) }
    var isParsingAttachedDoc by remember { mutableStateOf(false) }
    var selectedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // High-resolution camera launcher for homework photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val bitmap = HomeworkImageHelper.loadAndOptimizeImage(context, tempCameraUri!!)
            if (bitmap != null) {
                selectedPhotoBitmap = bitmap
                Toast.makeText(context, "📸 High-resolution homework photo attached!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not process photo. Please try capturing again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = HomeworkImageHelper.createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission required for homework scan", Toast.LENGTH_SHORT).show()
        }
    }

    fun launchCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasPermission) {
            val uri = HomeworkImageHelper.createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // High-resolution photo picker launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val bitmap = HomeworkImageHelper.loadAndOptimizeImage(context, uri)
            if (bitmap != null) {
                selectedPhotoBitmap = bitmap
                Toast.makeText(context, "📷 Homework photo selected! Ready to analyze.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not open or process selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Document picker launcher
    val docPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isParsingAttachedDoc = true
                val res = com.example.data.document.DocumentParserEngine.parseDocument(context, uri)
                isParsingAttachedDoc = false
                res.onSuccess { doc ->
                    attachedDocSummary = "📄 ${doc.fileName} (${doc.pageCount} pages)"
                    val preview = doc.textContent.take(3000)
                    inputText = "Please explain and answer questions about this document:\n[DOCUMENT: ${doc.fileName}]:\n$preview"
                    Toast.makeText(context, "Attached ${doc.fileName}", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Failed to read file: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Auto-scroll on new message or thinking state
    LaunchedEffect(messages.size, isAiThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Update voice transcription
    LaunchedEffect(spokenText) {
        if (spokenText.isNotBlank()) {
            inputText = spokenText
        }
    }

    val quickModifiers = listOf(
        Pair("💡 Socratic Hint", "GIVE_HINTS"),
        Pair("📐 Step-by-Step", "STEP_BY_STEP"),
        Pair("💻 Code Example", "GIVE_EXAMPLE"),
        Pair("🎯 Practice Problem", "PRACTICE_PROBLEM"),
        Pair("⚠️ Common Pitfalls", "MISCONCEPTION_CHECK"),
        Pair("🌱 Explain Simply", "EXPLAIN_SIMPLY"),
        Pair("🚀 What's Next?", "WHAT_TO_LEARN_NEXT")
    )

    var tutorLanguage by remember { mutableStateOf("English") } // "English" or "Hindi"
    var learnerLevel by remember { mutableStateOf("Beginner") } // "Beginner", "Intermediate", "Advanced"

    val popularSubjects = listOf(
        "All Tech",
        "🐍 Python & CS",
        "🤖 AI & LLMs",
        "🌐 Web & APIs",
        "📱 Android & Kotlin",
        "⚡ Data Structures",
        "🗄️ Databases & SQL",
        "☁️ Cloud & DevOps",
        "🛡️ Cybersecurity"
    )

    // Confirmation dialog before clearing chat
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Start New Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("This will clear your current chat history and restart with Avora's fresh learning session.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearChat()
                    }
                ) {
                    Text("New Chat", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // ==========================================
        // 1. PROFESSIONAL TOP APP BAR
        // ==========================================
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 1.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Avatar & Status Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(Primary600, Primary400)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                fontFamily = FontFamily.SansSerif
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Avora AI Tutor",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                // Active Indicator Dot
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(ElectricGreen)
                                )
                            }
                            Text(
                                text = "Made by Sir Barie Bilal • Future-Ready AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Action Icons: Research Mode & New Chat
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Research Mode Toggle Chip
                        FilterChip(
                            selected = isResearchMode,
                            onClick = onToggleResearchMode,
                            label = { Text(if (isResearchMode) "Deep Web" else "Speed", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isResearchMode) Icons.Filled.Public else Icons.Outlined.Public,
                                    contentDescription = "Toggle Web Grounding",
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanAccent.copy(alpha = 0.18f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(32.dp)
                        )

                        // New Chat / Clear Action
                        IconButton(
                            onClick = { showClearConfirmDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.EditNote,
                                contentDescription = "New Conversation",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Educational Subject Tags & Language Options
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Subject Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(popularSubjects) { subj ->
                            val isSelected = selectedSubjectFilter == subj
                            SuggestionChip(
                                onClick = { selectedSubjectFilter = subj },
                                label = { Text(subj, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = if (isSelected) SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.primary
                                ) else null,
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Language indicator chip (English / Hindi)
                    FilterChip(
                        selected = tutorLanguage == "Hindi",
                        onClick = {
                            tutorLanguage = if (tutorLanguage == "English") "Hindi" else "English"
                        },
                        label = { Text(if (tutorLanguage == "Hindi") "🇮🇳 Hinglish" else "🇬🇧 English", fontSize = 10.sp) },
                        modifier = Modifier.height(28.dp)
                    )
                }
            }
        }

        // ==========================================
        // VOICE TUTOR BANNER (SPEAKING / LISTENING)
        // ==========================================
        AnimatedVisibility(visible = voiceState != VoiceState.IDLE) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (voiceState == VoiceState.SPEAKING)
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f)
                    else
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (voiceState == VoiceState.SPEAKING) Icons.Filled.VolumeUp else Icons.Filled.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = when (voiceState) {
                                VoiceState.LISTENING -> "Listening... Speak your question"
                                VoiceState.PROCESSING -> "Analyzing speech..."
                                VoiceState.SPEAKING -> "Avora is reading answer aloud..."
                                else -> "Voice Tutor active"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            if (voiceState == VoiceState.SPEAKING) onStopSpeaking()
                            else onStopVoiceInput()
                        },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Stop", fontSize = 11.sp)
                    }
                }
            }
        }

        // ==========================================
        // 2. CONVERSATION MESSAGES / WELCOME EMPTY STATE
        // ==========================================
        val displayMessages = messages.filter { it.text.isNotBlank() }
        val isFirstLaunchWelcome = displayMessages.size <= 1

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Welcome Starter Card (shown when conversation is new)
            if (isFirstLaunchWelcome) {
                item {
                    TutorWelcomeCard(
                        onSelectTopic = { prompt ->
                            onSendMessage(prompt)
                        },
                        onCapturePhoto = {
                            launchCamera()
                        }
                    )
                }
            }

            items(displayMessages) { msg ->
                val isUser = msg.sender.equals("USER", ignoreCase = true)
                ProfessionalTutorBubble(
                    message = msg,
                    isUser = isUser,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Avora AI Tutor", msg.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onSpeak = { onSpeakText(msg.text) },
                    onExplainSimply = { onSendMessageWithModifier(msg.text, "EXPLAIN_SIMPLY") },
                    onStepByStep = { onSendMessageWithModifier(msg.text, "STEP_BY_STEP") }
                )
            }

            // AI "Thinking" state indicator
            if (isAiThinking) {
                item {
                    ProfessionalAiThinkingIndicator(isResearchMode = isResearchMode)
                }
            }
        }

        // ==========================================
        // 3. SOCRATIC PROGRESSIVE PROMPT CHIPS
        // ==========================================
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickModifiers) { (label, modKey) ->
                FilledTonalButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessageWithModifier(inputText, modKey)
                            inputText = ""
                        } else {
                            val lastUserMsg = messages.lastOrNull { it.sender.equals("USER", ignoreCase = true) }?.text
                            if (!lastUserMsg.isNullOrBlank()) {
                                onSendMessageWithModifier(lastUserMsg, modKey)
                            } else {
                                Toast.makeText(context, "Type a question or topic first!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }

        // ==========================================
        // 4. ATTACHMENT PREVIEW (PHOTO / DOCUMENT)
        // ==========================================
        if (selectedPhotoBitmap != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        bitmap = selectedPhotoBitmap!!.asImageBitmap(),
                        contentDescription = "Homework Photo Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📸 Homework Photo Attached",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap Send to analyze, or add an optional question",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { selectedPhotoBitmap = null }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Remove photo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // ==========================================
        // 5. MODERN CHAT COMPOSER DOCK
        // ==========================================
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Homework Camera Button
                IconButton(
                    onClick = {
                        launchCamera()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = "Photograph Homework",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Gallery / Document Attachment Button
                IconButton(
                    onClick = {
                        galleryPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = "Choose Photo from Gallery",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Multiline Input Field
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (selectedPhotoBitmap != null) "Optional: ask specific question..." else "Ask Avora about code, science, math...",
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tutor_chat_input"),
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Voice / Send Action
                val canSend = (inputText.isNotBlank() || selectedPhotoBitmap != null) && !isAiThinking

                if (canSend) {
                    IconButton(
                        onClick = {
                            if (selectedPhotoBitmap != null) {
                                val bitmap = selectedPhotoBitmap!!
                                selectedPhotoBitmap = null
                                val prompt = inputText
                                inputText = ""
                                onSendPhotoMessage?.invoke(bitmap, prompt, selectedSubjectFilter)
                            } else {
                                val contextSubject = if (selectedSubjectFilter != "All Subjects") "[$selectedSubjectFilter] " else ""
                                val langContext = if (tutorLanguage == "Hindi") "[Language: Hindi/Hinglish, keep code in English] " else ""
                                onSendMessage("$langContext$contextSubject$inputText")
                                inputText = ""
                            }
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("tutor_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                } else {
                    // Voice Mic Button when text is empty
                    IconButton(
                        onClick = {
                            if (voiceState == VoiceState.LISTENING) onStopVoiceInput()
                            else onStartVoiceInput()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (voiceState == VoiceState.LISTENING) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                            )
                    ) {
                        Icon(
                            imageVector = if (voiceState == VoiceState.LISTENING) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = "Voice Input",
                            tint = if (voiceState == VoiceState.LISTENING) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modern AI Chat Bubble supporting Markdown, Socratic Callouts, Code Blocks, and Action Controls
 */
@Composable
fun ProfessionalTutorBubble(
    message: ChatMessageEntity,
    isUser: Boolean,
    onCopy: () -> Unit,
    onSpeak: () -> Unit,
    onExplainSimply: () -> Unit,
    onStepByStep: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(Primary600, Primary400)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 330.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Render rich structured markdown and code blocks
                    StructuredAiContent(
                        text = message.text,
                        isUser = isUser
                    )

                    // Action Controls for AI responses
                    if (!isUser) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onCopy,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy message",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }

                            IconButton(
                                onClick = onSpeak,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.VolumeUp,
                                    contentDescription = "Read aloud",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            // Quick Socratic follow-up chips
                            AssistChip(
                                onClick = onExplainSimply,
                                label = { Text("Simpler", fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp)
                            )

                            AssistChip(
                                onClick = onStepByStep,
                                label = { Text("Steps", fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "You",
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * AI "Thinking" state indicator with pulse animation
 */
@Composable
fun ProfessionalAiThinkingIndicator(isResearchMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isResearchMode) "Avora is researching verified academic sources..." else "Avora is formulating guided explanation...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Empty Chat Welcome State with Starter Exploration Cards
 */
@Composable
fun TutorWelcomeCard(
    onSelectTopic: (String) -> Unit,
    onCapturePhoto: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Welcome to Avora AI Tutor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Learn Technology • Build Technology • Become Future-Ready",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "What would you like to master today? Pick a starter topic or photograph any homework problem:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Starter Option Cards Grid
            val starters = listOf(
                Pair("📸 Scan Homework Problem", "CAPTURE_PHOTO"),
                Pair("🐍 Python Functions & Loops", "Teach me Python functions, parameters, and loops with clean beginner examples."),
                Pair("⚡ Master Binary Search", "Explain Binary Search with an intuitive analogy, pseudocode, and time complexity."),
                Pair("🤖 What is an LLM Transformer?", "Explain how Large Language Model Transformers work in simple terms with an analogy."),
                Pair("📱 Build Android with Jetpack Compose", "What are the core fundamentals of building modern Android UI with Jetpack Compose?")
            )

            starters.forEach { (label, prompt) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            if (prompt == "CAPTURE_PHOTO") {
                                onCapturePhoto()
                            } else {
                                onSelectTopic(prompt)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
