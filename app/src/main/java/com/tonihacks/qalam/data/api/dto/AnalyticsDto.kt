package com.tonihacks.qalam.data.api.dto

import com.tonihacks.qalam.domain.model.HomeOverview
import com.tonihacks.qalam.domain.model.MasteryLevel
import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsOverviewDto(
    val words: WordStatsDto,
    val roots: RootStatsDto,
    val texts: TextStatsDto,
)

@Serializable
data class WordStatsDto(
    val total: Int,
    val byMastery: Map<String, Int> = emptyMap(),
)

@Serializable
data class RootStatsDto(val total: Int)

@Serializable
data class TextStatsDto(val total: Int)

fun AnalyticsOverviewDto.toDomain() = HomeOverview(
    totalWords = words.total,
    totalRoots = roots.total,
    totalTexts = texts.total,
    masteryCounts = words.byMastery.mapNotNull { (key, count) ->
        runCatching { MasteryLevel.valueOf(key) }.getOrNull()?.let { it to count }
    }.toMap(),
)
