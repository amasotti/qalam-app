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
    suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<Word?>
    suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>>
    suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>>
    suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word>

    suspend fun lookupInDictionary(baseUrl: String, query: String): Result<List<DictionaryLookupItem>>

    suspend fun saveExample(baseUrl: String, wordId: String, example: AiExample): Result<Example>
    suspend fun generateExamples(baseUrl: String, wordId: String): Result<List<AiExample>>
    suspend fun generateInsight(baseUrl: String, entityType: String, entityId: String): Result<String>
}
