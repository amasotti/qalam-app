package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.*
import kotlinx.serialization.Serializable

@Serializable
data class DictionaryLookupPluralDto(
    val arabicText: String,
    val transliteration: String? = null,
)

@Serializable
data class DictionaryLookupItemDto(
    val externalId: String,
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val plural: DictionaryLookupPluralDto? = null,
    val hasExactWordMatch: Boolean,
)

@Serializable
data class DictionaryLookupResponseDto(
    val source: String,
    val query: String,
    val items: List<DictionaryLookupItemDto>,
)

fun DictionaryLookupItemDto.toDomain() = DictionaryLookupItem(
    externalId = externalId,
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    pluralArabic = plural?.arabicText,
    hasExactWordMatch = hasExactWordMatch,
)

@Serializable
data class WordDto(
    val id: String,
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val partOfSpeech: String,
    val dialect: String,
    val difficulty: String,
    val masteryLevel: String, // backend sends "NEW" / "LEARNING" / "KNOWN" / "MASTERED"
    val rootId: String? = null,
    val notes: String? = null,
    val pronunciationUrl: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
)

@Serializable
data class DictionaryLinkDto(
    val id: String,
    val source: String,
    val url: String,
)

@Serializable
data class CreateDictionaryLinkRequestDto(
    val source: String,
    val url: String,
)

@Serializable
data class WordDraftDto(
    val arabicText: String,
    val translation: String,
    val transliteration: String? = null,
    val partOfSpeech: String? = null,
    val dialect: String? = null,
)

@Serializable
data class UpdateWordRequestDto(
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

@Serializable
data class WordAutocompleteDto(
    val id: String,
    val arabicText: String,
    val translation: String? = null,
)

@Serializable
data class WordMorphologyDto(
    val gender: String? = null,
    val verbPattern: String? = null,
    val plurals: List<WordPluralDto> = emptyList(),
)

@Serializable
data class WordPluralDto(
    val id: String,
    val pluralForm: String,
    val pluralType: String,
)

@Serializable
data class CreateWordPluralRequestDto(
    val pluralForm: String,
    val pluralType: String = "BROKEN",
)

@Serializable
data class UpsertWordMorphologyRequestDto(
    val gender: String? = null,
    val verbPattern: String? = null,
)

@Serializable
data class WordRelationDto(
    val relatedWordId: String,
    val relatedWordArabic: String,
    val relatedWordTranslation: String? = null,
    val relationType: String,
)

@Serializable
data class CreateWordRelationRequestDto(
    val relatedWordId: String,
    val relationType: String,
)

@Serializable
data class WordEnrichmentSuggestionDto(
    val notes: String? = null,
    val gender: String? = null,
    val verbPattern: String? = null,
    val plurals: List<AiPluralSuggestionDto> = emptyList(),
    val relations: List<AiRelationSuggestionDto> = emptyList(),
)

@Serializable
data class AiPluralSuggestionDto(
    val pluralForm: String,
    val pluralType: String,
)

@Serializable
data class AiRelationSuggestionDto(
    val arabicText: String,
    val relationType: String,
    val transliteration: String? = null,
    val translation: String? = null,
)

fun WordDto.toDomain() = Word(
    id = id,
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    partOfSpeech = partOfSpeech,
    dialect = dialect,
    masteryLevel = runCatching { MasteryLevel.valueOf(masteryLevel) }.getOrDefault(MasteryLevel.NEW),
    rootId = rootId,
    notes = notes,
    pronunciationUrl = pronunciationUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun ExampleDto.toDomain() = Example(id, arabic, transliteration, translation)

fun DictionaryLinkDto.toDomain() = DictionaryLink(id, source, url)

fun WordDraft.toDto() = WordDraftDto(arabicText, translation, transliteration, partOfSpeech, dialect)

fun WordUpdate.toDto() = UpdateWordRequestDto(
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    partOfSpeech = partOfSpeech,
    dialect = dialect,
    difficulty = difficulty,
    masteryLevel = masteryLevel,
    pronunciationUrl = pronunciationUrl,
    rootId = rootId,
    derivedFromId = derivedFromId,
    notes = notes,
)

fun DictionaryLinkDraft.toDto() = CreateDictionaryLinkRequestDto(source, url)

fun WordAutocompleteDto.toDomain() = WordAutocomplete(id, arabicText, translation)

fun WordMorphologyDto.toDomain() = WordMorphology(
    gender = gender,
    verbPattern = verbPattern,
    plurals = plurals.map { it.toDomain() },
)

fun WordPluralDto.toDomain() = WordPlural(id, pluralForm, pluralType)

fun WordPluralDraft.toDto() = CreateWordPluralRequestDto(pluralForm, pluralType)

fun WordRelationDto.toDomain() = WordRelation(
    relatedWordId = relatedWordId,
    relatedWordArabic = relatedWordArabic,
    relatedWordTranslation = relatedWordTranslation,
    relationType = relationType,
)

fun WordRelationDraft.toDto() = CreateWordRelationRequestDto(relatedWordId, relationType)

fun WordEnrichmentSuggestionDto.toDomain() = WordEnrichmentSuggestion(
    notes = notes,
    gender = gender,
    verbPattern = verbPattern,
    plurals = plurals.map { it.toDomain() },
    relations = relations.map { it.toDomain() },
)

fun AiPluralSuggestionDto.toDomain() = AiPluralSuggestion(pluralForm, pluralType)

fun AiRelationSuggestionDto.toDomain() = AiRelationSuggestion(
    arabicText = arabicText,
    relationType = relationType,
    transliteration = transliteration,
    translation = translation,
)
