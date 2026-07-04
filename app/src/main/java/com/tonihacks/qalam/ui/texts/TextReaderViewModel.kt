package com.tonihacks.qalam.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.model.TextToken
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.repository.TextRepository
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

sealed interface TextReaderUiState {
    data object Loading : TextReaderUiState
    data class Success(
        val text: TextPassage,
        val sentences: List<TextSentence>,
        val isLinkingWord: Boolean = false,
        val linkWordError: String? = null,
    ) : TextReaderUiState
    data class Error(val message: String) : TextReaderUiState
}

@HiltViewModel
class TextReaderViewModel @Inject constructor(
    private val textRepository: TextRepository,
    private val wordRepository: WordRepository,
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

    /**
     * Turn an interlinear token into a vocabulary entry, then link the token to it.
     *
     * The Arabic in [draft] may differ from [token].arabic — the user can edit the surface form to a
     * lemma inside the sheet. So we look up by the drafted Arabic: reuse an existing word if one matches
     * exactly, otherwise create a new one. Then we PUT the sentence's full token array with the target
     * token's wordId set (the backend has no single-token patch).
     */
    fun addTokenToVocabulary(token: TextToken, draft: WordDraft, onDone: () -> Unit) {
        val current = _uiState.value
        if (current !is TextReaderUiState.Success) return

        viewModelScope.launch {
            _uiState.update {
                (it as? TextReaderUiState.Success)?.copy(isLinkingWord = true, linkWordError = null) ?: it
            }
            val baseUrl = prefs.baseUrl.first()

            val wordResult = wordRepository.getWordByArabic(baseUrl, draft.arabicText).mapCatching { existing ->
                existing ?: wordRepository.createWord(baseUrl, draft).getOrThrow()
            }

            wordResult.fold(
                onSuccess = { word ->
                    val sentence = current.sentences.firstOrNull { it.id == token.sentenceId }
                    if (sentence == null) {
                        setLinkError("Sentence for this word could not be found")
                        return@fold
                    }
                    val updatedTokens = sentence.tokens.map {
                        if (it.id == token.id) it.copy(wordId = word.id) else it
                    }
                    textRepository.replaceTokens(baseUrl, current.text.id, sentence.id, updatedTokens).fold(
                        onSuccess = { updatedSentence ->
                            _uiState.update { state ->
                                (state as? TextReaderUiState.Success)?.copy(
                                    sentences = state.sentences.map { s ->
                                        if (s.id == updatedSentence.id) updatedSentence else s
                                    },
                                    isLinkingWord = false,
                                    linkWordError = null,
                                ) ?: state
                            }
                            onDone()
                        },
                        onFailure = { err -> setLinkError(err.message ?: "Failed to link word") },
                    )
                },
                onFailure = { err -> setLinkError(err.message ?: "Failed to add word") },
            )
        }
    }

    private fun setLinkError(message: String) {
        _uiState.update {
            (it as? TextReaderUiState.Success)?.copy(isLinkingWord = false, linkWordError = message) ?: it
        }
    }

    fun clearLinkError() {
        _uiState.update {
            (it as? TextReaderUiState.Success)?.copy(linkWordError = null) ?: it
        }
    }
}
