package com.tonihacks.qalam.domain.model

data class TextPassage(
    val id: String,
    val title: String,
    val body: String,
    val transliteration: String?,
    val translation: String?,
    val difficulty: String,
    val dialect: String,
    val tags: List<String> = emptyList(),
)

data class TextSentence(
    val id: String,
    val position: Int,
    val arabicText: String,
    val transliteration: String?,
    val freeTranslation: String?,
    val tokens: List<TextToken> = emptyList(),
)

data class TextToken(
    val id: String,
    val sentenceId: String,
    val position: Int,
    val arabic: String,
    val transliteration: String?,
    val translation: String?,
    val wordId: String?,
)
