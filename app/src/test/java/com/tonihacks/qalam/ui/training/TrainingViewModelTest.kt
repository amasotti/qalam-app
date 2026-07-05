package com.tonihacks.qalam.ui.training

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.FlashcardSide
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.RecordedTrainingResult
import com.tonihacks.qalam.domain.model.TrainingSession
import com.tonihacks.qalam.domain.model.TrainingSessionSummary
import com.tonihacks.qalam.domain.model.TrainingWord
import com.tonihacks.qalam.domain.model.TrainingWordResult
import com.tonihacks.qalam.domain.repository.TrainingRepository
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TrainingViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeTrainingRepository
    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeTrainingRepository()
        prefs = mockk()
        every { prefs.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSession stores returned session`() = runTest {
        repo.startResult = Result.success(aSession(words = listOf(aTrainingWord("w1"))))
        val vm = TrainingViewModel(repo, prefs)

        vm.startSession(mode = "LEARNING", size = 10)

        assertEquals("LEARNING", repo.lastMode)
        assertEquals(10, repo.lastSize)
        assertEquals("session-1", vm.uiState.value.session?.id)
        assertFalse(vm.uiState.value.isLoading)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `submitCurrentResult advances card and records local result`() = runTest {
        repo.startResult = Result.success(aSession(words = listOf(aTrainingWord("w1"), aTrainingWord("w2"))))
        val vm = TrainingViewModel(repo, prefs)
        vm.startSession()
        vm.revealAnswer()

        vm.submitCurrentResult(knewIt = true)

        with(vm.uiState.value) {
            assertEquals(1, currentIndex)
            assertFalse(isRevealed)
            assertEquals(listOf(TrainingWordResult("w1", knewIt = true)), results)
            assertEquals("w2", currentWord?.id)
        }
    }

    @Test
    fun `completeSession stores summary`() = runTest {
        repo.startResult = Result.success(aSession(words = listOf(aTrainingWord("w1"))))
        repo.summaryResult = Result.success(
            TrainingSessionSummary(
                sessionId = "session-1",
                mode = "MIXED",
                totalWords = 1,
                correct = 1,
                incorrect = 0,
                skipped = 0,
                accuracy = 1.0,
                promotions = emptyList(),
            ),
        )
        val vm = TrainingViewModel(repo, prefs)
        vm.startSession()

        vm.completeSession()

        assertEquals(1.0, vm.uiState.value.summary?.accuracy)
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `resetSession returns to idle state`() = runTest {
        repo.startResult = Result.success(aSession(words = listOf(aTrainingWord("w1"))))
        val vm = TrainingViewModel(repo, prefs)
        vm.startSession()

        vm.resetSession()

        with(vm.uiState.value) {
            assertNull(session)
            assertEquals(0, currentIndex)
            assertTrue(results.isEmpty())
        }
    }

    private class FakeTrainingRepository : TrainingRepository {
        var startResult: Result<TrainingSession> = Result.success(aSession())
        var submitResult: Result<RecordedTrainingResult> = Result.success(
            RecordedTrainingResult(wordId = "w1", result = "CORRECT", masteryPromotion = null),
        )
        var summaryResult: Result<TrainingSessionSummary> = Result.failure(RuntimeException("not stubbed"))
        var lastMode: String? = null
        var lastSize: Int? = null

        override suspend fun startSession(baseUrl: String, mode: String, size: Int): Result<TrainingSession> {
            lastMode = mode
            lastSize = size
            return startResult
        }

        override suspend fun submitResult(
            baseUrl: String,
            sessionId: String,
            result: TrainingWordResult,
        ): Result<RecordedTrainingResult> = submitResult

        override suspend fun completeSession(baseUrl: String, sessionId: String): Result<TrainingSessionSummary> =
            summaryResult
    }

    private companion object {
        const val BASE_URL = "http://localhost"

        fun aSession(words: List<TrainingWord> = emptyList()) = TrainingSession(
            id = "session-1",
            mode = "MIXED",
            status = "ACTIVE",
            words = words,
        )

        fun aTrainingWord(id: String) = TrainingWord(
            id = id,
            frontSide = FlashcardSide.ARABIC,
            arabicText = "كتب",
            transliteration = "kataba",
            translation = "to write",
            partOfSpeech = "VERB",
            position = 1,
            masteryLevel = MasteryLevel.NEW,
            root = null,
            notes = null,
            examples = emptyList(),
            relations = emptyList(),
        )
    }
}
