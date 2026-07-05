package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.WordListDetail
import com.tonihacks.qalam.domain.model.WordListDraft
import com.tonihacks.qalam.domain.model.WordListSuggestion
import com.tonihacks.qalam.domain.model.WordListSummary
import kotlinx.serialization.Serializable

@Serializable
data class CreateWordListRequestDto(
    val title: String,
    val description: String? = null,
)

@Serializable
data class AddWordToListRequestDto(
    val wordId: String,
)

@Serializable
data class WordListResponseDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val itemCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class WordListDetailResponseDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val words: List<WordDto> = emptyList(),
)

@Serializable
data class AiListWordSuggestionDto(
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val partOfSpeech: String? = null,
    val difficulty: String? = null,
)

@Serializable
data class WordListSuggestionsResponseDto(
    val suggestions: List<AiListWordSuggestionDto> = emptyList(),
)

fun WordListResponseDto.toDomain() = WordListSummary(
    id = id,
    title = title,
    description = description,
    itemCount = itemCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun WordListDetailResponseDto.toDomain() = WordListDetail(
    id = id,
    title = title,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    words = words.map { it.toDomain() },
)

fun AiListWordSuggestionDto.toDomain() = WordListSuggestion(
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    partOfSpeech = partOfSpeech,
    difficulty = difficulty,
)

fun WordListDraft.toDto() = CreateWordListRequestDto(
    title = title,
    description = description,
)
