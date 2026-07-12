package com.tonihacks.qalam.ui.words

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.AiRelationSuggestion
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.DictionaryLinkDraft
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.model.WordEnrichmentSuggestion
import com.tonihacks.qalam.domain.model.WordMorphology
import com.tonihacks.qalam.domain.model.WordPluralDraft
import com.tonihacks.qalam.domain.model.WordRelation
import com.tonihacks.qalam.domain.model.WordRelationDraft
import com.tonihacks.qalam.domain.model.WordUpdate
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

enum class InsightPhase { IDLE, LOADING, RESULT }

sealed interface WordDetailUiState {
    data object Loading : WordDetailUiState
    data class Success(
        val word: Word,
        val examples: List<Example>,
        val dictionaries: List<DictionaryLink>,
        val morphology: WordMorphology? = null,
        val relations: List<WordRelation> = emptyList(),
        val isSavingNotes: Boolean = false,
        val notesError: String? = null,
        val isSavingManualExample: Boolean = false,
        val isDeletingExample: Boolean = false,
        val isMutatingDictionary: Boolean = false,
        val dictionaryError: String? = null,
        val isSearchingRelations: Boolean = false,
        val relationSearchResults: List<WordAutocomplete> = emptyList(),
        val isMutatingRelation: Boolean = false,
        val relationError: String? = null,
        val isEnriching: Boolean = false,
        val enrichment: WordEnrichmentSuggestion? = null,
        val enrichmentError: String? = null,
        val enrichmentUnavailable: Boolean = false,
        val isSavingEnrichment: Boolean = false,
        val linkingSuggestedRelationIndexes: Set<Int> = emptySet(),
        val linkedSuggestedRelationIndexes: Set<Int> = emptySet(),
        // --- AI examples (ephemeral) ---
        val aiExamples: List<AiExample> = emptyList(),
        val isGeneratingExamples: Boolean = false,
        val savingExample: Boolean = false,
        val examplesError: String? = null,
        val aiExamplesUnavailable: Boolean = false,
        // --- AI insight (ephemeral) ---
        val insightPhase: InsightPhase = InsightPhase.IDLE,
        val insightText: String? = null,
        val insightError: String? = null,
        val insightUnavailable: Boolean = false,
    ) : WordDetailUiState
    data class Error(val message: String) : WordDetailUiState
}


