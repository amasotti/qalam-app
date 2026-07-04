package com.tonihacks.qalam.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.repository.TextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TextListUiState(
    val items: List<TextPassage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
)

@HiltViewModel
class TextListViewModel @Inject constructor(
    private val textRepository: TextRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextListUiState())
    val uiState: StateFlow<TextListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun loadMore() {
        val s = _uiState.value
        if (s.isLoading || !s.hasMore) return
        load()
    }

    private fun load() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val baseUrl = prefs.baseUrl.first()
            textRepository.getTexts(baseUrl, page = s.currentPage).fold(
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
