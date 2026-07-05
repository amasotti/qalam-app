package com.tonihacks.qalam.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.repository.AnalyticsRepository
import com.tonihacks.qalam.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val totalWords: Int = 0,
    val totalRoots: Int = 0,
    val totalTexts: Int = 0,
    val dueCount: Int = 0,
    val masteryCounts: Map<MasteryLevel, Int> = emptyMap(),
    val recentWords: List<Word> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val apiClient: ApiClient,
    private val analyticsRepository: AnalyticsRepository,
    private val wordRepository: WordRepository,
) : ViewModel() {

    sealed interface ConnectionState {
        data object Unknown : ConnectionState
        data object Online : ConnectionState
        data object Offline : ConnectionState
    }

    val baseUrl: StateFlow<String> = preferencesRepository.baseUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3_000),
        initialValue = PreferencesRepository.DEFAULT_URL,
    )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun ping() {
        viewModelScope.launch {
            val result = apiClient.testConnection(baseUrl.value)
            _connectionState.value = if (result.isSuccess) ConnectionState.Online else ConnectionState.Offline
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val url = preferencesRepository.baseUrl.first()

            val overviewDeferred = async { analyticsRepository.getOverview(url) }
            val recentDeferred = async {
                wordRepository.getWords(baseUrl = url, page = 1, size = 6, sortBy = "UPDATED_AT", sortDesc = true)
            }
            val overview = overviewDeferred.await()
            val recent = recentDeferred.await()

            val data = overview.getOrNull()
            if (data == null) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false, error = overview.exceptionOrNull()?.message ?: "Failed to load overview")
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = null,
                    totalWords = data.totalWords,
                    totalRoots = data.totalRoots,
                    totalTexts = data.totalTexts,
                    dueCount = data.dueCount,
                    masteryCounts = data.masteryCounts,
                    recentWords = recent.getOrNull()?.items ?: emptyList(),
                )
            }
        }
    }
}
