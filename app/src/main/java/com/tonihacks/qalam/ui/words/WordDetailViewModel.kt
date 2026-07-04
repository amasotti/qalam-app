package com.tonihacks.qalam.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InsightPhase { IDLE, LOADING, RESULT }

sealed interface WordDetailUiState {
    data object Loading : WordDetailUiState
    data class Success(
        val word: Word,
        val examples: List<Example>,
        val dictionaries: List<DictionaryLink>,
        // --- AI examples (ephemeral) ---
        val aiExamples: List<AiExample> = emptyList(),
        val isGeneratingExamples: Boolean = false,
        val savingExample: Boolean = false,
        val examplesError: String? = null,
        val aiExamplesUnavailable: Boolean = false,
        // --- AI insight (ephemeral) ---
        val insightPhase: InsightPhase = InsightPhase.IDLE,
        val insightText: String? = null,
        val insightError: String? = null,
        val insightUnavailable: Boolean = false,
    ) : WordDetailUiState
    data class Error(val message: String) : WordDetailUiState
}


@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val prefs: PreferencesRepository,
): ViewModel() {

    private var currentWordId : String? = null

    private val _uiState = MutableStateFlow<WordDetailUiState>(WordDetailUiState.Loading)
    val uiState : StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    fun load(wordId: String) {
        if (wordId == currentWordId) return
        currentWordId = wordId

        viewModelScope.launch {
            _uiState.value = WordDetailUiState.Loading
            val baseUrl = prefs.baseUrl.first()

            val wordDeferred = async { wordRepository.getWord(baseUrl, wordId) }
            val examplesDeferred = async { wordRepository.getExamples(baseUrl, wordId) }
            val dictionariesDeferred = async { wordRepository.getDictionaryLinks(baseUrl, wordId) }

            val wordResult = wordDeferred.await()
            val examplesResult = examplesDeferred.await()
            val dictionariesResult = dictionariesDeferred.await()

            _uiState.value = when {
                wordResult.isSuccess -> WordDetailUiState.Success(
                    word = wordResult.getOrThrow(),
                    examples = examplesResult.getOrDefault(emptyList()),
                    dictionaries = dictionariesResult.getOrDefault(emptyList()),
                )
                else -> WordDetailUiState.Error(wordResult.exceptionOrNull()?.message ?: "Failed to load word detail")
            }
        }
    }

    // ---------------- AI examples ----------------

    fun generateExamples() {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess {
                it.copy(
                    isGeneratingExamples = true,
                    aiExamples = emptyList(),
                    examplesError = null,
                    aiExamplesUnavailable = false,
                )
            }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.generateExamples(baseUrl, wordId).fold(
                onSuccess = { list ->
                    updateSuccess { it.copy(aiExamples = list, isGeneratingExamples = false) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            isGeneratingExamples = false,
                            aiExamplesUnavailable = err is AiUnavailableException,
                            examplesError = if (err is AiUnavailableException) null else err.message
                                ?: "Failed to generate examples",
                        )
                    }
                },
            )
        }
    }

    fun useExample(example: AiExample) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(savingExample = true, examplesError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.saveExample(baseUrl, wordId, example).fold(
                onSuccess = {
                    val refreshed = wordRepository.getExamples(baseUrl, wordId).getOrNull()
                    updateSuccess { s ->
                        s.copy(
                            examples = refreshed ?: s.examples,
                            aiExamples = s.aiExamples - example,
                            savingExample = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess { it.copy(savingExample = false, examplesError = err.message ?: "Failed to save example") }
                },
            )
        }
    }

    fun discardExample(example: AiExample) {
        updateSuccess { it.copy(aiExamples = it.aiExamples - example) }
    }

    fun dismissExamples() {
        updateSuccess {
            it.copy(aiExamples = emptyList(), examplesError = null, aiExamplesUnavailable = false)
        }
    }

    // ---------------- AI insight ----------------

    fun getInsight() {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(insightPhase = InsightPhase.LOADING, insightError = null, insightUnavailable = false) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.generateInsight(baseUrl, entityType = "WORD", entityId = wordId).fold(
                onSuccess = { text ->
                    updateSuccess { it.copy(insightPhase = InsightPhase.RESULT, insightText = text) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            insightPhase = InsightPhase.IDLE,
                            insightUnavailable = err is AiUnavailableException,
                            insightError = if (err is AiUnavailableException) null else err.message
                                ?: "Failed to load insight",
                        )
                    }
                },
            )
        }
    }

    fun dismissInsight() {
        updateSuccess { it.copy(insightPhase = InsightPhase.IDLE, insightText = null, insightError = null) }
    }

    private inline fun updateSuccess(transform: (WordDetailUiState.Success) -> WordDetailUiState.Success) {
        _uiState.update { (it as? WordDetailUiState.Success)?.let(transform) ?: it }
    }
}
