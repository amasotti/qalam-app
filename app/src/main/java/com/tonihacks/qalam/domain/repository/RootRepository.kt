package com.tonihacks.qalam.domain.repository

import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Root

interface RootRepository {
    suspend fun getRoots(
        baseUrl: String,
        page: Int = 1,
        size: Int = 20,
    ): Result<PagedResult<Root>>

    suspend fun getRoot(baseUrl: String, id: String): Result<Root>
}
