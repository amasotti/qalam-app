package com.tonihacks.qalam.ui.productionpractice

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.ProductionPracticePrompt
import com.tonihacks.qalam.domain.model.ProductionPracticeReview
import com.tonihacks.qalam.domain.model.ProductionPracticeSubmission
import com.tonihacks.qalam.domain.model.ProductionPracticeWord
import com.tonihacks.qalam.domain.repository.ProductionPracticeRepository
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
class ProductionPracticeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeProductionPracticeRepository
    private lateinit var preferences: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeProductionPracticeRepository()
        preferences = mockk()
        every { preferences.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `submit preserves prompt order for selected target words`() = runTest {
        val viewModel = ProductionPracticeViewModel(repository, preferences)

        viewModel.updateSentence("أكتب جملة")
        viewModel.toggleUsedWord("word-2")
        viewModel.toggleUsedWord("word-1")
        viewModel.submit()

        assertEquals(listOf("word-1", "word-2"), repository.submission?.usedWordIds)
        assertEquals((1..7).map { "word-$it" }, repository.submission?.targetWordIds)
    }

    private class FakeProductionPracticeRepository : ProductionPracticeRepository {
        var submission: ProductionPracticeSubmission? = null

        override suspend fun getPrompt(baseUrl: String): Result<ProductionPracticePrompt> = Result.success(
            ProductionPracticePrompt((1..7).map { index ->
                ProductionPracticeWord(
                    id = "word-$index",
                    arabicText = "كَلِمَة",
                    transliteration = null,
                    translation = "word",
                    partOfSpeech = "NOUN",
                    dialect = "MSA",
                )
            }),
        )

        override suspend fun review(
            baseUrl: String,
            submission: ProductionPracticeSubmission,
        ): Result<ProductionPracticeReview> {
            this.submission = submission
            return Result.success(ProductionPracticeReview("Feedback"))
        }
    }

    private companion object {
        const val BASE_URL = "http://localhost"
    }
}
