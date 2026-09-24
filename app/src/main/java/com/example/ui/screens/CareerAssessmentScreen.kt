package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.career.CareerPathEngine
import com.example.data.model.CareerAssessmentResult
import com.example.data.model.CareerOption
import com.example.ui.components.TechAtmosphereBackground
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500
import androidx.compose.foundation.isSystemInDarkTheme

@Composable
fun CareerAssessmentScreen(
    onAssessmentCompleted: (CareerAssessmentResult) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val questions = remember { CareerPathEngine.assessmentQuestions }
    var currentStepIndex by remember { mutableStateOf(0) }
    val selectedOptionByQuestion = remember { mutableStateMapOf<Int, CareerOption>() }

    val currentQuestion = questions[currentStepIndex]
    val totalSteps = questions.size
    val progress = (currentStepIndex + 1).toFloat() / totalSteps.toFloat()
    val scrollState = rememberScrollState()

    fun completeAssessment() {
        val q1 = selectedOptionByQuestion[1]?.title ?: "Medicine & Healthcare"
        val q2 = selectedOptionByQuestion[2]?.title ?: "Physician / Specialist"
        val q3 = selectedOptionByQuestion[3]?.title ?: "Undergraduate"
        val q4 = selectedOptionByQuestion[4]?.title ?: "10 - 20 Hours / week"
        val q5 = selectedOptionByQuestion[5]?.title ?: "Visual & Video Masterclasses"
        val q6 = selectedOptionByQuestion[6]?.title ?: "Time Management & Heavy Workload"
        val q7 = selectedOptionByQuestion[7]?.title ?: "1 - 2 Years"
        val q8 = selectedOptionByQuestion[8]?.title ?: "Professional Board / Licensure"
        val q9 = selectedOptionByQuestion[9]?.title ?: "University MOOCs & Courseware"
        val q10 = selectedOptionByQuestion[10]?.title ?: "Pomodoro Focus Sprints"

        val result = CareerAssessmentResult(
            fieldOfStudy = q1,
            specialization = q2,
            academicLevel = q3,
            weeklyStudyHours = q4,
            learningStyle = q5,
            primaryChallenge = q6,
            timelineGoal = q7,
            certificationGoal = q8,
            resourcePreference = q9,
            dailyStudyHabit = q10,
            completedAt = System.currentTimeMillis()
        )
        onAssessmentCompleted(result)
    }

    TechAtmosphereBackground(
        modifier = modifier.fillMaxSize(),
        isDark = isSystemInDarkTheme()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Header: Step progress & Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Primary500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Question ${currentStepIndex + 1} of $totalSteps",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Primary500,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Animated Linear Progress Bar
            LinearProgressIndicator(
                progress = progress,
                color = CyanAccent,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Question Content (Scrollable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = currentQuestion.title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.2.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = currentQuestion.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Options List
                currentQuestion.options.forEach { option ->
                    val isSelected = selectedOptionByQuestion[currentQuestion.id]?.id == option.id

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Primary500.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) Primary500 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selectedOptionByQuestion[currentQuestion.id] = option
                            }
                            .testTag("option_${option.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Emoji / Icon Box
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Primary500 else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = option.iconEmoji,
                                    fontSize = 20.sp
                                )
                            }

                            // Title & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = option.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    ),
                                    color = if (isSelected) Primary500 else MaterialTheme.colorScheme.onSurface
                                )
                                if (option.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = option.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Radio Check Icon
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Primary500 else Color.Transparent
                                    )
                                    .border(
                                        width = 2.dp,
                                        color = if (isSelected) Primary500 else MaterialTheme.colorScheme.outlineVariant,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Bottom Navigation Actions (Back / Next)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.4f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    val isOptionSelected = selectedOptionByQuestion.containsKey(currentQuestion.id)

                    Button(
                        onClick = {
                            if (currentStepIndex < totalSteps - 1) {
                                currentStepIndex++
                            } else {
                                completeAssessment()
                            }
                        },
                        enabled = isOptionSelected,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(if (currentStepIndex > 0) 0.6f else 1f)
                            .height(48.dp)
                            .testTag("assessment_next_button")
                    ) {
                        if (currentStepIndex < totalSteps - 1) {
                            Text(
                                text = "Next Question",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = "Generate My Career Path 🚀",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
