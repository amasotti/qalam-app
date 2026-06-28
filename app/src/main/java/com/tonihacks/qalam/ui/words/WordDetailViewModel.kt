package com.tonihacks.qalam.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tonihacks.qalam.data.local.PreferencesRepository
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
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface WordDetailUiState {
    data object Loading : WordDetailUiState
    data class Success(
        val word: Word,
        val examples: List<Example>,
        val dictionaries: List<DictionaryLink>,
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
}