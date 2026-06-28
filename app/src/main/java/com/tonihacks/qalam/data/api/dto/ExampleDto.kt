package com.tonihacks.qalam.data.api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExampleDto(
    val id: String,
    val arabicText: String,
    val transliteration: String? = null,
    val translation: String? = null,
)