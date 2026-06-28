package com.tonihacks.qalam.data.api

import com.tonihacks.qalam.data.api.dto.ExampleDto
import com.tonihacks.qalam.data.api.dto.PagedResponseDto
import com.tonihacks.qalam.data.api.dto.WordDraftDto
import com.tonihacks.qalam.data.api.dto.WordDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun testConnection(baseUrl: String): Result<Unit> =
        runCatching {
            httpClient.get("$baseUrl/health")
        }

    suspend fun getWords(
        baseUrl: String,
        query: String? = null,
        masteryLevel: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<PagedResponseDto<WordDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words") {
            parameter("page", page)
            parameter("size", size)
            if (!query.isNullOrEmpty()) parameter("q", query)
            if (!masteryLevel.isNullOrEmpty()) parameter("masteryLevel", masteryLevel)
        }.body()
    }

    suspend fun getWord(baseUrl: String, id: String): Result<WordDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$id").body()
    }

    suspend fun getExamples(baseUrl: String, wordId: String): Result<List<ExampleDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/examples").body()
    }

    suspend fun createWord(baseUrl: String, draft: WordDraftDto): Result<WordDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words") {
            contentType(ContentType.Application.Json)
            setBody(draft)
        }.body()
    }

}
