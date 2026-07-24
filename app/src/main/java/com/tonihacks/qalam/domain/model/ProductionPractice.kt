package com.tonihacks.qalam.domain.model

data class ProductionPracticePrompt(
    val words: List<ProductionPracticeWord>,
)

data class ProductionPracticeWord(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val partOfSpeech: String,
    val dialect: String,
)

data class ProductionPracticeSubmission(
    val sentence: String,
    val targetWordIds: List<String>,
    val usedWordIds: List<String>,
)

data class ProductionPracticeReview(
    val markdown: String,
)
