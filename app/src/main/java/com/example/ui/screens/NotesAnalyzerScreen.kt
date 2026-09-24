package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.document.DocumentParserEngine
import com.example.data.document.ImportedDocument
import com.example.data.model.DocumentAnalysisResult
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Primary500

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAnalyzerScreen(
    analysisResult: DocumentAnalysisResult?,
    isAnalyzing: Boolean,
    importedDocument: ImportedDocument? = null,
    isImportingDocument: Boolean = false,
    onImportDocument: (Uri) -> Unit = {},
    onClearImportedDocument: () -> Unit = {},
    onAnalyzeDocument: (String) -> Unit,
    onSaveFlashcards: (String) -> Unit,
    onStartDocumentQuiz: () -> Unit
) {
    var notesText by remember {
        mutableStateOf(
            """Cell Division and Mitosis Overview:
Mitosis is a process where a single cell divides into two identical daughter cells. The major phases are:
1. Prophase: Chromatin condenses into chromosomes, and the nuclear envelope begins to break down.
2. Metaphase: Chromosomes align along the equatorial metaphase plate of the cell. Spindle fibers attach to the centromeres.
3. Anaphase: Sister chromatids are pulled apart by spindle fibers toward opposite poles of the cell.
4. Telophase and Cytokinesis: Nuclear envelopes reform around each set of chromosomes, and the cytoplasm divides into two daughter cells.
Key Definition: Cytokinesis is the physical process of cell division which divides the cytoplasm of a parental cell into two daughter cells.
Key Formula: Total cells produced = N_0 * 2^n, where N_0 is initial cells and n is division cycles."""
        )
    }

    var documentSubject by remember { mutableStateOf("Biology") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Sync input text when a real document is imported
    LaunchedEffect(importedDocument) {
        if (importedDocument != null && importedDocument.textContent.isNotBlank()) {
            notesText = importedDocument.textContent
            documentSubject = when {
                importedDocument.fileName.contains("bio", ignoreCase = true) -> "Biology"
                importedDocument.fileName.contains("cs", ignoreCase = true) ||
                importedDocument.fileName.contains("code", ignoreCase = true) ||
                importedDocument.fileName.contains("algo", ignoreCase = true) -> "Computer Science"
                importedDocument.fileName.contains("phys", ignoreCase = true) -> "Physics"
                importedDocument.fileName.contains("chem", ignoreCase = true) -> "Chemistry"
                importedDocument.fileName.contains("math", ignoreCase = true) -> "Mathematics"
                else -> importedDocument.fileName.substringBeforeLast(".").take(15)
            }
        }
    }

    // Real Android Storage Access Framework File Picker Launcher
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onImportDocument(uri)
        }
    }

    val sampleNotesList = listOf(
        Pair("🧬 Biology: Mitosis", "Mitosis is a process where a single cell divides into two identical daughter cells. Key phases: Prophase (condensation), Metaphase (equatorial alignment), Anaphase (chromatid separation), Telophase & Cytokinesis. Total cells = N0 * 2^n."),
        Pair("⚙️ CS: Virtual Memory", "Virtual memory maps virtual addresses to physical RAM using page tables and TLBs. A Page Fault occurs when the requested page is in swap space. Page replacement algorithms include LRU, FIFO, and Clock."),
        Pair("⚡ Physics: Thermodynamics", "The First Law states delta U = Q - W (Energy Conservation). The Second Law states the total entropy of an isolated system never decreases over time. Carnot efficiency: eta = 1 - (Tc / Th)."),
        Pair("🧠 AI: Deep Learning", "Deep learning utilizes multi-layer artificial neural networks. Backpropagation calculates the gradient of the loss function with respect to weights using the chain rule. Activation functions include ReLU, Sigmoid, and GELU.")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header & File Import Card
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📑 PDF & Document AI Studio",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Import real PDF, Word DOCX, or text files to extract summaries, flashcards, formulas & quizzes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real File Import Area
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                width = 1.5.dp,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Primary500.copy(alpha = 0.6f),
                                        CyanAccent.copy(alpha = 0.6f)
                                    )
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(enabled = !isImportingDocument) {
                                documentPickerLauncher.launch(
                                    arrayOf(
                                        "application/pdf",
                                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                        "application/msword",
                                        "text/plain",
                                        "text/markdown",
                                        "text/csv",
                                        "application/json",
                                        "*/*"
                                    )
                                )
                            }
                            .testTag("import_document_button"),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isImportingDocument) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = CyanAccent,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Extracting document streams & parsing pages...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = "Import PDF or Document",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to Import PDF / Word / Notes Document",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Supports .pdf, .docx, .txt, .md, .csv files directly from your device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Supported format badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FormatBadge("PDF", Color(0xFFE53935))
                                    FormatBadge("DOCX", Color(0xFF1E88E5))
                                    FormatBadge("TXT", Color(0xFF43A047))
                                    FormatBadge("MARKDOWN", Color(0xFF8E24AA))
                                }
                            }
                        }
                    }

                    // Active Imported Document Card
                    if (importedDocument != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = CyanAccent.copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (importedDocument.fileName.endsWith(".pdf", true)) Color(0xFFE53935) else Primary500,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = if (importedDocument.fileName.endsWith(".pdf", true)) Icons.Filled.PictureAsPdf else Icons.Filled.Description,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = importedDocument.fileName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = DocumentParserEngine.formatFileSize(importedDocument.fileSizeBytes),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${importedDocument.pageCount} page(s)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = CyanAccent,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(
                                                text = "${importedDocument.wordCount} words",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = onClearImportedDocument,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear Document",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Text Input / Extracted Content Editor
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (importedDocument != null) "📄 Extracted Document Content:" else "✏️ Document Content / Notes:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (notesText.isNotBlank()) {
                            Text(
                                text = "${notesText.length} chars",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Paste lecture notes, textbook chapters, or import a PDF above...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp, max = 260.dp)
                            .testTag("notes_input_field"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pre-loaded Quick Samples
                    Text(
                        text = "Or try pre-loaded curriculum samples:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(sampleNotesList) { (title, content) ->
                            OutlinedButton(
                                onClick = {
                                    notesText = content
                                    documentSubject = title.split(":").firstOrNull()?.replace(Regex("[^a-zA-Z]"), "") ?: "General"
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text(title, fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Analyze Action Button
                    Button(
                        onClick = {
                            if (notesText.isNotBlank()) {
                                onAnalyzeDocument(notesText)
                            } else {
                                Toast.makeText(context, "Please enter or import notes text first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = notesText.isNotBlank() && !isAnalyzing && !isImportingDocument,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyze_notes_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing Document & Synthesizing Deck...")
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (importedDocument != null) "Analyze '${importedDocument.fileName}'" else "Analyze Document & Extract Insights")
                        }
                    }
                }
            }
        }

        // Analysis Results Section
        if (analysisResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Quick Action Buttons (Save Flashcards & Take Quiz)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    onSaveFlashcards(documentSubject)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Style, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save Cards (${analysisResult.generatedFlashcards.size})", fontSize = 12.sp)
                            }

                            Button(
                                onClick = onStartDocumentQuiz,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Filled.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Take Notes Quiz", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Executive Summary Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 Executive Summary:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(analysisResult.summary))
                                    Toast.makeText(context, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = "Copy Summary",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        SelectionContainer {
                            Text(
                                text = analysisResult.summary,
                                style = MaterialTheme.typography.bodyMedium,
                                lineHeight = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Key Takeaways
                        Text(
                            text = "🔑 Key Takeaways:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        analysisResult.keyPoints.forEach { point ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = "• $point",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        // Important Definitions
                        if (analysisResult.definitions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "📖 Key Definitions:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            analysisResult.definitions.forEach { (term, def) ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = term,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = def,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }

                        // Formulas and Facts
                        if (analysisResult.formulasOrFacts.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "📐 Formulas & Key Facts:",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            analysisResult.formulasOrFacts.forEach { item ->
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "⚡ $item",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }
                        }

                        // Flashcards Preview List
                        if (analysisResult.generatedFlashcards.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "🎴 Extracted Flashcard Pairs (${analysisResult.generatedFlashcards.size}):",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            analysisResult.generatedFlashcards.take(6).forEachIndexed { idx, pair ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "Q: ${pair.first}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "A: ${pair.second}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormatBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
