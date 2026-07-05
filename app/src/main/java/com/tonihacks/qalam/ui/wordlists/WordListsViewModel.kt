package com.tonihacks.qalam.ui.wordlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.WordListDraft
import com.tonihacks.qalam.domain.model.WordListSummary
import com.tonihacks.qalam.domain.repository.WordListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListsUiState(
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val lists: List<WordListSummary> = emptyList(),
    val error: String? = null,
    val createError: String? = null,
)

@HiltViewModel
class WordListsViewModel @Inject constructor(
    private val repository: WordListRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(WordListsUiState(isLoading = true))
    val uiState: StateFlow<WordListsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val baseUrl = prefs.baseUrl.first()
            repository.getWordLists(baseUrl, size = WORD_LIST_PAGE_SIZE).fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(isLoading = false, lists = result.items, error = null)
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isLoading = false, error = err.message ?: "Could not load word lists")
                    }
                },
            )
        }
    }

    fun createList(title: String, description: String?, onCreated: (String) -> Unit) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) {
            _uiState.update { it.copy(createError = "Give the list a title") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, createError = null) }
            val baseUrl = prefs.baseUrl.first()
            repository.createWordList(
                baseUrl = baseUrl,
                draft = WordListDraft(cleanTitle, description?.trim()?.takeIf { it.isNotBlank() }),
            ).fold(
                onSuccess = { created ->
                    _uiState.update {
                        it.copy(
                            isCreating = false,
                            lists = listOf(created) + it.lists,
                            createError = null,
                        )
                    }
                    onCreated(created.id)
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(isCreating = false, createError = err.message ?: "Could not create list")
                    }
                },
            )
        }
    }

    private companion object {
        const val WORD_LIST_PAGE_SIZE = 500
    }
}
