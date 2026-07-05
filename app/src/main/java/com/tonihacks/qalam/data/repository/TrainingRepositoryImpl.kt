package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.StartTrainingSessionRequestDto
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.data.api.dto.toDto
import com.tonihacks.qalam.domain.model.RecordedTrainingResult
import com.tonihacks.qalam.domain.model.TrainingSession
import com.tonihacks.qalam.domain.model.TrainingSessionSummary
import com.tonihacks.qalam.domain.model.TrainingWordResult
import com.tonihacks.qalam.domain.repository.TrainingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrainingRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : TrainingRepository {

    override suspend fun startSession(
        baseUrl: String,
        mode: String,
        size: Int,
        wordListIds: List<String>,
    ): Result<TrainingSession> =
        apiClient.startTrainingSession(
            baseUrl = baseUrl,
            request = StartTrainingSessionRequestDto(mode = mode, size = size, wordListIds = wordListIds),
        ).map { it.toDomain() }

    override suspend fun submitResult(
        baseUrl: String,
        sessionId: String,
        result: TrainingWordResult,
    ): Result<RecordedTrainingResult> =
        apiClient.submitTrainingResult(
            baseUrl = baseUrl,
            sessionId = sessionId,
            request = result.toDto(),
        ).map { it.toDomain() }

    override suspend fun completeSession(
        baseUrl: String,
        sessionId: String,
    ): Result<TrainingSessionSummary> =
        apiClient.completeTrainingSession(baseUrl, sessionId).map { it.toDomain() }
}
