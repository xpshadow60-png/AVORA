package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.CodeExplanationResult
import com.example.data.model.HomeworkSolution
import com.example.util.HomeworkImageHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkAndCodingScreen(
    homeworkSolution: HomeworkSolution?,
    isSolvingHomework: Boolean,
    codingResult: CodeExplanationResult?,
    isAnalyzingCode: Boolean,
    onSolveHomework: (String, String) -> Unit,
    onSolveHomeworkWithPhoto: ((Bitmap, String, String) -> Unit)? = null,
    onClearHomework: () -> Unit,
    onAnalyzeCode: (String, String, String, String) -> Unit,
    onClearCodingResult: () -> Unit,
    onNavigateToQuiz: (String, String) -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) } // 0: Homework Helper, 1: Coding Mentor

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Top Tab Selector
        PrimaryTabRow(
            selectedTabIndex = selectedSection,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("✍️ Homework Helper", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.EditNote, contentDescription = null) }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("💻 Coding Mentor", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Outlined.Code, contentDescription = null) }
            )
        }

        when (selectedSection) {
            0 -> HomeworkHelperView(
                solution = homeworkSolution,
                isLoading = isSolvingHomework,
                onSolve = onSolveHomework,
                onSolveWithPhoto = onSolveHomeworkWithPhoto,
                onClear = onClearHomework,
                onNavigateToQuiz = onNavigateToQuiz
            )
            1 -> CodingMentorView(
                result = codingResult,
                isLoading = isAnalyzingCode,
                onAnalyze = onAnalyzeCode,
                onClear = onClearCodingResult
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkHelperView(
    solution: HomeworkSolution?,
    isLoading: Boolean,
    onSolve: (String, String) -> Unit,
    onSolveWithPhoto: ((Bitmap, String, String) -> Unit)? = null,
    onClear: () -> Unit,
    onNavigateToQuiz: (String, String) -> Unit
) {
    var questionText by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Auto-detect") }
    var showHintOnly by remember { mutableStateOf(false) }
    var showFullSteps by remember { mutableStateOf(true) }
    var capturedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // High-resolution Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success && tempCameraUri != null) {
            val bitmap = HomeworkImageHelper.loadAndOptimizeImage(context, tempCameraUri!!)
            if (bitmap != null) {
                capturedPhotoBitmap = bitmap
                Toast.makeText(context, "📸 High-resolution photo captured! Ready for Avora to analyze.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not process captured photo. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission Launcher for CAMERA
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val uri = HomeworkImageHelper.createTempImageUri(context)
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Camera permission needed to photograph homework", Toast.LENGTH_LONG).show()
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

    // High-resolution Photo Picker Launcher (Gallery)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val bitmap = HomeworkImageHelper.loadAndOptimizeImage(context, uri)
            if (bitmap != null) {
                capturedPhotoBitmap = bitmap
                Toast.makeText(context, "📷 Homework photo selected!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Could not open or process selected image.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val subjects = listOf("Auto-detect", "Mathematics", "Physics", "Chemistry", "Biology", "Computer Science", "History", "Literature")

    val sampleProblems = listOf(
        "Find the derivative of f(x) = (3x^2 + 5)^4 using the Chain Rule.",
        "A 5kg object accelerates at 4 m/s^2. Calculate the net force acting on it.",
        "Balance the chemical equation: C3H8 + O2 -> CO2 + H2O.",
        "Explain the time complexity of Binary Search on a sorted array of N elements."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "AI Homework Helper",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hint-first step-by-step solutions for any subject",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (solution != null) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "New Question")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subject Selector
                    Text("Subject Area:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(subjects) { subj ->
                            val isSelected = selectedSubject == subj
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSubject = subj },
                                label = { Text(subj, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Camera & Gallery Capture Options
                    Text("Capture or Attach Homework:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { launchCamera() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("homework_camera_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Take Photo", fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("homework_gallery_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Image, contentDescription = "Gallery", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("From Gallery", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Captured Photo Preview Card
                    if (capturedPhotoBitmap != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "📸 Photo Attached",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { capturedPhotoBitmap = null },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove photo", modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Image(
                                    bitmap = capturedPhotoBitmap!!.asImageBitmap(),
                                    contentDescription = "Homework photo preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 180.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Input Text Field
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = {
                            Text(
                                if (capturedPhotoBitmap != null)
                                    "Optional: Add notes or problem # (e.g., 'Solve problem 4'), or leave empty for full page..."
                                else
                                    "Paste or type your homework problem, equation, or snap a photo above..."
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("homework_input_field"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick Sample Buttons
                    Text("Quick Sample Problems:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(sampleProblems) { sample ->
                            OutlinedButton(
                                onClick = { questionText = sample },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text(sample.take(24) + "...", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Row
                    val canSolve = (capturedPhotoBitmap != null || questionText.isNotBlank()) && !isLoading
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (capturedPhotoBitmap != null) {
                                    showHintOnly = false
                                    if (onSolveWithPhoto != null) {
                                        onSolveWithPhoto(capturedPhotoBitmap!!, questionText, selectedSubject)
                                    } else {
                                        onSolve(if (questionText.isNotBlank()) questionText else "Solve the problem in the attached photo", selectedSubject)
                                    }
                                } else if (questionText.isNotBlank()) {
                                    showHintOnly = false
                                    onSolve(questionText, selectedSubject)
                                } else {
                                    Toast.makeText(context, "Please snap a photo or type a question first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = canSolve,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("solve_homework_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (capturedPhotoBitmap != null) "Analyzing Photo..." else "Analyzing...")
                            } else {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (capturedPhotoBitmap != null) "Solve Photo" else "Solve Problem")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (capturedPhotoBitmap != null) {
                                    showHintOnly = true
                                    if (onSolveWithPhoto != null) {
                                        onSolveWithPhoto(capturedPhotoBitmap!!, questionText, selectedSubject)
                                    } else {
                                        onSolve(questionText, selectedSubject)
                                    }
                                } else if (questionText.isNotBlank()) {
                                    showHintOnly = true
                                    onSolve(questionText, selectedSubject)
                                } else {
                                    Toast.makeText(context, "Please snap a photo or type a question first", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = canSolve,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Outlined.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hint First")
                        }
                    }
                }
            }
        }

        // Solution Display Card
        if (solution != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Badge Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(solution.subject, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(Icons.Filled.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )

                            IconButton(onClick = {
                                val fullText = """
                                    Subject: ${solution.subject}
                                    Question: ${solution.understoodQuestion}
                                    Hint: ${solution.hint}
                                    Steps:
                                    ${solution.steps.joinToString("\n")}
                                    Final Answer: ${solution.finalAnswer}
                                    Why Correct: ${solution.whyCorrect}
                                """.trimIndent()
                                clipboardManager.setText(AnnotatedString(fullText))
                                Toast.makeText(context, "Solution copied to clipboard", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Solution")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // What is being asked
                        Text("🎯 What is being asked:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Text(solution.whatIsBeingAsked, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hint Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = "Hint",
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Guiding Clue / Formula", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                    Text(solution.hint, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }

                        if (!showHintOnly) {
                            Spacer(modifier = Modifier.height(14.dp))

                            // Steps Section
                            Text("📝 Step-by-Step Walkthrough:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            solution.steps.forEachIndexed { index, step ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Final Answer Box
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("🏆 Final Answer:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    SelectionContainer {
                                        Text(solution.finalAnswer, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Why this checks out: ${solution.whyCorrect}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showHintOnly = false },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reveal Full Step-by-Step Solution")
                            }
                        }

                        // Practice Question Recommendation
                        if (solution.similarPracticeQuestion.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("🔄 Similar Practice Challenge:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(solution.similarPracticeQuestion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        questionText = solution.similarPracticeQuestion
                                        onSolve(solution.similarPracticeQuestion, solution.subject)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Solve This One")
                                }
                                Button(
                                    onClick = {
                                        onNavigateToQuiz(solution.subject, solution.whatIsBeingAsked)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("🎯 Quiz Me on This")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodingMentorView(
    result: CodeExplanationResult?,
    isLoading: Boolean,
    onAnalyze: (String, String, String, String) -> Unit,
    onClear: () -> Unit
) {
    var codeText by remember {
        mutableStateOf(
            """fun findMax(arr: List<Int>): Int {
    var max = arr[0]
    for (i in 0..arr.size) {
        if (arr[i] > max) {
            max = arr[i]
        }
    }
    return max
}"""
        )
    }
    var selectedLanguage by remember { mutableStateOf("Kotlin") }
    var studentNote by remember { mutableStateOf("Why is this throwing IndexOutOfBoundsException?") }

    val languages = listOf("Kotlin", "Python", "Java", "JavaScript", "TypeScript", "C++", "SQL", "HTML/CSS")
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Avora Coding Mentor",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Instant debugging, line-by-line breakdown, and starter code patterns",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selector
                    Text("Programming Language:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(languages) { lang ->
                            FilterChip(
                                selected = selectedLanguage == lang,
                                onClick = { selectedLanguage = lang },
                                label = { Text(lang, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Code Input
                    Text("Code Editor:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = codeText,
                        onValueChange = { codeText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp)
                            .testTag("coding_editor_input"),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Student Error / Question Note
                    OutlinedTextField(
                        value = studentNote,
                        onValueChange = { studentNote = it },
                        placeholder = { Text("Error message or specific question (e.g. 'How to optimize to O(N)?')...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onAnalyze(codeText, selectedLanguage, studentNote, "DEBUG") },
                            enabled = codeText.isNotBlank() && !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                            } else {
                                Text("🐛 Debug")
                            }
                        }

                        FilledTonalButton(
                            onClick = { onAnalyze(codeText, selectedLanguage, studentNote, "EXPLAIN") },
                            enabled = codeText.isNotBlank() && !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔍 Explain")
                        }

                        OutlinedButton(
                            onClick = { onAnalyze(codeText, selectedLanguage, studentNote, "REVIEW") },
                            enabled = codeText.isNotBlank() && !isLoading,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("⭐ Review")
                        }
                    }
                }
            }
        }

        // Mentor Output
        if (result != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Code Analysis: ${result.language}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = onClear) {
                                Icon(Icons.Outlined.Close, contentDescription = "Close")
                            }
                        }

                        Text(result.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bugs / Issues Found
                        if (result.bugsOrIssues.isNotEmpty()) {
                            Text("⚠️ Issues & Bugs Detected:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            result.bugsOrIssues.forEach { issue ->
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "• $issue",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        // Explanation
                        Text("📖 Explanation:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Text(result.stepByStepExplanation, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(14.dp))

                        // Improved Code
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("✨ Corrected / Optimized Code:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(result.improvedCode))
                                Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy Code", modifier = Modifier.size(18.dp))
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SelectionContainer {
                                Text(
                                    text = result.improvedCode,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (result.practiceChallenge.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("🎯 Next Practice Challenge:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Text(result.practiceChallenge, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
