package com.tonihacks.qalam.ui.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.ExerciseAnswer
import com.tonihacks.qalam.domain.model.ExerciseItem
import com.tonihacks.qalam.domain.model.ExerciseSession
import com.tonihacks.qalam.domain.model.ExerciseSessionSummary
import com.tonihacks.qalam.domain.model.WordListSummary
import com.tonihacks.qalam.domain.repository.ExerciseRepository
import com.tonihacks.qalam.domain.repository.WordListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExerciseUiState(
    val isLoading: Boolean = false,
    val session: ExerciseSession? = null,
    val currentIndex: Int = 0,
    val selectedOptionId: String? = null,
    val answerFeedback: ExerciseAnswer? = null,
    val answers: List<ExerciseAnswer> = emptyList(),
    val summary: ExerciseSessionSummary? = null,
    val error: String? = null,
    val wordLists: List<WordListSummary> = emptyList(),
    val isLoadingWordLists: Boolean = false,
    val wordListError: String? = null,
) {
    val currentItem: ExerciseItem?
        get() = session?.items?.getOrNull(currentIndex)

    val isComplete: Boolean
        get() = session != null && currentIndex >= session.items.size
}

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository,
    private val wordListRepository: WordListRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExerciseUiState())
    val uiState: StateFlow<ExerciseUiState> = _uiState.asStateFlow()

    init {
        loadWordLists()
    }

    fun startSession(
        mode: String = "MIXED",
        size: Int = 10,
        wordListIds: List<String> = emptyList(),
    ) {
        viewModelScope.launch {
            _uiState.update {
                ExerciseUiState(
                    isLoading = true,
                    wordLists = it.wordLists,
                    isLoadingWordLists = it.isLoadingWordLists,
                    wordListError = it.wordListError,
                )
            }
            val baseUrl = prefs.baseUrl.first()
            exerciseRepository.startSession(
                baseUrl = baseUrl,
                mode = mode,
                size = size,
                wordListIds = wordListIds,
            ).fold(
                onSuccess = { session ->
                    _uiState.update {
                        ExerciseUiState(
                            session = session,
                            wordLists = it.wordLists,
                            isLoadingWordLists = it.isLoadingWordLists,
                            wordListError = it.wordListError,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = err.message ?: "Failed to start exercise")
                    }
                },
            )
        }
    }

    fun loadWordLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingWordLists = true, wordListError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordListRepository.getWordLists(baseUrl, size = WORD_LIST_PAGE_SIZE).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isLoadingWordLists = false,
                            wordLists = result.items,
                            wordListError = null,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingWordLists = false,
                            wordListError = err.message ?: "Could not load word lists",
                        )
                    }
                },
            )
        }
    }

    fun answer(optionId: String) {
        val state = _uiState.value
        if (state.answerFeedback != null || state.selectedOptionId != null) return
        val session = state.session ?: return
        val item = state.currentItem ?: return

        _uiState.update { it.copy(selectedOptionId = optionId, error = null) }
        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            exerciseRepository.answerItem(baseUrl, session.id, item.id, optionId).fold(
                onSuccess = { answer ->
                    _uiState.update {
                        it.copy(
                            answerFeedback = answer,
                            answers = it.answers + answer,
                            error = null,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            selectedOptionId = null,
                            error = err.message ?: "Failed to submit answer",
                        )
                    }
                },
            )
        }
    }

    fun nextItem() {
        _uiState.update {
            it.copy(
                currentIndex = it.currentIndex + 1,
                selectedOptionId = null,
                answerFeedback = null,
                error = null,
            )
        }
    }

    fun completeSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            exerciseRepository.completeSession(baseUrl, session.id).fold(
                onSuccess = { summary ->
                    _uiState.update { it.copy(summary = summary, error = null) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(error = err.message ?: "Failed to complete exercise")
                    }
                },
            )
        }
    }

    fun resetSession() {
        val state = _uiState.value
        _uiState.value = ExerciseUiState(
            wordLists = state.wordLists,
            isLoadingWordLists = state.isLoadingWordLists,
            wordListError = state.wordListError,
        )
    }

    private companion object {
        const val WORD_LIST_PAGE_SIZE = 500
    }
}
