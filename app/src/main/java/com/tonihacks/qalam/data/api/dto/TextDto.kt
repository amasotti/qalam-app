package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.model.TextToken
import kotlinx.serialization.Serializable

@Serializable
data class TextDto(
    val id: String,
    val title: String,
    val body: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val difficulty: String,
    val dialect: String,
    val tags: List<String> = emptyList(),
)

@Serializable
data class TokenDto(
    val id: String,
    val sentenceId: String,
    val position: Int,
    val arabic: String,
    val transliteration: String? = null,
    val translation: String? = null,
    val wordId: String? = null,
)

@Serializable
data class SentenceDto(
    val id: String,
    val textId: String,
    val position: Int,
    val arabicText: String,
    val transliteration: String? = null,
    val freeTranslation: String? = null,
    val tokens: List<TokenDto> = emptyList(),
)

fun TextDto.toDomain() = TextPassage(
    id = id,
    title = title,
    body = body,
    transliteration = transliteration,
    translation = translation,
    difficulty = difficulty,
    dialect = dialect,
    tags = tags,
)

fun TokenDto.toDomain() = TextToken(
    id = id,
    position = position,
    arabic = arabic,
    transliteration = transliteration,
    translation = translation,
    wordId = wordId,
)

fun SentenceDto.toDomain() = TextSentence(
    id = id,
    position = position,
    arabicText = arabicText,
    transliteration = transliteration,
    freeTranslation = freeTranslation,
    tokens = tokens.map { it.toDomain() },
)
