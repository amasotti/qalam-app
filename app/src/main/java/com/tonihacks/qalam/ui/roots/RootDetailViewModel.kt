package com.tonihacks.qalam.ui.roots

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.Root
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.repository.RootRepository
import com.tonihacks.qalam.domain.repository.WordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RootDetailUiState {
    data object Loading : RootDetailUiState
    data class Success(
        val root: Root,
        val forms: List<Word>,
    ) : RootDetailUiState
    data class Error(val message: String) : RootDetailUiState
}

@HiltViewModel
class RootDetailViewModel @Inject constructor(
    private val rootRepository: RootRepository,
    private val wordRepository: WordRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {

    private var currentRootId: String? = null

    private val _uiState = MutableStateFlow<RootDetailUiState>(RootDetailUiState.Loading)
    val uiState: StateFlow<RootDetailUiState> = _uiState.asStateFlow()

    fun load(rootId: String) {
        if (rootId == currentRootId) return
        currentRootId = rootId

        viewModelScope.launch {
            _uiState.value = RootDetailUiState.Loading
            val baseUrl = prefs.baseUrl.first()

            val rootDeferred = async { rootRepository.getRoot(baseUrl, rootId) }
            val formsDeferred = async {
                wordRepository.getWords(
                    baseUrl = baseUrl,
                    rootId = rootId,
                    page = 1,
                    size = 500,
                )
            }

            val rootResult = rootDeferred.await()
            val formsResult = formsDeferred.await()

            _uiState.value = when {
                rootResult.isSuccess && formsResult.isSuccess -> RootDetailUiState.Success(
                    root = rootResult.getOrThrow(),
                    forms = formsResult.getOrThrow().items,
                )
                rootResult.isFailure -> RootDetailUiState.Error(
                    rootResult.exceptionOrNull()?.message ?: "Failed to load root",
                )
                else -> RootDetailUiState.Error(
                    formsResult.exceptionOrNull()?.message ?: "Failed to load root forms",
                )
            }
        }
    }
}
