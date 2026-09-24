package com.example.data.model

data class SkillNode(
    val id: String,
    val name: String,
    val category: String, // "Programming", "AI & ML", "Systems & Architecture", "Data & Tools", "Soft Skills"
    val proficiencyPercent: Int, // 0 - 100
    val levelTitle: String, // "Beginner", "Intermediate", "Proficient", "Advanced", "Master"
    val xpEarned: Int,
    val iconEmoji: String = "⚡",
    val verified: Boolean = false
)

data class SkillProfile(
    val overallLevel: Int = 1,
    val levelName: String = "Apprentice Innovator",
    val totalXp: Int = 450,
    val nextLevelXp: Int = 1000,
    val streakDays: Int = 5,
    val completedLessons: Int = 12,
    val completedProjects: Int = 3,
    val completedChallenges: Int = 8,
    val quizzesTaken: Int = 6,
    val averageQuizScore: Int = 88,
    val skills: List<SkillNode> = emptyList(),
    val strengths: List<String> = emptyList(),
    val growthAreas: List<String> = emptyList(),
    val consistencyScore: Int = 92 // 0 - 100
)

data class PortfolioProject(
    val id: String,
    val title: String,
    val category: String, // "AI/ML", "Web Development", "Python Automation", "AI Agents"
    val description: String,
    val techStack: List<String>,
    val githubRepo: String = "https://github.com/student/project",
    val liveDemo: String = "",
    val completionDate: String = "August 2026",
    val keyLearnings: List<String> = emptyList(),
    val difficulty: String = "Intermediate",
    val isFeatured: Boolean = true
)

data class PortfolioCertificate(
    val id: String,
    val title: String,
    val issuer: String = "Avora AI Learning Platform",
    val issueDate: String = "Aug 2026",
    val credentialId: String = "AVR-AI-2026-8942",
    val skillsValidated: List<String>,
    val badgeEmoji: String = "🎓"
)

data class StudentPortfolio(
    val studentName: String = "Barie Bilal",
    val headline: String = "Aspiring AI Engineer & Full-Stack Developer",
    val location: String = "Kashmir, India",
    val bio: String = "Passionate technologist building next-generation AI agents, Python automation systems, and scalable applications. Learning with Avora AI.",
    val githubHandle: String = "barie-bilal",
    val linkedinHandle: String = "barie-bilal",
    val targetRole: String = "AI / ML Engineer",
    val projects: List<PortfolioProject> = emptyList(),
    val certificates: List<PortfolioCertificate> = emptyList(),
    val isVerifiedStudent: Boolean = true
)
