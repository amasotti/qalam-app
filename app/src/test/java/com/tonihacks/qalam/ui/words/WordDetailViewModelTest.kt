package com.tonihacks.qalam.ui.words

import com.tonihacks.qalam.data.local.PreferencesRepository
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.AiRelationSuggestion
import com.tonihacks.qalam.domain.model.AiUnavailableException
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.DictionaryLinkDraft
import com.tonihacks.qalam.domain.model.DictionaryLookupItem
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.PagedResult
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
class WordDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeWordRepository
    private lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeWordRepository()
        prefs = mockk()
        every { prefs.baseUrl } returns flowOf(BASE_URL)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load populates word detail with secondary sections`() = runTest {
        repo.word = aWord(notes = "remember the form")
        repo.examples = listOf(anExample("ex-1"))
        repo.dictionaryLinks = listOf(aDictionaryLink("dict-1"))
        repo.morphology = WordMorphology(
            gender = "FEMININE",
            verbPattern = null,
            plurals = listOf(aPlural("plural-1")),
        )
        repo.relations = listOf(aRelation("rel-1"))

        val vm = WordDetailViewModel(repo, prefs)
        vm.load(WORD_ID)

        val state = vm.successState()
        assertEquals("remember the form", state.word.notes)
        assertEquals(listOf("ex-1"), state.examples.map { it.id })
        assertEquals(listOf("dict-1"), state.dictionaries.map { it.id })
        assertEquals("FEMININE", state.morphology?.gender)
        assertEquals(listOf("rel-1"), state.relations.map { it.relatedWordId })
    }

    @Test
    fun `saveNotes trims non-empty notes and patches word state`() = runTest {
        val vm = loadedViewModel()

        vm.saveNotes("  stronger with على  ")

        val state = vm.successState()
        assertEquals("stronger with على", repo.lastWordUpdate?.notes)
        assertEquals("stronger with على", state.word.notes)
        assertFalse(state.isSavingNotes)
        assertNull(state.notesError)
    }

    @Test
    fun `saveNotes sends null for blank notes`() = runTest {
        val vm = loadedViewModel()

        vm.saveNotes("   ")

        assertNull(repo.lastWordUpdate?.notes)
        assertNull(vm.successState().word.notes)
    }

    @Test
    fun `manual example trims optional fields and appends saved example`() = runTest {
        val vm = loadedViewModel()

        vm.addManualExample("  هو يكتب رسالة  ", "  huwa yaktub risala  ", "  he writes a letter  ")

        val saved = repo.savedExamples.single()
        assertEquals("هو يكتب رسالة", saved.arabic)
        assertEquals("huwa yaktub risala", saved.transliteration)
        assertEquals("he writes a letter", saved.translation)
        assertEquals(listOf("saved-1"), vm.successState().examples.map { it.id })
    }

    @Test
    fun `dictionary add and delete update local state`() = runTest {
        val vm = loadedViewModel()

        vm.addDictionaryLink("WIKTIONARY", "https://en.wiktionary.org/wiki/كتب")
        assertEquals(listOf("WIKTIONARY"), vm.successState().dictionaries.map { it.source })

        vm.deleteDictionaryLink("dict-1")
        assertTrue(vm.successState().dictionaries.isEmpty())
    }

    @Test
    fun `relation search filters out current word and short queries clear results`() = runTest {
        repo.autocompleteResults = listOf(
            WordAutocomplete(id = WORD_ID, arabicText = "كتب", translation = "write"),
            WordAutocomplete(id = "other", arabicText = "دفتر", translation = "notebook"),
        )
        val vm = loadedViewModel()

        vm.searchRelations("دف")
        assertEquals(listOf("other"), vm.successState().relationSearchResults.map { it.id })

        vm.searchRelations("د")
        assertTrue(vm.successState().relationSearchResults.isEmpty())
    }

    @Test
    fun `add and delete relation mutate relation state`() = runTest {
        val vm = loadedViewModel()

        vm.addRelation("related-1", "SYNONYM")
        assertEquals(listOf("related-1"), vm.successState().relations.map { it.relatedWordId })

        vm.deleteRelation("related-1", "SYNONYM")
        assertTrue(vm.successState().relations.isEmpty())
    }

    @Test
    fun `enrichWord maps AI unavailable into unavailable state`() = runTest {
        repo.enrichmentResult = Result.failure(AiUnavailableException())
        val vm = loadedViewModel()

        vm.enrichWord()

        val state = vm.successState()
        assertTrue(state.enrichmentUnavailable)
        assertFalse(state.isEnriching)
        assertNull(state.enrichmentError)
    }

    @Test
    fun `saveEnrichment persists accepted notes morphology and selected plurals`() = runTest {
        repo.enrichmentResult = Result.success(
            WordEnrichmentSuggestion(
                notes = "AI note",
                gender = "FEMININE",
                verbPattern = "VIII",
                plurals = listOf(
                    WordPluralDraft("كُتُب", "BROKEN").toSuggestion(),
                    WordPluralDraft("كاتبات", "SOUND_FEM").toSuggestion(),
                ),
                relations = emptyList(),
            ),
        )
        val vm = loadedViewModel()
        vm.enrichWord()

        vm.saveEnrichment(
            acceptNotes = true,
            notes = "accepted note",
            acceptGender = true,
            acceptVerbPattern = false,
            acceptedPluralIndexes = setOf(1),
        )

        val state = vm.successState()
        assertEquals("accepted note", repo.lastWordUpdate?.notes)
        assertEquals("FEMININE", repo.lastMorphologyGender)
        assertNull(repo.lastMorphologyVerbPattern)
        assertEquals(listOf("كاتبات"), repo.addedPlurals.map { it.pluralForm })
        assertEquals("accepted note", state.word.notes)
        assertEquals("FEMININE", state.morphology?.gender)
        assertEquals(listOf("كاتبات"), state.morphology?.plurals?.map { it.pluralForm })
    }

    @Test
    fun `linkSuggestedRelation creates missing word and adds relation`() = runTest {
        repo.wordByArabic = null
        val vm = loadedViewModel()
        val suggestion = AiRelationSuggestion(
            arabicText = "دفتر",
            relationType = "RELATED",
            transliteration = "daftar",
            translation = "notebook",
        )

        vm.linkSuggestedRelation(index = 0, relation = suggestion)

        assertEquals("دفتر", repo.createdWords.single().arabicText)
        assertEquals("notebook", repo.createdWords.single().translation)
        assertEquals(listOf("created-1"), vm.successState().relations.map { it.relatedWordId })
        assertTrue(vm.successState().linkingSuggestedRelationIndexes.isEmpty())
    }

    private fun loadedViewModel(): WordDetailViewModel =
        WordDetailViewModel(repo, prefs).also { it.load(WORD_ID) }

    private fun WordDetailViewModel.successState(): WordDetailUiState.Success =
        uiState.value as WordDetailUiState.Success

    private class FakeWordRepository : WordRepository {
        var word: Word = aWord()
        var examples: List<Example> = emptyList()
        var dictionaryLinks: List<DictionaryLink> = emptyList()
        var morphology: WordMorphology = WordMorphology(gender = null, verbPattern = null, plurals = emptyList())
        var relations: List<WordRelation> = emptyList()
        var autocompleteResults: List<WordAutocomplete> = emptyList()
        var enrichmentResult: Result<WordEnrichmentSuggestion> = Result.success(
            WordEnrichmentSuggestion(notes = null, gender = null, verbPattern = null, plurals = emptyList(), relations = emptyList())
        )
        var wordByArabic: Word? = null

        val savedExamples = mutableListOf<AiExample>()
        val addedDictionaryLinks = mutableListOf<DictionaryLinkDraft>()
        val createdWords = mutableListOf<WordDraft>()
        val addedPlurals = mutableListOf<WordPluralDraft>()

        var lastWordUpdate: WordUpdate? = null
        var lastMorphologyGender: String? = null
        var lastMorphologyVerbPattern: String? = null

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

        override suspend fun getWord(baseUrl: String, id: String): Result<Word> =
            Result.success(word)

        override suspend fun updateWord(baseUrl: String, id: String, update: WordUpdate): Result<Word> {
            lastWordUpdate = update
            word = word.copy(notes = update.notes)
            return Result.success(word)
        }

        override suspend fun deleteWord(baseUrl: String, id: String): Result<Unit> =
            error("not stubbed")

        override suspend fun getWordByArabic(baseUrl: String, arabicText: String): Result<Word?> =
            Result.success(wordByArabic)

        override suspend fun autocompleteWords(baseUrl: String, query: String): Result<List<WordAutocomplete>> =
            Result.success(autocompleteResults)

        override suspend fun getExamples(baseUrl: String, wordId: String): Result<List<Example>> =
            Result.success(examples)

        override suspend fun getDictionaryLinks(baseUrl: String, wordId: String): Result<List<DictionaryLink>> =
            Result.success(dictionaryLinks)

        override suspend fun addDictionaryLink(
            baseUrl: String,
            wordId: String,
            draft: DictionaryLinkDraft,
        ): Result<DictionaryLink> {
            addedDictionaryLinks += draft
            val link = DictionaryLink(id = "dict-${addedDictionaryLinks.size}", source = draft.source, url = draft.url)
            dictionaryLinks = dictionaryLinks + link
            return Result.success(link)
        }

        override suspend fun deleteDictionaryLink(baseUrl: String, wordId: String, linkId: String): Result<Unit> {
            dictionaryLinks = dictionaryLinks.filterNot { it.id == linkId }
            return Result.success(Unit)
        }

        override suspend fun createWord(baseUrl: String, draft: WordDraft): Result<Word> {
            createdWords += draft
            val created = aWord(id = "created-${createdWords.size}", arabicText = draft.arabicText)
                .copy(translation = draft.translation, transliteration = draft.transliteration)
            return Result.success(created)
        }

        override suspend fun lookupInDictionary(baseUrl: String, query: String): Result<List<DictionaryLookupItem>> =
            error("not stubbed")

        override suspend fun saveExample(baseUrl: String, wordId: String, example: AiExample): Result<Example> {
            savedExamples += example
            val saved = Example(
                id = "saved-${savedExamples.size}",
                arabicText = example.arabic,
                transliteration = example.transliteration,
                translation = example.translation,
            )
            examples = examples + saved
            return Result.success(saved)
        }

        override suspend fun deleteExample(baseUrl: String, wordId: String, exampleId: String): Result<Unit> {
            examples = examples.filterNot { it.id == exampleId }
            return Result.success(Unit)
        }

        override suspend fun generateExamples(baseUrl: String, wordId: String): Result<List<AiExample>> =
            error("not stubbed")

        override suspend fun generateInsight(baseUrl: String, entityType: String, entityId: String): Result<String> =
            error("not stubbed")

        override suspend fun getMorphology(baseUrl: String, wordId: String): Result<WordMorphology> =
            Result.success(morphology)

        override suspend fun upsertMorphology(
            baseUrl: String,
            wordId: String,
            gender: String?,
            verbPattern: String?,
        ): Result<WordMorphology> {
            lastMorphologyGender = gender
            lastMorphologyVerbPattern = verbPattern
            morphology = morphology.copy(gender = gender ?: morphology.gender, verbPattern = verbPattern ?: morphology.verbPattern)
            return Result.success(morphology)
        }

        override suspend fun getPlurals(baseUrl: String, wordId: String): Result<List<WordPlural>> =
            Result.success(morphology.plurals)

        override suspend fun addPlural(baseUrl: String, wordId: String, draft: WordPluralDraft): Result<WordPlural> {
            addedPlurals += draft
            val plural = WordPlural(
                id = "plural-${addedPlurals.size}",
                pluralForm = draft.pluralForm,
                pluralType = draft.pluralType,
            )
            morphology = morphology.copy(plurals = morphology.plurals + plural)
            return Result.success(plural)
        }

        override suspend fun deletePlural(baseUrl: String, wordId: String, pluralId: String): Result<Unit> =
            error("not stubbed")

        override suspend fun getRelations(baseUrl: String, wordId: String): Result<List<WordRelation>> =
            Result.success(relations)

        override suspend fun addRelation(
            baseUrl: String,
            wordId: String,
            draft: WordRelationDraft,
        ): Result<WordRelation> {
            val relation = WordRelation(
                relatedWordId = draft.relatedWordId,
                relatedWordArabic = "دفتر",
                relatedWordTranslation = "notebook",
                relationType = draft.relationType,
            )
            relations = relations + relation
            return Result.success(relation)
        }

        override suspend fun deleteRelation(
            baseUrl: String,
            wordId: String,
            relatedWordId: String,
            relationType: String,
        ): Result<Unit> {
            relations = relations.filterNot {
                it.relatedWordId == relatedWordId && it.relationType == relationType
            }
            return Result.success(Unit)
        }

        override suspend fun enrichWord(baseUrl: String, wordId: String): Result<WordEnrichmentSuggestion> =
            enrichmentResult
    }

    private companion object {
        const val BASE_URL = "http://localhost"
        const val WORD_ID = "word-1"

        fun aWord(
            id: String = WORD_ID,
            arabicText: String = "كتب",
            notes: String? = null,
        ) = Word(
            id = id,
            arabicText = arabicText,
            transliteration = "kataba",
            translation = "to write",
            partOfSpeech = "VERB",
            dialect = "MSA",
            masteryLevel = MasteryLevel.NEW,
            rootId = null,
            notes = notes,
            pronunciationUrl = null,
        )

        fun anExample(id: String) = Example(
            id = id,
            arabicText = "كتب الولد",
            transliteration = "kataba al-walad",
            translation = "the boy wrote",
        )

        fun aDictionaryLink(id: String) = DictionaryLink(
            id = id,
            source = "ALMANY",
            url = "https://example.test",
        )

        fun aPlural(id: String) = WordPlural(
            id = id,
            pluralForm = "كُتُب",
            pluralType = "BROKEN",
        )

        fun aRelation(id: String) = WordRelation(
            relatedWordId = id,
            relatedWordArabic = "دفتر",
            relatedWordTranslation = "notebook",
            relationType = "RELATED",
        )

        fun WordPluralDraft.toSuggestion() = com.tonihacks.qalam.domain.model.AiPluralSuggestion(
            pluralForm = pluralForm,
            pluralType = pluralType,
        )
    }
}
