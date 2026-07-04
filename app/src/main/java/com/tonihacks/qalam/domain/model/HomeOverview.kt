package com.tonihacks.qalam.domain.model

data class HomeOverview(
    val totalWords: Int,
    val totalRoots: Int,
    val totalTexts: Int,
    val masteryCounts: Map<MasteryLevel, Int>,
) {
    // Words not yet MASTERED still cycle through training — that's the review queue.
    val dueCount: Int get() = totalWords - (masteryCounts[MasteryLevel.MASTERED] ?: 0)
}
