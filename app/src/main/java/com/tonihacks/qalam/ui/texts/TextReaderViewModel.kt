package com.tonihacks.qalam.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.repository.TextRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface TextReaderUiState {
    data object Loading : TextReaderUiState
    data class Success(
        val text: TextPassage,
        val sentences: List<TextSentence>,
    ) : TextReaderUiState
    data class Error(val message: String) : TextReaderUiState
}

@HiltViewModel
class TextReaderViewModel @Inject constructor(
    private val textRepository: TextRepository,
    private val prefs: PreferencesRepository,
) : ViewModel() {

    private var currentTextId: String? = null

    private val _uiState = MutableStateFlow<TextReaderUiState>(TextReaderUiState.Loading)
    val uiState: StateFlow<TextReaderUiState> = _uiState.asStateFlow()

    fun load(textId: String) {
        if (textId == currentTextId) return
        currentTextId = textId

        viewModelScope.launch {
            _uiState.value = TextReaderUiState.Loading
            val baseUrl = prefs.baseUrl.first()

            val textDeferred = async { textRepository.getText(baseUrl, textId) }
            val sentencesDeferred = async { textRepository.getSentences(baseUrl, textId) }

            val textResult = textDeferred.await()
            val sentencesResult = sentencesDeferred.await()

            _uiState.value = when {
                textResult.isSuccess -> TextReaderUiState.Success(
                    text = textResult.getOrThrow(),
                    sentences = sentencesResult.getOrDefault(emptyList()),
                )
                else -> TextReaderUiState.Error(
                    textResult.exceptionOrNull()?.message ?: "Failed to load text"
                )
            }
        }
    }
}
