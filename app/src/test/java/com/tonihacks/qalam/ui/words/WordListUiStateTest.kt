package com.tonihacks.qalam.ui.words

import com.tonihacks.qalam.domain.model.MasteryLevel
import org.junit.Assert.*
import org.junit.Test

class WordListUiStateTest {

    // ── Test 1 ─────────────────────────────────────────────────────────────
    // Verify the default constructor produces a sane "empty / idle" screen state.
    @Test
    fun `default state represents an empty idle screen`() {
        val state = WordListUiState()

        assertTrue(state.items.isEmpty())
        assertEquals("", state.query)
        assertNull(state.activeFilter)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.hasMore)
        assertEquals(1, state.currentPage)
        assertFalse(state.isCreating)
        assertNull(state.createWordError)
    }

    // ── Test 2 ─────────────────────────────────────────────────────────────
    // Kotlin data classes give copy() for free — use it to simulate what
    // ViewModel does when the user taps a filter chip.
    @Test
    fun `applying a filter resets pagination but preserves query`() {
        val state = WordListUiState(query = "كتب", currentPage = 3, hasMore = false)

        val filtered = state.copy(
            activeFilter = MasteryLevel.KNOWN,
            items = emptyList(),
            currentPage = 1,
            hasMore = true,
        )

        assertEquals(MasteryLevel.KNOWN, filtered.activeFilter)
        assertEquals(1, filtered.currentPage)
        assertTrue(filtered.hasMore)
        assertTrue(filtered.items.isEmpty())
        assertEquals("كتب", filtered.query)
    }
}
