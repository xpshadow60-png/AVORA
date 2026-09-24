package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FlashcardEntity
import com.example.data.srs.ReviewRating
import com.example.data.srs.SpacedRepetitionEngine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    flashcards: List<FlashcardEntity>,
    onAddFlashcard: (String, String, String, String) -> Unit,
    onUpdateFlashcard: (FlashcardEntity) -> Unit,
    onDeleteFlashcard: (FlashcardEntity) -> Unit,
    onReviewFlashcard: (FlashcardEntity, ReviewRating) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingCard by remember { mutableStateOf<FlashcardEntity?>(null) }
    var isReviewModeActive by remember { mutableStateOf(false) }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val now = System.currentTimeMillis()
    val dueCards = remember(flashcards, now) {
        flashcards.filter { it.nextReviewAt <= now }
    }

    // Retention Rate Calculation
    val totalReviewsLifetime = flashcards.sumOf { it.totalReviews }
    val totalSuccessfulLifetime = flashcards.sumOf { it.successfulReviews }
    val retentionRatePercent = if (totalReviewsLifetime > 0) {
        ((totalSuccessfulLifetime.toDouble() / totalReviewsLifetime) * 100).toInt()
    } else {
        100
    }

    val masteredCardsCount = flashcards.count { it.intervalDays >= 7 }
    val learningCardsCount = flashcards.count { it.intervalDays < 7 }

    val availableSubjects = remember(flashcards) {
        listOf("All") + flashcards.map { it.subject }.distinct().sorted()
    }

    val filteredCards = remember(flashcards, selectedSubjectFilter, searchQuery) {
        flashcards.filter { card ->
            val matchesSubject = selectedSubjectFilter == "All" || card.subject.equals(selectedSubjectFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || 
                card.question.contains(searchQuery, ignoreCase = true) || 
                card.answer.contains(searchQuery, ignoreCase = true) ||
                card.topic.contains(searchQuery, ignoreCase = true)
            matchesSubject && matchesQuery
        }
    }

    if (isReviewModeActive) {
        FlashcardReviewSession(
            dueCards = if (selectedSubjectFilter == "All") dueCards else dueCards.filter { it.subject.equals(selectedSubjectFilter, ignoreCase = true) },
            onReviewCard = onReviewFlashcard,
            onExitSession = { isReviewModeActive = false }
        )
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary500,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_flashcard_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Flashcard")
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
            ) {
                // Header & Review Hero Banner
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth().testTag("srs_hero_card")
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Spaced Repetition Decks",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                                    )
                                    Text(
                                        text = "SuperMemo SM-2 smart recall scheduling",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Primary500.copy(alpha = 0.15f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Psychology,
                                            contentDescription = "Memory",
                                            tint = Primary500,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // 3-Column Stats Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatMiniMetric(
                                    title = "Total Cards",
                                    value = "${flashcards.size}",
                                    subtitle = "$masteredCardsCount Mastered",
                                    color = Primary500
                                )
                                Divider(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .width(1.dp)
                                        .align(Alignment.CenterVertically),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                StatMiniMetric(
                                    title = "Due Today",
                                    value = "${dueCards.size}",
                                    subtitle = if (dueCards.isEmpty()) "All caught up" else "Ready to review",
                                    color = if (dueCards.isNotEmpty()) CoralPriority else GreenSuccess
                                )
                                Divider(
                                    modifier = Modifier
                                        .height(36.dp)
                                        .width(1.dp)
                                        .align(Alignment.CenterVertically),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                StatMiniMetric(
                                    title = "Retention Rate",
                                    value = "$retentionRatePercent%",
                                    subtitle = "$totalSuccessfulLifetime/$totalReviewsLifetime recalls",
                                    color = CyanAccent
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Start Review CTA Button
                            Button(
                                onClick = { isReviewModeActive = true },
                                enabled = dueCards.isNotEmpty() || flashcards.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (dueCards.isNotEmpty()) Primary500 else CyanAccentDark,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_review_button")
                            ) {
                                Icon(
                                    imageVector = if (dueCards.isNotEmpty()) Icons.Default.PlayArrow else Icons.Default.Replay,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (dueCards.isNotEmpty()) {
                                        "Review Due Cards (${dueCards.size} Due)"
                                    } else {
                                        "Practice All Cards (${flashcards.size} Cards)"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Search and Subject Filters
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search questions, topics, or answers...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Primary500)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("search_flashcards_field")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(availableSubjects) { subject ->
                                FilterChip(
                                    selected = selectedSubjectFilter == subject,
                                    onClick = { selectedSubjectFilter = subject },
                                    label = {
                                        val count = if (subject == "All") flashcards.size else flashcards.count { it.subject.equals(subject, ignoreCase = true) }
                                        Text("$subject ($count)")
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary500,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                // Flashcard List Items
                if (filteredCards.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Style,
                                    contentDescription = null,
                                    tint = Primary500,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (flashcards.isEmpty()) "No flashcards created yet!" else "No flashcards match your filter.",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Tap the + button to create your first question and answer card.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Deck Cards (${filteredCards.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(filteredCards, key = { it.id }) { card ->
                        FlashcardListItem(
                            card = card,
                            isDue = card.nextReviewAt <= now,
                            onEdit = { editingCard = card },
                            onDelete = { onDeleteFlashcard(card) }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Flashcard Dialog
    if (showAddDialog || editingCard != null) {
        FlashcardEditDialog(
            initialCard = editingCard,
            onDismiss = {
                showAddDialog = false
                editingCard = null
            },
            onSave = { question, answer, subject, topic ->
                if (editingCard != null) {
                    onUpdateFlashcard(
                        editingCard!!.copy(
                            question = question,
                            answer = answer,
                            subject = subject,
                            topic = topic
                        )
                    )
                } else {
                    onAddFlashcard(question, answer, subject, topic)
                }
                showAddDialog = false
                editingCard = null
            }
        )
    }
}

@Composable
private fun StatMiniMetric(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = color
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FlashcardListItem(
    card: FlashcardEntity,
    isDue: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDue) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("flashcard_item_${card.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = card.subject,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Primary500,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = card.topic,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isDue) CoralPriority.copy(alpha = 0.15f) else GreenSuccess.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isDue) Icons.Default.Schedule else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDue) CoralPriority else GreenSuccess,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isDue) "Due Now" else "Interval: ${card.intervalDays}d",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isDue) CoralPriority else GreenSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Question
            Text(
                text = card.question,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // Answer (Expanded)
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "ANSWER:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = CyanAccent
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val retention = if (card.totalReviews > 0) "${((card.successfulReviews.toDouble() / card.totalReviews) * 100).toInt()}% recall" else "New Card"
                        Text(
                            text = "Reviews: ${card.totalReviews} • $retention • Ease: ${String.format("%.1f", card.easeFactor)}x",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Primary500, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralPriority, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (!isExpanded) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap to view answer & stats",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = Primary500.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun FlashcardReviewSession(
    dueCards: List<FlashcardEntity>,
    onReviewCard: (FlashcardEntity, ReviewRating) -> Unit,
    onExitSession: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var completedCardsCount by remember { mutableStateOf(0) }
    var sessionSuccessCount by remember { mutableStateOf(0) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_flip"
    )

    if (dueCards.isEmpty() || currentIndex >= dueCards.size) {
        // Session Completed Celebration Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GreenSuccess.copy(alpha = 0.15f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Done",
                                tint = GreenSuccess,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Text(
                        text = "Session Complete! 🎉",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "You've reviewed $completedCardsCount flashcards in this session. Your future intervals have been updated with Spaced Repetition!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    if (completedCardsCount > 0) {
                        val scorePct = ((sessionSuccessCount.toDouble() / completedCardsCount) * 100).toInt()
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary500.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Session Accuracy:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("$scorePct% ($sessionSuccessCount / $completedCardsCount)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = Primary500)
                            }
                        }
                    }

                    Button(
                        onClick = onExitSession,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Return to Decks", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentCard = dueCards[currentIndex]
    val progress = (currentIndex.toFloat() / dueCards.size.toFloat())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Session Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExitSession) {
                Icon(Icons.Default.Close, contentDescription = "Exit Session")
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Card ${currentIndex + 1} of ${dueCards.size}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${currentCard.subject} • ${currentCard.topic}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Primary500.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "SRS Active",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Primary500,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Linear Progress Indicator
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Primary500,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        // 3D Flipping Flashcard Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFlipped) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isFlipped = !isFlipped
                    }
                    .testTag("flashcard_flip_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // FRONT: QUESTION
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "QUESTION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = Primary500
                                )

                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = "Tap to Flip",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = currentCard.question,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 32.sp
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "👆 Tap card to reveal answer",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    } else {
                        // BACK: ANSWER (mirror text so it isn't backwards after 180 deg flip)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer { rotationY = 180f },
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "ANSWER & EXPLANATION",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = CyanAccent
                                )

                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Text(
                                text = currentCard.answer,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 26.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text(
                                text = "Rate your recall quality below 👇",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Bottom Rating Controls (SM-2 SRS Buttons)
        if (!isFlipped) {
            Button(
                onClick = { isFlipped = true },
                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("reveal_answer_button")
            ) {
                Icon(Icons.Default.Visibility, contentDescription = "Reveal")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Show Answer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "How well did you remember this card?",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Rating 1: Again
                    SrsRatingButton(
                        rating = ReviewRating.AGAIN,
                        card = currentCard,
                        color = CoralPriority,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onReviewCard(currentCard, ReviewRating.AGAIN)
                            completedCardsCount++
                            isFlipped = false
                            currentIndex++
                        }
                    )

                    // Rating 2: Hard
                    SrsRatingButton(
                        rating = ReviewRating.HARD,
                        card = currentCard,
                        color = OrangePriority,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onReviewCard(currentCard, ReviewRating.HARD)
                            completedCardsCount++
                            isFlipped = false
                            currentIndex++
                        }
                    )

                    // Rating 3: Good
                    SrsRatingButton(
                        rating = ReviewRating.GOOD,
                        card = currentCard,
                        color = Primary500,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onReviewCard(currentCard, ReviewRating.GOOD)
                            completedCardsCount++
                            sessionSuccessCount++
                            isFlipped = false
                            currentIndex++
                        }
                    )

                    // Rating 4: Easy
                    SrsRatingButton(
                        rating = ReviewRating.EASY,
                        card = currentCard,
                        color = CyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onReviewCard(currentCard, ReviewRating.EASY)
                            completedCardsCount++
                            sessionSuccessCount++
                            isFlipped = false
                            currentIndex++
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SrsRatingButton(
    rating: ReviewRating,
    card: FlashcardEntity,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val estimatedLabel = SpacedRepetitionEngine.getEstimatedIntervalLabel(card, rating)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f)),
        modifier = modifier
            .height(58.dp)
            .clickable { onClick() }
            .testTag("rate_button_${rating.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = rating.label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Text(
                text = estimatedLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FlashcardEditDialog(
    initialCard: FlashcardEntity? = null,
    onDismiss: () -> Unit,
    onSave: (question: String, answer: String, subject: String, topic: String) -> Unit
) {
    var question by remember { mutableStateOf(initialCard?.question ?: "") }
    var answer by remember { mutableStateOf(initialCard?.answer ?: "") }
    var subject by remember { mutableStateOf(initialCard?.subject ?: "Mathematics") }
    var topic by remember { mutableStateOf(initialCard?.topic ?: "") }

    val presetSubjects = listOf("Mathematics", "Computer Science", "Physics", "Biology", "Chemistry", "History", "Literature", "General")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCard == null) "Create Flashcard" else "Edit Flashcard",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subject selector
                Text(
                    text = "Subject Category",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(presetSubjects) { s ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (subject == s) Primary500 else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { subject = s }
                        ) {
                            Text(
                                text = s,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (subject == s) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic / Chapter (e.g. Calculus, Data Structures)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Front (Question / Prompt)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("flashcard_question_input")
                )

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Back (Answer / Solution)") },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("flashcard_answer_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (question.isNotBlank() && answer.isNotBlank()) {
                        onSave(question, answer, subject, topic.ifBlank { "General" })
                    }
                },
                enabled = question.isNotBlank() && answer.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                modifier = Modifier.testTag("save_flashcard_button")
            ) {
                Text("Save Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
