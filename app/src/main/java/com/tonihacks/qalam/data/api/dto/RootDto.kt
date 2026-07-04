package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.Root
import kotlinx.serialization.Serializable

@Serializable
data class RootDto(
    val id: String,
    val letters: List<String>,
    val normalizedForm: String,
    val displayForm: String,
    val letterCount: Int,
    val meaning: String? = null,
    val analysis: String? = null,
)

fun RootDto.toDomain() = Root(
    id = id,
    letters = letters,
    normalizedForm = normalizedForm,
    displayForm = displayForm,
    letterCount = letterCount,
    meaning = meaning,
    analysis = analysis,
)
