package com.tonihacks.qalam.domain.model

data class Word(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val partOfSpeech: String,
    val dialect: String,
    val masteryLevel: MasteryLevel,
    val rootId: String?,
    val notes: String?,
    val pronunciationUrl: String?,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val dictionaries: List<DictionaryLink> = emptyList(),
)

data class DictionaryLink(
    val id: String,
    val source: String,
    val url: String,
)

data class Example(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
)

data class WordDraft(
    val arabicText: String,
    val translation: String,
    val transliteration: String? = null,
    val partOfSpeech: String? = null,
    val dialect: String? = null,
)

data class PagedResult<T>(
    val items: List<T>,
    val total: Int,
    val hasMore: Boolean,
)

