package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class SubjectInfo(
    val name: String,
    val emoji: String,
    val icon: ImageVector,
    val color: Color,
    val lightBgColor: Color,
    val description: String
)

object SubjectCategoryManager {
    val PRESET_SUBJECTS = listOf(
        SubjectInfo(
            name = "Mathematics",
            emoji = "📐",
            icon = Icons.Default.Calculate,
            color = Color(0xFF4F46E5), // Indigo
            lightBgColor = Color(0xFFEEF2FF),
            description = "Calculus, Algebra, Geometry, Statistics"
        ),
        SubjectInfo(
            name = "Science",
            emoji = "🔬",
            icon = Icons.Default.Science,
            color = Color(0xFF059669), // Emerald
            lightBgColor = Color(0xFFECFDF5),
            description = "Physics, Chemistry, Biology, Lab Work"
        ),
        SubjectInfo(
            name = "Literature",
            emoji = "📚",
            icon = Icons.Default.MenuBook,
            color = Color(0xFF7C3AED), // Violet
            lightBgColor = Color(0xFFF5F3FF),
            description = "English, Reading, Essays, Poetry"
        ),
        SubjectInfo(
            name = "Computer Science",
            emoji = "💻",
            icon = Icons.Default.Code,
            color = Color(0xFF0891B2), // Cyan
            lightBgColor = Color(0xFFECFEFF),
            description = "Programming, Algorithms, Data Structures"
        ),
        SubjectInfo(
            name = "History",
            emoji = "🏛️",
            icon = Icons.Default.Public,
            color = Color(0xFFD97706), // Amber
            lightBgColor = Color(0xFFFFFBEB),
            description = "World History, Civics, Social Studies"
        ),
        SubjectInfo(
            name = "Languages",
            emoji = "🗣️",
            icon = Icons.Default.Translate,
            color = Color(0xFFDB2777), // Pink
            lightBgColor = Color(0xFFFDF2F8),
            description = "Spanish, French, Japanese, Linguistics"
        ),
        SubjectInfo(
            name = "Art & Design",
            emoji = "🎨",
            icon = Icons.Default.Palette,
            color = Color(0xFFE11D48), // Rose
            lightBgColor = Color(0xFFFFF1F2),
            description = "Drawing, Graphic Design, Art History"
        ),
        SubjectInfo(
            name = "General",
            emoji = "📌",
            icon = Icons.Default.School,
            color = Color(0xFF475569), // Slate
            lightBgColor = Color(0xFFF1F5F9),
            description = "General Study, Projects, Homework"
        )
    )

    fun getSubjectInfo(name: String): SubjectInfo {
        val trimmed = name.trim()
        val exact = PRESET_SUBJECTS.find { it.name.equals(trimmed, ignoreCase = true) }
        if (exact != null) return exact

        // Fuzzy matches
        val lower = trimmed.lowercase()
        return when {
            lower.contains("math") || lower.contains("calc") || lower.contains("algeb") || lower.contains("stat") ->
                PRESET_SUBJECTS[0]
            lower.contains("physic") || lower.contains("chem") || lower.contains("bio") || lower.contains("sci") || lower.contains("lab") ->
                PRESET_SUBJECTS[1]
            lower.contains("liter") || lower.contains("eng") || lower.contains("essay") || lower.contains("read") || lower.contains("book") ->
                PRESET_SUBJECTS[2]
            lower.contains("comp") || lower.contains("code") || lower.contains("program") || lower.contains("cs") || lower.contains("software") ->
                PRESET_SUBJECTS[3]
            lower.contains("hist") || lower.contains("soc") || lower.contains("gov") || lower.contains("civic") ->
                PRESET_SUBJECTS[4]
            lower.contains("lang") || lower.contains("span") || lower.contains("french") || lower.contains("germ") ->
                PRESET_SUBJECTS[5]
            lower.contains("art") || lower.contains("design") || lower.contains("draw") || lower.contains("music") ->
                PRESET_SUBJECTS[6]
            else -> SubjectInfo(
                name = trimmed.ifBlank { "General" },
                emoji = "📖",
                icon = Icons.Default.School,
                color = Color(0xFF6366F1),
                lightBgColor = Color(0xFFEEF2FF),
                description = trimmed
            )
        }
    }
}
