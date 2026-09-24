package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Primary500

/**
 * Structured Markdown Parser for Avora AI Tutor responses.
 * Formats:
 * - Code blocks (```language ... ```) with syntax header, copy button, and horizontal scroll
 * - Socratic hint callout cards (> Hint or 💡 Hint)
 * - Section headers (###, ##, #)
 * - Bullet lists (- item or * item) and numbered lists (1. item)
 * - Bold (**text**) and inline code (`code`)
 */
@Composable
fun StructuredAiContent(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface

    // Parse code blocks with regex: ```lang?\ncode```
    val codeBlockRegex = Regex("```(?:(\\w+)?\\n)?([\\s\\S]*?)```")
    val matches = codeBlockRegex.findAll(text).toList()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (matches.isEmpty()) {
            RenderMarkdownSegments(text = text, isUser = isUser, defaultColor = textColor)
        } else {
            var lastIndex = 0
            matches.forEach { match ->
                val precedingText = text.substring(lastIndex, match.range.first).trim()
                if (precedingText.isNotEmpty()) {
                    RenderMarkdownSegments(text = precedingText, isUser = isUser, defaultColor = textColor)
                }

                val lang = match.groupValues.getOrNull(1)?.ifBlank { "code" } ?: "code"
                val codeSnippet = match.groupValues.getOrNull(2)?.trim() ?: ""

                TutorCodeBlock(
                    language = lang,
                    code = codeSnippet,
                    context = context
                )

                lastIndex = match.range.last + 1
            }

            if (lastIndex < text.length) {
                val remainingText = text.substring(lastIndex).trim()
                if (remainingText.isNotEmpty()) {
                    RenderMarkdownSegments(text = remainingText, isUser = isUser, defaultColor = textColor)
                }
            }
        }
    }
}

@Composable
private fun RenderMarkdownSegments(
    text: String,
    isUser: Boolean,
    defaultColor: Color
) {
    val lines = text.split("\n")
    var inBlockquote = false
    val blockquoteLines = mutableListOf<String>()

    lines.forEach { line ->
        val trimmed = line.trim()
        when {
            // Socratic Hint or Blockquote Callout (> or starts with 💡 Socratic Hint)
            trimmed.startsWith(">") || trimmed.startsWith("💡 Socratic Hint:") -> {
                val cleanLine = if (trimmed.startsWith(">")) trimmed.removePrefix(">").trim() else trimmed
                SocraticHintCard(hintText = cleanLine)
            }
            // Headers
            trimmed.startsWith("### ") -> {
                Text(
                    text = trimmed.removePrefix("### ").trim(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) defaultColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
            }
            trimmed.startsWith("## ") -> {
                Text(
                    text = trimmed.removePrefix("## ").trim(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) defaultColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }
            trimmed.startsWith("# ") -> {
                Text(
                    text = trimmed.removePrefix("# ").trim(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isUser) defaultColor else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
            }
            // Bullet Points
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ") -> {
                val bulletContent = trimmed.substring(2).trim()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) defaultColor else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    SelectionContainer {
                        Text(
                            text = parseInlineMarkdown(bulletContent, defaultColor),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            // Numbered List (e.g., 1. , 2. )
            Regex("^\\d+\\.\\s.*").matches(trimmed) -> {
                val dotIndex = trimmed.indexOf('.')
                val number = trimmed.substring(0, dotIndex + 1)
                val content = trimmed.substring(dotIndex + 1).trim()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) defaultColor else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    SelectionContainer {
                        Text(
                            text = parseInlineMarkdown(content, defaultColor),
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
            trimmed.isBlank() -> {
                Spacer(modifier = Modifier.height(3.dp))
            }
            // Normal paragraph
            else -> {
                SelectionContainer {
                    Text(
                        text = parseInlineMarkdown(line, defaultColor),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 21.sp
                    )
                }
            }
        }
    }
}

/**
 * Parses bold (**text**), italics (*text*), and inline code (`code`) into AnnotatedString
 */
private fun parseInlineMarkdown(rawText: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val pattern = Regex("(\\*\\*([^*]+)\\*\\*)|(`([^`]+)`)|(\\*([^*]+)\\*)")
        val matches = pattern.findAll(rawText).toList()

        if (matches.isEmpty()) {
            append(rawText)
            addStyle(SpanStyle(color = defaultColor), 0, rawText.length)
            return@buildAnnotatedString
        }

        matches.forEach { m ->
            if (m.range.first > cursor) {
                val normalText = rawText.substring(cursor, m.range.first)
                val start = length
                append(normalText)
                addStyle(SpanStyle(color = defaultColor), start, length)
            }

            when {
                // Bold: **text**
                m.value.startsWith("**") && m.value.endsWith("**") -> {
                    val boldText = m.value.removeSurrounding("**")
                    val start = length
                    append(boldText)
                    addStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor), start, length)
                }
                // Inline Code: `code`
                m.value.startsWith("`") && m.value.endsWith("`") -> {
                    val codeText = m.value.removeSurrounding("`")
                    val start = length
                    append(codeText)
                    addStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = Primary500,
                            background = Color(0x226366F1)
                        ),
                        start,
                        length
                    )
                }
                // Italic: *text*
                m.value.startsWith("*") && m.value.endsWith("*") -> {
                    val italicText = m.value.removeSurrounding("*")
                    val start = length
                    append(italicText)
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor), start, length)
                }
                else -> {
                    val start = length
                    append(m.value)
                    addStyle(SpanStyle(color = defaultColor), start, length)
                }
            }
            cursor = m.range.last + 1
        }

        if (cursor < rawText.length) {
            val tail = rawText.substring(cursor)
            val start = length
            append(tail)
            addStyle(SpanStyle(color = defaultColor), start, length)
        }
    }
}

/**
 * Dedicated Socratic Hint Card with callout styling
 */
@Composable
fun SocraticHintCard(hintText: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Socratic Hint",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(2.dp))
                SelectionContainer {
                    Text(
                        text = hintText.removePrefix("💡 Socratic Hint:").trim(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

/**
 * Professional Code Block with syntax language chip, copy button, and horizontal scroll
 */
@Composable
fun TutorCodeBlock(
    language: String,
    code: String,
    context: Context
) {
    var isCopied by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131722)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E2235))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                )

                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("code", code)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Code",
                        modifier = Modifier.size(13.dp),
                        tint = if (isCopied) Color(0xFF10B981) else Color(0xFF9E9E9E)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCopied) "Copied!" else "Copy",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (isCopied) Color(0xFF10B981) else Color(0xFF9E9E9E)
                    )
                }
            }

            // Code Content with Horizontal Scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                SelectionContainer {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    )
                }
            }
        }
    }
}
