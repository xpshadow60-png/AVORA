package com.example.data.career

import com.example.data.model.*

object CareerPathEngine {

    val assessmentQuestions: List<CareerAssessmentQuestion> = listOf(
        CareerAssessmentQuestion(
            id = 1,
            questionNumber = 1,
            title = "What is your primary field of interest?",
            subtitle = "This will anchor your entire academic roadmap and course recommendations.",
            options = listOf(
                CareerOption("med", "Medicine & Healthcare", "Doctors, Surgeons, Nursing, Pharmacy & Biomedical", "🩺"),
                CareerOption("cs", "Computer Science & AI", "Software Engineering, Machine Learning, Cloud & Security", "💻"),
                CareerOption("eng", "Engineering & Robotics", "Mechanical, Electrical, Aerospace & Mechatronics", "⚙️"),
                CareerOption("biz", "Business & Finance", "Investment, Economics, Entrepreneurship & Marketing", "📈"),
                CareerOption("law", "Law & Governance", "Jurisprudence, Corporate Law, Policy & Human Rights", "⚖️"),
                CareerOption("psych", "Psychology & Neuroscience", "Clinical Psychology, Cognitive Research & Mental Health", "🧠"),
                CareerOption("science", "Natural Sciences (Bio/Chem/Phys)", "Pure Research, Biotechnology & Astrophysics", "🔬"),
                CareerOption("design", "Design & Creative Tech", "UI/UX, Product Design, Game Art & Digital Media", "🎨")
            )
        ),
        CareerAssessmentQuestion(
            id = 2,
            questionNumber = 2,
            title = "What is your target specialization or dream role?",
            subtitle = "Select the specific niche you want to excel in.",
            options = listOf(
                CareerOption("spec_1", "Clinical Specialist / Physician", "Direct patient diagnostics, surgery or specialized clinic care", "👨‍⚕️"),
                CareerOption("spec_2", "Research Scientist & Academia", "Breakthrough lab studies, clinical trials and publications", "🔬"),
                CareerOption("spec_3", "Industry Practitioner / Engineer", "Applied high-level industry work, building products and systems", "🚀"),
                CareerOption("spec_4", "Leadership, Management & Advisory", "Leading teams, hospitals, startups or policy institutions", "👔"),
                CareerOption("spec_5", "Cross-Disciplinary Innovator", "Bridging tech, medicine, and human solutions", "💡")
            )
        ),
        CareerAssessmentQuestion(
            id = 3,
            questionNumber = 3,
            title = "What is your current academic or professional level?",
            subtitle = "We'll calibrate the difficulty of courses and milestones accordingly.",
            options = listOf(
                CareerOption("lvl_hs", "High School / College Prep", "Building foundational science and math fundamentals", "🎒"),
                CareerOption("lvl_ug1", "Undergraduate (1st - 2nd Year)", "Exploring core subjects and prerequisite coursework", "📚"),
                CareerOption("lvl_ug2", "Undergraduate (3rd - 4th Year)", "Advanced electives, lab research, and internship prep", "🎓"),
                CareerOption("lvl_grad", "Graduate / Post-Graduate", "Master's, MD, Ph.D. or residency candidates", "🏛️"),
                CareerOption("lvl_switch", "Self-Taught / Career Transition", "Accelerated learning for industry readiness", "🔄")
            )
        ),
        CareerAssessmentQuestion(
            id = 4,
            questionNumber = 4,
            title = "How many hours per week can you dedicate to study?",
            subtitle = "Helps establish realistic milestone pacing.",
            options = listOf(
                CareerOption("hrs_light", "5 - 10 Hours / week", "Steady part-time pace alongside heavy commitments", "⏳"),
                CareerOption("hrs_mod", "10 - 20 Hours / week", "Balanced regular study schedule with deep focus blocks", "⏱️"),
                CareerOption("hrs_heavy", "20 - 35 Hours / week", "Intensive full-time academic immersion", "🔥"),
                CareerOption("hrs_max", "35+ Hours / week", "Elite preparation for board exams or thesis research", "⚡")
            )
        ),
        CareerAssessmentQuestion(
            id = 5,
            questionNumber = 5,
            title = "What is your primary learning style?",
            subtitle = "We will prioritize media formats that match your cognition.",
            options = listOf(
                CareerOption("style_video", "Visual & Video Masterclasses", "High-yield animations, 3D anatomy, visual diagrams", "📺"),
                CareerOption("style_hands_on", "Hands-on Practice & Problem Sets", "Coding, lab simulations, sample exam questions", "🧪"),
                CareerOption("style_text", "Deep Reading & Research Papers", "Textbooks, scientific journals and systematic notes", "📖"),
                CareerOption("style_srs", "Spaced Repetition & Active Recall", "Flashcard decks, rapid self-testing, SuperMemo SM-2", "🗂️")
            )
        ),
        CareerAssessmentQuestion(
            id = 6,
            questionNumber = 6,
            title = "What is your biggest current academic challenge?",
            subtitle = "Your roadmap will incorporate tailored counter-strategies.",
            options = listOf(
                CareerOption("ch_time", "Time Management & Procrastination", "Balancing multiple subjects and staying on schedule", "⏰"),
                CareerOption("ch_volume", "Information Overload & Retention", "Memorizing massive volumes of terminology and concepts", "🌊"),
                CareerOption("ch_exam", "High-Stakes Exam Anxiety", "Performing under pressure in standardized tests", "😰"),
                CareerOption("ch_practical", "Lack of Real-World / Lab Experience", "Translating theory into practical clinical or coding skills", "🛠️")
            )
        ),
        CareerAssessmentQuestion(
            id = 7,
            questionNumber = 7,
            title = "What is your target timeline for this milestone?",
            subtitle = "When do you plan to achieve this career benchmark?",
            options = listOf(
                CareerOption("time_6m", "Next 6 Months", "Immediate upcoming semester or certification goal", "🎯"),
                CareerOption("time_1y", "1 - 2 Years", "Degree completion, MCAT/GRE, or entering graduate school", "📅"),
                CareerOption("time_3y", "3 - 4 Years", "Undergraduate degree to professional licensing", "🚀"),
                CareerOption("time_5y", "5+ Years", "Full residency, board licensing, or principal senior role", "🏆")
            )
        ),
        CareerAssessmentQuestion(
            id = 8,
            questionNumber = 8,
            title = "Which major certification or credential are you pursuing?",
            subtitle = "We'll highlight exam-focused resources for this target.",
            options = listOf(
                CareerOption("cert_board", "Professional Board / Licensure", "e.g. USMLE, NCLEX, Bar Exam, FE/PE Licensure", "📜"),
                CareerOption("cert_degree", "Top Tier University Degree", "BS, MS, MD, JD or Ph.D. accreditation", "🎓"),
                CareerOption("cert_industry", "Industry Recognized Certifications", "AWS, Google Cloud, Cisco, CFA, PMP", "💼"),
                CareerOption("cert_portfolio", "Verified Practical Project Portfolio", "Published research papers, clinical cases, or GitHub systems", "📂")
            )
        ),
        CareerAssessmentQuestion(
            id = 9,
            questionNumber = 9,
            title = "What is your learning resource preference?",
            subtitle = "Tailors our online course recommendations.",
            options = listOf(
                CareerOption("res_mooc", "University MOOCs & Courseware", "MIT OpenCourseWare, Harvard Online, Coursera, edX", "🏛️"),
                CareerOption("res_open", "100% Free & Open-Source Tools", "YouTube series, open textbooks, community repositories", "🌐"),
                CareerOption("res_hybrid", "Hybrid Structured & Self-Paced", "A blend of accredited courses and rapid video tutorials", "⚖️"),
                CareerOption("res_textbook", "Standard Authoritative Textbooks", "Harrison's, Guyton-Hall, CLRS, Campbell Biology", "📚")
            )
        ),
        CareerAssessmentQuestion(
            id = 10,
            questionNumber = 10,
            title = "What is your ideal daily study rhythm?",
            subtitle = "Helps optimize your daily focus schedule and timer blocks.",
            options = listOf(
                CareerOption("rhythm_morning", "Early Morning Deep Work (5AM - 9AM)", "Peak mental sharpness before daily distractions begin", "🌅"),
                CareerOption("rhythm_pomodoro", "Pomodoro Interval Sprints (25/5 min)", "High-intensity bursts with structured active recovery", "🍅"),
                CareerOption("rhythm_evening", "Afternoon & Evening Focus (2PM - 8PM)", "Steady multi-hour blocks after lectures and classes", "🌇"),
                CareerOption("rhythm_night", "Night Owl Deep Study (9PM - 1AM)", "Quiet, distraction-free late night retention sessions", "🌙")
            )
        )
    )

