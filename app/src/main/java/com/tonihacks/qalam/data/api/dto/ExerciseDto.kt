package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.ExerciseAnswer
import com.tonihacks.qalam.domain.model.ExerciseItem
import com.tonihacks.qalam.domain.model.ExerciseOption
import com.tonihacks.qalam.domain.model.ExercisePrompt
import com.tonihacks.qalam.domain.model.ExercisePromptKind
import com.tonihacks.qalam.domain.model.ExerciseResult
import com.tonihacks.qalam.domain.model.ExerciseSession
import com.tonihacks.qalam.domain.model.ExerciseSessionSummary
import kotlinx.serialization.Serializable

@Serializable
data class CreateExerciseSessionRequestDto(
    val mode: String,
    val size: Int,
    val wordListIds: List<String> = emptyList(),
    val exerciseTypes: List<String> = listOf("MULTIPLE_CHOICE_MEANING"),
    val optionCount: Int = 4,
)

@Serializable
data class AnswerExerciseItemRequestDto(
    val itemId: String,
    val selectedOptionId: String,
)

@Serializable
data class ExerciseSessionDto(
    val id: String,
    val mode: String,
    val status: String,
    val items: List<ExerciseSessionItemDto>,
)

@Serializable
data class ExerciseSessionItemDto(
    val itemId: String,
    val wordId: String,
    val type: String,
    val prompt: ExercisePromptDto,
    val options: List<ExerciseOptionDto>,
    val result: String? = null,
    val selectedOptionId: String? = null,
)

@Serializable
data class ExercisePromptDto(
    val kind: String,
    val text: String,
)

@Serializable
data class ExerciseOptionDto(
    val optionId: String,
    val wordId: String,
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
)

@Serializable
data class AnswerExerciseItemResponseDto(
    val itemId: String,
    val wordId: String,
    val result: String,
    val correctOptionId: String,
    val masteryPromotion: MasteryPromotionDto? = null,
)

@Serializable
data class ExerciseSessionSummaryDto(
    val sessionId: String,
    val mode: String,
    val totalItems: Int,
    val correct: Int,
    val incorrect: Int,
    val skipped: Int,
    val accuracy: Double,
    val promotions: List<MasteryPromotionDto>,
)

fun ExerciseSessionDto.toDomain() = ExerciseSession(
    id = id,
    mode = mode,
    status = status,
    items = items.map { it.toDomain() },
)

fun ExerciseSessionItemDto.toDomain() = ExerciseItem(
    id = itemId,
    wordId = wordId,
    type = type,
    prompt = prompt.toDomain(),
    options = options.map { it.toDomain() },
    result = result?.toExerciseResult(),
    selectedOptionId = selectedOptionId,
)

fun ExercisePromptDto.toDomain() = ExercisePrompt(
    kind = runCatching { ExercisePromptKind.valueOf(kind) }.getOrDefault(ExercisePromptKind.ARABIC_WORD),
    text = text,
)

fun ExerciseOptionDto.toDomain() = ExerciseOption(
    id = optionId,
    wordId = wordId,
    arabicText = arabicText,
    transliteration = transliteration,
    translation = translation,
)

fun AnswerExerciseItemResponseDto.toDomain() = ExerciseAnswer(
    itemId = itemId,
    wordId = wordId,
    result = result.toExerciseResult(),
    correctOptionId = correctOptionId,
    masteryPromotion = masteryPromotion?.toDomain(),
)

fun ExerciseSessionSummaryDto.toDomain() = ExerciseSessionSummary(
    sessionId = sessionId,
    mode = mode,
    totalItems = totalItems,
    correct = correct,
    incorrect = incorrect,
    skipped = skipped,
    accuracy = accuracy * 100.0,
    promotions = promotions.map { it.toDomain() },
)

private fun String.toExerciseResult(): ExerciseResult =
    runCatching { ExerciseResult.valueOf(this) }.getOrDefault(ExerciseResult.INCORRECT)
