package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.TrainingSession
import com.tonihacks.qalam.domain.model.TrainingSessionSummary
import com.tonihacks.qalam.domain.model.TrainingWordResult
import com.tonihacks.qalam.domain.model.RecordedTrainingResult

interface TrainingRepository {
    suspend fun startSession(
        baseUrl: String,
        mode: String = "MIXED",
        size: Int = 20,
        wordListIds: List<String> = emptyList(),
    ): Result<TrainingSession>

    suspend fun submitResult(
        baseUrl: String,
        sessionId: String,
        result: TrainingWordResult,
    ): Result<RecordedTrainingResult>

    suspend fun completeSession(
        baseUrl: String,
        sessionId: String,
    ): Result<TrainingSessionSummary>
}
