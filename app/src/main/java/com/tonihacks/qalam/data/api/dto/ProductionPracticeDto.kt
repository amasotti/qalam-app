package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.ProductionPracticePrompt
import com.tonihacks.qalam.domain.model.ProductionPracticeReview
import com.tonihacks.qalam.domain.model.ProductionPracticeSubmission
import com.tonihacks.qalam.domain.model.ProductionPracticeWord
import kotlinx.serialization.Serializable

@Serializable
data class ProductionPracticePromptDto(
    val words: List<ProductionPracticeWordDto>,
)

@Serializable
data class ProductionPracticeWordDto(
    val id: String,
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val partOfSpeech: String,
    val dialect: String,
)

@Serializable
data class ReviewProductionPracticeRequestDto(
    val sentence: String,
    val targetWordIds: List<String>,
    val usedWordIds: List<String>,
)

@Serializable
data class ProductionPracticeReviewDto(
    val reviewMarkdown: String,
)

fun ProductionPracticePromptDto.toDomain() = ProductionPracticePrompt(
    words = words.map { it.toDomain() },
)

fun ProductionPracticeWordDto.toDomain() = ProductionPracticeWord(
    id = id,
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    partOfSpeech = partOfSpeech,
    dialect = dialect,
)

fun ProductionPracticeSubmission.toDto() = ReviewProductionPracticeRequestDto(
    sentence = sentence,
    targetWordIds = targetWordIds,
    usedWordIds = usedWordIds,
)

fun ProductionPracticeReviewDto.toDomain() = ProductionPracticeReview(markdown = reviewMarkdown)
