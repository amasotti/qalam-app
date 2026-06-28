package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.*
import kotlinx.serialization.Serializable

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
    val dictionaries: List<DictionaryLinkDto> = emptyList(),
)

@Serializable
data class DictionaryLinkDto(
    val name: String,
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

fun WordDto.toDomain() = Word(
    id = id,
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    partOfSpeech = partOfSpeech,
    dialect = dialect,
    masteryLevel = runCatching { MasteryLevel.valueOf(masteryLevel) }.getOrDefault(MasteryLevel.NEW),
    rootId = rootId,
    dictionaries = dictionaries.map { DictionaryLink(it.name, it.url) },
)

fun ExampleDto.toDomain() = Example(id, arabicText, transliteration, translation)

fun WordDraft.toDto() = WordDraftDto(arabicText, translation, transliteration, partOfSpeech, dialect)
