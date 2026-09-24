package com.example.data.document

import android.content.Context
import android.database.Cursor
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.ZipInputStream

data class ImportedDocument(
    val uriString: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val mimeType: String,
    val textContent: String,
    val pageCount: Int = 1,
    val wordCount: Int = 0,
    val extractedAt: Long = System.currentTimeMillis()
)

object DocumentParserEngine {

    suspend fun parseDocument(context: Context, uri: Uri): Result<ImportedDocument> = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val (fileName, fileSize) = queryFileInfo(context, uri)
            val mimeType = contentResolver.getType(uri) ?: getMimeTypeFromExtension(fileName)

            val inputStream = contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open stream for selected document."))

            val (extractedText, pageCount) = when {
                fileName.endsWith(".pdf", ignoreCase = true) || mimeType == "application/pdf" -> {
                    parsePdfDocument(context, uri, inputStream)
                }
                fileName.endsWith(".docx", ignoreCase = true) ||
                mimeType.contains("wordprocessingml") -> {
                    Pair(parseDocxStream(inputStream), 1)
                }
                else -> {
                    val rawText = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    Pair(rawText, 1)
                }
            }

            val cleanedText = sanitizeAndCleanText(extractedText)
            val wordCount = cleanedText.split(Regex("\\s+")).count { it.isNotBlank() }

