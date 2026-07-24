package com.tonihacks.qalam.data.api

import com.tonihacks.qalam.data.api.dto.AiExamplesResponseDto
import com.tonihacks.qalam.data.api.dto.CreateDictionaryLinkRequestDto
import com.tonihacks.qalam.data.api.dto.CreateWordPluralRequestDto
import com.tonihacks.qalam.data.api.dto.CreateWordRelationRequestDto
import com.tonihacks.qalam.data.api.dto.DictionaryLookupResponseDto
import com.tonihacks.qalam.data.api.dto.AnalyticsOverviewDto
import com.tonihacks.qalam.data.api.dto.AnnotationDto
import com.tonihacks.qalam.data.api.dto.AnswerExerciseItemRequestDto
import com.tonihacks.qalam.data.api.dto.AnswerExerciseItemResponseDto
import com.tonihacks.qalam.data.api.dto.CreateAnnotationRequestDto
import com.tonihacks.qalam.data.api.dto.CreateExerciseSessionRequestDto
import com.tonihacks.qalam.data.api.dto.CreateWordExampleRequestDto
import com.tonihacks.qalam.data.api.dto.DictionaryLinkDto
import com.tonihacks.qalam.data.api.dto.ExampleDto
import com.tonihacks.qalam.data.api.dto.ExerciseSessionDto
import com.tonihacks.qalam.data.api.dto.ExerciseSessionSummaryDto
import com.tonihacks.qalam.data.api.dto.InsightRequestDto
import com.tonihacks.qalam.data.api.dto.InsightResponseDto
import com.tonihacks.qalam.data.api.dto.PagedResponseDto
import com.tonihacks.qalam.data.api.dto.ProductionPracticePromptDto
import com.tonihacks.qalam.data.api.dto.ProductionPracticeReviewDto
import com.tonihacks.qalam.data.api.dto.RootDto
import com.tonihacks.qalam.data.api.dto.ReviewProductionPracticeRequestDto
import com.tonihacks.qalam.data.api.dto.SentenceDto
import com.tonihacks.qalam.data.api.dto.RecordTrainingResultRequestDto
import com.tonihacks.qalam.data.api.dto.RecordTrainingResultResponseDto
import com.tonihacks.qalam.data.api.dto.StartTrainingSessionRequestDto
import com.tonihacks.qalam.data.api.dto.TextDto
import com.tonihacks.qalam.data.api.dto.TrainingSessionDto
import com.tonihacks.qalam.data.api.dto.TrainingSessionSummaryDto
import com.tonihacks.qalam.data.api.dto.UpdateWordRequestDto
import com.tonihacks.qalam.data.api.dto.UpsertWordMorphologyRequestDto
import com.tonihacks.qalam.data.api.dto.WordAutocompleteDto
import com.tonihacks.qalam.data.api.dto.WordDraftDto
import com.tonihacks.qalam.data.api.dto.WordDto
import com.tonihacks.qalam.data.api.dto.WordEnrichmentSuggestionDto
import com.tonihacks.qalam.data.api.dto.AddWordToListRequestDto
import com.tonihacks.qalam.data.api.dto.CreateWordListRequestDto
import com.tonihacks.qalam.data.api.dto.WordListDetailResponseDto
import com.tonihacks.qalam.data.api.dto.WordListResponseDto
import com.tonihacks.qalam.data.api.dto.WordListSuggestionsResponseDto
import com.tonihacks.qalam.data.api.dto.WordMorphologyDto
import com.tonihacks.qalam.data.api.dto.WordPluralDto
import com.tonihacks.qalam.data.api.dto.WordRelationDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
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

    suspend fun updateWord(
        baseUrl: String,
        id: String,
        request: UpdateWordRequestDto,
    ): Result<WordDto> = runCatching {
        httpClient.put("$baseUrl/api/v1/words/$id") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteWord(baseUrl: String, id: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/words/$id")
    }

    suspend fun autocompleteWords(
        baseUrl: String,
        query: String,
        limit: Int = 10,
    ): Result<List<WordAutocompleteDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/autocomplete") {
            parameter("q", query)
            parameter("limit", limit)
        }.body()
    }

    suspend fun getExamples(baseUrl: String, wordId: String): Result<List<ExampleDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/examples").body()
    }

    suspend fun saveExample(
        baseUrl: String,
        wordId: String,
        request: CreateWordExampleRequestDto,
    ): Result<ExampleDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words/$wordId/examples") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteExample(baseUrl: String, wordId: String, exampleId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/words/$wordId/examples/$exampleId")
    }

    suspend fun generateExamples(baseUrl: String, wordId: String): Result<AiExamplesResponseDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words/$wordId/examples/generate") {
            aiRequestTimeout()
        }.body()
    }

    suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLinkDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/dictionary-links").body()
    }

    suspend fun addDictionaryLink(
        baseUrl: String,
        wordId: String,
        request: CreateDictionaryLinkRequestDto,
    ): Result<DictionaryLinkDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words/$wordId/dictionary-links") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteDictionaryLink(baseUrl: String, wordId: String, linkId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/words/$wordId/dictionary-links/$linkId")
    }

    suspend fun createWord(baseUrl: String, draft: WordDraftDto): Result<WordDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words") {
            contentType(ContentType.Application.Json)
            setBody(draft)
        }.body()
    }

    suspend fun lookupInDictionary(
        baseUrl: String,
        query: String,
        source: String = "ASD",
    ): Result<DictionaryLookupResponseDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/dictionary-lookups") {
            parameter("source", source)
            parameter("query", query)
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

    suspend fun getWordMorphology(baseUrl: String, wordId: String): Result<WordMorphologyDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/morphology").body()
    }

    suspend fun upsertWordMorphology(
        baseUrl: String,
        wordId: String,
        request: UpsertWordMorphologyRequestDto,
    ): Result<WordMorphologyDto> = runCatching {
        httpClient.put("$baseUrl/api/v1/words/$wordId/morphology") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getWordPlurals(baseUrl: String, wordId: String): Result<List<WordPluralDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/plurals").body()
    }

    suspend fun addWordPlural(
        baseUrl: String,
        wordId: String,
        request: CreateWordPluralRequestDto,
    ): Result<WordPluralDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words/$wordId/plurals") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteWordPlural(baseUrl: String, wordId: String, pluralId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/words/$wordId/plurals/$pluralId")
    }

    suspend fun getWordRelations(baseUrl: String, wordId: String): Result<List<WordRelationDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/words/$wordId/relations").body()
    }

    suspend fun addWordRelation(
        baseUrl: String,
        wordId: String,
        request: CreateWordRelationRequestDto,
    ): Result<WordRelationDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words/$wordId/relations") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteWordRelation(
        baseUrl: String,
        wordId: String,
        relatedWordId: String,
        relationType: String,
    ): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/words/$wordId/relations/$relatedWordId/$relationType")
    }

    suspend fun enrichWord(baseUrl: String, wordId: String): Result<WordEnrichmentSuggestionDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/words/$wordId/enrich") {
            aiRequestTimeout()
        }.body()
    }

    // -------------- WORD LISTS -----------------
    suspend fun getWordLists(
        baseUrl: String,
        page: Int = 1,
        size: Int = 100,
    ): Result<PagedResponseDto<WordListResponseDto>> = runCatching {
        httpClient.get("$baseUrl/api/v1/word-lists") {
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    suspend fun getWordList(baseUrl: String, id: String): Result<WordListDetailResponseDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/word-lists/$id").body()
    }

    suspend fun createWordList(baseUrl: String, request: CreateWordListRequestDto): Result<WordListResponseDto> =
        runCatching {
            httpClient.post("$baseUrl/api/v1/word-lists") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
        }

    suspend fun deleteWordList(baseUrl: String, id: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/word-lists/$id")
    }

    suspend fun addWordToList(baseUrl: String, listId: String, wordId: String): Result<Unit> = runCatching {
        httpClient.post("$baseUrl/api/v1/word-lists/$listId/words") {
            contentType(ContentType.Application.Json)
            setBody(AddWordToListRequestDto(wordId))
        }
    }

    suspend fun removeWordFromList(baseUrl: String, listId: String, wordId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl/api/v1/word-lists/$listId/words/$wordId")
    }

    suspend fun suggestWordsForList(baseUrl: String, listId: String): Result<WordListSuggestionsResponseDto> =
        runCatching {
            httpClient.post("$baseUrl/api/v1/word-lists/$listId/suggest") {
                aiRequestTimeout()
            }.body()
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

    // -------------- EXERCISES -----------------
    suspend fun startExerciseSession(
        baseUrl: String,
        request: CreateExerciseSessionRequestDto,
    ): Result<ExerciseSessionDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/exercise-sessions") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun answerExerciseItem(
        baseUrl: String,
        sessionId: String,
        itemId: String,
        selectedOptionId: String,
    ): Result<AnswerExerciseItemResponseDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/exercise-sessions/$sessionId/answers") {
            contentType(ContentType.Application.Json)
            setBody(AnswerExerciseItemRequestDto(itemId = itemId, selectedOptionId = selectedOptionId))
        }.body()
    }

    suspend fun completeExerciseSession(
        baseUrl: String,
        sessionId: String,
    ): Result<ExerciseSessionSummaryDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/exercise-sessions/$sessionId/complete").body()
    }

    // -------------- PRODUCTION PRACTICE -----------------
    suspend fun getProductionPracticePrompt(baseUrl: String): Result<ProductionPracticePromptDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/production-practice/prompt").body()
    }

    suspend fun reviewProductionPractice(
        baseUrl: String,
        request: ReviewProductionPracticeRequestDto,
    ): Result<ProductionPracticeReviewDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/production-practice/reviews") {
            aiRequestTimeout()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // -------------- ANALYTICS -----------------
    suspend fun getAnalyticsOverview(baseUrl: String): Result<AnalyticsOverviewDto> = runCatching {
        httpClient.get("$baseUrl/api/v1/analytics/overview").body()
    }

    // -------------- AI -----------------
    suspend fun generateInsight(baseUrl: String, request: InsightRequestDto): Result<InsightResponseDto> = runCatching {
        httpClient.post("$baseUrl/api/v1/ai/insight") {
            aiRequestTimeout()
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    private fun io.ktor.client.request.HttpRequestBuilder.aiRequestTimeout() {
        timeout {
            requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS
        }
    }

    private companion object {
        const val AI_REQUEST_TIMEOUT_MILLIS = 90_000L
    }
}
