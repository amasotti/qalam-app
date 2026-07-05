package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.WordListDetail
import com.tonihacks.qalam.domain.model.WordListDraft
import com.tonihacks.qalam.domain.model.WordListSuggestion
import com.tonihacks.qalam.domain.model.WordListSummary

interface WordListRepository {
    suspend fun getWordLists(baseUrl: String, page: Int = 1, size: Int = 100): Result<PagedResult<WordListSummary>>
    suspend fun getWordList(baseUrl: String, id: String): Result<WordListDetail>
    suspend fun createWordList(baseUrl: String, draft: WordListDraft): Result<WordListSummary>
    suspend fun deleteWordList(baseUrl: String, id: String): Result<Unit>
    suspend fun addWord(baseUrl: String, listId: String, wordId: String): Result<Unit>
    suspend fun removeWord(baseUrl: String, listId: String, wordId: String): Result<Unit>
    suspend fun suggestWords(baseUrl: String, listId: String): Result<List<WordListSuggestion>>
}