@HiltViewModel
class WordDetailViewModel @Inject constructor(
    private val wordRepository: WordRepository,
    private val prefs: PreferencesRepository,
): ViewModel() {

    private var currentWordId : String? = null

    private val _uiState = MutableStateFlow<WordDetailUiState>(WordDetailUiState.Loading)
    val uiState : StateFlow<WordDetailUiState> = _uiState.asStateFlow()

    fun load(wordId: String) {
        if (wordId == currentWordId) return
        currentWordId = wordId

        viewModelScope.launch {
            _uiState.value = WordDetailUiState.Loading
            val baseUrl = prefs.baseUrl.first()

            val wordDeferred = async { wordRepository.getWord(baseUrl, wordId) }
            val examplesDeferred = async { wordRepository.getExamples(baseUrl, wordId) }
            val dictionariesDeferred = async { wordRepository.getDictionaryLinks(baseUrl, wordId) }
            val morphologyDeferred = async { wordRepository.getMorphology(baseUrl, wordId) }
            val relationsDeferred = async { wordRepository.getRelations(baseUrl, wordId) }

            val wordResult = wordDeferred.await()
            val examplesResult = examplesDeferred.await()
            val dictionariesResult = dictionariesDeferred.await()
            val morphologyResult = morphologyDeferred.await()
            val relationsResult = relationsDeferred.await()

            _uiState.value = when {
                wordResult.isSuccess -> WordDetailUiState.Success(
                    word = wordResult.getOrThrow(),
                    examples = examplesResult.getOrDefault(emptyList()),
                    dictionaries = dictionariesResult.getOrDefault(emptyList()),
                    morphology = morphologyResult.getOrNull(),
                    relations = relationsResult.getOrDefault(emptyList()),
                )
                else -> WordDetailUiState.Error(wordResult.exceptionOrNull()?.message ?: "Failed to load word detail")
            }
        }
    }

    // ---------------- core word mutations ----------------

    fun saveNotes(notes: String) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isSavingNotes = true, notesError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.updateWord(baseUrl, wordId, WordUpdate(notes = notes.trim().ifBlank { null })).fold(
                onSuccess = { updated ->
                    updateSuccess { it.copy(word = updated, isSavingNotes = false) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(isSavingNotes = false, notesError = err.message ?: "Failed to save notes")
                    }
                },
            )
        }
    }

    fun addManualExample(arabic: String, transliteration: String?, translation: String?) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isSavingManualExample = true, examplesError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.saveExample(
                baseUrl,
                wordId,
                AiExample(
                    arabic = arabic.trim(),
                    transliteration = transliteration?.trim()?.ifBlank { null },
                    translation = translation?.trim()?.ifBlank { null },
                ),
            ).fold(
                onSuccess = { saved ->
                    updateSuccess { it.copy(examples = it.examples + saved, isSavingManualExample = false) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(isSavingManualExample = false, examplesError = err.message ?: "Failed to save example")
                    }
                },
            )
        }
    }

    fun deleteExample(exampleId: String) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isDeletingExample = true, examplesError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.deleteExample(baseUrl, wordId, exampleId).fold(
                onSuccess = {
                    updateSuccess { s ->
                        s.copy(
                            examples = s.examples.filterNot { it.id == exampleId },
                            isDeletingExample = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(isDeletingExample = false, examplesError = err.message ?: "Failed to delete example")
                    }
                },
            )
        }
    }

    fun addDictionaryLink(source: String, url: String) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isMutatingDictionary = true, dictionaryError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.addDictionaryLink(baseUrl, wordId, DictionaryLinkDraft(source, url)).fold(
                onSuccess = { link ->
                    updateSuccess { it.copy(dictionaries = it.dictionaries + link, isMutatingDictionary = false) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            isMutatingDictionary = false,
                            dictionaryError = err.message ?: "Failed to add dictionary link",
                        )
                    }
                },
            )
        }
    }

    fun addDictionaryLinks(links: List<DictionaryLinkDraft>) {
        val wordId = currentWordId ?: return
        if (links.isEmpty()) return
        viewModelScope.launch {
            updateSuccess { it.copy(isMutatingDictionary = true, dictionaryError = null) }
            val baseUrl = prefs.baseUrl.first()
            val added = mutableListOf<DictionaryLink>()
            var failure: Throwable? = null
            for (link in links) {
                wordRepository.addDictionaryLink(baseUrl, wordId, link).fold(
                    onSuccess = { added += it },
                    onFailure = {
                        failure = it
                        return@fold
                    },
                )
                if (failure != null) break
            }
            updateSuccess {
                it.copy(
                    dictionaries = it.dictionaries + added,
                    isMutatingDictionary = false,
                    dictionaryError = failure?.message,
                )
            }
        }
    }

    fun deleteDictionaryLink(linkId: String) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isMutatingDictionary = true, dictionaryError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.deleteDictionaryLink(baseUrl, wordId, linkId).fold(
                onSuccess = {
                    updateSuccess { s ->
                        s.copy(
                            dictionaries = s.dictionaries.filterNot { it.id == linkId },
                            isMutatingDictionary = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            isMutatingDictionary = false,
                            dictionaryError = err.message ?: "Failed to remove dictionary link",
                        )
                    }
                },
            )
        }
    }

    fun searchRelations(query: String) {
        if (query.trim().length < MIN_RELATION_QUERY_LENGTH) {
            updateSuccess { it.copy(relationSearchResults = emptyList(), relationError = null) }
            return
        }
        viewModelScope.launch {
            updateSuccess { it.copy(isSearchingRelations = true, relationError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.autocompleteWords(baseUrl, query.trim()).fold(
                onSuccess = { words ->
                    val currentId = currentWordId
                    updateSuccess {
                        it.copy(
                            relationSearchResults = words.filterNot { word -> word.id == currentId },
                            isSearchingRelations = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(isSearchingRelations = false, relationError = err.message ?: "Failed to search words")
                    }
                },
            )
        }
    }

    fun addRelation(relatedWordId: String, relationType: String) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isMutatingRelation = true, relationError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.addRelation(baseUrl, wordId, WordRelationDraft(relatedWordId, relationType)).fold(
                onSuccess = { relation ->
                    updateSuccess {
                        it.copy(
                            relations = it.relations + relation,
                            relationSearchResults = emptyList(),
                            isMutatingRelation = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(isMutatingRelation = false, relationError = err.message ?: "Failed to add relation")
                    }
                },
            )
        }
    }

    fun deleteRelation(relatedWordId: String, relationType: String) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isMutatingRelation = true, relationError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.deleteRelation(baseUrl, wordId, relatedWordId, relationType).fold(
                onSuccess = {
                    updateSuccess { s ->
                        s.copy(
                            relations = s.relations.filterNot {
                                it.relatedWordId == relatedWordId && it.relationType == relationType
                            },
                            isMutatingRelation = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(isMutatingRelation = false, relationError = err.message ?: "Failed to remove relation")
                    }
                },
            )
        }
    }

    // ---------------- AI enrichment ----------------

    fun enrichWord() {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess {
                it.copy(
                    isEnriching = true,
                    enrichment = null,
                    enrichmentError = null,
                    enrichmentUnavailable = false,
                )
            }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.enrichWord(baseUrl, wordId).fold(
                onSuccess = { suggestion ->
                    updateSuccess { it.copy(enrichment = suggestion, isEnriching = false) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            isEnriching = false,
                            enrichmentUnavailable = err is AiUnavailableException,
                            enrichmentError = if (err is AiUnavailableException) null else err.message
                                ?: "Failed to enrich word",
                        )
                    }
                },
            )
        }
    }

    fun dismissEnrichment() {
        updateSuccess {
            it.copy(
                enrichment = null,
                enrichmentError = null,
                enrichmentUnavailable = false,
                isEnriching = false,
                isSavingEnrichment = false,
                linkingSuggestedRelationIndexes = emptySet(),
                linkedSuggestedRelationIndexes = emptySet(),
            )
        }
    }

    fun saveEnrichment(
        acceptNotes: Boolean,
        notes: String,
        acceptGender: Boolean,
        acceptVerbPattern: Boolean,
        acceptedPluralIndexes: Set<Int>,
    ) {
        val wordId = currentWordId ?: return
        val suggestion = (_uiState.value as? WordDetailUiState.Success)?.enrichment ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(isSavingEnrichment = true, enrichmentError = null) }
            val baseUrl = prefs.baseUrl.first()

            var nextWord = (_uiState.value as? WordDetailUiState.Success)?.word
            var nextMorphology = (_uiState.value as? WordDetailUiState.Success)?.morphology
            val addedPlurals = mutableListOf<com.tonihacks.qalam.domain.model.WordPlural>()

            if (acceptNotes && notes.trim().isNotEmpty()) {
                wordRepository.updateWord(baseUrl, wordId, WordUpdate(notes = notes.trim())).fold(
                    onSuccess = { nextWord = it },
                    onFailure = { return@launch failEnrichmentSave(it) },
                )
            }

            val gender = suggestion.gender.takeIf { acceptGender }
            val pattern = suggestion.verbPattern.takeIf { acceptVerbPattern }
            if (gender != null || pattern != null) {
                wordRepository.upsertMorphology(baseUrl, wordId, gender, pattern).fold(
                    onSuccess = { nextMorphology = it },
                    onFailure = { return@launch failEnrichmentSave(it) },
                )
            }

            suggestion.plurals.forEachIndexed { index, plural ->
                if (index in acceptedPluralIndexes) {
                    wordRepository.addPlural(
                        baseUrl,
                        wordId,
                        WordPluralDraft(plural.pluralForm, plural.pluralType),
                    ).fold(
                        onSuccess = { addedPlurals += it },
                        onFailure = { return@launch failEnrichmentSave(it) },
                    )
                }
            }

            updateSuccess { s ->
                s.copy(
                    word = nextWord ?: s.word,
                    morphology = (nextMorphology ?: s.morphology)?.let {
                        it.copy(plurals = (it.plurals + addedPlurals).distinctBy { plural -> plural.id })
                    },
                    isSavingEnrichment = false,
                )
            }
        }
    }

    fun linkSuggestedRelation(index: Int, relation: AiRelationSuggestion) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess {
                it.copy(
                    linkingSuggestedRelationIndexes = it.linkingSuggestedRelationIndexes + index,
                    enrichmentError = null,
                )
            }
            val baseUrl = prefs.baseUrl.first()
            val targetResult = wordRepository.getWordByArabic(baseUrl, relation.arabicText).fold(
                onSuccess = { existing ->
                    if (existing != null) {
                        Result.success(existing)
                    } else {
                        wordRepository.createWord(
                            baseUrl,
                            WordDraft(
                                arabicText = relation.arabicText,
                                translation = relation.translation.orEmpty(),
                                transliteration = relation.transliteration,
                                partOfSpeech = "UNKNOWN",
                                dialect = "MSA",
                            ),
                        )
                    }
                },
                onFailure = { Result.failure(it) },
            )
            val target = targetResult.getOrElse {
                failSuggestedRelation(index, it)
                return@launch
            }
            wordRepository.addRelation(
                baseUrl,
                wordId,
                WordRelationDraft(target.id, relation.relationType),
            ).fold(
                onSuccess = { newRelation ->
                    updateSuccess {
                        it.copy(
                            relations = (it.relations + newRelation).distinctBy { rel ->
                                "${rel.relatedWordId}:${rel.relationType}"
                            },
                            linkingSuggestedRelationIndexes = it.linkingSuggestedRelationIndexes - index,
                            linkedSuggestedRelationIndexes = it.linkedSuggestedRelationIndexes + index,
                        )
                    }
                },
                onFailure = { failSuggestedRelation(index, it) },
            )
        }
    }

    // ---------------- AI examples ----------------

    fun generateExamples() {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess {
                it.copy(
                    isGeneratingExamples = true,
                    aiExamples = emptyList(),
                    examplesError = null,
                    aiExamplesUnavailable = false,
                )
            }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.generateExamples(baseUrl, wordId).fold(
                onSuccess = { list ->
                    updateSuccess { it.copy(aiExamples = list, isGeneratingExamples = false) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            isGeneratingExamples = false,
                            aiExamplesUnavailable = err is AiUnavailableException,
                            examplesError = if (err is AiUnavailableException) null else err.message
                                ?: "Failed to generate examples",
                        )
                    }
                },
            )
        }
    }

    fun useExample(example: AiExample) {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(savingExample = true, examplesError = null) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.saveExample(baseUrl, wordId, example).fold(
                onSuccess = {
                    val refreshed = wordRepository.getExamples(baseUrl, wordId).getOrNull()
                    updateSuccess { s ->
                        s.copy(
                            examples = refreshed ?: s.examples,
                            aiExamples = s.aiExamples - example,
                            savingExample = false,
                        )
                    }
                },
                onFailure = { err ->
                    updateSuccess { it.copy(savingExample = false, examplesError = err.message ?: "Failed to save example") }
                },
            )
        }
    }

    fun discardExample(example: AiExample) {
        updateSuccess { it.copy(aiExamples = it.aiExamples - example) }
    }

    fun dismissExamples() {
        updateSuccess {
            it.copy(aiExamples = emptyList(), examplesError = null, aiExamplesUnavailable = false)
        }
    }

    // ---------------- AI insight ----------------

    fun getInsight() {
        val wordId = currentWordId ?: return
        viewModelScope.launch {
            updateSuccess { it.copy(insightPhase = InsightPhase.LOADING, insightError = null, insightUnavailable = false) }
            val baseUrl = prefs.baseUrl.first()
            wordRepository.generateInsight(baseUrl, entityType = "WORD", entityId = wordId).fold(
                onSuccess = { text ->
                    updateSuccess { it.copy(insightPhase = InsightPhase.RESULT, insightText = text) }
                },
                onFailure = { err ->
                    updateSuccess {
                        it.copy(
                            insightPhase = InsightPhase.IDLE,
                            insightUnavailable = err is AiUnavailableException,
                            insightError = if (err is AiUnavailableException) null else err.message
                                ?: "Failed to load insight",
                        )
                    }
                },
            )
        }
    }

    fun dismissInsight() {
        updateSuccess { it.copy(insightPhase = InsightPhase.IDLE, insightText = null, insightError = null) }
    }

    private fun failEnrichmentSave(err: Throwable) {
        updateSuccess {
            it.copy(
                isSavingEnrichment = false,
                enrichmentError = err.message ?: "Failed to save enrichment",
            )
        }
    }

    private fun failSuggestedRelation(index: Int, err: Throwable) {
        updateSuccess {
            it.copy(
                linkingSuggestedRelationIndexes = it.linkingSuggestedRelationIndexes - index,
                enrichmentError = err.message ?: "Failed to link relation",
            )
        }
    }

    private inline fun updateSuccess(transform: (WordDetailUiState.Success) -> WordDetailUiState.Success) {
        _uiState.update { (it as? WordDetailUiState.Success)?.let(transform) ?: it }
    }

    private companion object {
        const val MIN_RELATION_QUERY_LENGTH = 2
    }
}
