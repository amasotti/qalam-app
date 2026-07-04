package com.tonihacks.qalam.data.api

import com.tonihacks.qalam.data.api.dto.AnalyticsOverviewDto
import com.tonihacks.qalam.data.api.dto.AnnotationDto
import com.tonihacks.qalam.data.api.dto.CreateAnnotationRequestDto
import com.tonihacks.qalam.data.api.dto.DictionaryLinkDto
import com.tonihacks.qalam.data.api.dto.ExampleDto
import com.tonihacks.qalam.data.api.dto.PagedResponseDto
import com.tonihacks.qalam.data.api.dto.RootDto
import com.tonihacks.qalam.data.api.dto.SentenceDto
import com.tonihacks.qalam.data.api.dto.RecordTrainingResultRequestDto
import com.tonihacks.qalam.data.api.dto.RecordTrainingResultResponseDto
import com.tonihacks.qalam.data.api.dto.StartTrainingSessionRequestDto
import com.tonihacks.qalam.data.api.dto.TextDto
import com.tonihacks.qalam.data.api.dto.TrainingSessionDto
import com.tonihacks.qalam.data.api.dto.TrainingSessionSummaryDto
import com.tonihacks.qalam.data.api.dto.WordDraftDto
import com.tonihacks.qalam.data.api.dto.WordDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val httpClient: HttpClient
) {
    // -------------- HEALTH CHECK -----------------
    suspend fun testConnection(baseUrl: String): Result<Unit> =
        runCatching {
            httpClient.get("$baseUrl/health")
        }

    // -------------- WORDS -----------------
    suspend fun getWords(
        baseUrl: String,
        query: String? = null,
        masteryLevel: String? = null,
        rootId: String? = null,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "UPDATED_AT",
        sortDesc: Boolean = true,
    ): Result<PagedResponseDto<WordDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words") {
            parameter("page", page)
            parameter("size", size)
            if (!query.isNullOrEmpty()) parameter("q", query)
            if (!masteryLevel.isNullOrEmpty()) parameter("masteryLevel", masteryLevel)
            if (!rootId.isNullOrEmpty()) parameter("rootId", rootId)
            parameter("sortBy", sortBy)
            parameter("sortDesc", sortDesc)
        }.body()
    }

    suspend fun getWord(baseUrl: String, id: String): Result<WordDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$id").body()
    }

    suspend fun getExamples(baseUrl: String, wordId: String): Result<List<ExampleDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/examples").body()
    }

    suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLinkDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/dictionary-links").body()
    }

    suspend fun createWord(baseUrl: String, draft: WordDraftDto): Result<WordDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words") {
            contentType(ContentType.Application.Json)
            setBody(draft)
        }.body()
    }

    /** Exact Arabic match. Returns null on 404 (no word with that surface form). */
    suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<WordDto?> = runCatching {
        try {
            httpClient.get("$baseUrl/api/v1/words/by-arabic") {
                parameter("q", arabicText)
            }.body<WordDto>()
        } catch (e: ClientRequestException) {
            if (e.response.status == HttpStatusCode.NotFound) null else throw e
        }
    }

    // -------------- ROOTS -----------------
    suspend fun getRoots(
        baseUrl: String,
        page: Int = 1,
        size: Int = 20,
    ): Result<PagedResponseDto<RootDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/roots") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getRoot(baseUrl: String, id: String): Result<RootDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/roots/$id").body()
    }


    // -------------- TEXTS -----------------
    suspend fun getTexts(
        baseUrl: String,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "UPDATED_AT",
        sortDesc: Boolean = true,
    ): Result<PagedResponseDto<TextDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/texts") {
            parameter("page", page)
            parameter("size", size)
            parameter("sortBy", sortBy)
            parameter("sortDesc", sortDesc)
        }.body()
    }

    suspend fun getText(baseUrl: String, id: String): Result<TextDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/texts/$id").body()
    }

    suspend fun getSentences(baseUrl: String, textId: String): Result<List<SentenceDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/texts/$textId/sentences").body()
    }

    // -------------- ANNOTATIONS -----------------
    suspend fun getAnnotations(baseUrl: String, textId: String): Result<List<AnnotationDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/texts/$textId/annotations").body()
    }

    suspend fun createAnnotation(
        baseUrl: String,
        textId: String,
        request: CreateAnnotationRequestDto,
    ): Result<AnnotationDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/texts/$textId/annotations") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // -------------- TRAINING -----------------
    suspend fun startTrainingSession(
        baseUrl: String,
        request: StartTrainingSessionRequestDto,
    ): Result<TrainingSessionDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/training/sessions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun submitTrainingResult(
        baseUrl: String,
        sessionId: String,
        request: RecordTrainingResultRequestDto,
    ): Result<RecordTrainingResultResponseDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/training/sessions/$sessionId/results") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun completeTrainingSession(
        baseUrl: String,
        sessionId: String,
    ): Result<TrainingSessionSummaryDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/training/sessions/$sessionId/complete").body()
    }

    // -------------- ANALYTICS -----------------
    suspend fun getAnalyticsOverview(baseUrl: String): Result<AnalyticsOverviewDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/analytics/overview").body()
    }
}
