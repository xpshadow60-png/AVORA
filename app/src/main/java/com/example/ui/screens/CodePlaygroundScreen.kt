package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeExplanationResult
import com.example.data.model.CodingChallenge
import com.example.data.model.ExecutionResult
import com.example.data.remote.AvoraLearningDataEngine
import com.example.data.remote.SafePythonRunner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodePlaygroundScreen(
    codingResult: CodeExplanationResult?,
    isAnalyzingCode: Boolean,
    onAnalyzeCode: (String, String, String, String) -> Unit,
    onClearCodingResult: () -> Unit,
    onChallengeSolved: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Python Playground, 1: Coding Challenges, 2: AI Code Doctor
    var currentCode by remember {
        mutableStateOf(
            """
# 🐍 Avora Python Playground
# Write and execute your Python code safely below!

def calculate_fibonacci(n):
    fib_sequence = [0, 1]
    for i in range(2, n):
        fib_sequence.append(fib_sequence[i - 1] + fib_sequence[i - 2])
    return fib_sequence

# Compute first 8 numbers
result = calculate_fibonacci(8)
print("Fibonacci Sequence:", result)
print("Sum of Sequence:", sum(result))
            """.trimIndent()
        )
    }

    var executionResult by remember { mutableStateOf<ExecutionResult?>(null) }
    var isRunningCode by remember { mutableStateOf(false) }

    val challenges = remember { AvoraLearningDataEngine.getCodingChallenges() }
    var selectedChallengeIndex by remember { mutableStateOf(0) }
    val activeChallenge = challenges.getOrNull(selectedChallengeIndex) ?: challenges.first()
    var challengeUserCode by remember { mutableStateOf(activeChallenge.starterCode) }
    var challengeHintLevel by remember { mutableStateOf(0) } // 0: None, 1: Gentle, 2: Clue, 3: Logic
    var challengeExecutionResult by remember { mutableStateOf<ExecutionResult?>(null) }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Top Mode Switcher
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("🐍 Python Playground", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Outlined.Terminal, contentDescription = "Python Playground", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_python_runner")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    challengeUserCode = activeChallenge.starterCode
                    challengeExecutionResult = null
                    challengeHintLevel = 0
                },
                text = { Text("🎯 Coding Challenges", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Outlined.EmojiEvents, contentDescription = "Coding Challenges", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_challenges")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("🩺 AI Code Doctor", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                icon = { Icon(Icons.Outlined.BugReport, contentDescription = "AI Code Doctor", modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_debugger")
            )
        }

        when (selectedTab) {
            0 -> {
                // TAB 0: INTERACTIVE PYTHON PLAYGROUND
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Python Playground Header Banner
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "🐍 Python 3 Interactive Playground",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                        ) {
                                            Text(
                                                text = "OFFLINE SANDBOX",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color(0xFF10B981),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Client-side sandboxed evaluator for Python 3 syntax (functions, loops, recursion, data structures & math) running safely offline.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Code Templates Quick Bar
                    item {
                        Text(
                            text = "💡 Python Code Templates:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val templates = listOf(
                                "Fibonacci" to """
def fibonacci(n):
    seq = [0, 1]
    for i in range(2, n):
        seq.append(seq[i-1] + seq[i-2])
    return seq
print("Fibonacci:", fibonacci(7))
                                """.trimIndent(),
                                "Prime Checker" to """
def is_prime(num):
    if num <= 1:
        return False
    for i in range(2, int(num**0.5) + 1):
        if num % i == 0:
            return False
    return True
print("Is 29 prime?", is_prime(29))
                                """.trimIndent(),
                                "Word Frequency" to """
text = "ai study companion avora ai study python"
counts = {}
for word in text.split():
    counts[word] = counts.get(word, 0) + 1
print("Word Counts:", counts)
                                """.trimIndent(),
                                "AI Confidence" to """
confidence_scores = [0.92, 0.85, 0.78, 0.95]
avg_score = sum(confidence_scores) / len(confidence_scores)
print(f"AI Average Accuracy: {avg_score * 100:.1f}%")
                                """.trimIndent()
                            )
                            items(templates) { (title, snippet) ->
                                AssistChip(
                                    onClick = { currentCode = snippet },
                                    label = { Text(title, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }

                    // Code Editor Box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("🐍 main.py (Python 3 Playground)", color = Color(0xFFCDD6F4), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(currentCode))
                                                Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", tint = Color(0xFFBAC2DE), modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { currentCode = "" },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear", tint = Color(0xFFF38BA8), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                HorizontalDivider(color = Color(0xFF313244), modifier = Modifier.padding(vertical = 8.dp))

                                BasicTextField(
                                    value = currentCode,
                                    onValueChange = { currentCode = it },
                                    textStyle = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 13.sp,
                                        color = Color(0xFFA6E3A1),
                                        lineHeight = 18.sp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 160.dp, max = 320.dp)
                                        .verticalScroll(rememberScrollState())
                                        .testTag("python_code_input")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick helper typing buttons
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val helperTokens = listOf("def ", "for ", "in range(", "if ", "print(", ":", "return ", " = ", "[ ]", "{ }")
                                    items(helperTokens) { token ->
                                        Surface(
                                            onClick = { currentCode += token },
                                            color = Color(0xFF313244),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = token,
                                                color = Color(0xFF89B4FA),
                                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Action Run and Debug Buttons
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isRunningCode = true
                                    executionResult = SafePythonRunner.execute(currentCode)
                                    isRunningCode = false
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_run_python"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Run Python", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedTab = 2
                                    onAnalyzeCode(currentCode, "Python", "Please review and debug my Python code.", "DEBUG")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("btn_ai_debug_code")
                            ) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Debug", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Live Terminal Output Console
                    item {
                        executionResult?.let { res ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF11111B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(
                                                imageVector = if (res.isSuccess) Icons.Filled.CheckCircle else Icons.Filled.Error,
                                                contentDescription = null,
                                                tint = if (res.isSuccess) Color(0xFF27C93F) else Color(0xFFFF5F56),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = if (res.isSuccess) "Terminal Output (Success)" else "Terminal Error",
                                                color = Color(0xFFCDD6F4),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Text(
                                            text = "${res.executionTimeMs}ms",
                                            color = Color(0xFF6C7086),
                                            style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace)
                                        )
                                    }

                                    HorizontalDivider(color = Color(0xFF313244), modifier = Modifier.padding(vertical = 8.dp))

                                    SelectionContainer {
                                        Text(
                                            text = res.output,
                                            color = if (res.isSuccess) Color(0xFFCDD6F4) else Color(0xFFF38BA8),
                                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, lineHeight = 18.sp),
                                            modifier = Modifier.testTag("terminal_output_text")
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // TAB 1: CODING CHALLENGES WITH TEST CASES & HINT ESCALATION
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Challenge Selector
                    item {
                        Text(
                            text = "🏆 Select Challenge",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(challenges.indices.toList()) { idx ->
                                val ch = challenges[idx]
                                FilterChip(
                                    selected = selectedChallengeIndex == idx,
                                    onClick = {
                                        selectedChallengeIndex = idx
                                        challengeUserCode = ch.starterCode
                                        challengeExecutionResult = null
                                        challengeHintLevel = 0
                                    },
                                    label = { Text(ch.title, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        val iconColor = when (ch.difficulty) {
                                            "Beginner" -> Color(0xFF10B981)
                                            "Intermediate" -> Color(0xFFF59E0B)
                                            else -> Color(0xFFEF4444)
                                        }
                                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(iconColor))
                                    }
                                )
                            }
                        }
                    }

                    // Active Challenge Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = activeChallenge.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = activeChallenge.difficulty,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = activeChallenge.promptDescription,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Socratic Hint Escalator Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Socratic Hint Escalator",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    if (challengeHintLevel < activeChallenge.hints.size) {
                                        TextButton(onClick = { challengeHintLevel++ }) {
                                            Text("Get Next Hint (${challengeHintLevel + 1}/${activeChallenge.hints.size})", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }

                                if (challengeHintLevel > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    for (i in 0 until challengeHintLevel) {
                                        Text(
                                            text = activeChallenge.hints[i],
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Stuck? Request a hint step-by-step instead of peeking at the full answer.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Code Editor for Challenge
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("solution.py", color = Color(0xFFCDD6F4), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                    TextButton(onClick = { challengeUserCode = activeChallenge.starterCode }) {
                                        Text("Reset Starter Code", color = Color(0xFF89B4FA), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                HorizontalDivider(color = Color(0xFF313244), modifier = Modifier.padding(bottom = 8.dp))

                                BasicTextField(
                                    value = challengeUserCode,
                                    onValueChange = { challengeUserCode = it },
                                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, color = Color(0xFFA6E3A1), lineHeight = 18.sp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 140.dp, max = 280.dp)
                                        .verticalScroll(rememberScrollState())
                                )
                            }
                        }
                    }

                    // Run Test Cases Button
                    item {
                        Button(
                            onClick = {
                                val res = SafePythonRunner.execute(challengeUserCode, activeChallenge.testCases)
                                challengeExecutionResult = res
                                if (res.passedTests == res.totalTests && res.isSuccess) {
                                    onChallengeSolved(activeChallenge.id)
                                    Toast.makeText(context, "🎉 All Test Cases Passed! +60 XP Earned!", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_run_challenge_tests"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.FactCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Run Automated Test Cases", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Test Cases Results Display
                    item {
                        challengeExecutionResult?.let { res ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (res.passedTests == res.totalTests) Color(0xFF064E3B) else Color(0xFF450A0A)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (res.passedTests == res.totalTests) "✅ All Test Cases Passed (${res.passedTests}/${res.totalTests})" else "❌ Some Test Cases Failed (${res.passedTests}/${res.totalTests})",
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text("${res.executionTimeMs}ms", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = res.output,
                                        color = Color.White.copy(alpha = 0.9f),
                                        style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // TAB 2: AI CODE DOCTOR & DEEP BUG EXPLAINER
                CodingMentorView(
                    result = codingResult,
                    isLoading = isAnalyzingCode,
                    onAnalyze = onAnalyzeCode,
                    onClear = onClearCodingResult
                )
            }
        }
    }
}
