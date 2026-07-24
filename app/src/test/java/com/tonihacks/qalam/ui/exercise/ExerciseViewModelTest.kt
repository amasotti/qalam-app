package com.tonihacks.qalam.ui.exercise

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.ExerciseAnswer
import com.tonihacks.qalam.domain.model.ExerciseSession
import com.tonihacks.qalam.domain.model.ExerciseSessionSummary
import com.tonihacks.qalam.domain.model.ExerciseType
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.WordListSummary
import com.tonihacks.qalam.domain.repository.ExerciseRepository
import com.tonihacks.qalam.domain.repository.WordListRepository
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeExerciseRepository
    private lateinit var wordListRepository: WordListRepository
    private lateinit var preferences: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeExerciseRepository()
        wordListRepository = mockk()
        preferences = mockk()
        every { preferences.baseUrl } returns flowOf(BASE_URL)
        coEvery { wordListRepository.getWordLists(BASE_URL, any(), any()) } returns Result.success(
            PagedResult(items = emptyList<WordListSummary>(), total = 0, hasMore = false),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startSession forwards selected exercise types`() = runTest {
        val viewModel = ExerciseViewModel(repository, wordListRepository, preferences)
        val types = listOf(ExerciseType.MULTIPLE_CHOICE_ARABIC, ExerciseType.CONFUSABLE_MEANING)

        viewModel.startSession(exerciseTypes = types)

        assertEquals(types, repository.lastExerciseTypes)
    }

    private class FakeExerciseRepository : ExerciseRepository {
        var lastExerciseTypes: List<ExerciseType> = emptyList()

        override suspend fun startSession(
            baseUrl: String,
            mode: String,
            size: Int,
            wordListIds: List<String>,
            exerciseTypes: List<ExerciseType>,
            optionCount: Int,
        ): Result<ExerciseSession> {
            lastExerciseTypes = exerciseTypes
            return Result.success(ExerciseSession("session-1", mode, "ACTIVE", emptyList()))
        }

        override suspend fun answerItem(
            baseUrl: String,
            sessionId: String,
            itemId: String,
            selectedOptionId: String,
        ): Result<ExerciseAnswer> = Result.failure(UnsupportedOperationException())

        override suspend fun completeSession(
            baseUrl: String,
            sessionId: String,
        ): Result<ExerciseSessionSummary> = Result.failure(UnsupportedOperationException())
    }

    private companion object {
        const val BASE_URL = "http://localhost"
    }
}
