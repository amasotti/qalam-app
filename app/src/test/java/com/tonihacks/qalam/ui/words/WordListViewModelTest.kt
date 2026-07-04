package com.tonihacks.qalam.ui.words

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.*
import com.tonihacks.qalam.domain.repository.WordRepository
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WordListViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepo: FakeWordRepository
    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepo = FakeWordRepository()
        prefs = mockk()
        every { prefs.baseUrl } returns flowOf("http://localhost")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Test 3 ─────────────────────────────────────────────────────────────
    // ViewModel calls load() in init{}. After construction the state must
    // already contain the words returned by the repository.
    @Test
    fun `init triggers load and populates items`() = runTest {
        fakeRepo.wordsResult = Result.success(
            PagedResult(listOf(aWord("1"), aWord("2")), total = 2, hasMore = false)
        )

        val vm = WordListViewModel(fakeRepo, prefs)

        assertEquals(2, vm.uiState.value.items.size)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    // ── Test 4 ─────────────────────────────────────────────────────────────
    // onFilterChange must reset the list and page counter before fetching
    // the filtered results.  currentPage becomes 2 after one successful fetch
    // (ViewModel increments it on each successful page load).
    @Test
    fun `onFilterChange resets pagination and sets active filter`() = runTest {
        fakeRepo.wordsResult = Result.success(
            PagedResult(listOf(aWord("1")), total = 1, hasMore = false)
        )
        val vm = WordListViewModel(fakeRepo, prefs)

        vm.onFilterChange(MasteryLevel.KNOWN)

        with(vm.uiState.value) {
            assertEquals(MasteryLevel.KNOWN, activeFilter)
            assertEquals(1, items.size)        // re-fetched from page 1
            assertEquals(2, currentPage)       // 1 → incremented after fetch
        }
    }

    // ── Test 5 ─────────────────────────────────────────────────────────────
    // When the repository returns a failure the ViewModel must surface the
    // error message and must not stay in the loading state.
    @Test
    fun `repository failure sets error and clears loading flag`() = runTest {
        fakeRepo.wordsResult = Result.failure(RuntimeException("network down"))

        val vm = WordListViewModel(fakeRepo, prefs)

        with(vm.uiState.value) {
            assertEquals("network down", error)
            assertFalse(isLoading)
            assertTrue(items.isEmpty())
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun aWord(id: String) = Word(
        id = id,
        arabicText = "كتب",
        transliteration = "kataba",
        translation = "to write",
        partOfSpeech = "verb",
        dialect = "MSA",
        masteryLevel = MasteryLevel.NEW,
        rootId = null,
        notes = null,
        pronunciationUrl = null,
    )

    /**
     * Manual fake for WordRepository (an interface).
     *
     * Prefer fakes over mocks for interfaces — the fake is a real implementation
     * you control; it makes tests readable without `every { … }` noise.
     * Unused methods throw [error] so a test that accidentally calls them fails loudly.
     */
    private class FakeWordRepository : WordRepository {

        var wordsResult: Result<PagedResult<Word>> =
            Result.success(PagedResult(emptyList(), 0, false))

        override suspend fun getWords(
            baseUrl: String,
            query: String?,
            masteryLevel: String?,
            rootId: String?,
            page: Int,
            size: Int,
        ): Result<PagedResult<Word>> = wordsResult

        override suspend fun getWord(baseUrl: String, id: String): Result<Word> =
            error("not stubbed")

        override suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>> =
            error("not stubbed")

        override suspend fun getDictionaryLinks(
            baseUrl: String,
            wordId: String,
        ): Result<List<DictionaryLink>> =
            error("not stubbed")

        override suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word> =
            error("not stubbed")
    }
}
