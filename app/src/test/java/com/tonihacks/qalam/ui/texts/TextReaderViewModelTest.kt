package com.tonihacks.qalam.ui.texts

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.DictionaryLinkDraft
import com.tonihacks.qalam.domain.model.DictionaryLookupItem
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.PagedResult
import com.tonihacks.qalam.domain.model.TextAnnotation
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.model.TextToken
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.domain.model.WordEnrichmentSuggestion
import com.tonihacks.qalam.domain.model.WordMorphology
import com.tonihacks.qalam.domain.model.WordPlural
import com.tonihacks.qalam.domain.model.WordPluralDraft
import com.tonihacks.qalam.domain.model.WordRelation
import com.tonihacks.qalam.domain.model.WordRelationDraft
import com.tonihacks.qalam.domain.model.WordUpdate
import com.tonihacks.qalam.domain.repository.TextRepository
import com.tonihacks.qalam.domain.repository.WordRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TextReaderViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var textRepository: FakeTextRepository
    private lateinit var wordRepository: FakeWordRepository
    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        textRepository = FakeTextRepository()
        wordRepository = FakeWordRepository()
        prefs = mockk()
        every { prefs.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load enriches tokens from vocabulary annotations`() = runTest {
        textRepository.sentences = listOf(aSentence(tokenWordId = null))
        textRepository.annotations = listOf(aVocabularyAnnotation(anchor = "كتب", wordId = "word-1"))
        val vm = TextReaderViewModel(textRepository, wordRepository, prefs)

        vm.load(TEXT_ID)

        val state = vm.successState()
        assertEquals("word-1", state.sentences.single().tokens.single().wordId)
        assertEquals("text-1", state.text.id)
        assertEquals(listOf("ann-1"), state.annotations.map { it.id })
    }

    @Test
    fun `addTokenToVocabulary reuses exact existing word and creates annotation`() = runTest {
        wordRepository.wordByArabic = aWord("existing-word")
        val vm = loadedViewModel()
        var done = false

        vm.addTokenToVocabulary(
            token = aToken(wordId = null),
            draft = WordDraft(arabicText = "كتب", translation = "to write", transliteration = "kataba"),
            onDone = { done = true },
        )

        val state = vm.successState()
        assertTrue(done)
        assertTrue(wordRepository.createdWords.isEmpty())
        assertEquals(listOf("existing-word"), textRepository.createdAnnotations.single().linkedWordIds)
        assertEquals("existing-word", state.sentences.single().tokens.single().wordId)
        assertFalse(state.isLinkingWord)
    }

    @Test
    fun `addTokenToVocabulary creates missing word before annotation`() = runTest {
        wordRepository.wordByArabic = null
        val vm = loadedViewModel()

        vm.addTokenToVocabulary(
            token = aToken(wordId = null),
            draft = WordDraft(arabicText = "دفتر", translation = "notebook", transliteration = "daftar"),
            onDone = {},
        )

        assertEquals("دفتر", wordRepository.createdWords.single().arabicText)
        assertEquals(listOf("created-1"), textRepository.createdAnnotations.single().linkedWordIds)
        assertEquals("created-1", vm.successState().sentences.single().tokens.single().wordId)
    }

    @Test
    fun `lookupWord stores dictionary results and clearLookup removes them`() = runTest {
        wordRepository.lookupItems = listOf(aLookupItem("dict-1"))
        val vm = loadedViewModel()

        vm.lookupWord("كتب")
        assertEquals(listOf("dict-1"), vm.successState().lookupItems.map { it.externalId })
        assertNull(vm.successState().lookupError)

        vm.clearLookup()
        assertTrue(vm.successState().lookupItems.isEmpty())
        assertNull(vm.successState().lookupError)
    }

    private fun loadedViewModel(): TextReaderViewModel =
        TextReaderViewModel(textRepository, wordRepository, prefs).also { it.load(TEXT_ID) }

    private fun TextReaderViewModel.successState(): TextReaderUiState.Success =
        uiState.value as TextReaderUiState.Success

    private class FakeTextRepository : TextRepository {
        var text: TextPassage = aText()
        var sentences: List<TextSentence> = listOf(aSentence(tokenWordId = null))
        var annotations: List<TextAnnotation> = emptyList()
        val createdAnnotations = mutableListOf<TextAnnotation>()

        override suspend fun getTexts(
            baseUrl: String,
            page: Int,
            size: Int,
            sortBy: String,
            sortDesc: Boolean,
        ): Result<PagedResult<TextPassage>> = error("not stubbed")

        override suspend fun getText(baseUrl: String, id: String): Result<TextPassage> =
            Result.success(text)

        override suspend fun getSentences(baseUrl: String, textId: String): Result<List<TextSentence>> =
            Result.success(sentences)

        override suspend fun getAnnotations(baseUrl: String, textId: String): Result<List<TextAnnotation>> =
            Result.success(annotations)

        override suspend fun createAnnotation(
            baseUrl: String,
            textId: String,
            anchor: String,
            type: String,
            content: String?,
            linkedWordIds: List<String>,
        ): Result<TextAnnotation> {
            val annotation = TextAnnotation(
                id = "created-ann-${createdAnnotations.size + 1}",
                textId = textId,
                anchor = anchor,
                type = type,
                content = content,
                linkedWordIds = linkedWordIds,
            )
            createdAnnotations += annotation
            annotations = annotations + annotation
            return Result.success(annotation)
        }
    }

    private class FakeWordRepository : WordRepository {
        var wordByArabic: Word? = null
        var lookupItems: List<DictionaryLookupItem> = emptyList()
        val createdWords = mutableListOf<WordDraft>()

        override suspend fun getWords(
            baseUrl: String,
            query: String?,
            masteryLevel: String?,
            rootId: String?,
            page: Int,
            size: Int,
            sortBy: String,
            sortDesc: Boolean,
        ): Result<PagedResult<Word>> = error("not stubbed")

        override suspend fun getWord(baseUrl: String, id: String): Result<Word> = error("not stubbed")

        override suspend fun updateWord(baseUrl: String, id: String, update: WordUpdate): Result<Word> =
            error("not stubbed")

        override suspend fun deleteWord(baseUrl: String, id: String): Result<Unit> = error("not stubbed")

        override suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<Word?> =
            Result.success(wordByArabic)

        override suspend fun autocompleteWords(baseUrl: String, query: String): Result<List<WordAutocomplete>> =
            error("not stubbed")

        override suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>> =
            error("not stubbed")

        override suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>> =
            error("not stubbed")

        override suspend fun addDictionaryLink(
            baseUrl: String,
            wordId: String,
            draft: DictionaryLinkDraft,
        ): Result<DictionaryLink> = error("not stubbed")

        override suspend fun deleteDictionaryLink(baseUrl: String, wordId: String, linkId: String): Result<Unit> =
            error("not stubbed")

        override suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word> {
            createdWords += draft
            return Result.success(aWord("created-${createdWords.size}", arabicText = draft.arabicText))
        }

        override suspend fun lookupInDictionary(baseUrl: String, query: String): Result<List<DictionaryLookupItem>> =
            Result.success(lookupItems)

        override suspend fun saveExample(baseUrl: String, wordId: String, example: AiExample): Result<Example> =
            error("not stubbed")

        override suspend fun deleteExample(baseUrl: String, wordId: String, exampleId: String): Result<Unit> =
            error("not stubbed")

        override suspend fun generateExamples(baseUrl: String, wordId: String): Result<List<AiExample>> =
            error("not stubbed")

        override suspend fun generateInsight(baseUrl: String, entityType: String, entityId: String): Result<String> =
            error("not stubbed")

        override suspend fun getMorphology(baseUrl: String, wordId: String): Result<WordMorphology> =
            error("not stubbed")

        override suspend fun upsertMorphology(
            baseUrl: String,
            wordId: String,
            gender: String?,
            verbPattern: String?,
        ): Result<WordMorphology> = error("not stubbed")

        override suspend fun getPlurals(baseUrl: String, wordId: String): Result<List<WordPlural>> =
            error("not stubbed")

        override suspend fun addPlural(baseUrl: String, wordId: String, draft: WordPluralDraft): Result<WordPlural> =
            error("not stubbed")

        override suspend fun deletePlural(baseUrl: String, wordId: String, pluralId: String): Result<Unit> =
            error("not stubbed")

        override suspend fun getRelations(baseUrl: String, wordId: String): Result<List<WordRelation>> =
            error("not stubbed")

        override suspend fun addRelation(baseUrl: String, wordId: String, draft: WordRelationDraft): Result<WordRelation> =
            error("not stubbed")

        override suspend fun deleteRelation(
            baseUrl: String,
            wordId: String,
            relatedWordId: String,
            relationType: String,
        ): Result<Unit> = error("not stubbed")

        override suspend fun enrichWord(baseUrl: String, wordId: String): Result<WordEnrichmentSuggestion> =
            error("not stubbed")
    }

    private companion object {
        const val BASE_URL = "http://localhost"
        const val TEXT_ID = "text-1"

        fun aText() = TextPassage(
            id = TEXT_ID,
            title = "Story",
            body = "كتب",
            transliteration = null,
            translation = null,
            difficulty = "BEGINNER",
            dialect = "MSA",
            tags = emptyList(),
        )

        fun aSentence(tokenWordId: String?) = TextSentence(
            id = "sentence-1",
            position = 1,
            arabicText = "كتب",
            transliteration = "kataba",
            freeTranslation = "he wrote",
            tokens = listOf(aToken(wordId = tokenWordId)),
        )

        fun aToken(wordId: String?) = TextToken(
            id = "token-1",
            position = 1,
            arabic = "كتب",
            transliteration = "kataba",
            translation = "wrote",
            wordId = wordId,
        )

        fun aVocabularyAnnotation(anchor: String, wordId: String) = TextAnnotation(
            id = "ann-1",
            textId = TEXT_ID,
            anchor = anchor,
            type = "VOCABULARY",
            content = null,
            linkedWordIds = listOf(wordId),
        )

        fun aWord(id: String, arabicText: String = "كتب") = Word(
            id = id,
            arabicText = arabicText,
            transliteration = "kataba",
            translation = "to write",
            partOfSpeech = "VERB",
            dialect = "MSA",
            masteryLevel = MasteryLevel.NEW,
            rootId = null,
            notes = null,
            pronunciationUrl = null,
        )

        fun aLookupItem(id: String) = DictionaryLookupItem(
            externalId = id,
            arabicText = "كتب",
            transliteration = "kataba",
            translation = "to write",
            pluralArabic = null,
            hasExactWordMatch = true,
        )
    }
}
