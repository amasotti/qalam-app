package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.CreateAnnotationRequestDto
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.TextAnnotation
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.repository.TextRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : TextRepository {

    override suspend fun getTexts(
        baseUrl: String, page: Int, size: Int, sortBy: String, sortDesc: Boolean,
    ): Result<PagedResult<TextPassage>> =
        apiClient.getTexts(baseUrl, page, size, sortBy, sortDesc).map { dto ->
            PagedResult(
                items = dto.items.map { it.toDomain() },
                total = dto.total,
                hasMore = dto.items.size == size,
            )
        }

    override suspend fun getText(baseUrl: String, id: String): Result<TextPassage> =
        apiClient.getText(baseUrl, id).map { it.toDomain() }

    override suspend fun getSentences(baseUrl: String, textId: String): Result<List<TextSentence>> =
        apiClient.getSentences(baseUrl, textId).map { list -> list.map { it.toDomain() } }

    override suspend fun getAnnotations(baseUrl: String, textId: String): Result<List<TextAnnotation>> =
        apiClient.getAnnotations(baseUrl, textId).map { list -> list.map { it.toDomain() } }

    override suspend fun createAnnotation(
        baseUrl: String,
        textId: String,
        anchor: String,
        type: String,
        content: String?,
        linkedWordIds: List<String>,
    ): Result<TextAnnotation> =
        apiClient.createAnnotation(
            baseUrl,
            textId,
            CreateAnnotationRequestDto(anchor = anchor, type = type, content = content, linkedWordIds = linkedWordIds),
        ).map { it.toDomain() }
}
