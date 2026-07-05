package com.tonihacks.qalam.ui.wordlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordListDetail
import com.tonihacks.qalam.domain.model.WordListSuggestion
import com.tonihacks.qalam.domain.repository.WordListRepository
import com.tonihacks.qalam.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SuggestionUiItem(
    val suggestion: WordListSuggestion,
    val status: SuggestionStatus = SuggestionStatus.Checking,
    val existingWordId: String? = null,
)

enum class SuggestionStatus {
    Checking,
    Exists,
    Missing,
    Adding,
    Added,
    Error,
}

data class WordListDetailUiState(
    val isLoading: Boolean = false,
    val detail: WordListDetail? = null,
    val error: String? = null,
    val isSuggesting: Boolean = false,
    val suggestions: List<SuggestionUiItem> = emptyList(),
    val suggestionError: String? = null,
    val isAiUnavailable: Boolean = false,
    val addQuery: String = "",
    val addResults: List<WordAutocomplete> = emptyList(),
    val isSearchingWords: Boolean = false,
    val addWordError: String? = null,
)

@HiltViewModel
class WordListDetailViewModel @Inject constructor(
    private val wordListRepository: WordListRepository,
    private val wordRepository: WordRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WordListDetailUiState(isLoading = true))
    val uiState: StateFlow<WordListDetailUiState> = _uiState.asStateFlow()

    fun load(listId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val baseUrl = prefs.baseUrl.first()
            wordListRepository.getWordList(baseUrl, listId).fold(
                onSuccess = { detail ->
                    _uiState.update { it.copy(isLoading = false, detail = detail, error = null) }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = err.message ?: "Could not load word list")
                    }
                },
            )
        }
    }

    fun removeWord(wordId: String) {
        val listId = _uiState.value.detail?.id ?: return
        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            wordListRepository.removeWord(baseUrl, listId, wordId).onSuccess {
                _uiState.update { state ->
                    val detail = state.detail ?: return@update state
                    state.copy(detail = detail.copy(words = detail.words.filterNot { it.id == wordId }))
                }
            }
        }
    }

    fun searchWords(query: String) {
        _uiState.update { it.copy(addQuery = query, addWordError = null) }
        if (query.trim().length < MIN_SEARCH_LENGTH) {
            _uiState.update { it.copy(addResults = emptyList(), isSearchingWords = false) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingWords = true) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.autocompleteWords(baseUrl, query.trim()).fold(
                onSuccess = { words ->
                    val existingIds = _uiState.value.detail?.words?.map { it.id }?.toSet().orEmpty()
                    _uiState.update {
                        it.copy(
                            isSearchingWords = false,
                            addResults = words.filterNot { word -> word.id in existingIds },
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSearchingWords = false,
                            addWordError = err.message ?: "Could not search words",
                        )
                    }
                },
            )
        }
    }

    fun addExistingWord(wordId: String) {
        val listId = _uiState.value.detail?.id ?: return
        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            wordListRepository.addWord(baseUrl, listId, wordId).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(addQuery = "", addResults = emptyList(), addWordError = null)
                    }
                    load(listId)
                },
                onFailure = { err ->
                    _uiState.update { it.copy(addWordError = err.message ?: "Could not add word") }
                },
            )
        }
    }

    fun suggestWords() {
        val listId = _uiState.value.detail?.id ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSuggesting = true,
                    suggestions = emptyList(),
                    suggestionError = null,
                    isAiUnavailable = false,
                )
            }
            val baseUrl = prefs.baseUrl.first()
            wordListRepository.suggestWords(baseUrl, listId).fold(
                onSuccess = { suggestions ->
                    val items = suggestions.map { SuggestionUiItem(suggestion = it) }
                    _uiState.update { it.copy(isSuggesting = false, suggestions = items) }
                    suggestions.forEachIndexed { index, suggestion ->
                        checkSuggestion(baseUrl, index, suggestion)
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isSuggesting = false,
                            suggestionError = when (err) {
                                is AiUnavailableException -> null
                                else -> err.message ?: "Could not get suggestions"
                            },
                            isAiUnavailable = err is AiUnavailableException,
                        )
                    }
                },
            )
        }
    }

    fun addSuggestion(index: Int) {
        val state = _uiState.value
        val listId = state.detail?.id ?: return
        val item = state.suggestions.getOrNull(index) ?: return

        viewModelScope.launch {
            updateSuggestion(index) { it.copy(status = SuggestionStatus.Adding) }
            val baseUrl = prefs.baseUrl.first()
            val wordResult = if (item.existingWordId != null) {
                Result.success(item.existingWordId)
            } else {
                wordRepository.createWord(
                    baseUrl = baseUrl,
                    draft = WordDraft(
                        arabicText = item.suggestion.arabicText,
                        translation = item.suggestion.translation.orEmpty(),
                        transliteration = item.suggestion.transliteration,
                        partOfSpeech = item.suggestion.partOfSpeech ?: "UNKNOWN",
                        dialect = "MSA",
                    ),
                ).map { it.id }
            }

            wordResult.fold(
                onSuccess = { wordId ->
                    wordListRepository.addWord(baseUrl, listId, wordId).fold(
                        onSuccess = {
                            updateSuggestion(index) {
                                it.copy(status = SuggestionStatus.Added, existingWordId = wordId)
                            }
                            load(listId)
                        },
                        onFailure = {
                            updateSuggestion(index) { it.copy(status = SuggestionStatus.Error) }
                        },
                    )
                },
                onFailure = {
                    updateSuggestion(index) { it.copy(status = SuggestionStatus.Error) }
                },
            )
        }
    }

    private suspend fun checkSuggestion(baseUrl: String, index: Int, suggestion: WordListSuggestion) {
        wordRepository.getWordByArabic(baseUrl, suggestion.arabicText).fold(
            onSuccess = { word ->
                updateSuggestion(index) {
                    it.copy(
                        status = if (word == null) SuggestionStatus.Missing else SuggestionStatus.Exists,
                        existingWordId = word?.id,
                    )
                }
            },
            onFailure = {
                updateSuggestion(index) { it.copy(status = SuggestionStatus.Missing) }
            },
        )
    }

    private fun updateSuggestion(index: Int, transform: (SuggestionUiItem) -> SuggestionUiItem) {
        _uiState.update { state ->
            state.copy(
                suggestions = state.suggestions.mapIndexed { itemIndex, item ->
                    if (itemIndex == index) transform(item) else item
                },
            )
        }
    }

    private companion object {
        const val MIN_SEARCH_LENGTH = 2
    }
}
