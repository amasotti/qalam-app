package com.tonihacks.qalam.ui.productionpractice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.ProductionPracticePrompt
import com.tonihacks.qalam.domain.model.ProductionPracticeSubmission
import com.tonihacks.qalam.domain.repository.ProductionPracticeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductionPracticeUiState(
    val isLoadingPrompt: Boolean = true,
    val prompt: ProductionPracticePrompt? = null,
    val sentence: String = "",
    val usedWordIds: Set<String> = emptySet(),
    val isReviewing: Boolean = false,
    val reviewMarkdown: String? = null,
    val isAiUnavailable: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ProductionPracticeViewModel @Inject constructor(
    private val repository: ProductionPracticeRepository,
    private val preferences: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProductionPracticeUiState())
    val uiState: StateFlow<ProductionPracticeUiState> = _uiState.asStateFlow()

    init {
        loadPrompt()
    }

    fun loadPrompt() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingPrompt = true,
                    prompt = null,
                    sentence = "",
                    usedWordIds = emptySet(),
                    reviewMarkdown = null,
                    isAiUnavailable = false,
                    error = null,
                )
            }
            repository.getPrompt(preferences.baseUrl.first()).fold(
                onSuccess = { prompt ->
                    _uiState.update {
                        it.copy(
                            isLoadingPrompt = false,
                            prompt = prompt,
                            sentence = "",
                            usedWordIds = emptySet(),
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoadingPrompt = false,
                            error = error.message ?: "Could not load a production prompt",
                        )
                    }
                },
            )
        }
    }

    fun updateSentence(sentence: String) {
        _uiState.update { it.copy(sentence = sentence, error = null, reviewMarkdown = null) }
    }

    fun toggleUsedWord(wordId: String) {
        _uiState.update { state ->
            state.copy(
                usedWordIds = if (wordId in state.usedWordIds) state.usedWordIds - wordId else state.usedWordIds + wordId,
                error = null,
                reviewMarkdown = null,
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        val prompt = state.prompt ?: return
        when {
            state.sentence.isBlank() -> {
                _uiState.update { it.copy(error = "Write a sentence before requesting feedback.") }
                return
            }
            state.usedWordIds.size < MIN_USED_WORDS -> {
                _uiState.update { it.copy(error = "Select at least two words you used.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isReviewing = true, isAiUnavailable = false, error = null) }
            val submission = ProductionPracticeSubmission(
                sentence = state.sentence,
                targetWordIds = prompt.words.map { it.id },
                usedWordIds = prompt.words.map { it.id }.filter { it in state.usedWordIds },
            )
            repository.review(preferences.baseUrl.first(), submission).fold(
                onSuccess = { review ->
                    _uiState.update { it.copy(isReviewing = false, reviewMarkdown = review.markdown) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isReviewing = false,
                            isAiUnavailable = error is AiUnavailableException,
                            error = if (error is AiUnavailableException) null
                            else error.message ?: "Could not review your sentence",
                        )
                    }
                },
            )
        }
    }

    private companion object {
        const val MIN_USED_WORDS = 2
    }
}
