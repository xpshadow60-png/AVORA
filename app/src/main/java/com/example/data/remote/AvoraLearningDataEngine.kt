package com.example.data.remote

import com.example.data.model.*

object AvoraLearningDataEngine {

    // 1. CURATED 9-STAGE AI PROJECTS
    fun getCuratedProjects(): List<AiProjectGuide> = listOf(
        AiProjectGuide(
            id = "proj_ai_chatbot",
            title = "Build an AI Study Chatbot with Sentiment & RAG",
            subtitle = "Design a context-aware assistant for students with question memory",
            difficulty = "Intermediate",
            estimatedHours = "6-8 Hours",
            targetCategory = "AI & Chatbots",
            iconEmoji = "🤖",
            overview = "Create a complete Python-based intelligent conversational agent that parses student queries, classifies intent, retains conversation memory, and answers academic questions.",
            prerequisites = listOf("Python Basics (Functions, Dictionaries)", "REST APIs & JSON", "Basic Prompt Engineering"),
            conceptsToLearn = listOf("Tokenization & Context Windows", "Vector Embeddings & Cosine Similarity", "System Prompts & Guardrails", "Error Handling in AI Calls"),
            stages = listOf(
                ProjectStage(1, "Phase 1: Project Setup & Virtual Environment", "Initialize your Python project workspace, install requirements (requests, python-dotenv), and configure secure API keys in `.env`.", listOf("Create project directory `ai_study_tutor/`", "Create `requirements.txt`", "Setup environment variables securely"), "import os\nfrom dotenv import load_dotenv\n\nload_dotenv()\nAPI_KEY = os.getenv('AI_API_KEY')"),
                ProjectStage(2, "Phase 2: Conversation State & Memory Buffer", "Design a sliding memory buffer that stores the last N turns of user and assistant messages to preserve context.", listOf("Define `ChatMessage` class", "Implement `SlidingWindowMemory` with max 10 messages", "Format history for API consumption"), "class Message:\n    def __init__(self, role, text):\n        self.role = role\n        self.text = text"),
                ProjectStage(3, "Phase 3: Intent Classification & Guardrails", "Write a pre-filter function that detects whether a question is academic, coding-related, or off-topic.", listOf("Implement keyword & regex classifier", "Add polite refusal guardrails for off-topic queries"), "def classify_intent(query):\n    keywords = ['code', 'solve', 'explain', 'why', 'how']\n    return 'ACADEMIC' if any(k in query.lower() for k in keywords) else 'GENERAL'"),
                ProjectStage(4, "Phase 4: AI API Integration & Prompt Engineering", "Construct a structured system prompt directing the AI to act as a supportive Socratic coach.", listOf("Define system persona prompt", "Invoke API with timeout & retry logic", "Parse JSON or text response"), "def call_ai_tutor(prompt, history):\n    # Send formatted payload to AI API\n    pass"),
                ProjectStage(5, "Phase 5: Socratic Hint Escalator", "Ensure the chatbot gives step-by-step hints rather than dumping direct answers.", listOf("Implement hint level parameter (1 to 3)", "Enforce step-by-step breakdowns for math & code"), "def get_socratic_hint(problem, hint_level=1):\n    return f'Hint {hint_level}: Focus on the core formula...'"),
                ProjectStage(6, "Phase 6: Testing with Edge Cases", "Run automated test cases against tricky queries, empty inputs, network timeouts, and multilingual inputs (English + Hindi).", listOf("Test empty input handling", "Test token limit truncation", "Validate Hinglish comprehension")),
                ProjectStage(7, "Phase 7: Interactive CLI or Web Interface", "Wrap your chatbot logic into an interactive terminal CLI loop or lightweight FastAPI / Streamlit web interface.", listOf("Build continuous conversation while loop", "Add exit keywords ('quit', 'exit')", "Display pretty colored output")),
                ProjectStage(8, "Phase 8: Deployment & Cloud Hosting", "Containerize your app with Docker and deploy to a free cloud host or GitHub repository.", listOf("Write `Dockerfile`", "Create `.gitignore` excluding `.env`", "Push to GitHub")),
                ProjectStage(9, "Phase 9: Portfolio Documentation & Demonstration", "Write a professional `README.md` with system architecture diagrams, demo GIFs, and key engineering takeaways.", listOf("Generate project summary", "Add installation instructions", "Add to Avora Student Portfolio"))
            ),
            starterCodeTemplate = """
# Project: AI Study Chatbot
# Phase 1: Context-Aware Socratic AI Learning Architecture

class StudyChatbot:
    def __init__(self, bot_name="Avora Assistant"):
        self.bot_name = bot_name
        self.history = []
        self.socratic_knowledge_base = {
            "merge sort": [
                "Hint 1: Think about Divide-and-Conquer. How many times can you divide an array of size N in half?",
                "Hint 2: At each level of division, how much work is performed to merge the two sorted halves back together?",
                "Hint 3: The recursion tree has depth log2(N), and each level does O(N) comparisons.",
                "Hint 4: Solution: Time complexity is O(N log N) across worst, average, and best cases!"
            ],
            "binary search": [
                "Hint 1: What is the primary prerequisite for binary search? The elements must be sorted.",
                "Hint 2: Compare target with middle element. Which half can you discard immediately?",
                "Hint 3: Search space halves at each iteration: N -> N/2 -> N/4 -> ... -> 1.",
                "Hint 4: Solution: Time complexity is O(log N) and iterative space complexity is O(1)."
            ]
        }

    def generate_socratic_hint(self, topic, hint_level=1):
        clean_topic = topic.lower()
        for key, hints in self.socratic_knowledge_base.items():
            if key in clean_topic:
                idx = min(max(hint_level - 1, 0), len(hints) - 1)
                return hints[idx]
        return f"Hint Level {hint_level}: Break down '{topic}' into input state, transformation logic, and expected output."

    def send_message(self, user_input, hint_level=1):
        self.history.append({"role": "user", "content": user_input})
        hint = self.generate_socratic_hint(user_input, hint_level)
        reply = f"{self.bot_name}: {hint}"
        self.history.append({"role": "assistant", "content": reply})
        return reply

bot = StudyChatbot()
print(bot.send_message("How do I find time complexity of merge sort?", hint_level=1))
print(bot.send_message("How do I find time complexity of merge sort?", hint_level=4))
            """.trimIndent(),
            finalSolutionSnippet = """
# Completed AI Chatbot Architecture
import os

class ProductionStudyChatbot:
    def __init__(self):
        self.history = []
        self.system_prompt = "You are a patient Socratic AI tutor. Never give answers immediately."

    def ask(self, query, hint_level=1):
        self.history.append({"role": "user", "content": query})
        response = f"Hint Level {hint_level}: Break the problem into sub-tasks and inspect recursion depth."
        self.history.append({"role": "assistant", "content": response})
        return response

bot = ProductionStudyChatbot()
print("Chatbot Initialized Successfully!")
            """.trimIndent(),
            readmeTemplate = "# AI Study Chatbot\n\nA context-aware Socratic AI learning companion built in Python."
        ),

        AiProjectGuide(
            id = "proj_algo_visualizer",
            title = "Build an Algorithm & Sorting Visualizer in Python",
            subtitle = "Implement sorting, graph search, and complexity benchmarks",
            difficulty = "Beginner",
            estimatedHours = "4-5 Hours",
            targetCategory = "Python & Data",
            iconEmoji = "📊",
            overview = "Build an interactive algorithm benchmarking and visualization suite comparing Bubble Sort, Merge Sort, Quick Sort, and Binary Search.",
            prerequisites = listOf("Python Lists & Loops", "Time Complexity Big-O basics"),
            conceptsToLearn = listOf("Divide and Conquer", "Recursion", "In-Place Swapping", "Performance Profiling"),
            stages = listOf(
                ProjectStage(1, "Phase 1: Project Setup & Test Arrays", "Create test data generators for random, sorted, and reversed integer arrays.", listOf("Generate random arrays with `random.randint`", "Define timer utility")),
                ProjectStage(2, "Phase 2: Bubble & Insertion Sort", "Implement baseline quadratic O(N^2) sorting algorithms.", listOf("Write bubble sort with swap flag", "Write insertion sort")),
                ProjectStage(3, "Phase 3: Merge Sort & Divide-and-Conquer", "Implement O(N log N) Merge Sort recursively.", listOf("Write `merge()` helper", "Write recursive `merge_sort()`")),
                ProjectStage(4, "Phase 4: Quick Sort with Partitioning", "Implement Quick Sort with Lomuto or Hoare partitioning.", listOf("Choose pivot strategy", "Implement in-place swaps")),
                ProjectStage(5, "Phase 5: Binary Search & Search Benchmark", "Implement recursive and iterative Binary Search on sorted data.", listOf("Binary search implementation", "Verify O(log N) lookups")),
                ProjectStage(6, "Phase 6: Benchmarking & Profiling Engine", "Benchmark execution time across input sizes: N=100, 1000, 10000.", listOf("Measure runtime with `time.perf_counter()`", "Format markdown table")),
                ProjectStage(7, "Phase 7: ASCII Terminal Visualizer", "Render bar charts in the terminal (e.g. `|||||| 6`, `|||||||||| 10`).", listOf("Create visual step printer")),
                ProjectStage(8, "Phase 8: Unit Testing & Verification", "Write automated assertions checking sorted invariant on 100 random arrays.", listOf("Verify `sorted(arr) == my_sort(arr)`")),
                ProjectStage(9, "Phase 9: Portfolio Documentation", "Document Big-O space/time trade-offs in your portfolio.", listOf("Publish GitHub repo", "Export portfolio certificate"))
            ),
            starterCodeTemplate = """
# Algorithm Benchmark Suite
import time
import random

def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
    return arr

test_data = [64, 34, 25, 12, 22, 11, 90]
print("Original:", test_data)
print("Sorted:", bubble_sort(test_data.copy()))
            """.trimIndent(),
            finalSolutionSnippet = """
# Completed Sorting & Search Suite
def quick_sort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quick_sort(left) + middle + quick_sort(right)

print("QuickSort Result:", quick_sort([45, 12, 89, 3, 27]))
            """.trimIndent(),
            readmeTemplate = "# Algorithm Visualizer & Benchmark\n\nHigh-performance Python sorting and search benchmarking suite."
        ),

        AiProjectGuide(
            id = "proj_rag_agent",
            title = "Build an Autonomous RAG Knowledge Agent",
            subtitle = "Index PDF notes & answer technical questions with vector search",
            difficulty = "Advanced",
            estimatedHours = "8-10 Hours",
            targetCategory = "AI Agents & RAG",
            iconEmoji = "🧠",
            overview = "Construct an end-to-end Retrieval-Augmented Generation (RAG) agent that chunks textbooks and documents into embeddings and answers complex student inquiries.",
            prerequisites = listOf("Python OOP", "Vector Spaces", "LLM APIs"),
            conceptsToLearn = listOf("Text Chunking Strategies", "Dense Vector Embeddings", "Retrieval Ranking (Top-K)", "Source Grounding & Hallucination Prevention"),
            stages = listOf(
                ProjectStage(1, "Phase 1: Architecture & Pipeline Design", "Design the ingestion, embedding, vector storage, and query retrieval pipeline.", listOf("Create project layout", "Define chunk schema")),
                ProjectStage(2, "Phase 2: Document Ingestion & Chunking", "Build recursive chunker splitting texts into 300-token chunks with 50-token overlap.", listOf("Implement sliding window chunker", "Preserve source chapter metadata")),
                ProjectStage(3, "Phase 3: Embedding Generation", "Convert text chunks into dense floating-point vector representations.", listOf("Call embedding API", "Normalize vector norms")),
                ProjectStage(4, "Phase 4: In-Memory Vector Store & Cosine Similarity", "Implement dot-product cosine similarity search to retrieve top-3 most relevant chunks.", listOf("Implement math formula for cosine similarity", "Return top-K matches")),
                ProjectStage(5, "Phase 5: Grounded Augmented Prompting", "Inject retrieved document chunks into the AI context window with strict grounding directives.", listOf("Build context prompt", "Enforce 'Only answer using provided context'")),
                ProjectStage(6, "Phase 6: Citation & Confidence Scoring", "Attach exact page/paragraph citations to every generated response.", listOf("Parse citation tags", "Compute confidence metric")),
                ProjectStage(7, "Phase 7: Hallucination Guardrail Filter", "Check whether the generated answer is faithful to the source context.", listOf("Run verification check")),
                ProjectStage(8, "Phase 8: Evaluation & Benchmark Set", "Test against a dataset of 20 challenging academic questions.", listOf("Evaluate accuracy score")),
                ProjectStage(9, "Phase 9: Showcase & Portfolio README", "Document the RAG architecture with architecture diagrams for hiring managers.", listOf("Create project showcase card"))
            ),
            starterCodeTemplate = """
# RAG Knowledge Agent Starter
import math

def cosine_similarity(vec1, vec2):
    dot_product = sum(a * b for a, b in zip(vec1, vec2))
    norm_a = math.sqrt(sum(a * a for a in vec1))
    norm_b = math.sqrt(sum(b * b for b in vec2))
    return dot_product / (norm_a * norm_b) if norm_a and norm_b else 0.0

print("Cosine Similarity Test:", cosine_similarity([1, 0, 1], [1, 1, 0]))
            """.trimIndent(),
            finalSolutionSnippet = """
# Completed RAG Agent Pipeline
class RAGPipeline:
    def __init__(self):
        self.documents = []

    def add_doc(self, text, metadata):
        self.documents.append({"text": text, "meta": metadata})

    def search(self, query):
        # Retrieve top relevant context
        return self.documents[0]["text"] if self.documents else "No context"

rag = RAGPipeline()
rag.add_doc("Python was created by Guido van Rossum in 1991.", {"topic": "Python"})
print("Retrieved:", rag.search("creator of python"))
            """.trimIndent(),
            readmeTemplate = "# Autonomous RAG Knowledge Agent\n\nProduction-ready Retrieval-Augmented Generation agent for academic documents."
        )
    )

