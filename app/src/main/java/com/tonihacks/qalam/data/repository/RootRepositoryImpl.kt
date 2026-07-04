package com.tonihacks.qalam.data.repository

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.api.dto.toDomain
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Root
import com.tonihacks.qalam.domain.repository.RootRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RootRepositoryImpl @Inject constructor(
    private val apiClient: ApiClient,
) : RootRepository {

    override suspend fun getRoots(
        baseUrl: String,
        page: Int,
        size: Int,
    ): Result<PagedResult<Root>> =
        apiClient.getRoots(baseUrl, page, size).map { dto ->
            PagedResult(
                items = dto.items.map { it.toDomain() },
                total = dto.total,
                hasMore = dto.items.size == size,
            )
        }

    override suspend fun getRoot(baseUrl: String, id: String): Result<Root> =
        apiClient.getRoot(baseUrl, id).map { it.toDomain() }
}
