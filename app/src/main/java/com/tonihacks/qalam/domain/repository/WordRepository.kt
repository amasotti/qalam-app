package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.*

interface WordRepository {
    suspend fun getWords(
        baseUrl: String,
        query: String? = null,
        masteryLevel: String? = null,
        rootId: String? = null,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "UPDATED_AT",
        sortDesc: Boolean = true,
    ): Result<PagedResult<Word>>

    suspend fun getWord(baseUrl: String, id: String): Result<Word>
    suspend fun updateWord(baseUrl: String, id: String, update: WordUpdate): Result<Word>
    suspend fun deleteWord(baseUrl: String, id: String): Result<Unit>
    suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<Word?>
    suspend fun autocompleteWords(baseUrl: String, query: String): Result<List<WordAutocomplete>>
    suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>>
    suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>>
    suspend fun addDictionaryLink(baseUrl: String, wordId: String, draft: DictionaryLinkDraft): Result<DictionaryLink>
    suspend fun deleteDictionaryLink(baseUrl: String, wordId: String, linkId: String): Result<Unit>
    suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word>

    suspend fun lookupInDictionary(baseUrl: String, query: String): Result<List<DictionaryLookupItem>>

    suspend fun saveExample(baseUrl: String, wordId: String, example: AiExample): Result<Example>
    suspend fun deleteExample(baseUrl: String, wordId: String, exampleId: String): Result<Unit>
    suspend fun generateExamples(baseUrl: String, wordId: String): Result<List<AiExample>>
    suspend fun generateInsight(baseUrl: String, entityType: String, entityId: String): Result<String>

    suspend fun getMorphology(baseUrl: String, wordId: String): Result<WordMorphology>
    suspend fun upsertMorphology(
        baseUrl: String,
        wordId: String,
        gender: String?,
        verbPattern: String?,
    ): Result<WordMorphology>
    suspend fun getPlurals(baseUrl: String, wordId: String): Result<List<WordPlural>>
    suspend fun addPlural(baseUrl: String, wordId: String, draft: WordPluralDraft): Result<WordPlural>
    suspend fun deletePlural(baseUrl: String, wordId: String, pluralId: String): Result<Unit>
    suspend fun getRelations(baseUrl: String, wordId: String): Result<List<WordRelation>>
    suspend fun addRelation(baseUrl: String, wordId: String, draft: WordRelationDraft): Result<WordRelation>
    suspend fun deleteRelation(
        baseUrl: String,
        wordId: String,
        relatedWordId: String,
        relationType: String,
    ): Result<Unit>
    suspend fun enrichWord(baseUrl: String, wordId: String): Result<WordEnrichmentSuggestion>
}
