package com.tonihacks.qalam.ui.roots

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Root
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.repository.RootRepository
import com.tonihacks.qalam.domain.repository.WordRepository
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
class RootListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var rootRepository: RootRepository
    private lateinit var wordRepository: WordRepository
    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        rootRepository = mockk()
        wordRepository = mockk()
        prefs = mockk()
        every { prefs.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads roots with derived form counts`() = runTest {
        coEvery { rootRepository.getRoots(BASE_URL, page = 1, size = 20) } returns Result.success(
            PagedResult(listOf(aRoot("r1"), aRoot("r2")), total = 2, hasMore = false),
        )
        coEvery {
            wordRepository.getWords(BASE_URL, any(), any(), "r1", 1, 1, "UPDATED_AT", true)
        } returns Result.success(PagedResult<Word>(emptyList(), total = 3, hasMore = false))
        coEvery {
            wordRepository.getWords(BASE_URL, any(), any(), "r2", 1, 1, "UPDATED_AT", true)
        } returns Result.success(PagedResult<Word>(emptyList(), total = 1, hasMore = false))

        val vm = RootListViewModel(rootRepository, wordRepository, prefs)

        with(vm.uiState.value) {
            assertFalse(isLoading)
            assertEquals(listOf("r1", "r2"), items.map { it.root.id })
            assertEquals(listOf(3, 1), items.map { it.formCount })
            assertFalse(hasMore)
            assertEquals(2, currentPage)
        }
    }

    @Test
    fun `refresh resets items and reloads first page`() = runTest {
        coEvery { rootRepository.getRoots(BASE_URL, page = 1, size = 20) } returns Result.success(
            PagedResult(listOf(aRoot("r1")), total = 1, hasMore = false),
        )
        coEvery {
            wordRepository.getWords(BASE_URL, any(), any(), any(), 1, 1, "UPDATED_AT", true)
        } returns Result.success(PagedResult<Word>(emptyList(), total = 0, hasMore = false))
        val vm = RootListViewModel(rootRepository, wordRepository, prefs)

        vm.refresh()

        with(vm.uiState.value) {
            assertEquals(listOf("r1"), items.map { it.root.id })
            assertFalse(isRefreshing)
            assertEquals(2, currentPage)
        }
    }

    @Test
    fun `repository failure sets error and clears loading`() = runTest {
        coEvery { rootRepository.getRoots(BASE_URL, page = 1, size = 20) } returns
            Result.failure(RuntimeException("roots unavailable"))

        val vm = RootListViewModel(rootRepository, wordRepository, prefs)

        with(vm.uiState.value) {
            assertFalse(isLoading)
            assertEquals("roots unavailable", error)
            assertTrue(items.isEmpty())
        }
    }

    private fun aRoot(id: String) = Root(
        id = id,
        letters = listOf("ك", "ت", "ب"),
        normalizedForm = "كتب",
        displayForm = "ك ت ب",
        letterCount = 3,
        meaning = "writing",
        analysis = null,
    )

    private companion object {
        const val BASE_URL = "http://localhost"
    }
}
