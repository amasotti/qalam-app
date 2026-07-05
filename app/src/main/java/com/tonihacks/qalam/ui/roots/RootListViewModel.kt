package com.tonihacks.qalam.ui.roots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.RootListItem
import com.tonihacks.qalam.domain.repository.RootRepository
import com.tonihacks.qalam.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RootListUiState(
    val items: List<RootListItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
)

@HiltViewModel
class RootListViewModel @Inject constructor(
    private val rootRepository: RootRepository,
    private val wordRepository: WordRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RootListUiState())
    val uiState: StateFlow<RootListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoading || !state.hasMore) return
        load()
    }

    fun refresh() {
        _uiState.update { it.copy(items = emptyList(), currentPage = 1, hasMore = true, isRefreshing = true) }
        load()
    }

    private fun load() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val baseUrl = prefs.baseUrl.first()
            val rootsResult = rootRepository.getRoots(baseUrl, page = state.currentPage)

            if (rootsResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = rootsResult.exceptionOrNull()?.message ?: "Failed to load roots",
                    )
                }
                return@launch
            }

            val rootsPage = rootsResult.getOrThrow()
            val rootItems = rootsPage.items.map { root ->
                async {
                    val formCount = wordRepository.getWords(
                        baseUrl = baseUrl,
                        rootId = root.id,
                        page = 1,
                        size = 1,
                    ).getOrNull()?.total ?: 0
                    RootListItem(root = root, formCount = formCount)
                }
            }.awaitAll()

            _uiState.update { current ->
                current.copy(
                    items = current.items + rootItems,
                    isLoading = false,
                    isRefreshing = false,
                    hasMore = rootsPage.hasMore,
                    currentPage = current.currentPage + 1,
                )
            }
        }
    }
}
