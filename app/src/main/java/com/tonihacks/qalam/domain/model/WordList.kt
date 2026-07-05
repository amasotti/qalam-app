package com.tonihacks.qalam.domain.model

data class WordListSummary(
    val id: String,
    val title: String,
    val description: String?,
    val itemCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

data class WordListDetail(
    val id: String,
    val title: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val words: List<Word>,
)

data class WordListDraft(
    val title: String,
    val description: String?,
)

data class WordListSuggestion(
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val partOfSpeech: String?,
    val difficulty: String?,
)
