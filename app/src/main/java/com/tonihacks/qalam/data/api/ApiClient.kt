package com.tonihacks.qalam.data.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor (
    private val httpClient: HttpClient
) {
    suspend fun testConnection(baseUrl: String) : Result<Unit> =
        runCatching {
            httpClient.get("$baseUrl/health")
        }
    }
