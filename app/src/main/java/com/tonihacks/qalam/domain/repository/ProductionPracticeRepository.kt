package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.ProductionPracticePrompt
import com.tonihacks.qalam.domain.model.ProductionPracticeReview
import com.tonihacks.qalam.domain.model.ProductionPracticeSubmission

interface ProductionPracticeRepository {
    suspend fun getPrompt(baseUrl: String): Result<ProductionPracticePrompt>

    suspend fun review(
        baseUrl: String,
        submission: ProductionPracticeSubmission,
    ): Result<ProductionPracticeReview>
}
