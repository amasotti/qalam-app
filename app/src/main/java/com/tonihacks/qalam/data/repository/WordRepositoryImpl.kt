package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.DictionaryLinkDto
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.data.api.dto.toDto
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.repository.WordRepository
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

    override suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>> =
        apiClient.getExamples(baseUrl, wordId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>> =
        apiClient.getDictionaryLinks(baseUrl, wordId).map { list ->
            list.map { dto: DictionaryLinkDto -> dto.toDomain() }
        }

    override suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word> =
        apiClient.createWord(baseUrl, draft.toDto()).map { it.toDomain() }
}
