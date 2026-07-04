package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.model.TextToken

interface TextRepository {
    suspend fun getTexts(
        baseUrl: String,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "UPDATED_AT",
        sortDesc: Boolean = true,
    ): Result<PagedResult<TextPassage>>

    suspend fun getText(baseUrl: String, id: String): Result<TextPassage>
    suspend fun getSentences(baseUrl: String, textId: String): Result<List<TextSentence>>

    suspend fun replaceTokens(
        baseUrl: String,
        textId: String,
        sentenceId: String,
        tokens: List<TextToken>,
    ): Result<TextSentence>
}
