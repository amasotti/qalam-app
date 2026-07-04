package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.CreateWordExampleRequestDto
import com.tonihacks.qalam.data.api.dto.DictionaryLinkDto
import com.tonihacks.qalam.data.api.dto.InsightRequestDto
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.data.api.dto.toDto
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.repository.WordRepository
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : WordRepository {

    override suspend fun getWords(
        baseUrl: String,
        query: String?,
        masteryLevel: String?,
        rootId: String?,
        page: Int,
        size: Int,
        sortBy: String,
        sortDesc: Boolean,
    ): Result<PagedResult<Word>> =
        apiClient.getWords(baseUrl, query, masteryLevel, rootId, page, size, sortBy, sortDesc).map { dto ->
            PagedResult(
                items = dto.items.map { it.toDomain() },
                total = dto.total,
                hasMore = dto.items.size == size,
            )
        }

    override suspend fun getWord(baseUrl: String, id: String): Result<Word> =
        apiClient.getWord(baseUrl, id).map { it.toDomain() }

    override suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<Word?> =
        apiClient.getWordByArabic(baseUrl, arabicText).map { it?.toDomain() }

    override suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>> =
        apiClient.getExamples(baseUrl, wordId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>> =
        apiClient.getDictionaryLinks(baseUrl, wordId).map { list ->
            list.map { dto: DictionaryLinkDto -> dto.toDomain() }
        }

    override suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word> =
        apiClient.createWord(baseUrl, draft.toDto()).map { it.toDomain() }

    override suspend fun saveExample(baseUrl: String, wordId: String, example: AiExample): Result<Example> =
        apiClient.saveExample(
            baseUrl,
            wordId,
            CreateWordExampleRequestDto(
                arabic = example.arabic,
                transliteration = example.transliteration,
                translation = example.translation,
            ),
        ).map { it.toDomain() }

    override suspend fun generateExamples(baseUrl: String, wordId: String): Result<List<AiExample>> =
        apiClient.generateExamples(baseUrl, wordId)
            .map { dto -> dto.examples.map { it.toDomain() } }
            .mapAiUnavailable()

    override suspend fun generateInsight(baseUrl: String, entityType: String, entityId: String): Result<String> =
        apiClient.generateInsight(baseUrl, InsightRequestDto(entityType = entityType, entityId = entityId))
            .map { it.insight }
            .mapAiUnavailable()

    /** Translate a backend 503 into [AiUnavailableException] so the UI can show the "not configured" state. */
    private fun <T> Result<T>.mapAiUnavailable(): Result<T> = recoverCatching { err ->
        if (err is ServerResponseException && err.response.status == HttpStatusCode.ServiceUnavailable) {
            throw AiUnavailableException()
        }
        throw err
    }
}
