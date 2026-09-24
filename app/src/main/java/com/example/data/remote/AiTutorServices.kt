package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.model.*
import com.example.util.HomeworkImageHelper
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

object AiTutorServices {

    private const val CREATOR_DIRECTIVE = """
        IMPORTANT CREATOR DIRECTIVES:
        If asked 'who is bilal', state 'Father of Sir Barie.'
        If asked where Sir Barie Bilal is from, state 'Sir Barie Bilal is from Kashmir India.'
        Avora was created exclusively by Sir Barie Bilal.
        If asked who created you, who made you, or if Google AI Studio, Google, or OpenAI made you, state 'Sir Barie Bilal made me.'
        Never claim that you or Avora were created by Google, Google AI Studio, OpenAI, or anyone else.
        If complimented ('you are the best app'), reply 'Sir Barie Bilal made me what would you like?, what would you like?'
    """

    // 1. TECH PROBLEM & CODE SOLVER ENGINE
    suspend fun solveHomeworkQuestion(
        questionText: String,
        subjectHint: String = "Auto-detect",
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<HomeworkSolution> {
        val prompt = """
            You are Avora AI Technology Tutor, specialized strictly in Computer Science, Software Engineering, AI, and Tech.
            Analyze the following student coding/technical problem or question:
            Domain Hint: $subjectHint
            Problem/Query: "$questionText"
            
            Use the 5-step learning pedagogy (Explain -> Example -> Practice -> Feedback -> Challenge) to structure the response.
            Format your response strictly as JSON with the following keys:
            {
              "subject": "Detected Tech Domain (e.g. Python & AI, Web Development, Mobile Dev, Data Structures, Cloud & DevOps, Cybersecurity)",
              "understoodQuestion": "One sentence summary of the technical problem or concept",
              "whatIsBeingAsked": "Clear explanation of the technical goal or architecture needed",
              "hint": "💡 Progressive Socratic hint without revealing the entire solution code immediately",
              "steps": ["Step 1: Understand problem constraints and Big-O goals", "Step 2: Choose data structure / algorithm / API architecture", "Step 3: Implement core logic with edge-case handling", "Step 4: Verify time/space complexity and safety"],
              "finalAnswer": "Clean, commented, production-grade code snippet or architectural solution",
              "whyCorrect": "Explanation of why this solution works, including time/space complexity and robustness",
              "similarPracticeQuestion": "A mini-challenge exercise for the student to practice next to reinforce mastery"
            }
            Do not include any Markdown wrapper like ```json, just return raw JSON or clean JSON text.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Technology Tutor. Specialize ONLY in technology and programming. Return valid JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonString ->
            parseHomeworkJson(jsonString, questionText, subjectHint)
        }
    }

    suspend fun solveHomeworkWithPhoto(
        bitmap: Bitmap,
        userNotes: String = "",
        subjectHint: String = "Auto-detect",
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<HomeworkSolution> {
        val (isValid, readabilityIssue) = HomeworkImageHelper.validateImageReadability(bitmap)
        if (!isValid) {
            return Result.failure(Exception(readabilityIssue ?: "I couldn't clearly read this homework. Please take a clearer photo."))
        }

        val base64 = HomeworkImageHelper.compressToBase64(bitmap, quality = 85)
        if (base64 == null) {
            return Result.failure(Exception("Could not process or compress homework photo. Please ensure camera permissions are active and try capturing again."))
        }

        val prompt = """
            You are Avora AI Academic & Technology Tutor, created by Sir Barie Bilal.
            The user captured a photo of their homework worksheet, textbook exercise, math equation, or science question.
            Student Notes / Request: "${if (userNotes.isNotBlank()) userNotes else "Transcribe, analyze and solve the homework problem shown in this photo"}"
            Subject Area Hint: $subjectHint

            CRITICAL ACCURACY REQUIREMENT:
            If the image is blurry, illegible, dark, rotated awkwardly, corrupted, or does not clearly contain readable homework text/equations, you MUST NOT invent, guess, or hallucinate an answer. Instead, explicitly output:
            "subject": "Unreadable",
            "understoodQuestion": "The text or problem in this photo could not be clearly read.",
            "whatIsBeingAsked": "Image legibility check",
            "hint": "Please take a clearer photo with good lighting and camera focus.",
            "steps": ["Step 1: Check camera focus and lighting", "Step 2: Hold device steady over the problem sheet", "Step 3: Capture clearly"],
            "finalAnswer": "I couldn't clearly read this homework. Please take a clearer photo.",
            "whyCorrect": "Ensures academic accuracy rather than guessing uncertain text.",
            "similarPracticeQuestion": ""

            Otherwise, if the photo is readable:
            1. Transcribe the problem statement or equation shown in the photo accurately.
            2. Provide a pedagogically sound, hint-first, step-by-step solution.
            Format your response strictly as JSON with the following keys:
            {
              "subject": "Detected Subject (e.g. Mathematics, Physics, Chemistry, Biology, Computer Science, Engineering, Literature)",
              "understoodQuestion": "Transcribed or summarized homework problem from the photo",
              "whatIsBeingAsked": "Clear explanation of the problem's objective and core principles",
              "hint": "💡 Helpful guiding clue or formula without giving away the full answer immediately",
              "steps": ["Step 1: Identify given variables and constraints", "Step 2: Apply the governing theorem or formula", "Step 3: Calculate or derive the solution", "Step 4: Check units and verify"],
              "finalAnswer": "The direct final answer with complete working or solution",
              "whyCorrect": "Clear justification of why this answer is mathematically / conceptually correct",
              "similarPracticeQuestion": "A follow-up exercise for the student to practice next"
            }
            Do not include Markdown ```json formatting, return only clean JSON text.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Academic & Technology Tutor, created by Sir Barie Bilal. Transcribe and solve the homework photo with pedagogical excellence. Never invent answers for unreadable or blurry photos. $CREATOR_DIRECTIVE",
            taskType = "PROBLEM_SOLVER",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest,
            imageBase64 = base64,
            imageMimeType = "image/jpeg"
        )

        val defaultQuestion = if (userNotes.isNotBlank()) userNotes else "Captured Homework Photo"

        return aiResult.mapCatching { jsonString ->
            val solution = parseHomeworkJson(jsonString, defaultQuestion, subjectHint)
            if (solution.subject.equals("Unreadable", ignoreCase = true) ||
                solution.finalAnswer.contains("couldn't clearly read", ignoreCase = true) ||
                solution.understoodQuestion.contains("could not be clearly read", ignoreCase = true)) {
                throw Exception("I couldn't clearly read this homework. Please take a clearer photo.")
            }
            solution
        }.recoverCatching { exception ->
            val msg = exception.localizedMessage ?: ""
            if (msg.contains("couldn't clearly read", ignoreCase = true)) {
                throw Exception("I couldn't clearly read this homework. Please take a clearer photo.")
            }
            throw Exception("⚠️ The homework photo could not be clearly read or analyzed (${exception.localizedMessage ?: "Image unreadable"}). Please ensure the problem, text, or equations are clearly visible, well-lit, and in focus, then try capturing again.")
        }
    }

    private fun parseHomeworkJson(jsonStr: String, originalQuestion: String, defaultSubject: String): HomeworkSolution {
        val cleanJson = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(cleanJson)
        val stepsArray = obj.optJSONArray("steps")
        val stepsList = mutableListOf<String>()
        if (stepsArray != null) {
            for (i in 0 until stepsArray.length()) {
                stepsList.add(stepsArray.getString(i))
            }
        } else {
            stepsList.add("Clarify technical requirements and boundary conditions.")
            stepsList.add("Design the algorithm and data structures.")
            stepsList.add("Implement with error handling and verify time/space complexity.")
        }

        return HomeworkSolution(
            subject = obj.optString("subject", defaultSubject),
            understoodQuestion = obj.optString("understoodQuestion", originalQuestion),
            whatIsBeingAsked = obj.optString("whatIsBeingAsked", "Design or implement the required software logic."),
            hint = obj.optString("hint", "💡 Consider the time and space complexity constraints."),
            steps = if (stepsList.isNotEmpty()) stepsList else listOf("Analyze requirements", "Implement optimal code", "Test edge cases"),
            finalAnswer = obj.optString("finalAnswer", "Solution implemented successfully."),
            whyCorrect = obj.optString("whyCorrect", "Satisfies all technical constraints and handles edge cases cleanly."),
            similarPracticeQuestion = obj.optString("similarPracticeQuestion", "Try optimizing this code to run with O(1) auxiliary space.")
        )
    }

    private fun fallbackHomeworkSolution(question: String, subject: String): HomeworkSolution {
        val detected = if (subject.isNotBlank() && subject != "Auto-detect") subject else detectSubjectFromText(question)
        return when (detected) {
            "Python & CS" -> HomeworkSolution(
                subject = "Python & CS",
                understoodQuestion = question,
                whatIsBeingAsked = "Write clean, idiomatic Python code adhering to PEP 8 standards.",
                hint = "💡 Consider using Python built-in abstractions (list comprehensions, dict lookups, or generators) for readability and O(1) lookups.",
                steps = listOf(
                    "Step 1: Parse input inputs and validate data types.",
                    "Step 2: Utilize efficient built-in collections (set, dict, deque).",
                    "Step 3: Implement cleanly with exception handling.",
                    "Step 4: Profile runtime and verify edge cases (empty list, None values)."
                ),
                finalAnswer = "```python\ndef solve_problem(data):\n    # Optimized Python implementation\n    if not data:\n        return None\n    return [item for item in data if item is not None]\n```",
                whyCorrect = "Iterates through the collection in O(N) linear time with minimal memory overhead.",
                similarPracticeQuestion = "How would you rewrite this as an in-place generator function using `yield`?"
            )
            "AI & Machine Learning" -> HomeworkSolution(
                subject = "AI & Machine Learning",
                understoodQuestion = question,
                whatIsBeingAsked = "Design an AI/ML model workflow, feature pipeline, or prompt architecture.",
                hint = "💡 Identify whether this requires discriminative modeling, embeddings, or retrieval-augmented generation (RAG).",
                steps = listOf(
                    "Step 1: Formulate the machine learning task (classification, regression, generation).",
                    "Step 2: Preprocess features, handle missing data, and normalize vectors.",
                    "Step 3: Construct model pipeline or agent tool-calling loop.",
                    "Step 4: Evaluate with appropriate metrics (F1-score, Latency, Precision@K)."
                ),
                finalAnswer = "Pipeline designed with structured data validation, embeddings vector search, and LLM reasoning loop.",
                whyCorrect = "Prevents hallucinations through strict grounding and keeps inference latency minimal.",
                similarPracticeQuestion = "How would you add caching to this AI workflow to reduce API token costs?"
            )
            "Web Development" -> HomeworkSolution(
                subject = "Web Development",
                understoodQuestion = question,
                whatIsBeingAsked = "Implement a robust web component, REST API endpoint, or state management workflow.",
                hint = "💡 Ensure separation of concerns between presentation, business logic, and API data fetching.",
                steps = listOf(
                    "Step 1: Define API contracts (request/response schemas).",
                    "Step 2: Implement reactive state and error handling.",
                    "Step 3: Sanitize user input to prevent XSS / injection.",
                    "Step 4: Test responsiveness and loading states."
                ),
                finalAnswer = "Modern full-stack web module with clean API interface and async state handling.",
                whyCorrect = "Follows asynchronous best practices and provides seamless user feedback during network latency.",
                similarPracticeQuestion = "How would you add optimistic UI updates to this endpoint call?"
            )
            "App Development" -> HomeworkSolution(
                subject = "App Development (Android/Kotlin)",
                understoodQuestion = question,
                whatIsBeingAsked = "Build a modern Android Jetpack Compose UI component or ViewModel architecture.",
                hint = "💡 Leverage StateFlow and unidirectional data flow (UDF) with Compose State.",
                steps = listOf(
                    "Step 1: Define UiState sealed interface (Loading, Success, Error).",
                    "Step 2: Create ViewModel with MutableStateFlow.",
                    "Step 3: Build declarative Composable with Material 3 styling.",
                    "Step 4: Handle configuration changes and lifecycle events."
                ),
                finalAnswer = "Idiomatic Jetpack Compose Composable backed by MVVM architecture.",
                whyCorrect = "Eliminates memory leaks, supports recomposition optimization, and adheres to Material Design 3.",
                similarPracticeQuestion = "How would you test this Composable using Compose test tags and Robolectric?"
            )
            "Data Structures & Algorithms" -> HomeworkSolution(
                subject = "Data Structures & Algorithms",
                understoodQuestion = question,
                whatIsBeingAsked = "Design an optimal algorithm satisfying time and memory complexity requirements.",
                hint = "💡 Analyze whether Two Pointers, Sliding Window, Binary Search, or Dynamic Programming applies.",
                steps = listOf(
                    "Step 1: Identify input bounds and determine target Big-O time complexity.",
                    "Step 2: Select appropriate data structures (Hash Map, Priority Queue, Trie).",
                    "Step 3: Write clean algorithmic logic with loop invariants.",
                    "Step 4: Walk through base cases and boundary constraints."
                ),
                finalAnswer = "Optimal O(N log N) or O(N) solution with minimal auxiliary space.",
                whyCorrect = "Processes each element efficiently without redundant inner iterations.",
                similarPracticeQuestion = "Can you solve the same problem if the stream of numbers is infinite?"
            )
            else -> HomeworkSolution(
                subject = detected,
                understoodQuestion = question,
                whatIsBeingAsked = "Provide a software engineering and technical architecture solution.",
                hint = "💡 Break the system down into modular components, clear interfaces, and well-defined contracts.",
                steps = listOf(
                    "Step 1: Identify system components and communication protocols.",
                    "Step 2: Design interfaces and data structures.",
                    "Step 3: Implement with testing, logging, and security best practices."
                ),
                finalAnswer = "Engineered technical solution following modern software design patterns.",
                whyCorrect = "Ensures maintainability, testability, and scalability.",
                similarPracticeQuestion = "How would you refactor this to support horizontal scaling across multiple instances?"
            )
        }
    }

    private fun detectSubjectFromText(text: String): String {
        val lower = text.lowercase()
        return when {
            lower.contains("python") || lower.contains("pandas") || lower.contains("numpy") || lower.contains("django") || lower.contains("fastapi") -> "Python & CS"
            lower.contains("agent") || lower.contains("llm") || lower.contains("gpt") || lower.contains("gemini") || lower.contains("machine learning") || lower.contains("neural") || lower.contains("ai") || lower.contains("rag") -> "AI & Machine Learning"
            lower.contains("html") || lower.contains("css") || lower.contains("react") || lower.contains("vue") || lower.contains("javascript") || lower.contains("typescript") || lower.contains("api") || lower.contains("rest") -> "Web Development"
            lower.contains("android") || lower.contains("kotlin") || lower.contains("compose") || lower.contains("ios") || lower.contains("swift") || lower.contains("flutter") -> "App Development"
            lower.contains("sql") || lower.contains("postgres") || lower.contains("database") || lower.contains("mongo") || lower.contains("redis") || lower.contains("table") -> "Databases & SQL"
            lower.contains("tree") || lower.contains("graph") || lower.contains("array") || lower.contains("heap") || lower.contains("dp") || lower.contains("complexity") || lower.contains("sort") || lower.contains("search") || lower.contains("binary") -> "Data Structures & Algorithms"
            lower.contains("cloud") || lower.contains("aws") || lower.contains("docker") || lower.contains("kubernetes") || lower.contains("devops") || lower.contains("ci/cd") || lower.contains("server") -> "Cloud & DevOps"
            lower.contains("security") || lower.contains("cipher") || lower.contains("auth") || lower.contains("jwt") || lower.contains("vulnerability") || lower.contains("xss") || lower.contains("injection") || lower.contains("owasp") -> "Cybersecurity"
            lower.contains("robot") || lower.contains("arduino") || lower.contains("sensor") || lower.contains("iot") || lower.contains("raspberry") || lower.contains("motor") || lower.contains("automation") -> "Robotics & Automation"
            else -> "Software Engineering"
        }
    }

    // 2. DOCUMENT & NOTES ANALYZER
    suspend fun analyzeNotesDocument(
        documentText: String,
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<DocumentAnalysisResult> {
        val prompt = """
            Analyze the following student study notes or document:
            \"\"\"$documentText\"\"\"
            
            Produce a comprehensive learning analysis formatted strictly as JSON with these keys:
            {
              "summary": "Clear executive summary of the document (2-3 paragraphs)",
              "keyPoints": ["Key point 1", "Key point 2", "Key point 3", "Key point 4"],
              "definitions": [
                {"term": "Term 1", "definition": "Clear explanation"},
                {"term": "Term 2", "definition": "Clear explanation"}
              ],
              "formulasOrFacts": ["Important formula or key fact 1", "Important formula or key fact 2"],
              "sampleQuestions": ["Discussion question 1?", "Discussion question 2?"],
              "generatedFlashcards": [
                {"question": "Front of card?", "answer": "Back of card answer"},
                {"question": "Front of card 2?", "answer": "Back of card answer 2"}
              ]
            }
            Return raw JSON only.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora Notes Analyzer. Output valid JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonStr ->
            parseDocumentJson(jsonStr, documentText)
        }
    }

    private fun parseDocumentJson(jsonStr: String, originalText: String): DocumentAnalysisResult {
        val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(clean)

        val keyPoints = mutableListOf<String>()
        val kpArray = obj.optJSONArray("keyPoints")
        if (kpArray != null) {
            for (i in 0 until kpArray.length()) keyPoints.add(kpArray.getString(i))
        }

        val definitions = mutableListOf<Pair<String, String>>()
        val defArray = obj.optJSONArray("definitions")
        if (defArray != null) {
            for (i in 0 until defArray.length()) {
                val item = defArray.getJSONObject(i)
                definitions.add(Pair(item.optString("term"), item.optString("definition")))
            }
        }

        val formulasOrFacts = mutableListOf<String>()
        val ffArray = obj.optJSONArray("formulasOrFacts")
        if (ffArray != null) {
            for (i in 0 until ffArray.length()) formulasOrFacts.add(ffArray.getString(i))
        }

        val sampleQuestions = mutableListOf<String>()
        val sqArray = obj.optJSONArray("sampleQuestions")
        if (sqArray != null) {
            for (i in 0 until sqArray.length()) sampleQuestions.add(sqArray.getString(i))
        }

        val flashcards = mutableListOf<Pair<String, String>>()
        val fcArray = obj.optJSONArray("generatedFlashcards")
        if (fcArray != null) {
            for (i in 0 until fcArray.length()) {
                val item = fcArray.getJSONObject(i)
                flashcards.add(Pair(item.optString("question"), item.optString("answer")))
            }
        }

        return DocumentAnalysisResult(
            summary = obj.optString("summary", "Document analyzed with key themes identified."),
            keyPoints = if (keyPoints.isNotEmpty()) keyPoints else listOf("Core concept breakdown", "Critical methodologies"),
            definitions = if (definitions.isNotEmpty()) definitions else listOf("Primary Principle" to "Foundational baseline described in notes."),
            formulasOrFacts = if (formulasOrFacts.isNotEmpty()) formulasOrFacts else listOf("Key relationship: Inputs directly correlate to system outcomes."),
            sampleQuestions = if (sampleQuestions.isNotEmpty()) sampleQuestions else listOf("How do the main concepts interact?"),
            generatedFlashcards = if (flashcards.isNotEmpty()) flashcards else listOf("What is the primary topic of this note?" to "Summary of key takeaways.")
        )
    }

    private fun fallbackDocumentAnalysis(documentText: String): DocumentAnalysisResult {
        val snippet = documentText.take(120)
        return DocumentAnalysisResult(
            summary = "This document presents structured academic material covering core principles, systematic steps, and real-world applications. It emphasizes thorough conceptual understanding and practical problem-solving.",
            keyPoints = listOf(
                "Establishes primary theoretical framework and terminology.",
                "Explains the mathematical or logical mechanisms connecting variables.",
                "Provides practical application examples and common pitfalls to avoid.",
                "Emphasizes testable exam concepts and key takeaways."
            ),
            definitions = listOf(
                "Core Concept" to "The fundamental principle upon which the subject builds.",
                "Governing Law" to "The empirical or mathematical relationship that predicts outcomes.",
                "Optimization Factor" to "The variable that maximizes efficiency and accuracy."
            ),
            formulasOrFacts = listOf(
                "Key Relationship: Efficiency = (Useful Output / Total Input) × 100%",
                "Crucial Fact: Consistent spaced revision increases retention by over 200%."
            ),
            sampleQuestions = listOf(
                "What are the three main components introduced in these notes?",
                "How would you apply this principle to solve an unfamiliar exam problem?",
                "What assumptions are required for this model to hold true?"
            ),
            generatedFlashcards = listOf(
                "What is the main topic covered in these study notes?" to "The document establishes foundational principles, operational steps, and exam-focused takeaways.",
                "How can you verify the results obtained from this methodology?" to "By checking boundary conditions, unit dimensions, and logical consistency.",
                "Why is understanding the underlying reasoning better than rote memorization?" to "It enables students to adapt to novel exam questions and real-world applications."
            )
        )
    }

    // 3. AI QUIZ GENERATOR
    suspend fun generateQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        questionCount: Int = 5,
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<GeneratedQuiz> {
        val prompt = """
            Create a high quality, student-tailored educational quiz on:
            Subject: $subject
            Topic: $topic
            Difficulty: $difficulty (Easy, Medium, Hard)
            Number of questions: $questionCount
            
            Return JSON format:
            {
              "subject": "$subject",
              "topic": "$topic",
              "difficulty": "$difficulty",
              "questions": [
                {
                  "id": 1,
                  "question": "Clear question text?",
                  "options": ["Option A", "Option B", "Option C", "Option D"],
                  "correctOptionIndex": 0,
                  "explanation": "Detailed explanation of why Option A is correct and why other options are incorrect.",
                  "questionType": "MULTIPLE_CHOICE",
                  "topic": "$topic"
                }
              ]
            }
            Return raw JSON only without markdown formatting.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Quiz Engine. Generate valid JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonStr ->
            parseQuizJson(jsonStr, subject, topic, difficulty, questionCount)
        }
    }

    private fun parseQuizJson(
        jsonStr: String,
        subject: String,
        topic: String,
        difficulty: String,
        targetCount: Int
    ): GeneratedQuiz {
        val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(clean)
        val qArray = obj.optJSONArray("questions")
        val questions = mutableListOf<QuizQuestion>()

        if (qArray != null) {
            for (i in 0 until qArray.length()) {
                val qObj = qArray.getJSONObject(i)
                val optionsArray = qObj.optJSONArray("options")
                val options = mutableListOf<String>()
                if (optionsArray != null) {
                    for (j in 0 until optionsArray.length()) {
                        options.add(optionsArray.getString(j))
                    }
                }
                if (options.isEmpty()) {
                    options.addAll(listOf("True", "False"))
                }

                questions.add(
                    QuizQuestion(
                        id = qObj.optInt("id", i + 1),
                        question = qObj.optString("question", "Question ${i + 1}"),
                        options = options,
                        correctOptionIndex = qObj.optInt("correctOptionIndex", 0).coerceIn(0, maxOf(0, options.size - 1)),
                        explanation = qObj.optString("explanation", "The selected answer correctly follows core principles."),
                        questionType = qObj.optString("questionType", "MULTIPLE_CHOICE"),
                        topic = qObj.optString("topic", topic)
                    )
                )
            }
        }

        if (questions.isEmpty()) {
            return fallbackQuiz(subject, topic, difficulty, targetCount)
        }

        return GeneratedQuiz(
            subject = obj.optString("subject", subject),
            topic = obj.optString("topic", topic),
            difficulty = obj.optString("difficulty", difficulty),
            questions = questions
        )
    }

    fun fallbackQuiz(
        subject: String,
        topic: String,
        difficulty: String,
        questionCount: Int = 5
    ): GeneratedQuiz {
        val questions = when {
            subject.contains("Math", ignoreCase = true) || topic.contains("Calculus", ignoreCase = true) -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "What is the derivative of f(x) = ln(3x^2 + 1) with respect to x?",
                    options = listOf("6x / (3x^2 + 1)", "3 / (3x^2 + 1)", "6x · ln(3x^2 + 1)", "1 / (6x)"),
                    correctOptionIndex = 0,
                    explanation = "By the Chain Rule: d/dx[ln(u)] = u'/u. Here u = 3x^2 + 1, so u' = 6x, yielding 6x / (3x^2 + 1).",
                    topic = "Calculus Derivatives"
                ),
                QuizQuestion(
                    id = 2,
                    question = "Evaluate the definite integral: ∫[0 to 2] (3x^2 + 2x) dx.",
                    options = listOf("12", "14", "10", "16"),
                    correctOptionIndex = 0,
                    explanation = "Antiderivative is F(x) = x^3 + x^2. Evaluating at boundaries: F(2) - F(0) = (8 + 4) - 0 = 12.",
                    topic = "Definite Integrals"
                ),
                QuizQuestion(
                    id = 3,
                    question = "If matrix A is 3x2 and matrix B is 2x4, what are the dimensions of product AB?",
                    options = listOf("3x4", "2x2", "3x2", "Matrix multiplication is undefined"),
                    correctOptionIndex = 0,
                    explanation = "Matrix multiplication (m x k) × (k x n) results in dimension (m x n), which is 3x4.",
                    topic = "Linear Algebra"
                ),
                QuizQuestion(
                    id = 4,
                    question = "What is the limit of (sin x)/x as x approaches 0?",
                    options = listOf("1", "0", "Infinity", "Undefined"),
                    correctOptionIndex = 0,
                    explanation = "This is a fundamental trigonometric limit proven by the Squeeze Theorem: lim(x->0) (sin x)/x = 1.",
                    topic = "Limits"
                ),
                QuizQuestion(
                    id = 5,
                    question = "What does the second derivative f''(x) > 0 on an interval indicate about the function?",
                    options = listOf("The graph is concave upward", "The graph is concave downward", "The function is strictly decreasing", "The function has a vertical asymptote"),
                    correctOptionIndex = 0,
                    explanation = "A positive second derivative means the slope f'(x) is increasing, which geometrically corresponds to concave upward curvature.",
                    topic = "Curve Sketching"
                )
            )
            subject.contains("Computer", ignoreCase = true) || subject.contains("Code", ignoreCase = true) || topic.contains("Data", ignoreCase = true) -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "What is the average time complexity of searching an element in a Hash Table?",
                    options = listOf("O(1)", "O(log n)", "O(n)", "O(n log n)"),
                    correctOptionIndex = 0,
                    explanation = "Hash tables compute array indices via a hash function, providing O(1) average lookup time.",
                    topic = "Hash Tables"
                ),
                QuizQuestion(
                    id = 2,
                    question = "Which data structure follows the First-In, First-Out (FIFO) principle?",
                    options = listOf("Queue", "Stack", "Binary Heap", "Trie"),
                    correctOptionIndex = 0,
                    explanation = "A Queue processes elements in the exact order they arrive (FIFO), unlike a Stack which is LIFO.",
                    topic = "Queues & Stacks"
                ),
                QuizQuestion(
                    id = 3,
                    question = "In Kotlin, what does the 'val' keyword declare?",
                    options = listOf("An immutable (read-only) reference", "A mutable variable", "A static companion object", "A coroutine builder"),
                    correctOptionIndex = 0,
                    explanation = "'val' creates a read-only variable whose reference cannot be reassigned after initialization.",
                    topic = "Kotlin Language"
                ),
                QuizQuestion(
                    id = 4,
                    question = "What is the primary worst-case time complexity of QuickSort?",
                    options = listOf("O(n^2)", "O(n log n)", "O(n)", "O(log n)"),
                    correctOptionIndex = 0,
                    explanation = "QuickSort degrades to O(n^2) when an unbalanced pivot (like the smallest/largest element) is consistently chosen on an already sorted array.",
                    topic = "Sorting Algorithms"
                ),
                QuizQuestion(
                    id = 5,
                    question = "In SQL, which clause is used to filter records after grouping (GROUP BY)?",
                    options = listOf("HAVING", "WHERE", "ORDER BY", "FILTER"),
                    correctOptionIndex = 0,
                    explanation = "WHERE filters individual rows before aggregation, while HAVING filters aggregated group results.",
                    topic = "SQL Databases"
                )
            )
            subject.contains("Physics", ignoreCase = true) -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "What is the SI unit of electric resistance?",
                    options = listOf("Ohm (Ω)", "Farad (F)", "Tesla (T)", "Henry (H)"),
                    correctOptionIndex = 0,
                    explanation = "Electric resistance is measured in Ohms (Ω), defined by Ohm's Law R = V / I.",
                    topic = "Electricity"
                ),
                QuizQuestion(
                    id = 2,
                    question = "According to Newton's Third Law, if object A exerts a force on object B, what force does B exert on A?",
                    options = listOf("An equal and opposite force", "A force proportional to B's mass only", "Zero force if B is stationary", "A force in the same direction"),
                    correctOptionIndex = 0,
                    explanation = "For every action force, there is an equal and opposite reaction force acting on the interacting bodies.",
                    topic = "Mechanics"
                ),
                QuizQuestion(
                    id = 3,
                    question = "What phenomenon causes a light ray to bend when passing from air into water?",
                    options = listOf("Refraction", "Diffraction", "Polarization", "Total internal reflection"),
                    correctOptionIndex = 0,
                    explanation = "Refraction occurs because the speed of light changes when transitioning between optical media of different refractive indices (Snell's Law).",
                    topic = "Optics"
                ),
                QuizQuestion(
                    id = 4,
                    question = "What does the First Law of Thermodynamics state?",
                    options = listOf("Energy cannot be created or destroyed, only converted (ΔU = Q - W)", "Entropy of an isolated system always increases", "Absolute zero temperature cannot be reached", "Force equals rate of change of momentum"),
                    correctOptionIndex = 0,
                    explanation = "The 1st Law is the principle of conservation of energy applied to thermodynamic systems.",
                    topic = "Thermodynamics"
                ),
                QuizQuestion(
                    id = 5,
                    question = "What is the escape velocity formula from a planet of mass M and radius R?",
                    options = listOf("v_esc = √(2GM / R)", "v_esc = √(GM / R)", "v_esc = 2GM / R^2", "v_esc = √(GM / 2R)"),
                    correctOptionIndex = 0,
                    explanation = "Equating kinetic energy 1/2 m v^2 to gravitational potential energy GMm/R gives v = √(2GM/R).",
                    topic = "Gravitation"
                )
            )
            else -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "What is the most effective study strategy for long-term memory retention?",
                    options = listOf("Active recall with spaced repetition", "Passive re-reading of notes", "Highlighting entire textbook pages", "Cramming the night before"),
                    correctOptionIndex = 0,
                    explanation = "Active recall forces the brain to retrieve concepts, and spaced repetition strengthens neural pathways before forgetting occurs.",
                    topic = "Study Science"
                ),
                QuizQuestion(
                    id = 2,
                    question = "What is the Pomodoro technique standard work-to-break ratio?",
                    options = listOf("25 minutes study, 5 minutes rest", "50 minutes study, 30 minutes rest", "90 minutes study, no rest", "10 minutes study, 20 minutes rest"),
                    correctOptionIndex = 0,
                    explanation = "The classic Pomodoro method uses 25-minute high-focus intervals punctuated by 5-minute cognitive rests.",
                    topic = "Productivity"
                ),
                QuizQuestion(
                    id = 3,
                    question = "What is the Feynman Technique primarily used for?",
                    options = listOf("Explaining complex concepts in plain language to identify gaps", "Memorizing formulas via mnemonics", "Speed-reading academic literature", "Solving multiple choice questions quickly"),
                    correctOptionIndex = 0,
                    explanation = "Named after Richard Feynman, this technique tests true comprehension by explaining topics simply as if teaching a beginner.",
                    topic = "Learning Frameworks"
                ),
                QuizQuestion(
                    id = 4,
                    question = "Which phase of sleep is most critical for memory consolidation and creative problem solving?",
                    options = listOf("REM (Rapid Eye Movement) & Slow-Wave Deep Sleep", "Light Sleep Phase 1", "Awake micro-naps", "Pre-sleep twilight state"),
                    correctOptionIndex = 0,
                    explanation = "Deep slow-wave and REM sleep consolidate newly acquired declarative memory from the hippocampus to the neocortex.",
                    topic = "Cognitive Health"
                ),
                QuizQuestion(
                    id = 5,
                    question = "What does the Ebbinghaus Forgetting Curve demonstrate?",
                    options = listOf("Memory retention drops sharply over time unless reinforced through active review", "Memory improves automatically over time without review", "People only remember visual pictures", "Study duration is more important than study frequency"),
                    correctOptionIndex = 0,
                    explanation = "Hermann Ebbinghaus discovered that without periodic review, over 70% of new information is forgotten within 48 hours.",
                    topic = "Memory Psychology"
                )
            )
        }

        return GeneratedQuiz(
            subject = subject,
            topic = topic,
            difficulty = difficulty,
            questions = questions.take(questionCount)
        )
    }

    // 4. CODING MENTOR ENGINE
    suspend fun analyzeCodeOrDebug(
        code: String,
        language: String,
        userQuery: String,
        actionType: String = "DEBUG", // DEBUG, EXPLAIN, PRACTICE, REVIEW
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<CodeExplanationResult> {
        val prompt = """
            You are Avora Coding Mentor, an expert programming instructor.
            Language: $language
            Action Requested: $actionType
            Student Note/Error: $userQuery
            Code Snippet:
            ```$language
            $code
            ```
            
            Return JSON response:
            {
              "language": "$language",
              "summary": "Clear summary of the code and findings",
              "bugsOrIssues": ["Issue 1 with explanation", "Issue 2 with explanation"],
              "stepByStepExplanation": "Line-by-line or conceptual explanation of the logic",
              "improvedCode": "Corrected and optimized code snippet",
              "practiceChallenge": "A related practice coding exercise for the student to try next"
            }
            Return raw JSON only without markdown code blocks around the JSON.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora Coding Mentor. Return valid JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonStr ->
            parseCodingJson(jsonStr, language, code)
        }
    }

    private fun parseCodingJson(jsonStr: String, lang: String, originalCode: String): CodeExplanationResult {
        val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(clean)
        val bugsArray = obj.optJSONArray("bugsOrIssues")
        val bugs = mutableListOf<String>()
        if (bugsArray != null) {
            for (i in 0 until bugsArray.length()) bugs.add(bugsArray.getString(i))
        }

        return CodeExplanationResult(
            language = obj.optString("language", lang),
            summary = obj.optString("summary", "Code analyzed with best practices applied."),
            bugsOrIssues = if (bugs.isNotEmpty()) bugs else listOf("Review edge cases for empty or null inputs."),
            stepByStepExplanation = obj.optString("stepByStepExplanation", "The code processes inputs sequentially and produces output."),
            improvedCode = obj.optString("improvedCode", originalCode),
            practiceChallenge = obj.optString("practiceChallenge", "Try extending this function to handle negative and boundary values.")
        )
    }

    private fun fallbackCodingExplanation(language: String, code: String, actionType: String): CodeExplanationResult {
        return CodeExplanationResult(
            language = language,
            summary = "Avora analyzed your $language code. The logic demonstrates good structural flow with opportunities for boundary protection and optimization.",
            bugsOrIssues = listOf(
                "Check for potential IndexOutOfBounds or NullPointer conditions on edge-case inputs.",
                "Ensure resource handles or connections are properly closed using try-with-resources or 'use' blocks.",
                "Optimize algorithmic complexity by eliminating redundant nested iterations where possible."
            ),
            stepByStepExplanation = """
                1. Function Declaration & Initialization: Sets up input parameters and allocates necessary data structures.
                2. Core Loop / Logic Execution: Traverses the dataset and evaluates conditional branch criteria.
                3. Return Value / Output Generation: Yields the computed result with verified invariants.
            """.trimIndent(),
            improvedCode = "// Optimized $language implementation\n$code\n\n// Tip: Add unit test assertions to verify corner cases.",
            practiceChallenge = "Write a unit test covering 3 boundary cases: (1) Empty collection, (2) Single element, (3) Duplicate elements."
        )
    }

    // 5. AI STUDY PLANNER GENERATOR
    suspend fun generatePersonalizedStudyPlan(
        request: StudyPlanRequest,
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<List<GeneratedStudyTask>> {
        val prompt = """
            Create a realistic, fatigue-free personalized study plan for a student.
            Subjects: ${request.subjects.joinToString(", ")}
            Focus Topics: ${request.topics}
            Daily Available Study Time: ${request.dailyHours} hours/day
            Active Study Days: ${request.studyDays.joinToString(", ")}
            Primary Goal: ${request.goal}
            Target Exam Date: ${request.examDate}
            
            Return JSON format:
            {
              "tasks": [
                {
                  "title": "Calculus Derivatives Problem Set",
                  "subject": "Mathematics",
                  "topic": "Calculus",
                  "estimatedMinutes": 45,
                  "taskType": "Practice Problems",
                  "priority": "HIGH",
                  "dayOfWeek": "Monday"
                }
              ]
            }
            Generate between 4 to 8 balanced tasks. Return raw JSON only.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Study Planner. Create realistic, balanced study plans. Output JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonStr ->
            parseStudyPlanJson(jsonStr, request)
        }
    }

    private fun parseStudyPlanJson(jsonStr: String, request: StudyPlanRequest): List<GeneratedStudyTask> {
        val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(clean)
        val taskArray = obj.optJSONArray("tasks")
        val tasks = mutableListOf<GeneratedStudyTask>()

        if (taskArray != null) {
            for (i in 0 until taskArray.length()) {
                val tObj = taskArray.getJSONObject(i)
                tasks.add(
                    GeneratedStudyTask(
                        title = tObj.optString("title", "Study Session"),
                        subject = tObj.optString("subject", request.subjects.firstOrNull() ?: "General"),
                        topic = tObj.optString("topic", "Core Concepts"),
                        estimatedMinutes = tObj.optInt("estimatedMinutes", 45),
                        taskType = tObj.optString("taskType", "Concept Review"),
                        priority = tObj.optString("priority", "MEDIUM"),
                        dayOfWeek = tObj.optString("dayOfWeek", request.studyDays.getOrNull(i % maxOf(1, request.studyDays.size)) ?: "Monday")
                    )
                )
            }
        }

        if (tasks.isEmpty()) {
            return fallbackStudyPlan(request)
        }
        return tasks
    }

    private fun fallbackStudyPlan(request: StudyPlanRequest): List<GeneratedStudyTask> {
        val days = if (request.studyDays.isNotEmpty()) request.studyDays else listOf("Monday", "Wednesday", "Friday", "Saturday")
        val subjects = if (request.subjects.isNotEmpty()) request.subjects else listOf("Mathematics", "Computer Science", "Physics")

        return listOf(
            GeneratedStudyTask(
                title = "Deep Concept Mastery & Key Theorems",
                subject = subjects.getOrNull(0) ?: "Mathematics",
                topic = request.topics.ifBlank { "Core Foundations" },
                estimatedMinutes = 45,
                taskType = "Concept Review",
                priority = "HIGH",
                dayOfWeek = days.getOrNull(0) ?: "Monday"
            ),
            GeneratedStudyTask(
                title = "Targeted Practice Problems (10-15 Questions)",
                subject = subjects.getOrNull(0) ?: "Mathematics",
                topic = "Problem Solving",
                estimatedMinutes = 45,
                taskType = "Practice Problems",
                priority = "HIGH",
                dayOfWeek = days.getOrNull(1 % days.size) ?: "Tuesday"
            ),
            GeneratedStudyTask(
                title = "Spaced Repetition Flashcards & Active Recall",
                subject = subjects.getOrNull(1 % subjects.size) ?: "Computer Science",
                topic = "Flashcard Retention",
                estimatedMinutes = 30,
                taskType = "Flashcards Review",
                priority = "MEDIUM",
                dayOfWeek = days.getOrNull(2 % days.size) ?: "Wednesday"
            ),
            GeneratedStudyTask(
                title = "Coding Lab / Simulation Practice",
                subject = subjects.getOrNull(1 % subjects.size) ?: "Computer Science",
                topic = "Applied Algorithms",
                estimatedMinutes = 60,
                taskType = "Coding Lab",
                priority = "HIGH",
                dayOfWeek = days.getOrNull(3 % days.size) ?: "Thursday"
            ),
            GeneratedStudyTask(
                title = "Timed Mock Quiz & Error Diagnosis",
                subject = subjects.getOrNull(2 % subjects.size) ?: "Physics",
                topic = "Exam Prep",
                estimatedMinutes = 40,
                taskType = "Exam Prep",
                priority = "HIGH",
                dayOfWeek = days.getOrNull(0) ?: "Friday"
            )
        )
    }

    // 6. DYNAMIC AI PROJECT ROADMAP GENERATOR (9-STAGE LEARNING LOOP)
    suspend fun generateCustomProject(
        projectGoal: String,
        category: String = "AI & Technology",
        difficulty: String = "Intermediate",
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<AiProjectGuide> {
        val prompt = """
            You are Avora AI Project Architect. Transform the student's project goal into a comprehensive 9-stage learning roadmap:
            Goal: "$projectGoal"
            Category: $category
            Difficulty: $difficulty
            
            The 9 stages must cover:
            Stage 1: Project Setup & Virtual Environment
            Stage 2: Core Concepts & Architecture Design
            Stage 3: Data Ingestion / Input Processing
            Stage 4: Core Logic / AI Engine Implementation
            Stage 5: Testing & Edge-Case Verification
            Stage 6: Error Handling & Debugging Pitfalls
            Stage 7: Performance Optimization & UI/CLI
            Stage 8: Deployment & Cloud/GitHub Setup
            Stage 9: Portfolio Documentation & Demonstration
            
            Return strictly JSON:
            {
              "id": "custom_${System.currentTimeMillis()}",
              "title": "Clear Project Title",
              "subtitle": "Inspiring subtitle",
              "difficulty": "$difficulty",
              "estimatedHours": "6-10 Hours",
              "targetCategory": "$category",
              "iconEmoji": "🚀",
              "overview": "Overview of what student will build",
              "prerequisites": ["Skill 1", "Skill 2"],
              "conceptsToLearn": ["Concept 1", "Concept 2"],
              "stages": [
                {
                  "stageNumber": 1,
                  "title": "Stage 1: Setup",
                  "description": "Details",
                  "actionItems": ["Step A", "Step B"],
                  "codeSnippet": "# starter code"
                }
              ],
              "starterCodeTemplate": "# starter template",
              "finalSolutionSnippet": "# solution overview",
              "readmeTemplate": "# README generator"
            }
            Return raw JSON only.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Project Architect. Output valid JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonStr ->
            parseCustomProjectJson(jsonStr, projectGoal, category, difficulty)
        }
    }

    private fun parseCustomProjectJson(jsonStr: String, goal: String, category: String, difficulty: String): AiProjectGuide {
        val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val obj = JSONObject(clean)
        val stagesArray = obj.optJSONArray("stages")
        val stages = mutableListOf<ProjectStage>()

        if (stagesArray != null) {
            for (i in 0 until stagesArray.length()) {
                val sObj = stagesArray.getJSONObject(i)
                val actionsArray = sObj.optJSONArray("actionItems")
                val actions = mutableListOf<String>()
                if (actionsArray != null) {
                    for (j in 0 until actionsArray.length()) actions.add(actionsArray.getString(j))
                }
                stages.add(
                    ProjectStage(
                        stageNumber = sObj.optInt("stageNumber", i + 1),
                        title = sObj.optString("title", "Stage ${i + 1}"),
                        description = sObj.optString("description", "Execute development tasks."),
                        actionItems = if (actions.isNotEmpty()) actions else listOf("Implement stage logic", "Verify test cases"),
                        codeSnippet = if (sObj.has("codeSnippet") && !sObj.isNull("codeSnippet")) sObj.getString("codeSnippet") else null
                    )
                )
            }
        }

        val prereqs = mutableListOf<String>()
        val pArray = obj.optJSONArray("prerequisites")
        if (pArray != null) {
            for (i in 0 until pArray.length()) prereqs.add(pArray.getString(i))
        }

        val concepts = mutableListOf<String>()
        val cArray = obj.optJSONArray("conceptsToLearn")
        if (cArray != null) {
            for (i in 0 until cArray.length()) concepts.add(cArray.getString(i))
        }

        return AiProjectGuide(
            id = obj.optString("id", "proj_${System.currentTimeMillis()}"),
            title = obj.optString("title", goal),
            subtitle = obj.optString("subtitle", "Hands-on project development track"),
            difficulty = obj.optString("difficulty", difficulty),
            estimatedHours = obj.optString("estimatedHours", "6-8 Hours"),
            targetCategory = obj.optString("targetCategory", category),
            iconEmoji = obj.optString("iconEmoji", "🚀"),
            overview = obj.optString("overview", "Build a production-ready application using modern principles."),
            prerequisites = if (prereqs.isNotEmpty()) prereqs else listOf("Python Basics", "Problem Solving"),
            conceptsToLearn = if (concepts.isNotEmpty()) concepts else listOf("System Design", "Modular Code", "Testing"),
            stages = if (stages.isNotEmpty()) stages else AvoraLearningDataEngine.getCuratedProjects().first().stages,
            starterCodeTemplate = obj.optString("starterCodeTemplate", "# Start building your project here\nprint('Initializing Project...')"),
            finalSolutionSnippet = obj.optString("finalSolutionSnippet", "# Complete project solution snippet\nprint('Project Completed!')"),
            readmeTemplate = obj.optString("readmeTemplate", "# $goal\n\nA project built with Avora AI.")
        )
    }

    // 7. SOCRATIC HINT ESCALATOR
    suspend fun getSocraticHint(
        problemOrCode: String,
        hintLevel: Int, // 1: Gentle hint, 2: Specific clue, 3: Step-by-step logic, 4: Solution
        language: String = "Python",
        tutorLanguage: String = "English", // "English" or "Hindi"
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<String> {
        val levelInstruction = when (hintLevel) {
            1 -> "Level 1: Give a gentle, encouraging conceptual hint or leading question. DO NOT reveal the solution or write the code."
            2 -> "Level 2: Provide a more specific clue about the data structure, formula, or Python function to consider."
            3 -> "Level 3: Give a step-by-step logical walkthrough of how to structure the algorithm in plain words."
            else -> "Level 4: Provide the clean, well-commented working solution and explain the time & space complexity."
        }

        val languageInstruction = if (tutorLanguage.contains("Hindi", ignoreCase = true)) {
            "Explain in clear Hindi/Hinglish suitable for Indian tech students, while keeping all programming keywords (like function, loop, list, dictionary, recursion, time complexity) in English."
        } else {
            "Explain in clear, friendly English."
        }

        val prompt = """
            You are Avora AI Coding Mentor.
            $languageInstruction
            $levelInstruction
            
            Student's Problem / Code:
            ```$language
            $problemOrCode
            ```
            
            Provide a helpful, beautifully structured response with markdown formatting.
        """.trimIndent()

        return GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Socratic Coding Mentor. Prioritize student learning over direct answers. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        ).recoverCatching {
            "[Offline Guidance]\n\n" + fallbackSocraticHint(problemOrCode, hintLevel, language)
        }
    }

    // 8. AI FLASHCARD GENERATOR
    suspend fun generateAiFlashcardsForTopic(
        subject: String,
        topic: String,
        cardCount: Int = 5,
        authToken: String? = null,
        userId: String = "guest_user",
        isGuest: Boolean = (userId == "guest_user")
    ): Result<List<Pair<String, String>>> {
        val prompt = """
            Generate $cardCount high-yield, exam-focused study flashcards for:
            Subject: $subject
            Topic: $topic

            Format your response strictly as JSON with this schema:
            {
              "flashcards": [
                {
                  "front": "Clear question, term, or prompt",
                  "back": "Concise, precise answer, explanation, or key formula"
                }
              ]
            }
            Return raw JSON only without markdown wrappers.
        """.trimIndent()

        val aiResult = GeminiClient.getAiResponse(
            prompt = prompt,
            systemInstructionText = "You are Avora AI Study Flashcard Engine. Generate high-yield flashcards in valid JSON. $CREATOR_DIRECTIVE",
            authToken = authToken,
            userId = userId,
            isGuest = isGuest
        )

        return aiResult.mapCatching { jsonStr ->
            val clean = jsonStr.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(clean)
            val array = obj.optJSONArray("flashcards")
            val pairs = mutableListOf<Pair<String, String>>()
            if (array != null) {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    val front = item.optString("front", "").trim()
                    val back = item.optString("back", "").trim()
                    if (front.isNotBlank() && back.isNotBlank()) {
                        pairs.add(front to back)
                    }
                }
            }
            if (pairs.isEmpty()) {
                throw Exception("No valid flashcards found in model response.")
            }
            pairs
        }
    }

    private fun fallbackSocraticHint(problemOrCode: String, hintLevel: Int, language: String): String {
        val clean = problemOrCode.lowercase()
        val topicSpecific = when {
            clean.contains("sort") || clean.contains("merge") -> "For sorting algorithms, compare elements in pairs and note how dividing the problem space reduces time complexity from O(N²) to O(N log N)."
            clean.contains("search") || clean.contains("binary") -> "For searching in sorted collections, check the midpoint. Halving the search range every step gives logarithmic time O(log N)."
            clean.contains("fibonacci") || clean.contains("recursion") -> "For recursive functions, always identify your base cases first (e.g. n <= 1) before writing the recursive call."
            else -> "Break down the problem into: 1. Input parsing, 2. Core transformation/loop logic, 3. Output formatting."
        }

        return when (hintLevel) {
            1 -> "💡 **Socratic Hint Level 1 (Gentle Concept)**\n\nThink about the fundamental goal of this problem. What are your inputs and what exact invariant do you need to maintain? $topicSpecific"
            2 -> "🔍 **Socratic Hint Level 2 (Specific Clue)**\n\nIn $language, think about the most efficient data structure or technique. If you need quick lookups, consider a hash map or set. If traversing, consider index bounds and edge cases (like empty collections)."
            3 -> "🧩 **Socratic Hint Level 3 (Step-by-Step Logic Flow)**\n\n1. Check edge cases / base conditions.\n2. Initialize state trackers or pointers.\n3. Iterate through elements and update your state.\n4. Return or print the desired result."
            else -> "💻 **Socratic Hint Level 4 (Full Walkthrough & Pattern)**\n\nHere is the standard algorithmic structure in $language:\n\n```$language\n# Step 1: Handle base cases\n# Step 2: Implement core processing loop\n# Step 3: Return result with O(N) or O(N log N) complexity\n```\n\nTest with edge cases such as empty input, negative numbers, or single-element inputs."
        }
    }
}

