package com.tonihacks.qalam.domain.model

data class TrainingSession(
    val id: String,
    val mode: String,
    val status: String,
    val words: List<TrainingWord>,
)

data class TrainingWord(
    val id: String,
    val frontSide: FlashcardSide,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val partOfSpeech: String?,
    val position: Int,
    val masteryLevel: MasteryLevel,
    val root: String?,
    val notes: String?,
    val examples: List<Example>,
    val relations: List<TrainingWordRelation>,
)

enum class FlashcardSide { ARABIC, TRANSLATION }

data class TrainingWordResult(
    val wordId: String,
    val knewIt: Boolean,
)

data class TrainingWordRelation(
    val relatedWordId: String,
    val relatedWordArabic: String,
    val relatedWordTranslation: String?,
    val relationType: String,
)

data class RecordedTrainingResult(
    val wordId: String,
    val result: String,
    val masteryPromotion: MasteryPromotion?,
)

data class MasteryPromotion(
    val wordId: String,
    val from: MasteryLevel,
    val to: MasteryLevel,
)

data class TrainingSessionSummary(
    val sessionId: String,
    val mode: String,
    val totalWords: Int,
    val correct: Int,
    val incorrect: Int,
    val skipped: Int,
    val accuracy: Double,
    val promotions: List<MasteryPromotion>,
)
