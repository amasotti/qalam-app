package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.domain.model.HomeOverview
import com.tonihacks.qalam.domain.repository.AnalyticsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : AnalyticsRepository {
    override suspend fun getOverview(baseUrl: String): Result<HomeOverview> =
        apiClient.getAnalyticsOverview(baseUrl).map { it.toDomain() }
}
