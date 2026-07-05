package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.TrainingSession
import com.tonihacks.qalam.domain.model.TrainingSessionSummary
import com.tonihacks.qalam.domain.model.TrainingWord
import com.tonihacks.qalam.domain.model.TrainingWordRelation
import com.tonihacks.qalam.domain.model.TrainingWordResult
import com.tonihacks.qalam.domain.model.RecordedTrainingResult
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.MasteryPromotion
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.FlashcardSide
import kotlinx.serialization.Serializable

@Serializable
data class StartTrainingSessionRequestDto(
    val mode: String,
    val size: Int,
)

@Serializable
data class TrainingSessionDto(
    val id: String,
    val mode: String,
    val status: String,
    val words: List<TrainingSessionWordDto>,
)

@Serializable
data class TrainingSessionWordDto(
    val wordId: String,
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val frontSide: String,
    val position: Int,
    val masteryLevel: String,
    val root: String? = null,
    val notes: String? = null,
    val examples: List<TrainingWordExampleDto> = emptyList(),
    val relations: List<TrainingWordRelationDto> = emptyList(),
)

@Serializable
data class TrainingWordExampleDto(
    val arabic: String,
    val transliteration: String? = null,
    val translation: String? = null,
)

@Serializable
data class TrainingWordRelationDto(
    val relatedWordId: String,
    val relatedWordArabic: String,
    val relatedWordTranslation: String? = null,
    val relationType: String,
)

@Serializable
data class RecordTrainingResultRequestDto(
    val wordId: String,
    val result: String,
)

@Serializable
data class RecordTrainingResultResponseDto(
    val wordId: String,
    val result: String,
    val masteryPromotion: MasteryPromotionDto? = null,
)

@Serializable
data class MasteryPromotionDto(
    val wordId: String,
    val from: String,
    val to: String,
)

@Serializable
data class TrainingSessionSummaryDto(
    val sessionId: String,
    val mode: String,
    val totalWords: Int,
    val correct: Int,
    val incorrect: Int,
    val skipped: Int,
    val accuracy: Double,
    val promotions: List<MasteryPromotionDto>,
)

fun TrainingSessionDto.toDomain() = TrainingSession(
    id = id,
    mode = mode,
    status = status,
    words = words.map { it.toDomain() },
)

fun TrainingSessionWordDto.toDomain() = TrainingWord(
    id = wordId,
    frontSide = frontSide.toFlashcardSide(),
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
    partOfSpeech = null,
    position = position,
    masteryLevel = masteryLevel.toMasteryLevel(),
    root = root,
    notes = notes,
    examples = examples.map { it.toDomain() },
    relations = relations.map { it.toDomain() },
)

fun TrainingWordExampleDto.toDomain() = Example(
    id = arabic,
    arabicText = arabic,
    transliteration = transliteration,
    translation = translation,
)

fun TrainingWordRelationDto.toDomain() = TrainingWordRelation(
    relatedWordId = relatedWordId,
    relatedWordArabic = relatedWordArabic,
    relatedWordTranslation = relatedWordTranslation,
    relationType = relationType,
)

fun TrainingWordResult.toDto() = RecordTrainingResultRequestDto(
    wordId = wordId,
    result = if (knewIt) "CORRECT" else "INCORRECT",
)

fun RecordTrainingResultResponseDto.toDomain() = RecordedTrainingResult(
    wordId = wordId,
    result = result,
    masteryPromotion = masteryPromotion?.toDomain(),
)

fun MasteryPromotionDto.toDomain() = MasteryPromotion(
    wordId = wordId,
    from = from.toMasteryLevel(),
    to = to.toMasteryLevel(),
)

fun TrainingSessionSummaryDto.toDomain() = TrainingSessionSummary(
    sessionId = sessionId,
    mode = mode,
    totalWords = totalWords,
    correct = correct,
    incorrect = incorrect,
    skipped = skipped,
    accuracy = accuracy * 100.0, // backend sends fraction (0.0-1.0); domain uses percentage (0.0-100.0)
    promotions = promotions.map { it.toDomain() },
)

private fun String.toMasteryLevel(): MasteryLevel =
    runCatching { MasteryLevel.valueOf(this) }.getOrDefault(MasteryLevel.NEW)

private fun String.toFlashcardSide(): FlashcardSide =
    runCatching { FlashcardSide.valueOf(this) }.getOrDefault(FlashcardSide.ARABIC)
