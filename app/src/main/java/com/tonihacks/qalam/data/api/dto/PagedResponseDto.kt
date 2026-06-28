package com.tonihacks.qalam.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class PagedResponseDto<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val size: Int,
)