    // 2. CURATED CODING CHALLENGES WITH HINT ESCALATION
    fun getCodingChallenges(): List<CodingChallenge> = listOf(
        CodingChallenge(
            id = "ch_two_sum",
            title = "Two Sum - Target Pair Finder",
            difficulty = "Beginner",
            language = "Python",
            topic = "Hash Maps & Arrays",
            promptDescription = "Given a list of integers `nums` and an integer `target`, return indices of the two numbers such that they add up to `target`.\n\nExample:\nnums = [2, 7, 11, 15], target = 9 -> Output: [0, 1]",
            starterCode = """
def two_sum(nums, target):
    # Your code here
    # Return a list of two indices [i, j]
    pass

# Test execution
print(two_sum([2, 7, 11, 15], 9))
            """.trimIndent(),
            testCases = listOf(
                TestCase("nums = [2, 7, 11, 15], target = 9", "[0, 1]"),
                TestCase("nums = [3, 2, 4], target = 6", "[1, 2]"),
                TestCase("nums = [3, 3], target = 6", "[0, 1]")
            ),
            hints = listOf(
                "💡 Hint 1: What is the complement number you need when visiting `nums[i]`? Complement = `target - nums[i]`.",
                "🔍 Hint 2: Can you store previously seen numbers in a dictionary `{number: index}` for O(1) instant lookup?",
                "📝 Hint 3 (Logic): Loop through `enumerate(nums)`. If `target - num` is already in your dictionary, return `[dict[target - num], index]`. Otherwise, store `dict[num] = index`."
            ),
            solutionCode = """
def two_sum(nums, target):
    seen = {}
    for i, num in enumerate(nums):
        complement = target - num
        if complement in seen:
            return [seen[complement], i]
        seen[num] = i
    return []

print(two_sum([2, 7, 11, 15], 9))
            """.trimIndent(),
            explanation = "Using a Hash Map (dictionary) achieves O(N) Time Complexity and O(N) Space Complexity by checking complements in a single pass."
        ),

        CodingChallenge(
            id = "ch_palindrome",
            title = "Valid Palindrome & String Sanitizer",
            difficulty = "Beginner",
            language = "Python",
            topic = "Two Pointers & Strings",
            promptDescription = "A phrase is a palindrome if, after converting all uppercase letters to lowercase and removing all non-alphanumeric characters, it reads the same forward and backward.\n\nReturn `True` if it is a palindrome, or `False` otherwise.",
            starterCode = """
def is_palindrome(s):
    # Your code here
    pass

print(is_palindrome("A man, a plan, a canal: Panama"))
            """.trimIndent(),
            testCases = listOf(
                TestCase("s = 'A man, a plan, a canal: Panama'", "True"),
                TestCase("s = 'race a car'", "False"),
                TestCase("s = ' '", "True")
            ),
            hints = listOf(
                "💡 Hint 1: First sanitize the string by keeping only alphanumeric characters using `c.isalnum()` and converting to lowercase `c.lower()`.",
                "🔍 Hint 2: In Python, you can reverse a string cleanly with slice notation `cleaned[::-1]`.",
                "📝 Hint 3 (Logic): Check `cleaned == cleaned[::-1]` or use two pointers (`left` and `right`) moving toward the center."
            ),
            solutionCode = """
def is_palindrome(s):
    cleaned = ''.join(c.lower() for c in s if c.isalnum())
    return cleaned == cleaned[::-1]

print(is_palindrome("A man, a plan, a canal: Panama"))
            """.trimIndent(),
            explanation = "Filtering alphanumeric characters in O(N) time and comparing with reversed slice yields optimal O(N) Time and O(N) Space."
        ),

        CodingChallenge(
            id = "ch_binary_search",
            title = "Binary Search on Sorted Array",
            difficulty = "Intermediate",
            language = "Python",
            topic = "Binary Search & Divide-and-Conquer",
            promptDescription = "Given a sorted array of integers `nums` in ascending order and a target value, return the index of `target` if it exists in `nums`, or `-1` if not found. Must run in O(log N) runtime.",
            starterCode = """
def binary_search(nums, target):
    # Your code here
    # Must achieve O(log N) complexity
    pass

print(binary_search([-1, 0, 3, 5, 9, 12], 9))
            """.trimIndent(),
            testCases = listOf(
                TestCase("nums = [-1, 0, 3, 5, 9, 12], target = 9", "4"),
                TestCase("nums = [-1, 0, 3, 5, 9, 12], target = 2", "-1"),
                TestCase("nums = [5], target = 5", "0")
            ),
            hints = listOf(
                "💡 Hint 1: Maintain two pointers: `low = 0` and `high = len(nums) - 1`.",
                "🔍 Hint 2: Find `mid = (low + high) // 2`. If `nums[mid] == target`, you found it!",
                "📝 Hint 3 (Logic): If `nums[mid] < target`, discard the left half (`low = mid + 1`). If `nums[mid] > target`, discard the right half (`high = mid - 1`). Repeat while `low <= high`."
            ),
            solutionCode = """
def binary_search(nums, target):
    low, high = 0, len(nums) - 1
    while low <= high:
        mid = (low + high) // 2
        if nums[mid] == target:
            return mid
        elif nums[mid] < target:
            low = mid + 1
        else:
            high = mid - 1
    return -1

print(binary_search([-1, 0, 3, 5, 9, 12], 9))
            """.trimIndent(),
            explanation = "By halving the search space on each comparison, Binary Search executes in O(log N) Time Complexity and O(1) Space Complexity."
        )
    )

