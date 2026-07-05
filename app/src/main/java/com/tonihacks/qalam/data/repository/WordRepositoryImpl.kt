package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.CreateWordExampleRequestDto
import com.tonihacks.qalam.data.api.dto.DictionaryLinkDto
import com.tonihacks.qalam.data.api.dto.InsightRequestDto
import com.tonihacks.qalam.data.api.dto.UpsertWordMorphologyRequestDto
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.data.api.dto.toDto
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.DictionaryLinkDraft
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.DictionaryLookupItem
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.model.WordEnrichmentSuggestion
import com.tonihacks.qalam.domain.model.WordMorphology
import com.tonihacks.qalam.domain.model.WordPlural
import com.tonihacks.qalam.domain.model.WordPluralDraft
import com.tonihacks.qalam.domain.model.WordRelation
import com.tonihacks.qalam.domain.model.WordRelationDraft
import com.tonihacks.qalam.domain.model.WordUpdate
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

    override suspend fun updateWord(baseUrl: String, id: String, update: WordUpdate): Result<Word> =
        apiClient.updateWord(baseUrl, id, update.toDto()).map { it.toDomain() }

    override suspend fun deleteWord(baseUrl: String, id: String): Result<Unit> =
        apiClient.deleteWord(baseUrl, id)

    override suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<Word?> =
        apiClient.getWordByArabic(baseUrl, arabicText).map { it?.toDomain() }

    override suspend fun autocompleteWords(baseUrl: String, query: String): Result<List<WordAutocomplete>> =
        apiClient.autocompleteWords(baseUrl, query).map { list -> list.map { it.toDomain() } }

    override suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>> =
        apiClient.getExamples(baseUrl, wordId).map { list -> list.map { it.toDomain() } }

    override suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>> =
        apiClient.getDictionaryLinks(baseUrl, wordId).map { list ->
            list.map { dto: DictionaryLinkDto -> dto.toDomain() }
        }

    override suspend fun addDictionaryLink(
        baseUrl: String,
        wordId: String,
        draft: DictionaryLinkDraft,
    ): Result<DictionaryLink> =
        apiClient.addDictionaryLink(baseUrl, wordId, draft.toDto()).map { it.toDomain() }

    override suspend fun deleteDictionaryLink(baseUrl: String, wordId: String, linkId: String): Result<Unit> =
        apiClient.deleteDictionaryLink(baseUrl, wordId, linkId)

    override suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word> =
        apiClient.createWord(baseUrl, draft.toDto()).map { it.toDomain() }

    override suspend fun lookupInDictionary(baseUrl: String, query: String): Result<List<DictionaryLookupItem>> =
        apiClient.lookupInDictionary(baseUrl, query = query).map { dto ->
            dto.items.map { it.toDomain() }
        }

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

    override suspend fun deleteExample(baseUrl: String, wordId: String, exampleId: String): Result<Unit> =
        apiClient.deleteExample(baseUrl, wordId, exampleId)

    override suspend fun generateExamples(baseUrl: String, wordId: String): Result<List<AiExample>> =
        apiClient.generateExamples(baseUrl, wordId)
            .map { dto -> dto.examples.map { it.toDomain() } }
            .mapAiUnavailable()

    override suspend fun generateInsight(baseUrl: String, entityType: String, entityId: String): Result<String> =
        apiClient.generateInsight(baseUrl, InsightRequestDto(entityType = entityType, entityId = entityId))
            .map { it.insight }
            .mapAiUnavailable()

    override suspend fun getMorphology(baseUrl: String, wordId: String): Result<WordMorphology> =
        apiClient.getWordMorphology(baseUrl, wordId).map { it.toDomain() }

    override suspend fun upsertMorphology(
        baseUrl: String,
        wordId: String,
        gender: String?,
        verbPattern: String?,
    ): Result<WordMorphology> =
        apiClient.upsertWordMorphology(
            baseUrl,
            wordId,
            UpsertWordMorphologyRequestDto(
                gender = gender,
                verbPattern = verbPattern,
            ),
        ).map { it.toDomain() }

    override suspend fun getPlurals(baseUrl: String, wordId: String): Result<List<WordPlural>> =
        apiClient.getWordPlurals(baseUrl, wordId).map { list -> list.map { it.toDomain() } }

    override suspend fun addPlural(baseUrl: String, wordId: String, draft: WordPluralDraft): Result<WordPlural> =
        apiClient.addWordPlural(baseUrl, wordId, draft.toDto()).map { it.toDomain() }

    override suspend fun deletePlural(baseUrl: String, wordId: String, pluralId: String): Result<Unit> =
        apiClient.deleteWordPlural(baseUrl, wordId, pluralId)

    override suspend fun getRelations(baseUrl: String, wordId: String): Result<List<WordRelation>> =
        apiClient.getWordRelations(baseUrl, wordId).map { list -> list.map { it.toDomain() } }

    override suspend fun addRelation(
        baseUrl: String,
        wordId: String,
        draft: WordRelationDraft,
    ): Result<WordRelation> =
        apiClient.addWordRelation(baseUrl, wordId, draft.toDto()).map { it.toDomain() }

    override suspend fun deleteRelation(
        baseUrl: String,
        wordId: String,
        relatedWordId: String,
        relationType: String,
    ): Result<Unit> =
        apiClient.deleteWordRelation(baseUrl, wordId, relatedWordId, relationType)

    override suspend fun enrichWord(baseUrl: String, wordId: String): Result<WordEnrichmentSuggestion> =
        apiClient.enrichWord(baseUrl, wordId)
            .map { it.toDomain() }
            .mapAiUnavailable()

    /** Translate a backend 503 into [AiUnavailableException] so the UI can show the "not configured" state. */
    private fun <T> Result<T>.mapAiUnavailable(): Result<T> = recoverCatching { err ->
        if (err is ServerResponseException && err.response.status == HttpStatusCode.ServiceUnavailable) {
            throw AiUnavailableException()
        }
        throw err
    }
}
