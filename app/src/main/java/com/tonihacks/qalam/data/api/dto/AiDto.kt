package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.AiExample
import kotlinx.serialization.Serializable

@Serializable
data class AiExampleSentenceDto(
    val arabic: String,
    val transliteration: String? = null,
    val translation: String? = null,
)

@Serializable
data class AiExamplesResponseDto(
    val examples: List<AiExampleSentenceDto> = emptyList(),
)

@Serializable
data class CreateWordExampleRequestDto(
    val arabic: String,
    val transliteration: String? = null,
    val translation: String? = null,
)

@Serializable
data class InsightRequestDto(
    val entityType: String,
    val entityId: String,
    val mode: String? = null,
)

@Serializable
data class InsightResponseDto(
    val insight: String,
)

fun AiExampleSentenceDto.toDomain() = AiExample(
    arabic = arabic,
    transliteration = transliteration,
    translation = translation,
)