    fun generateCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        val field = assessment.fieldOfStudy.lowercase()

        return when {
            field.contains("med") || field.contains("health") || field.contains("doctor") -> {
                generateMedicalCareerPath(assessment)
            }
            field.contains("cs") || field.contains("computer") || field.contains("ai") || field.contains("software") -> {
                generateComputerScienceCareerPath(assessment)
            }
            field.contains("eng") || field.contains("robot") || field.contains("mech") -> {
                generateEngineeringCareerPath(assessment)
            }
            field.contains("biz") || field.contains("fin") || field.contains("econ") -> {
                generateBusinessCareerPath(assessment)
            }
            field.contains("law") || field.contains("gov") || field.contains("juris") -> {
                generateLawCareerPath(assessment)
            }
            field.contains("psych") || field.contains("neuro") -> {
                generatePsychologyCareerPath(assessment)
            }
            else -> {
                generateGeneralScienceCareerPath(assessment)
            }
        }
    }

    private fun generateMedicalCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A rigorous, comprehensive pathway from Foundational Biomedical Sciences to USMLE/Board Exams, Clinical Clerkships, and Medical Residency specialization.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: Foundational Pre-Med & Biomedical Sciences",
                    duration = "Months 1 - 12",
                    description = "Master core anatomical structures, cellular biology, biochemistry pathways, and general physiology.",
                    milestones = listOf(
                        RoadmapMilestone("m1_1", "Human Gross Anatomy & Embryology", "Master muscular, skeletal, neurovascular, and organ systems with 3D atlas visualization.", false),
                        RoadmapMilestone("m1_2", "Medical Biochemistry & Metabolic Pathways", "Understand glycolysis, Krebs cycle, lipid metabolism, enzyme kinetics and metabolic pathology.", false),
                        RoadmapMilestone("m1_3", "Medical Physiology & Homeostasis", "Study cardiovascular, renal, respiratory, endocrine, and GI physiological loops (Guyton & Hall).", false),
                        RoadmapMilestone("m1_4", "Medical Terminology & Latin Roots", "Build rapid recall for 1,000+ clinical anatomical and diagnostic terms via Anki/SRS.", false)
                    ),
                    coreCompetencies = listOf("Gross Anatomy", "Biochemistry", "Organ Physiology", "Medical Terminology", "Active Recall")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: Pathology, Microbiology & Pharmacology",
                    duration = "Months 13 - 24",
                    description = "Transition into disease mechanisms, infectious agents, and therapeutic drug mechanisms.",
                    milestones = listOf(
                        RoadmapMilestone("m2_1", "General & Systemic Pathology (Pathoma)", "Master cellular injury, inflammation, neoplasia, and organ-specific disease pathophysiology.", false),
                        RoadmapMilestone("m2_2", "Medical Microbiology & Immunology", "Bacteria, viruses, fungi, parasites, and host adaptive/innate immune responses.", false),
                        RoadmapMilestone("m2_3", "Medical Pharmacology & Mechanisms of Action", "Antimicrobials, autonomic drugs, CNS agents, cardiovascular pharmaceuticals.", false),
                        RoadmapMilestone("m2_4", "Clinical Lab Diagnostics & Histology", "Blood smears, biopsy interpretation, arterial blood gas (ABG) analysis.", false)
                    ),
                    coreCompetencies = listOf("Pathophysiology", "Immunology", "Pharmacokinetics", "Microbiology", "Differential Diagnosis")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: USMLE / Board Preparation & Clinical Reasoning",
                    duration = "Months 25 - 36",
                    description = "High-yield board exam practice, question banks (UWorld/Amboss), and clinical reasoning protocols.",
                    milestones = listOf(
                        RoadmapMilestone("m3_1", "Complete 2,500+ Board Style Vignettes", "Daily spaced repetition sets on USMLE Step 1 / Board question banks with >75% accuracy.", false),
                        RoadmapMilestone("m3_2", "Physical Examination & History Taking (OSCE)", "Standardized patient assessments, cardiac auscultation, cranial nerve exams.", false),
                        RoadmapMilestone("m3_3", "Clinical Pharmacology & Emergency Protocols", "ACLS algorithms, toxicology protocols, fluid resuscitation dynamics.", false),
                        RoadmapMilestone("m3_4", "Biostatistics & Medical Ethics", "Sensitivity, specificity, positive predictive value, Belmont report ethical principles.", false)
                    ),
                    coreCompetencies = listOf("Board Vignettes", "Clinical Auscultation", "Biostatistics", "Medical Ethics", "ACLS Protocols")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: Clinical Clerkships & Residency Specialization",
                    duration = "Months 37 - 48+",
                    description = "Hospital ward rotations in Internal Medicine, Surgery, Pediatrics, OB/GYN, and match preparation.",
                    milestones = listOf(
                        RoadmapMilestone("m4_1", "Core Hospital Rotations (Internal Med & General Surgery)", "Active participation in rounds, patient notes (SOAP), bedside procedures.", false),
                        RoadmapMilestone("m4_2", "Sub-Internship in Chosen Specialization", "Four-week audition rotation in Surgery, Cardiology, Neurology, or Pediatrics.", false),
                        RoadmapMilestone("m4_3", "Clinical Research & Case Report Publication", "Submit at least 1 peer-reviewed clinical case report or scientific poster.", false),
                        RoadmapMilestone("m4_4", "Residency Application & Match (ERAS/NRMP)", "Personal statement refinement, Dean's letter, and residency interviews.", false)
                    ),
                    coreCompetencies = listOf("Hospital Rounds", "SOAP Charting", "Bedside Procedures", "Clinical Research", "Residency Match")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_ninja_nerd",
                    title = "Ninja Nerd Medicine - Full System Physiology & Biochemistry",
                    channel = "Ninja Nerd",
                    topic = "Human Physiology & Pathophysiology",
                    duration = "Comprehensive Playlist",
                    searchQuery = "Ninja Nerd Medicine Physiology Biochemistry",
                    youtubeUrl = "https://www.youtube.com/results?search_query=ninja+nerd+medicine+lectures"
                ),
                VideoRecommendation(
                    id = "v_anatomy_zone",
                    title = "AnatomyZone - 3D Gross Anatomy Tutorials",
                    channel = "AnatomyZone",
                    topic = "Musculoskeletal & Neuroanatomy",
                    duration = "3D Interactive Series",
                    searchQuery = "AnatomyZone 3D Human Anatomy",
                    youtubeUrl = "https://www.youtube.com/results?search_query=anatomyzone+tutorials"
                ),
                VideoRecommendation(
                    id = "v_armando",
                    title = "Armando Hasudungan - Illustrated Medical Science & Immunology",
                    channel = "Armando Hasudungan",
                    topic = "Immunology, Endocrinology & Pharmacology",
                    duration = "Hand-drawn Lectures",
                    searchQuery = "Armando Hasudungan Medical Science",
                    youtubeUrl = "https://www.youtube.com/results?search_query=armando+hasudungan+medicine"
                ),
                VideoRecommendation(
                    id = "v_khan_med",
                    title = "Khan Academy Medicine & NCLEX/MCAT Preparation",
                    channel = "Khan Academy Medicine",
                    topic = "Cardiovascular, Respiratory & Renal Systems",
                    duration = "100+ High Yield Modules",
                    searchQuery = "Khan Academy Medicine MCAT",
                    youtubeUrl = "https://www.youtube.com/results?search_query=khan+academy+medicine"
                ),
                VideoRecommendation(
                    id = "v_dirty_med",
                    title = "Dirty Medicine - High Yield Board Mnemonics & Rapid Review",
                    channel = "Dirty Medicine",
                    topic = "USMLE Step 1 / COMLEX High Yield Mnemonics",
                    duration = "Quick Recall Episodes",
                    searchQuery = "Dirty Medicine High Yield",
                    youtubeUrl = "https://www.youtube.com/results?search_query=dirty+medicine+high+yield"
                ),
                VideoRecommendation(
                    id = "v_osmosis",
                    title = "Osmosis from Elsevier - Clinical Pathophysiology Animations",
                    channel = "Osmosis",
                    topic = "Clinical Pathology & Pharmacology",
                    duration = "Visual Summaries",
                    searchQuery = "Osmosis from Elsevier Medicine",
                    youtubeUrl = "https://www.youtube.com/results?search_query=osmosis+medicine"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_harvard_physio",
                    title = "Harvard Online: Human Anatomy & Physiology Masterclass",
                    platform = "edX / Harvard Online",
                    institution = "Harvard University",
                    level = "Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.edx.org/school/harvardx",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_stanford_med",
                    title = "Stanford Online: Introductory Human Physiology & Health",
                    platform = "Coursera",
                    institution = "Stanford University",
                    level = "Beginner - Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/learn/physiology",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_michigan_anatomy",
                    title = "University of Michigan: Human Anatomy Specialization",
                    platform = "Coursera",
                    institution = "University of Michigan",
                    level = "Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/specializations/human-anatomy",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_mit_biochem",
                    title = "MIT OpenCourseWare: Principles of Biochemistry (7.05)",
                    platform = "MIT OpenCourseWare",
                    institution = "Massachusetts Institute of Technology",
                    level = "Advanced",
                    rating = 5.0,
                    courseUrl = "https://ocw.mit.edu/courses/7-05-general-biochemistry-spring-2020/",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_jh_epidemiology",
                    title = "Johns Hopkins: Epidemiology in Public Health & Medicine",
                    platform = "Coursera",
                    institution = "Johns Hopkins University",
                    level = "Beginner - Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/specializations/epidemiology-public-health-practice",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "3D Anatomical Spatial Recognition",
                "Differential Diagnosis Protocol",
                "Pharmacological Mechanism Decoding",
                "Active Spaced Repetition (Anki/SM-2)",
                "Clinical Case Vignette Speed Reading",
                "Biostatistical Interpretation"
            )
        )
    }

    private fun generateComputerScienceCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A complete engineering track spanning Data Structures & Algorithms, Modern Full-Stack & System Design, Machine Learning, and Production Deployment.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: Programming Foundations & Algorithmic Thinking",
                    duration = "Months 1 - 6",
                    description = "Master Python, Kotlin or TypeScript, Object-Oriented Programming, and Data Structures.",
                    milestones = listOf(
                        RoadmapMilestone("cs1_1", "Core Syntax & Memory Models", "Understand pointers/references, memory stacks/heaps, recursion, and OOP design patterns.", false),
                        RoadmapMilestone("cs1_2", "Essential Data Structures (Arrays, Trees, Graphs, HashMaps)", "Implement balanced trees, BFS/DFS graph traversals, and hash functions from scratch.", false),
                        RoadmapMilestone("cs1_3", "Time & Space Complexity Analysis (Big-O)", "Analyze asymptotic runtime of recursive and iterative sorting and search routines.", false),
                        RoadmapMilestone("cs1_4", "Solve 100+ LeetCode Problem Sets", "Consistent problem-solving on Arrays, Two Pointers, Dynamic Programming, and Graphs.", false)
                    ),
                    coreCompetencies = listOf("Python/Kotlin", "Data Structures", "Big-O Notation", "Git Version Control", "Algorithms")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: Full-Stack Architecture, Databases & APIs",
                    duration = "Months 7 - 14",
                    description = "Build full-stack applications with relational databases, REST/GraphQL APIs, and reactive frontend UI.",
                    milestones = listOf(
                        RoadmapMilestone("cs2_1", "Relational Databases & SQL Schema Design", "Complex JOINs, ACID transactions, indexing strategies (PostgreSQL / Room).", false),
                        RoadmapMilestone("cs2_2", "RESTful & Real-time API Microservices", "Design authenticated JSON endpoints with JWT, WebSockets, and rate limiting.", false),
                        RoadmapMilestone("cs2_3", "Modern Frontend & Mobile UI (Compose / React)", "Declarative state management, responsive adaptive layouts, and UI caching.", false),
                        RoadmapMilestone("cs2_4", "Build 2 Production Full-Stack Projects", "Deploy full-fledged end-to-end apps with user auth, database, and cloud backend.", false)
                    ),
                    coreCompetencies = listOf("SQL & Room", "RESTful APIs", "Jetpack Compose / React", "JWT Auth", "Cloud Hosting")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: System Design, Cloud Infrastructure & DevOps",
                    duration = "Months 15 - 24",
                    description = "Design distributed scalable architectures, CI/CD pipelines, Docker containers, and caching layers.",
                    milestones = listOf(
                        RoadmapMilestone("cs3_1", "Distributed System Design", "Load balancers, CDN caching, database sharding, CAP theorem, message queues (Kafka).", false),
                        RoadmapMilestone("cs3_2", "Docker Containerization & Kubernetes", "Containerize apps into multi-stage Dockerfiles and deploy with Helm/K8s.", false),
                        RoadmapMilestone("cs3_3", "CI/CD Automated Pipelines", "Automated linting, unit testing, and continuous deployment workflows (GitHub Actions).", false),
                        RoadmapMilestone("cs3_4", "Security Best Practices & OWASP Top 10", "Mitigate SQL injection, XSS, CSRF, and implement TLS and secret encryption.", false)
                    ),
                    coreCompetencies = listOf("System Design", "Docker & K8s", "CI/CD Workflows", "Redis Caching", "Cloud Security")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: AI/ML Integration, Open Source & Senior Engineering",
                    duration = "Months 25 - 36+",
                    description = "Incorporate AI/ML models, contribute to large open source codebases, and crack technical interviews.",
                    milestones = listOf(
                        RoadmapMilestone("cs4_1", "LLM & Vector Embeddings Integration", "Build AI agents with Neural APIs, RAG architectures, and vector embeddings.", false),
                        RoadmapMilestone("cs4_2", "Open Source Contributions", "Submit merged pull requests to widely used GitHub developer tools or libraries.", false),
                        RoadmapMilestone("cs4_3", "Technical Coding & Behavioral Mock Interviews", "Cracking the Coding Interview scenarios, concurrency locks, and architecture deep dives.", false),
                        RoadmapMilestone("cs4_4", "Publish Portfolio & Land Senior/Staff Tier Role", "Showcase GitHub repositories, technical articles, and launch production apps.", false)
                    ),
                    coreCompetencies = listOf("AI APIs & Agents", "Vector RAG", "Open Source", "Technical Interviews", "Engineering Leadership")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_cs_fcc",
                    title = "freeCodeCamp - Computer Science & Full Stack Engineering",
                    channel = "freeCodeCamp.org",
                    topic = "Data Structures, Algorithms & Full-Stack",
                    duration = "10+ Hour Deep Dives",
                    searchQuery = "freeCodeCamp Computer Science course",
                    youtubeUrl = "https://www.youtube.com/results?search_query=freecodecamp+computer+science"
                ),
                VideoRecommendation(
                    id = "v_cs_mit",
                    title = "MIT 6.006 - Introduction to Algorithms (Prof. Erik Demaine)",
                    channel = "MIT OpenCourseWare",
                    topic = "Algorithms, Graph Theory, Dynamic Programming",
                    duration = "Full University Course",
                    searchQuery = "MIT 6.006 Introduction to Algorithms",
                    youtubeUrl = "https://www.youtube.com/results?search_query=mit+6.006+algorithms"
                ),
                VideoRecommendation(
                    id = "v_cs_3b1b",
                    title = "3Blue1Brown - Neural Networks, Linear Algebra & Calculus",
                    channel = "3Blue1Brown",
                    topic = "Visual Mathematics & Machine Learning",
                    duration = "Animated Geometry Series",
                    searchQuery = "3Blue1Brown Neural Networks Linear Algebra",
                    youtubeUrl = "https://www.youtube.com/results?search_query=3blue1brown+neural+networks"
                ),
                VideoRecommendation(
                    id = "v_cs_fireship",
                    title = "Fireship - 100 Seconds of Code & System Architecture",
                    channel = "Fireship",
                    topic = "DevOps, Databases, Cloud & Architecture",
                    duration = "High-speed Summaries",
                    searchQuery = "Fireship System Design 100 seconds of code",
                    youtubeUrl = "https://www.youtube.com/results?search_query=fireship+code"
                ),
                VideoRecommendation(
                    id = "v_cs_bytebytego",
                    title = "ByteByteGo - System Design & Large Scale Architecture",
                    channel = "ByteByteGo",
                    topic = "Distributed Systems, Microservices, Caching",
                    duration = "Visual Diagram Series",
                    searchQuery = "ByteByteGo System Design",
                    youtubeUrl = "https://www.youtube.com/results?search_query=bytebytego+system+design"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_cs_cs50",
                    title = "Harvard CS50: Introduction to Computer Science",
                    platform = "edX / Harvard Online",
                    institution = "Harvard University",
                    level = "Beginner - Intermediate",
                    rating = 5.0,
                    courseUrl = "https://www.edx.org/course/introduction-computer-science-harvardx-cs50x",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_cs_stanford_algo",
                    title = "Stanford Algorithms Specialization (Tim Roughgarden)",
                    platform = "Coursera",
                    institution = "Stanford University",
                    level = "Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/specializations/algorithms",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_cs_mit_sys",
                    title = "MIT 6.824: Distributed Systems Engineering",
                    platform = "MIT OpenCourseWare",
                    institution = "Massachusetts Institute of Technology",
                    level = "Advanced",
                    rating = 5.0,
                    courseUrl = "https://pdos.csail.mit.edu/6.824/",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_cs_deeplearning",
                    title = "DeepLearning.AI: Machine Learning Specialization (Andrew Ng)",
                    platform = "Coursera",
                    institution = "DeepLearning.AI / Stanford",
                    level = "Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/specializations/machine-learning-introduction",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "Data Structures (Graphs, Trees, HashTables)",
                "Clean Architecture & Design Patterns",
                "Relational & NoSQL Database Schema Optimization",
                "Distributed System Scalability & Caching",
                "AI Agent & REST API Integration",
                "CI/CD Pipelines & Docker Containers"
            )
        )
    }

    private fun generateEngineeringCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A comprehensive Engineering Roadmap spanning Calculus, Thermodynamics, CAD/CAM Prototyping, Robotics, and FE/PE Professional Licensure.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: Advanced Mathematics & Physics Foundations",
                    duration = "Months 1 - 12",
                    description = "Master Multivariable Calculus, Differential Equations, Classical Mechanics, and Electromagnetism.",
                    milestones = listOf(
                        RoadmapMilestone("eng1_1", "Multivariable Calculus & Linear Algebra", "Vector calculus, partial derivatives, eigenvalues, Laplace transforms.", false),
                        RoadmapMilestone("eng1_2", "Statics & Dynamics (Engineering Mechanics)", "Force equilibrium, free-body diagrams, particle kinetics, and trusses.", false),
                        RoadmapMilestone("eng1_3", "Thermodynamics & Heat Transfer", "Laws of thermodynamics, Carnot cycles, enthalpy, conduction/convection heat transfer.", false),
                        RoadmapMilestone("eng1_4", "Materials Science & Mechanics of Materials", "Stress-strain curves, tensile strength, fatigue failure, crystal structures.", false)
                    ),
                    coreCompetencies = listOf("Vector Calculus", "Statics & Dynamics", "Thermodynamics", "Materials Science")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: CAD Design, Simulation & Circuit Analysis",
                    duration = "Months 13 - 24",
                    description = "Industry-standard CAD modeling (SolidWorks/Fusion360), FEA simulation, and circuit prototyping.",
                    milestones = listOf(
                        RoadmapMilestone("eng2_1", "Parametric 3D CAD & Mechanical Assemblies", "Create complex multi-part parametric models, engineering drawings, and GD&T.", false),
                        RoadmapMilestone("eng2_2", "Finite Element Analysis (FEA) & CFD Simulation", "Simulate structural stress concentrations and fluid dynamics (ANSYS).", false),
                        RoadmapMilestone("eng2_3", "Circuits, Signals & Microcontrollers", "Ohm's/Kirchhoff's laws, op-amps, Arduino/STM32 microcontroller programming in C++.", false),
                        RoadmapMilestone("eng2_4", "Rapid Prototyping & 3D Printing / CNC", "Fabricate physical functional prototypes using additive manufacturing and machining.", false)
                    ),
                    coreCompetencies = listOf("SolidWorks / Fusion 360", "FEA & CFD", "C++ Microcontrollers", "Rapid Prototyping")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: Control Systems, Robotics & Capstone Engineering",
                    duration = "Months 25 - 36",
                    description = "Design feedback control loops, sensor fusion, robotics kinematics, and major engineering capstone.",
                    milestones = listOf(
                        RoadmapMilestone("eng3_1", "Classical & Modern Control Theory", "PID controllers, root locus, Bode plots, state-space representations (MATLAB/Simulink).", false),
                        RoadmapMilestone("eng3_2", "Robotics Kinematics & Embedded Systems", "Forward/inverse kinematics, brushless motor ESC drivers, ROS (Robot Operating System).", false),
                        RoadmapMilestone("eng3_3", "Engineering Economics & Project Management", "Cost-benefit lifecycle analysis, failure mode and effects analysis (FMEA).", false),
                        RoadmapMilestone("eng3_4", "Senior Engineering Capstone Project", "Deliver a fully documented, tested electro-mechanical functional engineering system.", false)
                    ),
                    coreCompetencies = listOf("Control Theory (PID)", "MATLAB / Simulink", "ROS & Kinematics", "FMEA Safety Analysis")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: FE/PE Licensure & Industry Leadership",
                    duration = "Months 37 - 48+",
                    description = "Pass the Fundamentals of Engineering (FE) exam, obtain EIT certification, and lead engineering programs.",
                    milestones = listOf(
                        RoadmapMilestone("eng4_1", "Pass the NCEES Fundamentals of Engineering (FE) Exam", "Earn Engineer-in-Training (EIT) professional certification.", false),
                        RoadmapMilestone("eng4_2", "Lead Multi-disciplinary Technical Teams", "Coordinate between mechanical, electrical, software, and manufacturing teams.", false),
                        RoadmapMilestone("eng4_3", "Quality Assurance & ISO 9001 Compliance", "Implement Six Sigma methodologies and manufacturing tolerances.", false),
                        RoadmapMilestone("eng4_4", "Prepare for Professional Engineer (PE) License", "Accumulate verified qualifying engineering experience under a licensed PE.", false)
                    ),
                    coreCompetencies = listOf("FE / PE Licensure", "Six Sigma Quality", "Cross-functional Leadership", "Systems Architecture")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_real_eng",
                    title = "Real Engineering - Deep Applied Engineering Physics",
                    channel = "Real Engineering",
                    topic = "Aerospace, Materials & Thermodynamics",
                    duration = "High-Quality Case Studies",
                    searchQuery = "Real Engineering channel physics",
                    youtubeUrl = "https://www.youtube.com/results?search_query=real+engineering"
                ),
                VideoRecommendation(
                    id = "v_eng_crash",
                    title = "CrashCourse Engineering - Complete Core Discipline Primer",
                    channel = "CrashCourse",
                    topic = "Mechanical, Civil, Chemical & Electrical Principles",
                    duration = "40+ Episode Series",
                    searchQuery = "CrashCourse Engineering playlist",
                    youtubeUrl = "https://www.youtube.com/results?search_query=crashcourse+engineering"
                ),
                VideoRecommendation(
                    id = "v_brian_douglas",
                    title = "Brian Douglas - Control Systems & State Space Theory",
                    channel = "Brian Douglas",
                    topic = "PID Controls, Bode Plots & Nyquist Stability",
                    duration = "University-level Lessons",
                    searchQuery = "Brian Douglas Control Systems",
                    youtubeUrl = "https://www.youtube.com/results?search_query=brian+douglas+control+systems"
                ),
                VideoRecommendation(
                    id = "v_lesics",
                    title = "Lesics (Learn Engineering) - 3D Mechanical Visualizations",
                    channel = "Lesics",
                    topic = "Turbines, IC Engines, Gears & Transformers",
                    duration = "3D Animation Series",
                    searchQuery = "Lesics Learn Engineering",
                    youtubeUrl = "https://www.youtube.com/results?search_query=lesics+learn+engineering"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_mit_dynamics",
                    title = "MIT OpenCourseWare: Dynamics and Control I (2.003SC)",
                    platform = "MIT OpenCourseWare",
                    institution = "Massachusetts Institute of Technology",
                    level = "Intermediate",
                    rating = 5.0,
                    courseUrl = "https://ocw.mit.edu/courses/2-003sc-engineering-dynamics-fall-2011/",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_georgia_mat",
                    title = "Georgia Tech: Introduction to Engineering Mechanics",
                    platform = "Coursera",
                    institution = "Georgia Institute of Technology",
                    level = "Beginner - Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/learn/engineering-mechanics-statics",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_cad_autodesk",
                    title = "Autodesk Certified CAD/CAM for Mechanical Engineers",
                    platform = "Coursera",
                    institution = "Autodesk",
                    level = "Beginner - Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/specializations/autodesk-cad-cam-cae-mechanical-engineering",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "Multivariable Calculus & Statics Equilibrium",
                "Parametric 3D CAD Modeling & Tolerancing",
                "Finite Element Stress Analysis (FEA)",
                "Microcontroller C++ Hardware Interfacing",
                "PID Feedback Control Tuning",
                "NCEES FE Exam Competency"
            )
        )
    }

    private fun generateBusinessCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A powerhouse track spanning Financial Modeling, Corporate Strategy, Data Analytics, Quantitative Valuations, and Venture Leadership.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: Accounting, Microeconomics & Financial Fundamentals",
                    duration = "Months 1 - 6",
                    description = "Master financial statements, discounted cash flow valuation, and market microeconomics.",
                    milestones = listOf(
                        RoadmapMilestone("biz1_1", "Three-Statement Accounting & Cash Flow Modeling", "Master Income Statement, Balance Sheet, and Cash Flow Statement linkages.", false),
                        RoadmapMilestone("biz1_2", "Micro & Macroeconomics Principles", "Supply-demand elasticity, monetary policy, inflation, and interest rate curves.", false),
                        RoadmapMilestone("biz1_3", "Advanced Excel & Financial Modeling", "INDEX/MATCH, XLOOKUP, sensitivity tables, Monte Carlo simulations, and macros.", false),
                        RoadmapMilestone("biz1_4", "Corporate Valuation Fundamentals (DCF & Comps)", "Discounted Cash Flow, WACC calculations, Trading Comps and Precedent Transactions.", false)
                    ),
                    coreCompetencies = listOf("Accounting Linkages", "Financial Modeling", "DCF Valuation", "Macroeconomics")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: Data Analytics, Marketing & Strategic Management",
                    duration = "Months 7 - 14",
                    description = "Leverage SQL/Python for business intelligence, customer acquisition economics, and competitive strategy.",
                    milestones = listOf(
                        RoadmapMilestone("biz2_1", "SQL & PowerBI for Business Intelligence", "Write multi-table SQL queries, Cohort analysis, customer churn modeling in PowerBI.", false),
                        RoadmapMilestone("biz2_2", "Unit Economics & Customer Acquisition (CAC/LTV)", "Calculate CAC, LTV, churn rates, Payback periods, and viral coefficients.", false),
                        RoadmapMilestone("biz2_3", "Strategic Management & Competitive Moats", "Porter's 5 Forces, Blue Ocean strategy, network effects, and pricing strategy.", false),
                        RoadmapMilestone("biz2_4", "Investment Pitch Deck & Venture Case Study", "Build a complete 12-slide venture capital pitch deck with 5-year financial forecast.", false)
                    ),
                    coreCompetencies = listOf("SQL & Tableau/PowerBI", "Unit Economics", "Competitive Strategy", "Pitch Decks")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: Portfolio Management, Mergers & Acquisitions",
                    duration = "Months 15 - 24",
                    description = "LBO modeling, M&A due diligence, asset allocation, and CFA curriculum preparation.",
                    milestones = listOf(
                        RoadmapMilestone("biz3_1", "Leveraged Buyout (LBO) Modeling", "Build dynamic debt schedules, returns waterfalls, and sensitivity matrix in Excel.", false),
                        RoadmapMilestone("biz3_2", "M&A Due Diligence & Synergy Analysis", "Accretion/dilution modeling, purchase price allocation, antitrust scrutiny.", false),
                        RoadmapMilestone("biz3_3", "Modern Portfolio Theory & Risk Management", "Markowitz efficient frontier, Sharpe ratio, Value at Risk (VaR), beta hedging.", false),
                        RoadmapMilestone("biz3_4", "CFA / Financial Licensure Preparation", "Ethical standards, quantitative methods, fixed income derivatives analysis.", false)
                    ),
                    coreCompetencies = listOf("LBO Modeling", "M&A Diligence", "Portfolio Risk Theory", "CFA Fundamentals")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: Executive Leadership & Venture Scalability",
                    duration = "Months 25 - 36+",
                    description = "Lead enterprise operations, capital allocation, board governance, and global market expansion.",
                    milestones = listOf(
                        RoadmapMilestone("biz4_1", "Capital Structure Optimization", "Balancing equity vs debt financing, convertible notes, and Series A/B syndicates.", false),
                        RoadmapMilestone("biz4_2", "Executive Negotiation & Contract Structuring", "Term sheets, non-competes, stakeholder buy-in, and international trade law.", false),
                        RoadmapMilestone("biz4_3", "Enterprise P&L Management & Scale", "Oversee cross-functional budget allocations and quarterly EBITDA targets.", false),
                        RoadmapMilestone("biz4_4", "Launch Scaled Venture or Direct Fund Portfolio", "Manage institutional capital or scale a profitable high-growth enterprise.", false)
                    ),
                    coreCompetencies = listOf("Capital Allocation", "Negotiation", "P&L Management", "Venture Scaling")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_damodaran",
                    title = "Aswath Damodaran - Corporate Finance & Valuation Masterclasses",
                    channel = "Aswath Damodaran (NYU Stern)",
                    topic = "DCF Valuation, Equity Risk Premiums & Narrative",
                    duration = "Full NYU MBA Lectures",
                    searchQuery = "Aswath Damodaran Valuation Corporate Finance",
                    youtubeUrl = "https://www.youtube.com/results?search_query=aswath+damodaran+valuation"
                ),
                VideoRecommendation(
                    id = "v_biz_hbs",
                    title = "Harvard Business Review - Strategy & Leadership Case Studies",
                    channel = "Harvard Business Review",
                    topic = "Executive Leadership, Negotiation & Innovation",
                    duration = "Case Study Breakdowns",
                    searchQuery = "Harvard Business Review case study",
                    youtubeUrl = "https://www.youtube.com/results?search_query=harvard+business+review"
                ),
                VideoRecommendation(
                    id = "v_plain_bagel",
                    title = "The Plain Bagel - Financial Markets, Macroeconomics & Investing",
                    channel = "The Plain Bagel (CFA)",
                    topic = "Bonds, Equities, Market Cycles & Derivatives",
                    duration = "Engaging Educational Series",
                    searchQuery = "The Plain Bagel financial markets",
                    youtubeUrl = "https://www.youtube.com/results?search_query=the+plain+bagel"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_wharton_fin",
                    title = "Wharton: Business and Financial Modeling Specialization",
                    platform = "Coursera",
                    institution = "University of Pennsylvania (Wharton)",
                    level = "Beginner - Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/specializations/wharton-business-financial-modeling",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_yale_fin",
                    title = "Yale: Financial Markets (Prof. Robert Shiller)",
                    platform = "Coursera",
                    institution = "Yale University",
                    level = "Beginner - Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/learn/financial-markets-global",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_uva_strat",
                    title = "University of Virginia: Business Strategy Specialization",
                    platform = "Coursera",
                    institution = "Darden School of Business",
                    level = "Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/specializations/darden-business-strategy",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "3-Statement Dynamic Financial Modeling in Excel",
                "DCF & Multiples Valuation Metrics (EV/EBITDA, P/E)",
                "SQL & Business Intelligence Dashboarding",
                "Venture Capital & Private Equity Term Sheets",
                "Unit Economics (LTV/CAC Optimization)",
                "Strategic Frameworks & M&A Diligence"
            )
        )
    }

    private fun generateLawCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A prestigious path through Legal Reasoning, Constitutional Law, Case Briefing, Moot Court, and Bar Licensure.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: Legal Reasoning, Logic & LSAT Mastery",
                    duration = "Months 1 - 6",
                    description = "Develop rigorous logical deduction, analytical reasoning, and foundational jurisprudential philosophies.",
                    milestones = listOf(
                        RoadmapMilestone("law1_1", "Formal Logic & Critical Thinking (LSAT)", "Syllogistic logic, flaw identification, conditional reasoning statements.", false),
                        RoadmapMilestone("law1_2", "Introduction to American & Global Legal Systems", "Common law vs civil law traditions, statutory interpretation, judicial review.", false),
                        RoadmapMilestone("law1_3", "Case Law Briefing Method (IRAC)", "Master Issue, Rule, Application, and Conclusion legal writing formatting.", false),
                        RoadmapMilestone("law1_4", "Legal Research Fundamentals (Westlaw/Lexis)", "Shepardizing cases, secondary sources, statutory code index lookups.", false)
                    ),
                    coreCompetencies = listOf("IRAC Writing", "LSAT Logic", "Judicial Precedent", "Legal Research")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: Core 1L Doctrine (Torts, Contracts, Criminal, Property)",
                    duration = "Months 7 - 18",
                    description = "Deep study of the four pillars of foundational jurisprudence.",
                    milestones = listOf(
                        RoadmapMilestone("law2_1", "Contracts & Commercial Transactions (UCC)", "Offer, acceptance, consideration, breach, remedies, promissory estoppel.", false),
                        RoadmapMilestone("law2_2", "Torts & Civil Liability", "Negligence, duty of care, proximate causation, intentional torts, strict liability.", false),
                        RoadmapMilestone("law2_3", "Criminal Law & Constitutional Criminal Procedure", "Mens rea, actus reus, 4th/5th/6th Amendment rights, exclusionary rule.", false),
                        RoadmapMilestone("law2_4", "Property & Real Estate Law", "Fee simple, adverse possession, easements, landlord-tenant law, deeds.", false)
                    ),
                    coreCompetencies = listOf("Contract Law", "Tort Liability", "Constitutional Rights", "Property Law")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: Constitutional Law, Evidence & Advocacy",
                    duration = "Months 19 - 30",
                    description = "Federal jurisdiction, Rules of Evidence, Moot Court, and appellate brief drafting.",
                    milestones = listOf(
                        RoadmapMilestone("law3_1", "Constitutional Law & Equal Protection", "Commerce clause, separation of powers, First Amendment speech, strict scrutiny.", false),
                        RoadmapMilestone("law3_2", "Federal Rules of Evidence", "Hearsay exceptions, character evidence, relevance, witness impeachment.", false),
                        RoadmapMilestone("law3_3", "Moot Court & Oral Argument Advocacy", "Deliver persuasive 15-minute appellate arguments under intense bench questioning.", false),
                        RoadmapMilestone("law3_4", "Legal Clinic & Pro Bono Client Representation", "Draft discovery requests, client affidavits, and negotiate settlements.", false)
                    ),
                    coreCompetencies = listOf("Constitutional Scrutiny", "Rules of Evidence", "Appellate Oral Advocacy", "Client Counseling")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: Bar Examination & Practice Licensure",
                    duration = "Months 31 - 42+",
                    description = "Intensive Multistate Bar Exam (MBE), MEE essays, and professional character accreditation.",
                    milestones = listOf(
                        RoadmapMilestone("law4_1", "Complete 2,000+ MBE Practice Questions", "Daily spaced repetition sets on CivPro, ConLaw, Evidence, Torts, Contracts.", false),
                        RoadmapMilestone("law4_2", "Multistate Essay Examination (MEE) Mastery", "Write 50+ timed 30-minute legal analysis essay responses.", false),
                        RoadmapMilestone("law4_3", "Pass the MPRE (Multistate Professional Responsibility Exam)", "Master Model Rules of Professional Conduct and legal ethics.", false),
                        RoadmapMilestone("law4_4", "Admission to State Bar & Active Law Practice", "Sworn into the jurisdiction bar and begin active counsel or litigation.", false)
                    ),
                    coreCompetencies = listOf("MBE Bar Mastery", "MEE Essay Speed", "Professional Ethics (MPRE)", "Courtroom Litigation")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_law_harvard",
                    title = "Harvard Law School - Masterclasses & Distinguished Lectures",
                    channel = "Harvard Law School",
                    topic = "Constitutional Law, Jurisprudence & Supreme Court Analysis",
                    duration = "University Law Lectures",
                    searchQuery = "Harvard Law School lectures jurisprudence",
                    youtubeUrl = "https://www.youtube.com/results?search_query=harvard+law+school"
                ),
                VideoRecommendation(
                    id = "v_law_quimbee",
                    title = "Quimbee - High Yield Case Briefs & Legal Principles",
                    channel = "Quimbee",
                    topic = "1L Subjects, Torts, Contracts, Constitutional Law",
                    duration = "Visual Animated Case Summaries",
                    searchQuery = "Quimbee legal case briefs",
                    youtubeUrl = "https://www.youtube.com/results?search_query=quimbee+law"
                ),
                VideoRecommendation(
                    id = "v_law_crash",
                    title = "CrashCourse U.S. Government and Politics",
                    channel = "CrashCourse",
                    topic = "Supreme Court, Federal Court System & Civil Liberties",
                    duration = "50 Episode Primer",
                    searchQuery = "CrashCourse US Government and Politics",
                    youtubeUrl = "https://www.youtube.com/results?search_query=crashcourse+government+law"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_upenn_law",
                    title = "Penn Law: An Introduction to American Law",
                    platform = "Coursera",
                    institution = "University of Pennsylvania Carey Law School",
                    level = "Beginner - Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/learn/american-law",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_yale_conlaw",
                    title = "Yale: America's Written Constitution (Prof. Akhil Amar)",
                    platform = "Coursera",
                    institution = "Yale University",
                    level = "Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/learn/constitution",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_harvard_justice",
                    title = "Harvard Online: Justice (Prof. Michael Sandel)",
                    platform = "edX / Harvard Online",
                    institution = "Harvard University",
                    level = "Beginner - Intermediate",
                    rating = 5.0,
                    courseUrl = "https://www.edx.org/course/justice-harvardx-er22-1x",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "IRAC Analytical Legal Writing",
                "Statutory Interpretation & Shepardizing Precedent",
                "Federal Rules of Evidence Application",
                "Moot Court & Persuasive Oral Argumentation",
                "Contract Drafting & Due Diligence",
                "Bar Exam Speed Reading & Precision"
            )
        )
    }

    private fun generatePsychologyCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A scientific track exploring Cognitive Neuroscience, DSM-5 Clinical Diagnostics, Behavioral Research, and Psychotherapeutic Protocols.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: Foundations of Brain, Mind & Research Statistics",
                    duration = "Months 1 - 8",
                    description = "Master cognitive psychology, neuroanatomy, neurochemical transmission, and experimental design statistics.",
                    milestones = listOf(
                        RoadmapMilestone("psy1_1", "Functional Neuroanatomy & Neural Networks", "Limbic system, prefrontal cortex, synaptic plasticity, and neurotransmitters.", false),
                        RoadmapMilestone("psy1_2", "Psychological Research Methods & SPSS/R Statistics", "ANOVA, regression models, double-blind trials, and statistical significance (p-values).", false),
                        RoadmapMilestone("psy1_3", "Cognitive & Developmental Psychology", "Memory formation, Piaget/Vygotsky developmental stages, executive function.", false),
                        RoadmapMilestone("psy1_4", "Behavioral Neuroscience & Sensation/Perception", "Visual cortex pathways, auditory processing, sensory threshold psychophysics.", false)
                    ),
                    coreCompetencies = listOf("Neuroanatomy", "Research Statistics (SPSS/R)", "Memory & Cognition", "Experimental Design")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: Psychopathology & DSM-5 Diagnostic Criteria",
                    duration = "Months 9 - 18",
                    description = "Understand etiology, symptomatology, and diagnostic criteria for mood, anxiety, trauma, and psychotic disorders.",
                    milestones = listOf(
                        RoadmapMilestone("psy2_1", "DSM-5 Mood & Anxiety Disorders", "Major depressive disorder, bipolar affective disorder, GAD, panic, OCD.", false),
                        RoadmapMilestone("psy2_2", "Trauma, Stressor & Personality Disorders", "PTSD neurobiology, borderline, narcissistic, and antisocial personality structures.", false),
                        RoadmapMilestone("psy2_3", "Psychopharmacology & Neurotransmitters", "SSRIs, SNRIs, antipsychotics, mood stabilizers, and GABAergic mechanisms.", false),
                        RoadmapMilestone("psy2_4", "Psychological Assessment Instruments", "MMPI-3, WAIS-IV cognitive battery, Beck Depression Inventory (BDI).", false)
                    ),
                    coreCompetencies = listOf("DSM-5 Diagnostics", "Psychopharmacology", "Psychological Testing", "Trauma Neurobiology")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: Evidence-Based Psychotherapy & Clinical Practice",
                    duration = "Months 19 - 30",
                    description = "Master Cognitive Behavioral Therapy (CBT), Acceptance and Commitment Therapy (ACT), and clinical interviewing.",
                    milestones = listOf(
                        RoadmapMilestone("psy3_1", "Cognitive Behavioral Therapy (CBT) Protocols", "Cognitive restructuring, behavioral activation, exposure therapy hierarchies.", false),
                        RoadmapMilestone("psy3_2", "Mindfulness & ACT Protocols", "Psychological flexibility, defusion techniques, values clarification.", false),
                        RoadmapMilestone("psy3_3", "Clinical Interviewing & Suicide Risk Assessment", "C-SSRS protocols, mental status examinations (MSE), empathetic active listening.", false),
                        RoadmapMilestone("psy3_4", "Supervised Clinical Practicum / Lab Research", "Complete 200+ supervised direct clinical contact hours with case formulations.", false)
                    ),
                    coreCompetencies = listOf("CBT Protocols", "Risk Assessment (MSE)", "Clinical Formulations", "Supervised Practicum")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: Licensure (EPPP), Clinical Specialization & Ph.D./Psy.D.",
                    duration = "Months 31 - 48+",
                    description = "Complete doctoral dissertation or licensing examinations (EPPP/LCSW/LMFT) for full independent practice.",
                    milestones = listOf(
                        RoadmapMilestone("psy4_1", "Pass the Examination for Professional Practice in Psychology (EPPP)", "Score >70% across biological, cognitive, social, and ethical domains.", false),
                        RoadmapMilestone("psy4_2", "Doctoral Dissertation or Capstone Defense", "Publish original empirical research in a peer-reviewed psychology journal.", false),
                        RoadmapMilestone("psy4_3", "Post-Doctoral Clinical Fellowship", "Specialization in Neuropsychology, Pediatric Psychology, or Forensic Psychology.", false),
                        RoadmapMilestone("psy4_4", "Obtain State License & Open Independent Clinic", "Full state board licensure for independent psychotherapeutic practice.", false)
                    ),
                    coreCompetencies = listOf("EPPP Board Licensure", "Empirical Publications", "Neuropsychological Testing", "Independent Clinic")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_psy_huberman",
                    title = "Huberman Lab - Neuroscience, Neuroplasticity & Mental Health",
                    channel = "Andrew Huberman (Stanford)",
                    topic = "Dopamine, Sleep, Neuroplasticity & Stress",
                    duration = "2-Hour Deep Dives",
                    searchQuery = "Huberman Lab Neuroscience psychology",
                    youtubeUrl = "https://www.youtube.com/results?search_query=huberman+lab+neuroscience"
                ),
                VideoRecommendation(
                    id = "v_psy_crash",
                    title = "CrashCourse Psychology - Complete 40-Episode Curriculum",
                    channel = "CrashCourse",
                    topic = "Consciousness, Cognition, DSM Disorders & Therapy",
                    duration = "Comprehensive Playlist",
                    searchQuery = "CrashCourse Psychology complete series",
                    youtubeUrl = "https://www.youtube.com/results?search_query=crashcourse+psychology"
                ),
                VideoRecommendation(
                    id = "v_psy_yale",
                    title = "Yale: Introduction to Psychology (Prof. Paul Bloom)",
                    channel = "YaleCourses",
                    topic = "Development, Freudian theory, Morality & Emotion",
                    duration = "University Lecture Series",
                    searchQuery = "YaleCourses Introduction to Psychology Paul Bloom",
                    youtubeUrl = "https://www.youtube.com/results?search_query=yale+introduction+to+psychology+bloom"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_yale_wellbeing",
                    title = "Yale: The Science of Well-Being & Positive Psychology",
                    platform = "Coursera",
                    institution = "Yale University",
                    level = "Beginner",
                    rating = 4.9,
                    courseUrl = "https://www.coursera.org/learn/the-science-of-well-being",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_chicago_neuro",
                    title = "University of Chicago: Understanding the Brain (Neurobiology)",
                    platform = "Coursera",
                    institution = "The University of Chicago",
                    level = "Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/learn/neurobiology",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_uoft_cbt",
                    title = "University of Toronto: Introduction to Psychology & Clinical CBT",
                    platform = "Coursera",
                    institution = "University of Toronto",
                    level = "Beginner - Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/learn/introduction-psych",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "Functional Neuroanatomy & Synaptic Circuits",
                "DSM-5 Differential Diagnostic Classification",
                "CBT Cognitive Restructuring Protocols",
                "SPSS/R Statistical Experimental Modeling",
                "Mental Status Exam (MSE) & Crisis Assessment",
                "EPPP Licensure Exam Mastery"
            )
        )
    }

    private fun generateGeneralScienceCareerPath(assessment: CareerAssessmentResult): FullCareerPath {
        return FullCareerPath(
            assessment = assessment,
            overallSummary = "A rigorous scientific pathway covering Advanced Calculus, Cellular & Molecular Biology, Organic Chemistry, and Academic Research Publishing.",
            phases = listOf(
                CareerRoadmapPhase(
                    phaseNumber = 1,
                    title = "Phase 1: General Chemistry, Calculus & Scientific Method",
                    duration = "Months 1 - 8",
                    description = "Atomic theory, chemical stoichiometry, thermodynamics, calculus differentiation and integration.",
                    milestones = listOf(
                        RoadmapMilestone("sci1_1", "Stoichiometry & Chemical Equilibrium", "Le Chatelier's principle, acid-base titrations, buffer calculations.", false),
                        RoadmapMilestone("sci1_2", "Calculus & Quantitative Data Modeling", "Integration techniques, differential rate laws, scientific error propagation.", false),
                        RoadmapMilestone("sci1_3", "Cellular Biology & Genetics", "DNA replication, transcription, translation, Mendelian and non-Mendelian inheritance.", false),
                        RoadmapMilestone("sci1_4", "Laboratory Safety & Wet Lab Techniques", "Pipetting accuracy, spectrophotometry, centrifuge protocols, lab notebook ethics.", false)
                    ),
                    coreCompetencies = listOf("General Chemistry", "Calculus", "Cellular Genetics", "Wet Lab Safety")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 2,
                    title = "Phase 2: Organic Chemistry & Molecular Biology",
                    duration = "Months 9 - 18",
                    description = "Reaction mechanisms (SN1/SN2/E1/E2), NMR/IR spectroscopy, recombinant DNA, PCR and electrophoresis.",
                    milestones = listOf(
                        RoadmapMilestone("sci2_1", "Organic Reaction Mechanisms (Arrow Pushing)", "Electrophilic additions, carbonyl chemistry, retrosynthetic analysis.", false),
                        RoadmapMilestone("sci2_2", "Spectroscopy Analysis (1H-NMR, 13C-NMR, IR, Mass Spec)", "Deduce complete unknown chemical structures from spectral peaks.", false),
                        RoadmapMilestone("sci2_3", "Molecular Biology & CRISPR Gene Editing", "PCR amplification, gel electrophoresis, plasmid vector cloning.", false),
                        RoadmapMilestone("sci2_4", "Biochemistry & Enzyme Kinetics", "Michaelis-Menten kinetics, Lineweaver-Burk plots, allosteric regulation.", false)
                    ),
                    coreCompetencies = listOf("Organic Synthesis", "NMR Spectroscopy", "PCR & Cloning", "Enzyme Kinetics")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 3,
                    title = "Phase 3: Advanced Physical Chemistry & Thesis Research",
                    duration = "Months 19 - 30",
                    description = "Quantum chemistry, thermodynamics, statistical mechanics, and conducting faculty laboratory research.",
                    milestones = listOf(
                        RoadmapMilestone("sci3_1", "Quantum Mechanics & Molecular Orbitals", "Schrödinger wave equation, particle in a box, HOMO-LUMO transitions.", false),
                        RoadmapMilestone("sci3_2", "Literature Review & Hypothesis Formulation", "Synthesize 30+ peer-reviewed papers on PubMed/ScienceDirect.", false),
                        RoadmapMilestone("sci3_3", "Experimental Data Collection & Python Data Analysis", "Run automated batch data processing using NumPy/SciPy/Pandas.", false),
                        RoadmapMilestone("sci3_4", "Undergraduate Senior Thesis Defense", "Present research findings before a faculty academic committee.", false)
                    ),
                    coreCompetencies = listOf("Quantum Chemistry", "Literature Synthesis", "Python Data Science", "Thesis Defense")
                ),
                CareerRoadmapPhase(
                    phaseNumber = 4,
                    title = "Phase 4: Graduate Fellowship (Ph.D.) & Journal Publication",
                    duration = "Months 31 - 48+",
                    description = "Publish in peer-reviewed scientific journals, secure NSF/NIH fellowships, and defend doctoral dissertation.",
                    milestones = listOf(
                        RoadmapMilestone("sci4_1", "Submit First-Author Research Article", "Prepare manuscript according to ACS / Nature / Cell publishing guidelines.", false),
                        RoadmapMilestone("sci4_2", "Secure Competitive Research Grant / Fellowship", "Draft NSF Graduate Research Fellowship (GRFP) proposal.", false),
                        RoadmapMilestone("sci4_3", "Present at National Scientific Conferences", "Deliver oral podium presentation and research poster session.", false),
                        RoadmapMilestone("sci4_4", "Complete Doctoral Dissertation & Lab Leadership", "Lead postdoctoral research team or join industrial biotech R&D.", false)
                    ),
                    coreCompetencies = listOf("Peer-Reviewed Publishing", "Grant Writing (NSF/NIH)", "Conference Presentations", "Lab Leadership")
                )
            ),
            videoSuggestions = listOf(
                VideoRecommendation(
                    id = "v_sci_mit_ocw",
                    title = "MIT OpenCourseWare - Complete Chemistry & Biology Courses",
                    channel = "MIT OpenCourseWare",
                    topic = "Organic Chemistry (5.12) & Molecular Biology (7.01)",
                    duration = "Full University Courses",
                    searchQuery = "MIT OpenCourseWare Organic Chemistry Biology",
                    youtubeUrl = "https://www.youtube.com/results?search_query=mit+opencourseware+chemistry"
                ),
                VideoRecommendation(
                    id = "v_sci_prof_dave",
                    title = "Professor Dave Explains - General, Organic & Physical Chemistry",
                    channel = "Professor Dave Explains",
                    topic = "Step-by-step Chemical Reaction Mechanisms",
                    duration = "High-Yield Tutorials",
                    searchQuery = "Professor Dave Explains Organic Chemistry",
                    youtubeUrl = "https://www.youtube.com/results?search_query=professor+dave+explains+chemistry"
                ),
                VideoRecommendation(
                    id = "v_sci_veritasium",
                    title = "Veritasium - Experimental Physics & Scientific Discoveries",
                    channel = "Veritasium",
                    topic = "Quantum Physics, Optics & Applied Science",
                    duration = "Documentary Style",
                    searchQuery = "Veritasium science physics",
                    youtubeUrl = "https://www.youtube.com/results?search_query=veritasium"
                )
            ),
            classSuggestions = listOf(
                OnlineClassRecommendation(
                    id = "c_mit_bioc",
                    title = "MIT: Introduction to Biology - The Secret of Life",
                    platform = "edX / MITx",
                    institution = "Massachusetts Institute of Technology",
                    level = "Intermediate",
                    rating = 4.9,
                    courseUrl = "https://www.edx.org/course/introduction-to-biology-the-secret-of-life-3",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_rice_ochem",
                    title = "Rice University: Organic Chemistry I & II Specialization",
                    platform = "Coursera",
                    institution = "Rice University",
                    level = "Intermediate",
                    rating = 4.8,
                    courseUrl = "https://www.coursera.org/specializations/organic-chemistry",
                    isFree = true
                ),
                OnlineClassRecommendation(
                    id = "c_harvard_genetics",
                    title = "Harvard Online: Case Studies in Functional Genomics",
                    platform = "edX / Harvard Online",
                    institution = "Harvard University",
                    level = "Advanced",
                    rating = 4.9,
                    courseUrl = "https://www.edx.org/course/case-studies-in-functional-genomics",
                    isFree = true
                )
            ),
            keySkillsToMaster = listOf(
                "Organic Reaction Mechanism Synthesis",
                "NMR & IR Spectroscopy Structure Elucidation",
                "PCR, Recombinant Cloning & CRISPR Editing",
                "Python (NumPy/Pandas) Data Modeling",
                "Scientific Manuscript Peer Review",
                "NSF/NIH Grant Proposal Preparation"
            )
        )
    }
}
