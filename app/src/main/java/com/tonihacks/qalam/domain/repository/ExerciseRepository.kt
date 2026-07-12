package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.ExerciseAnswer
import com.tonihacks.qalam.domain.model.ExerciseSession
import com.tonihacks.qalam.domain.model.ExerciseSessionSummary

interface ExerciseRepository {
    suspend fun startSession(
        baseUrl: String,
        mode: String = "MIXED",
        size: Int = 10,
        wordListIds: List<String> = emptyList(),
        exerciseTypes: List<String> = listOf("MULTIPLE_CHOICE_MEANING"),
        optionCount: Int = 4,
    ): Result<ExerciseSession>

    suspend fun answerItem(
        baseUrl: String,
        sessionId: String,
        itemId: String,
        selectedOptionId: String,
    ): Result<ExerciseAnswer>

    suspend fun completeSession(
        baseUrl: String,
        sessionId: String,
    ): Result<ExerciseSessionSummary>
}
