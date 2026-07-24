package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.data.api.dto.toDto
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.ProductionPracticePrompt
import com.tonihacks.qalam.domain.model.ProductionPracticeReview
import com.tonihacks.qalam.domain.model.ProductionPracticeSubmission
import com.tonihacks.qalam.domain.repository.ProductionPracticeRepository
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductionPracticeRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : ProductionPracticeRepository {
    override suspend fun getPrompt(baseUrl: String): Result<ProductionPracticePrompt> =
        apiClient.getProductionPracticePrompt(baseUrl).map { it.toDomain() }

    override suspend fun review(
        baseUrl: String,
        submission: ProductionPracticeSubmission,
    ): Result<ProductionPracticeReview> =
        apiClient.reviewProductionPractice(baseUrl, submission.toDto())
            .map { it.toDomain() }
            .mapAiUnavailable()

    private fun <T> Result<T>.mapAiUnavailable(): Result<T> = recoverCatching { error ->
        if (error is ServerResponseException && error.response.status == HttpStatusCode.ServiceUnavailable) {
            throw AiUnavailableException()
        }
        throw error
    }
}
