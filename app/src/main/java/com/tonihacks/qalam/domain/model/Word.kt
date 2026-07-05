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

data class DictionaryLinkDraft(
    val source: String,
    val url: String,
)

data class Example(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
)

/** AI-generated example sentence — ephemeral until the user saves it as an [Example]. */
data class AiExample(
    val arabic: String,
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

data class WordUpdate(
    val arabicText: String? = null,
    val transliteration: String? = null,
    val translation: String? = null,
    val partOfSpeech: String? = null,
    val dialect: String? = null,
    val difficulty: String? = null,
    val masteryLevel: String? = null,
    val pronunciationUrl: String? = null,
    val rootId: String? = null,
    val derivedFromId: String? = null,
    val notes: String? = null,
)

data class PagedResult<T>(
    val items: List<T>,
    val total: Int,
    val hasMore: Boolean,
)

data class DictionaryLookupItem(
    val externalId: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val pluralArabic: String?,
    val hasExactWordMatch: Boolean,
)

data class WordAutocomplete(
    val id: String,
    val arabicText: String,
    val translation: String?,
)

data class WordMorphology(
    val gender: String?,
    val verbPattern: String?,
    val plurals: List<WordPlural>,
)

data class WordPlural(
    val id: String,
    val pluralForm: String,
    val pluralType: String,
)

data class WordPluralDraft(
    val pluralForm: String,
    val pluralType: String = "BROKEN",
)

data class WordRelation(
    val relatedWordId: String,
    val relatedWordArabic: String,
    val relatedWordTranslation: String?,
    val relationType: String,
)

data class WordRelationDraft(
    val relatedWordId: String,
    val relationType: String,
)

data class WordEnrichmentSuggestion(
    val notes: String?,
    val gender: String?,
    val verbPattern: String?,
    val plurals: List<AiPluralSuggestion>,
    val relations: List<AiRelationSuggestion>,
)

data class AiPluralSuggestion(
    val pluralForm: String,
    val pluralType: String,
)

data class AiRelationSuggestion(
    val arabicText: String,
    val relationType: String,
    val transliteration: String?,
    val translation: String?,
)
