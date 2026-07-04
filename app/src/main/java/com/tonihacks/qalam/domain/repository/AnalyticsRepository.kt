package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.HomeOverview

interface AnalyticsRepository {
    suspend fun getOverview(baseUrl: String): Result<HomeOverview>
}
