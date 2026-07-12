package com.tonihacks.qalam.ui.texts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.DictionaryLookupItem
import com.tonihacks.qalam.domain.model.TextAnnotation
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.model.TextToken
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.repository.TextRepository
import com.tonihacks.qalam.domain.repository.WordRepository
import com.tonihacks.qalam.util.stripDiacritics
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
        val annotations: List<TextAnnotation> = emptyList(),
        val isLinkingWord: Boolean = false,
        val linkWordError: String? = null,
        val lookupItems: List<DictionaryLookupItem> = emptyList(),
        val isLookingUp: Boolean = false,
        val lookupError: String? = null,
        val duplicateCandidates: List<WordAutocomplete> = emptyList(),
        val isCheckingDuplicates: Boolean = false,
    ) : TextReaderUiState
    data class Error(val message: String) : TextReaderUiState
}

private const val VOCABULARY_TYPE = "VOCABULARY"

/**
 * Alignment tokens don't carry a wordId once linking moves to annotations, so we derive the linked
 * state here: a token counts as linked when a VOCABULARY annotation's anchor matches the token's
 * surface form and carries at least one linked word. The first linked word id drives "View full entry".
 */
private fun enrichTokens(
    sentences: List<TextSentence>,
    annotations: List<TextAnnotation>,
): List<TextSentence> {
    val wordIdByAnchor = annotations
        .filter { it.type == VOCABULARY_TYPE && it.linkedWordIds.isNotEmpty() }
        .associate { it.anchor to it.linkedWordIds.first() }
    if (wordIdByAnchor.isEmpty()) return sentences
    return sentences.map { sentence ->
        sentence.copy(
            tokens = sentence.tokens.map { token ->
                if (token.wordId == null) {
                    wordIdByAnchor[token.arabic]?.let { token.copy(wordId = it) } ?: token
                } else {
                    token
                }
            },
        )
    }
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
            val annotationsDeferred = async { textRepository.getAnnotations(baseUrl, textId) }

            val textResult = textDeferred.await()
            val sentencesResult = sentencesDeferred.await()
            val annotations = annotationsDeferred.await().getOrDefault(emptyList())

            _uiState.value = when {
                textResult.isSuccess -> TextReaderUiState.Success(
                    text = textResult.getOrThrow(),
                    sentences = enrichTokens(sentencesResult.getOrDefault(emptyList()), annotations),
                    annotations = annotations,
                )
                else -> TextReaderUiState.Error(
                    textResult.exceptionOrNull()?.message ?: "Failed to load text"
                )
            }
        }
    }

    /**
     * Turn an interlinear token into a vocabulary entry, then link it to the text via an annotation.
     *
     * The Arabic in [draft] may differ from [token].arabic — the user can edit the surface form to a
     * lemma inside the sheet. So we look up the *word* by the drafted Arabic (reuse on exact match,
     * otherwise create). The *annotation* anchor keeps the original surface form ([token].arabic), which
     * is what appears in the text, and links to the word id. Backend has no per-token wordId patch;
     * linkage lives in annotations.
     */
    fun addTokenToVocabulary(token: TextToken, draft: WordDraft, onDone: () -> Unit) {
        val current = _uiState.value
        if (current !is TextReaderUiState.Success) return

        viewModelScope.launch {
            _uiState.update {
                (it as? TextReaderUiState.Success)?.copy(isLinkingWord = true, linkWordError = null) ?: it
            }
            val baseUrl = prefs.baseUrl.first()

            val stripped = stripDiacritics(draft.arabicText)
            val wordResult = wordRepository.getWordByArabic(baseUrl, draft.arabicText).mapCatching { existing ->
                if (existing != null) return@mapCatching existing
                val byStripped = if (stripped != draft.arabicText) {
                    wordRepository.getWordByArabic(baseUrl, stripped).getOrNull()
                } else null
                byStripped ?: wordRepository.createWord(baseUrl, draft).getOrThrow()
            }

            wordResult.fold(
                onSuccess = { word ->
                    val content = listOfNotNull(
                        draft.transliteration?.ifBlank { null },
                        draft.translation.ifBlank { null },
                    ).joinToString(" - ").ifBlank { null }

                    textRepository.createAnnotation(
                        baseUrl = baseUrl,
                        textId = current.text.id,
                        anchor = token.arabic,
                        type = VOCABULARY_TYPE,
                        content = content,
                        linkedWordIds = listOf(word.id),
                    ).fold(
                        onSuccess = { annotation ->
                            // Re-fetch annotations so the reader reflects the backend's real link state
                            // (already-linked words show "View full entry", not "Add to vocabulary").
                            // Fall back to the just-created annotation if the refetch fails.
                            val refreshed = textRepository.getAnnotations(baseUrl, current.text.id)
                                .getOrDefault(current.annotations + annotation)
                            _uiState.update { state ->
                                val s = state as? TextReaderUiState.Success ?: return@update state
                                s.copy(
                                    annotations = refreshed,
                                    sentences = enrichTokens(s.sentences, refreshed),
                                    isLinkingWord = false,
                                    linkWordError = null,
                                )
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

    fun lookupWord(query: String) {
        viewModelScope.launch {
            _uiState.update {
                (it as? TextReaderUiState.Success)?.copy(
                    isLookingUp = true, lookupError = null, lookupItems = emptyList(),
                ) ?: it
            }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.lookupInDictionary(baseUrl, query).fold(
                onSuccess = { items ->
                    _uiState.update {
                        (it as? TextReaderUiState.Success)?.copy(isLookingUp = false, lookupItems = items) ?: it
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        (it as? TextReaderUiState.Success)?.copy(
                            isLookingUp = false, lookupError = err.message ?: "Lookup failed",
                        ) ?: it
                    }
                },
            )
        }
    }

    fun clearLookup() {
        _uiState.update {
            (it as? TextReaderUiState.Success)?.copy(lookupItems = emptyList(), lookupError = null) ?: it
        }
    }

    fun checkDuplicates(arabic: String) {
        val stripped = stripDiacritics(arabic).ifBlank { arabic }
        viewModelScope.launch {
            _uiState.update {
                (it as? TextReaderUiState.Success)?.copy(
                    isCheckingDuplicates = true, duplicateCandidates = emptyList(),
                ) ?: it
            }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.autocompleteWords(baseUrl, stripped).fold(
                onSuccess = { candidates ->
                    _uiState.update {
                        (it as? TextReaderUiState.Success)?.copy(
                            duplicateCandidates = candidates, isCheckingDuplicates = false,
                        ) ?: it
                    }
                },
                onFailure = {
                    _uiState.update {
                        (it as? TextReaderUiState.Success)?.copy(isCheckingDuplicates = false) ?: it
                    }
                },
            )
        }
    }

    fun clearDuplicateCandidates() {
        _uiState.update {
            (it as? TextReaderUiState.Success)?.copy(
                duplicateCandidates = emptyList(), isCheckingDuplicates = false,
            ) ?: it
        }
    }

    /**
     * Link a token directly to an existing vocabulary word without creating a new entry.
     * Used when the user taps an existing word from the duplicate-candidates list.
     */
    fun linkTokenToExistingWord(token: TextToken, word: WordAutocomplete, onDone: () -> Unit) {
        val current = _uiState.value
        if (current !is TextReaderUiState.Success) return
        viewModelScope.launch {
            _uiState.update {
                (it as? TextReaderUiState.Success)?.copy(isLinkingWord = true, linkWordError = null) ?: it
            }
            val baseUrl = prefs.baseUrl.first()
            val content = word.translation?.ifBlank { null }
            textRepository.createAnnotation(
                baseUrl = baseUrl,
                textId = current.text.id,
                anchor = token.arabic,
                type = VOCABULARY_TYPE,
                content = content,
                linkedWordIds = listOf(word.id),
            ).fold(
                onSuccess = { annotation ->
                    val refreshed = textRepository.getAnnotations(baseUrl, current.text.id)
                        .getOrDefault(current.annotations + annotation)
                    _uiState.update { state ->
                        val s = state as? TextReaderUiState.Success ?: return@update state
                        s.copy(
                            annotations = refreshed,
                            sentences = enrichTokens(s.sentences, refreshed),
                            isLinkingWord = false,
                            linkWordError = null,
                            duplicateCandidates = emptyList(),
                        )
                    }
                    onDone()
                },
                onFailure = { err -> setLinkError(err.message ?: "Failed to link word") },
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