    // 3. CURATED PERSONALIZED LEARNING TRACKS
    fun getLearningTracks(): List<LearningTrack> = listOf(
        LearningTrack(
            id = "track_python",
            title = "Python Mastery: Zero to AI Engineer",
            targetRole = "Python & AI Developer",
            iconEmoji = "🐍",
            description = "Master Python from variables and data structures to object-oriented programming, asynchronous coding, API integration, and AI libraries.",
            difficultyLevel = "Beginner to Pro",
            estimatedWeeks = 8,
            isIndiaFocused = true,
            keySkills = listOf("Python Syntax", "OOP", "APIs & JSON", "NumPy & Pandas", "Automation Scripts"),
            modules = listOf(
                LearningTrackModule("py_m1", 1, "Variables, Data Types & Operators", "Understand numbers, strings, booleans, arithmetic, and logical operators.", listOf("Variables & Constants", "String Manipulation & F-strings", "Type Conversion"), practiceGoal = "Solve 5 arithmetic & string challenges", projectMilestone = "Interactive CLI Greeting & Calculator"),
                LearningTrackModule("py_m2", 2, "Control Flow, Conditionals & Loops", "Master if-elif-else statements, for loops, while loops, and list comprehensions.", listOf("Boolean Logic", "Range & Enumeration", "Break & Continue", "List Comprehensions"), practiceGoal = "Build number guessing game with attempts tracker", projectMilestone = "Prime Number & Factorization Engine"),
                LearningTrackModule("py_m3", 3, "Functions, Scope & Error Handling", "Write modular, reusable functions with default parameters, *args, **kwargs, and try-except blocks.", listOf("Function Definitions & Return Values", "Lambda Functions", "Exception Handling"), practiceGoal = "Write robust input sanitizers", projectMilestone = "Modular Student Grade Calculator"),
                LearningTrackModule("py_m4", 4, "Data Structures: Lists, Dictionaries, Sets & Tuples", "Deep dive into collections, dictionary lookups, set operations, and sorting keys.", listOf("List Slicing & Methods", "Hash Map Dictionaries", "Set Intersections", "Tuples"), practiceGoal = "Implement frequency counters", projectMilestone = "Student Record Database in Memory"),
                LearningTrackModule("py_m5", 5, "Object-Oriented Programming (OOP)", "Classes, objects, constructors (__init__), encapsulation, inheritance, and polymorphism.", listOf("Class vs Instance Attributes", "Inheritance & super()", "Dunder Methods"), practiceGoal = "Design a Bank Account class hierarchy", projectMilestone = "E-Commerce Cart System"),
                LearningTrackModule("py_m6", 6, "File I/O, JSON & REST APIs", "Reading/writing files, parsing JSON data, and calling web APIs with requests.", listOf("File Context Managers (with)", "JSON Serializing", "HTTP GET & POST"), practiceGoal = "Fetch weather from public API", projectMilestone = "AI Weather & Study Assistant CLI")
            )
        ),

        LearningTrack(
            id = "track_ai_engineer",
            title = "Full-Stack AI & Machine Learning Engineer",
            targetRole = "AI / ML Engineer",
            iconEmoji = "🤖",
            description = "Comprehensive curriculum covering Math for ML, Neural Networks, PyTorch, Generative AI, Prompt Engineering, RAG, and AI Agent deployment.",
            difficultyLevel = "Intermediate to Advanced",
            estimatedWeeks = 12,
            isIndiaFocused = true,
            keySkills = listOf("Machine Learning", "Neural Networks", "Generative AI", "RAG Architectures", "AI Agents"),
            modules = listOf(
                LearningTrackModule("ai_m1", 1, "Mathematics for AI: Linear Algebra & Calculus", "Vectors, matrices, dot products, eigenvalues, gradients, and partial derivatives.", listOf("Matrix Multiplications", "Gradient Descent", "Loss Functions"), practiceGoal = "Compute matrix operations in pure Python", projectMilestone = "Vector Similarity Calculator"),
                LearningTrackModule("ai_m2", 2, "Data Science Foundations: NumPy & Pandas", "Manipulate tabular data, compute statistical distributions, and handle missing values.", listOf("NumPy Arrays & Broadcasting", "Pandas DataFrames", "Data Cleaning"), practiceGoal = "Clean Indian student dataset", projectMilestone = "Automated CSV Data Analyzer"),
                LearningTrackModule("ai_m3", 3, "Classical Machine Learning Algorithms", "Supervised and unsupervised algorithms: Linear Regression, Logistic Regression, Decision Trees, K-Means.", listOf("Classification vs Regression", "Train/Test Splits", "Accuracy & F1-Score"), practiceGoal = "Train student performance predictor", projectMilestone = "Exam Score Prediction Model"),
                LearningTrackModule("ai_m4", 4, "Deep Learning & Neural Networks", "Perceptrons, multi-layer neural nets, activation functions (ReLU, Sigmoid), backpropagation.", listOf("Feedforward Networks", "Backpropagation", "Overfitting & Dropout"), practiceGoal = "Implement single perceptron classifier", projectMilestone = "Handwritten Digit Classifier"),
                LearningTrackModule("ai_m5", 5, "Generative AI, Large Language Models & Prompting", "Transformer architectures, attention mechanisms, token embeddings, and advanced prompting techniques.", listOf("Transformers & Attention", "Few-Shot Prompting", "Temperature & Top-P"), practiceGoal = "Construct zero-shot and few-shot prompts", projectMilestone = "Custom AI Study Mentor"),
                LearningTrackModule("ai_m6", 6, "RAG (Retrieval-Augmented Generation) & AI Agents", "Connect LLMs to external databases using vector embeddings and build multi-step autonomous agents.", listOf("Vector Databases & Embeddings", "Retrieval Chunking", "ReAct Agent Framework"), practiceGoal = "Build local knowledge base search", projectMilestone = "Autonomous Academic Research Agent")
            )
        ),

        LearningTrack(
            id = "track_web_dev",
            title = "Modern Full-Stack Web Development",
            targetRole = "Full-Stack Web Developer",
            iconEmoji = "🌐",
            description = "Build modern, responsive, database-driven web applications using HTML5, CSS3, JavaScript, TypeScript, React, Node.js, and databases.",
            difficultyLevel = "Beginner to Intermediate",
            estimatedWeeks = 10,
            isIndiaFocused = true,
            keySkills = listOf("HTML & CSS", "JavaScript / TypeScript", "React & Modern UI", "Node.js & Express", "PostgreSQL / MongoDB"),
            modules = listOf(
                LearningTrackModule("web_m1", 1, "Semantic HTML5, CSS3 & Responsive Design", "Modern layouts with Flexbox, CSS Grid, mobile-first media queries, and accessibility.", listOf("HTML5 Semantic Tags", "Flexbox & Grid Layouts", "Responsive Breakpoints"), practiceGoal = "Build responsive landing page", projectMilestone = "Personal Developer Portfolio Site"),
                LearningTrackModule("web_m2", 2, "JavaScript Essentials & DOM Manipulation", "Variables, arrow functions, event listeners, async/await, and Fetch API.", listOf("ES6+ Modern JS", "DOM Events", "Async / Await & Promises"), practiceGoal = "Build interactive interactive quiz widget", projectMilestone = "Dynamic Task Tracker Web App"),
                LearningTrackModule("web_m3", 3, "React Components, State & Hooks", "Component-driven architecture, JSX, useState, useEffect, and custom hooks.", listOf("Component Lifecycle", "State Management", "Props & Hooks"), practiceGoal = "Build interactive flashcard review deck", projectMilestone = "Avora Web Study Hub"),
                LearningTrackModule("web_m4", 4, "Backend APIs with Node.js & Express", "Routing, middleware, request validation, CORS, and RESTful API conventions.", listOf("Express Server Setup", "CRUD Endpoints", "Error Handling"), practiceGoal = "Create REST API for note taking", projectMilestone = "Secure Notes & Quiz REST API"),
                LearningTrackModule("web_m5", 5, "Database Integration & Authentication", "Relational SQL queries, schema migrations, JWT token authentication, and password hashing.", listOf("SQL Queries & Indexes", "JWT Auth Tokens", "bcrypt Password Hashing"), practiceGoal = "Implement secure login/register flow", projectMilestone = "Full-Stack Student Collaboration Portal")
            )
        )
    )

