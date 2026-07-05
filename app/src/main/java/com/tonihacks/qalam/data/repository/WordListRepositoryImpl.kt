package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.data.api.dto.toDto
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.WordListDetail
import com.tonihacks.qalam.domain.model.WordListDraft
import com.tonihacks.qalam.domain.model.WordListSuggestion
import com.tonihacks.qalam.domain.model.WordListSummary
import com.tonihacks.qalam.domain.repository.WordListRepository
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordListRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : WordListRepository {
    override suspend fun getWordLists(
        baseUrl: String,
        page: Int,
        size: Int,
    ): Result<PagedResult<WordListSummary>> =
        apiClient.getWordLists(baseUrl, page, size).map { dto ->
            PagedResult(
                items = dto.items.map { it.toDomain() },
                total = dto.total,
                hasMore = dto.items.size == size,
            )
        }

    override suspend fun getWordList(baseUrl: String, id: String): Result<WordListDetail> =
        apiClient.getWordList(baseUrl, id).map { it.toDomain() }

    override suspend fun createWordList(baseUrl: String, draft: WordListDraft): Result<WordListSummary> =
        apiClient.createWordList(baseUrl, draft.toDto()).map { it.toDomain() }

    override suspend fun deleteWordList(baseUrl: String, id: String): Result<Unit> =
        apiClient.deleteWordList(baseUrl, id)

    override suspend fun addWord(baseUrl: String, listId: String, wordId: String): Result<Unit> =
        apiClient.addWordToList(baseUrl, listId, wordId)

    override suspend fun removeWord(baseUrl: String, listId: String, wordId: String): Result<Unit> =
        apiClient.removeWordFromList(baseUrl, listId, wordId)

    override suspend fun suggestWords(baseUrl: String, listId: String): Result<List<WordListSuggestion>> =
        apiClient.suggestWordsForList(baseUrl, listId)
            .map { response -> response.suggestions.map { it.toDomain() } }
            .mapAiUnavailable()

    private fun <T> Result<T>.mapAiUnavailable(): Result<T> = recoverCatching { err ->
        if (err is ServerResponseException && err.response.status == HttpStatusCode.ServiceUnavailable) {
            throw AiUnavailableException()
        }
        throw err
    }
}
