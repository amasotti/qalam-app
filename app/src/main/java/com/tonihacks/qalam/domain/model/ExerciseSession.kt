package com.tonihacks.qalam.domain.model

data class ExerciseSession(
    val id: String,
    val mode: String,
    val status: String,
    val items: List<ExerciseItem>,
)

enum class ExerciseType {
    MULTIPLE_CHOICE_MEANING,
    MULTIPLE_CHOICE_ARABIC,
    CONFUSABLE_MEANING,
    CONFUSABLE_ARABIC,
}

data class ExerciseItem(
    val id: String,
    val wordId: String,
    val type: String,
    val prompt: ExercisePrompt,
    val options: List<ExerciseOption>,
    val result: ExerciseResult?,
    val selectedOptionId: String?,
)

data class ExercisePrompt(
    val kind: ExercisePromptKind,
    val text: String,
)

enum class ExercisePromptKind { ARABIC_WORD, TRANSLATION }

data class ExerciseOption(
    val id: String,
    val wordId: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
)

enum class ExerciseResult { CORRECT, INCORRECT, SKIPPED }

data class ExerciseAnswer(
    val itemId: String,
    val wordId: String,
    val result: ExerciseResult,
    val correctOptionId: String,
    val masteryPromotion: MasteryPromotion?,
)

data class ExerciseSessionSummary(
    val sessionId: String,
    val mode: String,
    val totalItems: Int,
    val correct: Int,
    val incorrect: Int,
    val skipped: Int,
    val accuracy: Double,
    val promotions: List<MasteryPromotion>,
)