    // 4. CALCULATE SKILL PROFILE FROM USER ACTIVITY
    fun calculateSkillProfile(
        completedTasksCount: Int = 0,
        quizResultsCount: Int = 0,
        averageQuizScore: Int = 0,
        studyHours: Double = 0.0,
        solvedChallengesCount: Int = 0
    ): SkillProfile {
        val totalXp = (completedTasksCount * 25) + (quizResultsCount * 40) + (studyHours * 50).toInt() + (solvedChallengesCount * 60)
        val level = (totalXp / 300) + 1
        val levelNames = listOf("Novice Scholar", "Apprentice Coder", "Junior AI Builder", "Proficient Engineer", "AI Master Innovator")
        val levelTitle = levelNames.getOrElse(level - 1) { "Master AI Technologist" }
        val nextLevelXp = level * 300

        val hasActivity = completedTasksCount > 0 || quizResultsCount > 0 || studyHours > 0.0 || solvedChallengesCount > 0

        val pythonProficiency = if (hasActivity) {
            (10 + (solvedChallengesCount * 15) + (completedTasksCount * 4)).coerceIn(5, 95)
        } else 5

        val aiProficiency = if (hasActivity) {
            (10 + (quizResultsCount * 10) + (studyHours * 5).toInt()).coerceIn(5, 92)
        } else 5

        val algorithmsProficiency = if (hasActivity) {
            (10 + (solvedChallengesCount * 18)).coerceIn(5, 90)
        } else 5

        val apisProficiency = if (hasActivity) {
            (10 + (completedTasksCount * 5)).coerceIn(5, 88)
        } else 5

        val mlProficiency = if (hasActivity) {
            (10 + (quizResultsCount * 8)).coerceIn(5, 85)
        } else 5

        val agentsProficiency = if (hasActivity) {
            (10 + (studyHours * 4).toInt()).coerceIn(5, 80)
        } else 5

        val skills = listOf(
            SkillNode("sk_py", "Python Programming", "Programming", pythonProficiency, getProficiencyTitle(pythonProficiency), pythonProficiency * 10, "🐍", true),
            SkillNode("sk_ai", "AI & Prompt Engineering", "AI & ML", aiProficiency, getProficiencyTitle(aiProficiency), aiProficiency * 10, "🤖", true),
            SkillNode("sk_algo", "Data Structures & Algorithms", "Programming", algorithmsProficiency, getProficiencyTitle(algorithmsProficiency), algorithmsProficiency * 10, "⚡", true),
            SkillNode("sk_api", "REST APIs & Backend", "Systems & Architecture", apisProficiency, getProficiencyTitle(apisProficiency), apisProficiency * 10, "🔌", false),
            SkillNode("sk_ml", "Machine Learning Fundamentals", "AI & ML", mlProficiency, getProficiencyTitle(mlProficiency), mlProficiency * 10, "🧠", false),
            SkillNode("sk_agent", "Autonomous AI Agents & RAG", "AI & ML", agentsProficiency, getProficiencyTitle(agentsProficiency), agentsProficiency * 10, "🦾", false)
        )

        val strengths = mutableListOf<String>()
        val growthAreas = mutableListOf<String>()

        if (pythonProficiency >= 50) strengths.add("Strong Python fundamentals and function decomposition")
        if (aiProficiency >= 50) strengths.add("Good conceptual grasp of AI models and prompt techniques")
        if (algorithmsProficiency >= 50) strengths.add("Consistent algorithmic problem solving")

        if (hasActivity) {
            if (agentsProficiency < 40) growthAreas.add("Explore Vector Embeddings & Autonomous AI Agents")
            if (mlProficiency < 40) growthAreas.add("Practice classical Machine Learning algorithms with NumPy")
            if (apisProficiency < 50) growthAreas.add("Deepen understanding of asynchronous API calls and JSON structures")
        } else {
            growthAreas.add("Complete your first study lesson and coding challenge to start leveling up")
        }

        val streak = if (completedTasksCount > 0) (completedTasksCount / 2).coerceAtLeast(1) else 0

        return SkillProfile(
            overallLevel = level,
            levelName = levelTitle,
            totalXp = totalXp,
            nextLevelXp = nextLevelXp,
            streakDays = streak,
            completedLessons = completedTasksCount,
            completedProjects = if (solvedChallengesCount >= 5) 1 else 0,
            completedChallenges = solvedChallengesCount,
            quizzesTaken = quizResultsCount,
            averageQuizScore = averageQuizScore,
            skills = skills,
            strengths = if (strengths.isNotEmpty()) strengths else listOf("Curious and ready to master technology"),
            growthAreas = growthAreas,
            consistencyScore = if (hasActivity) (70 + (completedTasksCount * 3)).coerceIn(50, 98) else 0
        )
    }

