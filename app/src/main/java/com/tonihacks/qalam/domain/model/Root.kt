package com.tonihacks.qalam.domain.model

data class Root(
    val id: String,
    val letters: List<String>,
    val normalizedForm: String,
    val displayForm: String,
    val letterCount: Int,
    val meaning: String?,
    val analysis: String?,
)

data class RootListItem(
    val root: Root,
    val formCount: Int,
)
