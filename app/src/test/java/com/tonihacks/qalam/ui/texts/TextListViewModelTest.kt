package com.tonihacks.qalam.ui.texts

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.repository.TextRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TextListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var textRepository: TextRepository
    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        textRepository = mockk()
        prefs = mockk()
        every { prefs.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads first page`() = runTest {
        coEvery { textRepository.getTexts(BASE_URL, page = 1, size = 20, sortBy = "UPDATED_AT", sortDesc = true) } returns
            Result.success(PagedResult(listOf(aText("t1")), total = 1, hasMore = false))

        val vm = TextListViewModel(textRepository, prefs)

        with(vm.uiState.value) {
            assertEquals(listOf("t1"), items.map { it.id })
            assertFalse(isLoading)
            assertFalse(hasMore)
            assertEquals(2, currentPage)
        }
    }

    @Test
    fun `loadMore appends next page`() = runTest {
        coEvery { textRepository.getTexts(BASE_URL, page = 1, size = 20, sortBy = "UPDATED_AT", sortDesc = true) } returns
            Result.success(PagedResult(listOf(aText("t1")), total = 2, hasMore = true))
        coEvery { textRepository.getTexts(BASE_URL, page = 2, size = 20, sortBy = "UPDATED_AT", sortDesc = true) } returns
            Result.success(PagedResult(listOf(aText("t2")), total = 2, hasMore = false))
        val vm = TextListViewModel(textRepository, prefs)

        vm.loadMore()

        with(vm.uiState.value) {
            assertEquals(listOf("t1", "t2"), items.map { it.id })
            assertFalse(hasMore)
            assertEquals(3, currentPage)
        }
    }

    @Test
    fun `failure sets error and keeps existing items empty`() = runTest {
        coEvery { textRepository.getTexts(BASE_URL, page = 1, size = 20, sortBy = "UPDATED_AT", sortDesc = true) } returns
            Result.failure(RuntimeException("texts down"))

        val vm = TextListViewModel(textRepository, prefs)

        with(vm.uiState.value) {
            assertFalse(isLoading)
            assertEquals("texts down", error)
            assertTrue(items.isEmpty())
        }
    }

    private fun aText(id: String) = TextPassage(
        id = id,
        title = "A text",
        body = "كتب الولد",
        transliteration = null,
        translation = null,
        difficulty = "BEGINNER",
        dialect = "MSA",
        tags = emptyList(),
    )

    private companion object {
        const val BASE_URL = "http://localhost"
    }
}
