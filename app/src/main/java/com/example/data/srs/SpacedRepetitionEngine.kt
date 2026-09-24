package com.example.data.srs

import com.example.data.local.FlashcardEntity

enum class ReviewRating(val value: Int, val label: String, val subtitle: String) {
    AGAIN(1, "Again", "Forgot • 1d"),
    HARD(2, "Hard", "Struggled"),
    GOOD(3, "Good", "Normal"),
    EASY(4, "Easy", "Mastered")
}

object SpacedRepetitionEngine {
    private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L

    fun calculateNextReview(
        card: FlashcardEntity,
        rating: ReviewRating,
        now: Long = System.currentTimeMillis()
    ): FlashcardEntity {
        val newInterval: Int
        val newRepetitions: Int
        var newEaseFactor = card.easeFactor
        val isSuccess = rating.value >= 3
        val newTotalReviews = card.totalReviews + 1
        val newSuccessfulReviews = if (isSuccess) card.successfulReviews + 1 else card.successfulReviews

        when (rating) {
            ReviewRating.AGAIN -> {
                newRepetitions = 0
                newInterval = 1
                newEaseFactor = maxOf(1.3, card.easeFactor - 0.2)
            }
            ReviewRating.HARD -> {
                newRepetitions = card.repetitions + 1
                newInterval = if (card.repetitions == 0) 1 else maxOf(1, (card.intervalDays * 1.2).toInt())
                newEaseFactor = maxOf(1.3, card.easeFactor - 0.15)
            }
            ReviewRating.GOOD -> {
                newRepetitions = card.repetitions + 1
                newInterval = when (card.repetitions) {
                    0 -> 1
                    1 -> 3
                    else -> maxOf(1, (card.intervalDays * card.easeFactor).toInt())
                }
            }
            ReviewRating.EASY -> {
                newRepetitions = card.repetitions + 1
                newInterval = when (card.repetitions) {
                    0 -> 4
                    1 -> 7
                    else -> maxOf(2, (card.intervalDays * card.easeFactor * 1.3).toInt())
                }
                newEaseFactor = minOf(3.0, card.easeFactor + 0.15)
            }
        }

        val nextReviewTimestamp = now + (newInterval.toLong() * ONE_DAY_MS)

        return card.copy(
            intervalDays = newInterval,
            easeFactor = newEaseFactor,
            repetitions = newRepetitions,
            nextReviewAt = nextReviewTimestamp,
            lastReviewedAt = now,
            totalReviews = newTotalReviews,
            successfulReviews = newSuccessfulReviews,
            lastRating = rating.name
        )
    }

    fun getEstimatedIntervalLabel(card: FlashcardEntity, rating: ReviewRating): String {
        val days = when (rating) {
            ReviewRating.AGAIN -> 1
            ReviewRating.HARD -> if (card.repetitions == 0) 1 else maxOf(1, (card.intervalDays * 1.2).toInt())
            ReviewRating.GOOD -> when (card.repetitions) {
                0 -> 1
                1 -> 3
                else -> maxOf(1, (card.intervalDays * card.easeFactor).toInt())
            }
            ReviewRating.EASY -> when (card.repetitions) {
                0 -> 4
                1 -> 7
                else -> maxOf(2, (card.intervalDays * card.easeFactor * 1.3).toInt())
            }
        }
        return if (days == 1) "1 day" else "$days days"
    }
}
