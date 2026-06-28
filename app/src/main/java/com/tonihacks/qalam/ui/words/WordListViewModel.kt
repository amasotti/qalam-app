package com.tonihacks.qalam.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.*
import com.tonihacks.qalam.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WordListUiState(
    val items: List<Word> = emptyList(),
    val query: String = "",
    val activeFilter: MasteryLevel? = null,   // null = "All"
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,   // API is 1-based
)

@HiltViewModel
class WordListViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WordListUiState())
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    init { load() }

    fun onQueryChange(q: String) {
        _uiState.update { it.copy(query = q, items = emptyList(), currentPage = 1, hasMore = true) }
        load()
        // No debounce here for simplicity. If the backend gets hammered on fast typing,
        // wrap load() in a Job + cancel pattern or use Flow.debounce() from the query StateFlow.
    }

    fun onFilterChange(filter: MasteryLevel?) {
        _uiState.update { it.copy(activeFilter = filter, items = emptyList(), currentPage = 1, hasMore = true) }
        load()
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || !s.hasMore) return
        load()
    }

    fun createWord(draft: WordDraft) {
        viewModelScope.launch {
            val baseUrl = prefs.baseUrl.first()
            wordRepository.createWord(baseUrl, draft).onSuccess {
                _uiState.update { it.copy(items = emptyList(), currentPage = 1, hasMore = true) }
                load()
            }
        }
    }

    private fun load() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.getWords(
                baseUrl = baseUrl,
                query = s.query.ifBlank { null },
                masteryLevel = s.activeFilter?.name,
                page = s.currentPage,
            ).fold(
                onSuccess = { paged ->
                    _uiState.update { cur ->
                        cur.copy(
                            items = cur.items + paged.items,
                            isLoading = false,
                            hasMore = paged.hasMore,
                            currentPage = cur.currentPage + 1,
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message) }
                },
            )
        }
    }
}