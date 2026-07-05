package com.tonihacks.qalam.ui.home

import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.HomeOverview
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.repository.AnalyticsRepository
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
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var prefs: PreferencesRepository
    private lateinit var apiClient: ApiClient
    private lateinit var analyticsRepository: AnalyticsRepository
    private lateinit var wordRepository: WordRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        prefs = mockk()
        apiClient = mockk()
        analyticsRepository = mockk()
        wordRepository = mockk()
        every { prefs.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init loads overview and recent words`() = runTest {
        coEvery { analyticsRepository.getOverview(BASE_URL) } returns Result.success(
            HomeOverview(
                totalWords = 9,
                totalRoots = 3,
                totalTexts = 2,
                masteryCounts = mapOf(MasteryLevel.MASTERED to 4, MasteryLevel.LEARNING to 5),
            ),
        )
        coEvery {
            wordRepository.getWords(BASE_URL, any(), any(), any(), 1, 6, "UPDATED_AT", true)
        } returns Result.success(PagedResult(listOf(aWord("1"), aWord("2")), total = 2, hasMore = false))

        val vm = HomeViewModel(prefs, apiClient, analyticsRepository, wordRepository)

        with(vm.uiState.value) {
            assertFalse(isLoading)
            assertEquals(9, totalWords)
            assertEquals(3, totalRoots)
            assertEquals(2, totalTexts)
            assertEquals(5, dueCount)
            assertEquals(listOf("1", "2"), recentWords.map { it.id })
        }
    }

    @Test
    fun `overview failure surfaces error and clears loading`() = runTest {
        coEvery { analyticsRepository.getOverview(BASE_URL) } returns Result.failure(RuntimeException("backend asleep"))
        coEvery {
            wordRepository.getWords(BASE_URL, any(), any(), any(), 1, 6, "UPDATED_AT", true)
        } returns Result.success(PagedResult(emptyList(), total = 0, hasMore = false))

        val vm = HomeViewModel(prefs, apiClient, analyticsRepository, wordRepository)

        with(vm.uiState.value) {
            assertFalse(isLoading)
            assertEquals("backend asleep", error)
            assertTrue(recentWords.isEmpty())
        }
    }

    @Test
    fun `ping maps connection result to online and offline`() = runTest {
        coEvery { analyticsRepository.getOverview(BASE_URL) } returns Result.success(
            HomeOverview(0, 0, 0, emptyMap()),
        )
        coEvery {
            wordRepository.getWords(BASE_URL, any(), any(), any(), 1, 6, "UPDATED_AT", true)
        } returns Result.success(PagedResult(emptyList(), total = 0, hasMore = false))
        coEvery { apiClient.testConnection(any()) } returns Result.success(Unit) andThen
            Result.failure(RuntimeException("no route"))
        val vm = HomeViewModel(prefs, apiClient, analyticsRepository, wordRepository)

        vm.ping()
        assertEquals(HomeViewModel.ConnectionState.Online, vm.connectionState.value)

        vm.ping()
        assertEquals(HomeViewModel.ConnectionState.Offline, vm.connectionState.value)
    }

    private fun aWord(id: String) = Word(
        id = id,
        arabicText = "كتب",
        transliteration = "kataba",
        translation = "to write",
        partOfSpeech = "VERB",
        dialect = "MSA",
        masteryLevel = MasteryLevel.NEW,
        rootId = null,
        notes = null,
        pronunciationUrl = null,
    )

    private companion object {
        const val BASE_URL = "http://localhost"
    }
}