    private fun getProficiencyTitle(percent: Int): String = when {
        percent >= 85 -> "Master"
        percent >= 70 -> "Advanced"
        percent >= 50 -> "Proficient"
        percent >= 30 -> "Intermediate"
        else -> "Beginner"
    }

    // 5. DEFAULT STUDENT PORTFOLIO (Sample Template)
    fun getDefaultPortfolio(): StudentPortfolio = StudentPortfolio(
        studentName = "Barie Bilal (Creator Showcase)",
        headline = "Sample Student Portfolio • AI Engineer & Full-Stack Builder",
        location = "Kashmir, India",
        bio = "[Sample Showcase Template] Computer science student dedicated to building intelligent AI agents, robust backend systems, and solving real-world challenges through code.",
        githubHandle = "barie-bilal",
        linkedinHandle = "barie-bilal",
        targetRole = "AI / ML Engineer",
        projects = listOf(
            PortfolioProject(
                id = "p1",
                title = "Avora Socratic AI Study Companion (Sample)",
                category = "AI/ML & EdTech",
                description = "[Sample Project] An intelligent multi-lingual AI tutoring platform featuring step-by-step Socratic hint escalation, automated flashcard generation, and spaced repetition.",
                techStack = listOf("Kotlin", "Jetpack Compose", "Gemini AI REST", "Room Database", "Python"),
                githubRepo = "https://github.com/barie-bilal/avora-ai-companion",
                completionDate = "August 2026",
                keyLearnings = listOf("Socratic Hint Protocol", "Type-safe Compose Navigation", "Local Room Persistence"),
                difficulty = "Advanced",
                isFeatured = true
            ),
            PortfolioProject(
                id = "p2",
                title = "Algorithm Visualizer & Complexity Profiler (Sample)",
                category = "Python & Systems",
                description = "[Sample Project] High-efficiency sorting and search benchmarking suite evaluating runtime complexity on large datasets.",
                techStack = listOf("Python 3", "Time Profiling", "Divide & Conquer", "Unit Testing"),
                githubRepo = "https://github.com/barie-bilal/algo-profiler",
                completionDate = "July 2026",
                keyLearnings = listOf("Recursion Stack Limits", "In-Place QuickSort", "Big-O Empirical Analysis"),
                difficulty = "Intermediate",
                isFeatured = true
            )
        ),
        certificates = listOf(
            PortfolioCertificate(
                id = "cert_1",
                title = "Python Programming & Data Structures (Sample Certificate)",
                issuer = "Avora AI Academy",
                issueDate = "August 2026",
                credentialId = "SAMPLE-AVR-PY-2026",
                skillsValidated = listOf("Python OOP", "Algorithm Efficiency", "Exception Handling", "File I/O")
            ),
            PortfolioCertificate(
                id = "cert_2",
                title = "AI Prompt Engineering & LLM Architecture (Sample Certificate)",
                issuer = "Avora AI Academy",
                issueDate = "August 2026",
                credentialId = "SAMPLE-AVR-AI-2026",
                skillsValidated = listOf("Context Windows", "Few-Shot Prompting", "Socratic Guidance", "Safety Guardrails")
            )
        ),
        isVerifiedStudent = true
    )
}
