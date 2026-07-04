package com.tonihacks.qalam.ui.training

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.TrainingSession
import com.tonihacks.qalam.domain.model.TrainingSessionSummary
import com.tonihacks.qalam.domain.model.TrainingWord
import com.tonihacks.qalam.domain.model.TrainingWordResult
import com.tonihacks.qalam.domain.repository.TrainingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrainingUiState(
    val isLoading: Boolean = false,
    val session: TrainingSession? = null,
    val currentIndex: Int = 0,
    val isRevealed: Boolean = false,
    val results: List<TrainingWordResult> = emptyList(),
    val summary: TrainingSessionSummary? = null,
    val error: String? = null,
) {
    val currentWord: TrainingWord?
        get() = session?.words?.getOrNull(currentIndex)

    val isComplete: Boolean
        get() = session != null && currentIndex >= session.words.size
}

@HiltViewModel
class TrainingViewModel @Inject constructor(
    private val trainingRepository: TrainingRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrainingUiState())
    val uiState: StateFlow<TrainingUiState> = _uiState.asStateFlow()

    fun startSession() {
        viewModelScope.launch {
            _uiState.update { TrainingUiState(isLoading = true) }
            val baseUrl = prefs.baseUrl.first()
            trainingRepository.startSession(baseUrl).fold(
                onSuccess = { session ->
                    _uiState.update { TrainingUiState(session = session) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = err.message ?: "Failed to start training")
                    }
                },
            )
        }
    }

    fun revealAnswer() {
        _uiState.update { it.copy(isRevealed = true) }
    }

    fun submitCurrentResult(knewIt: Boolean) {
        val state = _uiState.value
        val session = state.session ?: return
        val word = state.currentWord ?: return
        val result = TrainingWordResult(wordId = word.id, knewIt = knewIt)

        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            trainingRepository.submitResult(baseUrl, session.id, result).fold(
                onSuccess = {
                    _uiState.update { current ->
                        current.copy(
                            currentIndex = current.currentIndex + 1,
                            isRevealed = false,
                            results = current.results + result,
                            error = null,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(error = err.message ?: "Failed to record result")
                    }
                },
            )
        }
    }

    fun completeSession() {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            trainingRepository.completeSession(baseUrl, session.id).fold(
                onSuccess = { summary ->
                    _uiState.update { it.copy(summary = summary, error = null) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(error = err.message ?: "Failed to complete training")
                    }
                },
            )
        }
    }
}
