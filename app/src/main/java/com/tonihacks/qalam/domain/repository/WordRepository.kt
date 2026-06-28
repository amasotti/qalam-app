package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.*

interface WordRepository {
    suspend fun getWords(
        baseUrl: String,
        query: String? = null,
        masteryLevel: String? = null,
        page: Int = 0,
        size: Int = 20,
    ): Result<PagedResult<Word>>

    suspend fun getWord(baseUrl: String, id: String): Result<Word>
    suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>>
    suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>>
    suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word>
}