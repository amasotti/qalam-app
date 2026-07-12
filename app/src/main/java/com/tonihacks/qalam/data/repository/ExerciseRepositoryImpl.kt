package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.CreateExerciseSessionRequestDto
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.domain.model.ExerciseAnswer
import com.tonihacks.qalam.domain.model.ExerciseSession
import com.tonihacks.qalam.domain.model.ExerciseSessionSummary
import com.tonihacks.qalam.domain.repository.ExerciseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : ExerciseRepository {

    override suspend fun startSession(
        baseUrl: String,
        mode: String,
        size: Int,
        wordListIds: List<String>,
        exerciseTypes: List<String>,
        optionCount: Int,
    ): Result<ExerciseSession> =
        apiClient.startExerciseSession(
            baseUrl = baseUrl,
            request = CreateExerciseSessionRequestDto(
                mode = mode,
                size = size,
                wordListIds = wordListIds,
                exerciseTypes = exerciseTypes,
                optionCount = optionCount,
            ),
        ).map { it.toDomain() }

    override suspend fun answerItem(
        baseUrl: String,
        sessionId: String,
        itemId: String,
        selectedOptionId: String,
    ): Result<ExerciseAnswer> =
        apiClient.answerExerciseItem(
            baseUrl = baseUrl,
            sessionId = sessionId,
            itemId = itemId,
            selectedOptionId = selectedOptionId,
        ).map { it.toDomain() }

    override suspend fun completeSession(
        baseUrl: String,
        sessionId: String,
    ): Result<ExerciseSessionSummary> =
        apiClient.completeExerciseSession(baseUrl, sessionId).map { it.toDomain() }
}