            Result.success(
                ImportedDocument(
                    uriString = uri.toString(),
                    fileName = fileName,
                    fileSizeBytes = fileSize,
                    mimeType = mimeType,
                    textContent = cleanedText,
                    pageCount = maxOf(1, pageCount),
                    wordCount = wordCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryFileInfo(context: Context, uri: Uri): Pair<String, Long> {
        var name = "Imported_Document"
        var size = 0L
        try {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex) ?: name
                    }
                    if (sizeIndex != -1) {
                        size = it.getLong(sizeIndex)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("DocumentParserEngine", "Failed to query document metadata for $uri", e)
        }

        if (name == "Imported_Document") {
            uri.lastPathSegment?.let { name = it }
        }
        return Pair(name, size)
    }

    private fun getMimeTypeFromExtension(fileName: String): String {
        return when {
            fileName.endsWith(".pdf", ignoreCase = true) -> "application/pdf"
            fileName.endsWith(".docx", ignoreCase = true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            fileName.endsWith(".txt", ignoreCase = true) -> "text/plain"
            fileName.endsWith(".md", ignoreCase = true) -> "text/markdown"
            fileName.endsWith(".csv", ignoreCase = true) -> "text/csv"
            fileName.endsWith(".json", ignoreCase = true) -> "application/json"
            else -> "text/plain"
        }
    }

    private fun parsePdfDocument(context: Context, uri: Uri, inputStream: InputStream): Pair<String, Int> {
        val tempPdfFile = File(context.cacheDir, "temp_import_${System.currentTimeMillis()}.pdf")
        var pageCount = 1
        try {
            tempPdfFile.outputStream().use { out ->
                inputStream.copyTo(out)
            }

            // 1. Get exact page count from native PdfRenderer if possible
            try {
                val pfd = ParcelFileDescriptor.open(tempPdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
                renderer.close()
                pfd.close()
            } catch (e: Exception) {
                Log.w("DocumentParserEngine", "Could not determine exact PDF page count via PdfRenderer", e)
            }

            // 2. Extract textual streams from PDF bytes
            val pdfBytes = tempPdfFile.readBytes()
            val extractedText = extractPdfTextFromBytes(pdfBytes, pageCount)
            return Pair(extractedText, pageCount)
        } finally {
            try {
                if (tempPdfFile.exists()) {
                    tempPdfFile.delete()
                }
            } catch (e: Exception) {
                Log.w("DocumentParserEngine", "Could not delete temporary PDF file", e)
            }
        }
    }

    private fun extractPdfTextFromBytes(bytes: ByteArray, estimatedPages: Int): String {
        val textBuilder = StringBuilder()
        val rawContent = String(bytes, Charsets.ISO_8859_1)

        // Parse streams
        val streamRegex = Regex("stream\\r?\\n([\\s\\S]*?)\\r?\\nendstream")
        val streamMatches = streamRegex.findAll(rawContent)

        var pageIndex = 1
        for (match in streamMatches) {
            val streamBody = match.groupValues[1]
            val decompressed = tryDecompressStream(streamBody.toByteArray(Charsets.ISO_8859_1))
            val textContent = decompressed ?: streamBody

            val pageText = extractTextOperators(textContent)
            if (pageText.isNotBlank()) {
                if (estimatedPages > 1) {
                    textBuilder.append("--- [Page $pageIndex] ---\n")
                }
                textBuilder.append(pageText).append("\n\n")
                pageIndex++
            }
        }

        // Fallback if compressed stream extraction yielded minimal text
        if (textBuilder.length < 50) {
            val fallbackMatches = Regex("\\(([\\w\\s.,;:!?'\"()\\-\\/+*%=<>\\[\\]{}]+)\\)\\s*Tj").findAll(rawContent)
            val fallbackLines = fallbackMatches.map { it.groupValues[1] }.joinToString(" ")
            if (fallbackLines.isNotBlank()) {
                return fallbackLines
            }

            // General string block extraction
            val generalStrings = Regex("\\(([^)]{3,})\\)").findAll(rawContent)
                .map { decodePdfEscapes(it.groupValues[1]) }
                .filter { it.length > 3 && it.any { c -> c.isLetter() } }
                .joinToString(" ")

            if (generalStrings.isNotBlank()) {
                return generalStrings
            }
        }

        return if (textBuilder.isNotBlank()) textBuilder.toString() else "PDF document imported ($estimatedPages pages). Content contains binary media, diagrams, or scanned pages."
    }

    private fun tryDecompressStream(data: ByteArray): String? {
        return try {
            val inflater = Inflater(false)
            inflater.setInput(data)
            val outputStream = ByteArrayOutputStream(data.size * 2)
            val buffer = ByteArray(1024)
            while (!inflater.finished()) {
                val count = inflater.inflate(buffer)
                if (count == 0 && inflater.needsInput()) break
                outputStream.write(buffer, 0, count)
            }
            inflater.end()
            outputStream.toString("UTF-8")
        } catch (_: Exception) {
            null
        }
    }

    private fun extractTextOperators(streamContent: String): String {
        val pageBuilder = StringBuilder()

        // 1. Tj operators: (text) Tj
        val tjRegex = Regex("\\((.*?)\\)\\s*Tj")
        for (m in tjRegex.findAll(streamContent)) {
            val decoded = decodePdfEscapes(m.groupValues[1])
            pageBuilder.append(decoded).append(" ")
        }

        // 2. TJ array operators: [(t) 20 (e) 10 (x) (t)] TJ
        val tjArrayRegex = Regex("\\[(.*?)\\]\\s*TJ")
        for (m in tjArrayRegex.findAll(streamContent)) {
            val arrayContent = m.groupValues[1]
            val innerMatches = Regex("\\((.*?)\\)").findAll(arrayContent)
            for (im in innerMatches) {
                pageBuilder.append(decodePdfEscapes(im.groupValues[1]))
            }
            pageBuilder.append(" ")
        }

        // 3. Single quote ' operator (T*)
        val quoteRegex = Regex("\\((.*?)\\)\\s*'")
        for (m in quoteRegex.findAll(streamContent)) {
            pageBuilder.append("\n").append(decodePdfEscapes(m.groupValues[1]))
        }

        return pageBuilder.toString().trim()
    }

    private fun decodePdfEscapes(text: String): String {
        return text
            .replace("\\n", "\n")
            .replace("\\r", "\r")
            .replace("\\t", "\t")
            .replace("\\b", "\b")
            .replace("\\f", "\u000C")
            .replace("\\(", "(")
            .replace("\\)", ")")
            .replace("\\\\", "\\")
    }

    private fun parseDocxStream(inputStream: InputStream): String {
        val stringBuilder = StringBuilder()
        ZipInputStream(inputStream).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val xmlContent = zip.bufferedReader(Charsets.UTF_8).readText()
                    // Extract text between <w:t>...</w:t> tags
                    val textTags = Regex("<w:t[^>]*>(.*?)</w:t>")
                    val paragraphTags = Regex("<w:p[ >](.*?)</w:p>")

                    for (pMatch in paragraphTags.findAll(xmlContent)) {
                        val pContent = pMatch.groupValues[1]
                        val pText = textTags.findAll(pContent).joinToString("") { it.groupValues[1] }
                        if (pText.isNotBlank()) {
                            stringBuilder.append(pText).append("\n")
                        }
                    }
                    break
                }
                entry = zip.nextEntry
            }
        }
        return if (stringBuilder.isNotBlank()) stringBuilder.toString() else "Word document imported successfully."
    }

    private fun sanitizeAndCleanText(text: String): String {
        return text
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")
            .replace(Regex("[ \\t]+"), " ")
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Unknown size"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.1f KB", kb)
            else -> "$bytes B"
        }
    }
}
