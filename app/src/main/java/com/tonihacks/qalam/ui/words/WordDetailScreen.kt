package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection.Companion.Rtl
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.AiRelationSuggestion
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.domain.model.DictionaryLinkDraft
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordEnrichmentSuggestion
import com.tonihacks.qalam.domain.model.WordMorphology
import com.tonihacks.qalam.domain.model.WordRelation
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamGoldC
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamInk3
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamPaper
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography
import java.net.URLEncoder

private enum class WordDetailSheet {
    Notes,
    Example,
    Dictionaries,
    Relations,
    Enrichment,
}

@Composable
fun WordDetailScreen(
    wordId: String,
    onBack: () -> Unit,
    onNavigateToRoot: (String) -> Unit,
    onNavigateToWord: (String) -> Unit,
    viewModel: WordDetailViewModel = hiltViewModel()
) {

    LaunchedEffect(wordId) { viewModel.load(wordId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is WordDetailUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = QalamPrimary)
        }

        is WordDetailUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(s.message, color = QalamTerra, modifier = Modifier.padding(20.dp))
        }

        is WordDetailUiState.Success -> WordDetailContent(
            state = s,
            onBack = onBack,
            onNavigateToRoot = onNavigateToRoot,
            onNavigateToWord = onNavigateToWord,
            onGenerateExamples = viewModel::generateExamples,
            onUseExample = viewModel::useExample,
            onDiscardExample = viewModel::discardExample,
            onDismissExamples = viewModel::dismissExamples,
            onAddManualExample = viewModel::addManualExample,
            onDeleteExample = viewModel::deleteExample,
            onSaveNotes = viewModel::saveNotes,
            onAddDictionaryLink = viewModel::addDictionaryLink,
            onAddDictionaryLinks = viewModel::addDictionaryLinks,
            onDeleteDictionaryLink = viewModel::deleteDictionaryLink,
            onSearchRelations = viewModel::searchRelations,
            onAddRelation = viewModel::addRelation,
            onDeleteRelation = viewModel::deleteRelation,
            onEnrichWord = viewModel::enrichWord,
            onDismissEnrichment = viewModel::dismissEnrichment,
            onSaveEnrichment = viewModel::saveEnrichment,
            onLinkSuggestedRelation = viewModel::linkSuggestedRelation,
            onGetInsight = viewModel::getInsight,
            onDismissInsight = viewModel::dismissInsight,
        )
    }
}


@Composable
fun WordDetailContent(
    state: WordDetailUiState.Success,
    onBack: () -> Unit,
    onNavigateToRoot: (String) -> Unit,
    onNavigateToWord: (String) -> Unit,
    onGenerateExamples: () -> Unit,
    onUseExample: (AiExample) -> Unit,
    onDiscardExample: (AiExample) -> Unit,
    onDismissExamples: () -> Unit,
    onAddManualExample: (String, String?, String?) -> Unit,
    onDeleteExample: (String) -> Unit,
    onSaveNotes: (String) -> Unit,
    onAddDictionaryLink: (String, String) -> Unit,
    onAddDictionaryLinks: (List<DictionaryLinkDraft>) -> Unit,
    onDeleteDictionaryLink: (String) -> Unit,
    onSearchRelations: (String) -> Unit,
    onAddRelation: (String, String) -> Unit,
    onDeleteRelation: (String, String) -> Unit,
    onEnrichWord: () -> Unit,
    onDismissEnrichment: () -> Unit,
    onSaveEnrichment: (Boolean, String, Boolean, Boolean, Set<Int>) -> Unit,
    onLinkSuggestedRelation: (Int, AiRelationSuggestion) -> Unit,
    onGetInsight: () -> Unit,
    onDismissInsight: () -> Unit,
) {
    val word = state.word
    val examples = state.examples
    val dictionaries = state.dictionaries
    var openSheet by remember { mutableStateOf<WordDetailSheet?>(null) }

    WordDetailSheetHost(
        openSheet = openSheet,
        state = state,
        onDismiss = { openSheet = null },
        onSaveNotes = onSaveNotes,
        onAddManualExample = onAddManualExample,
        onAddDictionaryLink = onAddDictionaryLink,
        onAddDictionaryLinks = onAddDictionaryLinks,
        onDeleteDictionaryLink = onDeleteDictionaryLink,
        onSearchRelations = onSearchRelations,
        onAddRelation = onAddRelation,
        onDeleteRelation = onDeleteRelation,
        onStartEnrichment = onEnrichWord,
        onDismissEnrichment = onDismissEnrichment,
        onSaveEnrichment = onSaveEnrichment,
        onLinkSuggestedRelation = onLinkSuggestedRelation,
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        // 1. Top bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                }
                Row {
                    IconButton(onClick = { openSheet = WordDetailSheet.Enrichment }) {
                        Icon(Icons.Outlined.AutoAwesome, contentDescription = "AI enrich")
                    }
                    IconButton(onClick = { /* future Word List/bookmark surface */ }) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
                    }
                }
            }
        }

        // 2. Hero — Arabic centered, RTL subtree
        item {
            SelectionContainer {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = word.arabicText,
                            style = Typography.displayLarge,     // Amiri 57sp — adjust in Type.kt if needed
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    word.transliteration?.let {
                        Text(
                            it,
                            style = Typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = QalamInk2,
                        )
                    }
                    word.translation?.let {
                        Text(it, style = Typography.titleMedium, color = QalamInk)
                    }
                }
            }
        }

        // 3. Badges
        item {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SuggestionChip(onClick = {}, label = { Text(word.partOfSpeech) })
                SuggestionChip(onClick = {}, label = { Text(word.dialect) })
            }
        }

        // 4. Mastery card
        item { MasteryCard(level = word.masteryLevel, modifier = Modifier.padding(22.dp)) }

        item {
            MorphologyStrip(morphology = state.morphology)
        }

        item {
            RelationsPreview(
                relations = state.relations,
                onManage = { openSheet = WordDetailSheet.Relations },
                onNavigateToWord = onNavigateToWord,
            )
        }

        // 5. Examples
        item {
            SectionHeader(
                title = "EXAMPLES",
                actionLabel = "Add",
                actionIcon = Icons.Outlined.Add,
                onAction = { openSheet = WordDetailSheet.Example },
            )
        }
        if (examples.isEmpty()) {
            item {
                Text(
                    "No saved examples yet.",
                    style = Typography.bodySmall,
                    color = QalamInk2,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                )
            }
        } else {
            items(examples, key = { it.id }) { ex ->
                ExampleCardWithActions(
                    example = ex,
                    isDeleting = state.isDeletingExample,
                    onDelete = { onDeleteExample(ex.id) },
                )
            }
        }

        // 5b. AI example generation
        item {
            Text(
                "AI EXAMPLES",
                style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 4.dp),
                color = QalamInk2,
            )
            AiExamplesSection(
                aiExamples = state.aiExamples,
                isGenerating = state.isGeneratingExamples,
                isSaving = state.savingExample,
                error = state.examplesError,
                unavailable = state.aiExamplesUnavailable,
                onGenerate = onGenerateExamples,
                onUse = onUseExample,
                onDiscard = onDiscardExample,
                onDismiss = onDismissExamples,
            )
        }

        // 6. Root link card
        word.rootId?.let { rid ->
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                        .clickable { onNavigateToRoot(rid) },
                    colors = CardDefaults.cardColors(containerColor = QalamGoldC),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(
                        "View root →",
                        modifier = Modifier.padding(16.dp),
                        style = Typography.labelLarge,
                        color = QalamGold,
                    )
                }
            }
        }

        // 7. Notes
        item {
            SectionHeader(
                title = "NOTES",
                actionLabel = if (word.notes.isNullOrBlank()) "Add" else "Edit",
                actionIcon = Icons.Outlined.Edit,
                onAction = { openSheet = WordDetailSheet.Notes },
            )
            if (word.notes.isNullOrBlank()) {
                Text(
                    "No notes yet.",
                    style = Typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                    color = QalamInk2,
                )
            } else {
                SelectionContainer {
                    Text(
                        word.notes,
                        style = Typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                        color = QalamInk,
                    )
                }
            }
        }

        // 8. Dictionary links (from /words/{id}/dictionary-links) + pronunciationUrl fallback
        val allLinks = buildList {
            addAll(dictionaries)
            word.pronunciationUrl?.let { add(DictionaryLink(id = "pronunciation", source = "Pronunciation", url = it)) }
        }
        item {
            SectionHeader(
                title = "DICTIONARIES",
                actionLabel = "Manage",
                actionIcon = Icons.Outlined.Link,
                onAction = { openSheet = WordDetailSheet.Dictionaries },
            )
            if (allLinks.isEmpty()) {
                Text(
                    "No dictionary links yet.",
                    style = Typography.bodySmall,
                    color = QalamInk2,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
                )
            }
        }
        items(allLinks, key = { it.id }) { link ->
            DictionaryRow(name = link.source, url = link.url)
        }

        // 9. AI insight (hidden entirely when the backend AI is unavailable)
        if (!state.insightUnavailable) {
            item {
                Text(
                    "AI INSIGHT",
                    style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                    modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 4.dp),
                    color = QalamInk2,
                )
                AiInsightSection(
                    phase = state.insightPhase,
                    insightText = state.insightText,
                    error = state.insightError,
                    unavailable = state.insightUnavailable,
                    onGet = onGetInsight,
                    onDismiss = onDismissInsight,
                )
            }
        }

        // Same root section — Phase 4
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordDetailSheetHost(
    openSheet: WordDetailSheet?,
    state: WordDetailUiState.Success,
    onDismiss: () -> Unit,
    onSaveNotes: (String) -> Unit,
    onAddManualExample: (String, String?, String?) -> Unit,
    onAddDictionaryLink: (String, String) -> Unit,
    onAddDictionaryLinks: (List<DictionaryLinkDraft>) -> Unit,
    onDeleteDictionaryLink: (String) -> Unit,
    onSearchRelations: (String) -> Unit,
    onAddRelation: (String, String) -> Unit,
    onDeleteRelation: (String, String) -> Unit,
    onStartEnrichment: () -> Unit,
    onDismissEnrichment: () -> Unit,
    onSaveEnrichment: (Boolean, String, Boolean, Boolean, Set<Int>) -> Unit,
    onLinkSuggestedRelation: (Int, AiRelationSuggestion) -> Unit,
) {
    if (openSheet == null) return

    ModalBottomSheet(
        onDismissRequest = {
            if (openSheet == WordDetailSheet.Enrichment) onDismissEnrichment()
            onDismiss()
        },
        containerColor = QalamPaper,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    ) {
        when (openSheet) {
            WordDetailSheet.Notes -> NotesSheet(
                notes = state.word.notes.orEmpty(),
                isSaving = state.isSavingNotes,
                error = state.notesError,
                onSave = onSaveNotes,
            )
            WordDetailSheet.Example -> ManualExampleSheet(
                isSaving = state.isSavingManualExample,
                error = state.examplesError,
                onSave = onAddManualExample,
            )
            WordDetailSheet.Dictionaries -> DictionaryManagementSheet(
                arabicText = state.word.arabicText,
                links = state.dictionaries,
                isMutating = state.isMutatingDictionary,
                error = state.dictionaryError,
                onAddOne = onAddDictionaryLink,
                onAddMany = onAddDictionaryLinks,
                onDelete = onDeleteDictionaryLink,
            )
            WordDetailSheet.Relations -> RelationsSheet(
                relations = state.relations,
                searchResults = state.relationSearchResults,
                isSearching = state.isSearchingRelations,
                isMutating = state.isMutatingRelation,
                error = state.relationError,
                onSearch = onSearchRelations,
                onAdd = onAddRelation,
                onDelete = onDeleteRelation,
            )
            WordDetailSheet.Enrichment -> EnrichmentSheet(
                suggestion = state.enrichment,
                isLoading = state.isEnriching,
                isSaving = state.isSavingEnrichment,
                unavailable = state.enrichmentUnavailable,
                error = state.enrichmentError,
                linkingIndexes = state.linkingSuggestedRelationIndexes,
                onStart = onStartEnrichment,
                onSave = onSaveEnrichment,
                onLinkRelation = onLinkSuggestedRelation,
            )
        }
    }
}

@Composable
private fun SheetColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content,
    )
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = Typography.labelSmall.copy(letterSpacing = 2.sp),
            color = QalamInk2,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                actionIcon?.let {
                    Icon(it, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(4.dp))
                }
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ExampleCardWithActions(
    example: Example,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    Column {
        ExampleCard(example)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDelete, enabled = !isDeleting) {
                Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(Modifier.size(4.dp))
                Text("Delete")
            }
        }
    }
}

@Composable
private fun MorphologyStrip(morphology: WordMorphology?) {
    val plurals = morphology?.plurals.orEmpty()
    val hasMorphology = morphology?.gender != null || morphology?.verbPattern != null || plurals.isNotEmpty()
    if (!hasMorphology) return

    Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp)) {
        Text("MORPHOLOGY", style = Typography.labelSmall.copy(letterSpacing = 2.sp), color = QalamInk2)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            morphology?.gender?.let { InfoChip("Gender: ${it.lowercase()}") }
            morphology?.verbPattern?.let { InfoChip("Form $it") }
            plurals.forEach { plural ->
                ArabicInfoChip("${plural.pluralForm} · ${plural.pluralType.lowercase().replace('_', ' ')}")
            }
        }
    }
}

@Composable
private fun RelationsPreview(
    relations: List<WordRelation>,
    onManage: () -> Unit,
    onNavigateToWord: (String) -> Unit,
) {
    SectionHeader(
        title = "RELATIONS",
        actionLabel = "Manage",
        actionIcon = Icons.Outlined.Link,
        onAction = onManage,
    )
    if (relations.isEmpty()) {
        Text(
            "No related words yet.",
            style = Typography.bodySmall,
            color = QalamInk2,
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 4.dp),
        )
        return
    }
    Row(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        relations.forEach { relation ->
            AssistChip(
                onClick = { onNavigateToWord(relation.relatedWordId) },
                label = {
                    Text("${relation.relatedWordArabic} · ${relation.relationType.lowercase()}")
                },
            )
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    AssistChip(onClick = {}, label = { Text(label, style = Typography.labelMedium) })
}

@Composable
private fun ArabicInfoChip(label: String) {
    AssistChip(
        onClick = {},
        label = {
            Text(
                label,
                style = Typography.labelMedium.copy(fontFamily = NotoNaskh),
            )
        },
    )
}

@Composable
private fun NotesSheet(
    notes: String,
    isSaving: Boolean,
    error: String?,
    onSave: (String) -> Unit,
) {
    var edited by remember(notes) { mutableStateOf(notes) }
    SheetColumn {
        Text("Notes", style = Typography.titleLarge)
        OutlinedTextField(
            value = edited,
            onValueChange = { edited = it },
            label = { Text("Learning notes") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 5,
        )
        error?.let { Text(it, color = QalamTerra, style = Typography.bodySmall) }
        Button(
            onClick = { onSave(edited) },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = QalamPaper)
            } else {
                Text("Save notes")
            }
        }
    }
}

@Composable
private fun ManualExampleSheet(
    isSaving: Boolean,
    error: String?,
    onSave: (String, String?, String?) -> Unit,
) {
    var arabic by remember { mutableStateOf("") }
    var transliteration by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    SheetColumn {
        Text("Add example", style = Typography.titleLarge)
        OutlinedTextField(
            value = arabic,
            onValueChange = { arabic = it },
            label = { Text("Arabic") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = Typography.titleLarge.copy(
                fontFamily = NotoNaskh,
                fontSize = 24.sp,
                textDirection = Rtl,
            ),
            minLines = 2,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )
        OutlinedTextField(
            value = transliteration,
            onValueChange = { transliteration = it },
            label = { Text("Transliteration") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = translation,
            onValueChange = { translation = it },
            label = { Text("Translation") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        error?.let { Text(it, color = QalamTerra, style = Typography.bodySmall) }
        Button(
            onClick = { onSave(arabic, transliteration, translation) },
            enabled = !isSaving && arabic.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(if (isSaving) "Saving..." else "Save example")
        }
    }
}

@Composable
private fun DictionaryManagementSheet(
    arabicText: String,
    links: List<DictionaryLink>,
    isMutating: Boolean,
    error: String?,
    onAddOne: (String, String) -> Unit,
    onAddMany: (List<DictionaryLinkDraft>) -> Unit,
    onDelete: (String) -> Unit,
) {
    var selectedSource by remember { mutableStateOf("ALMANY") }
    var url by remember { mutableStateOf("") }
    val existingSources = links.map { it.source }.toSet()
    val missingTemplated = dictionaryUrlTemplates.filterKeys { it !in existingSources }
    val generatedUrl = dictionaryUrlTemplates[selectedSource]?.formatArabicUrl(arabicText)

    SheetColumn {
        Text("Dictionary links", style = Typography.titleLarge)
        if (missingTemplated.isNotEmpty()) {
            OutlinedButton(
                onClick = {
                    onAddMany(
                        missingTemplated.map { (source, template) ->
                            DictionaryLinkDraft(source = source, url = template.formatArabicUrl(arabicText))
                        },
                    )
                },
                enabled = !isMutating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("Add all missing dictionaries")
            }
        }

        if (links.isNotEmpty()) {
            links.forEach { link ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(link.source, style = Typography.labelLarge, color = QalamLapis)
                    TextButton(onClick = { onDelete(link.id) }, enabled = !isMutating) {
                        Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text("Remove")
                    }
                }
                HorizontalDivider(color = QalamSurface2)
            }
        }

        Text("Add one", style = Typography.titleMedium)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            dictionarySources.forEach { source ->
                AssistChip(
                    onClick = {
                        selectedSource = source
                        url = dictionaryUrlTemplates[source]?.formatArabicUrl(arabicText).orEmpty()
                    },
                    label = { Text(dictionarySourceLabels[source] ?: source) },
                )
            }
        }
        OutlinedTextField(
            value = url.ifBlank { generatedUrl.orEmpty() },
            onValueChange = { url = it },
            label = { Text("URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        error?.let { Text(it, color = QalamTerra, style = Typography.bodySmall) }
        Button(
            onClick = { onAddOne(selectedSource, url.ifBlank { generatedUrl.orEmpty() }) },
            enabled = !isMutating && (url.isNotBlank() || generatedUrl != null),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(if (isMutating) "Working..." else "Add link")
        }
    }
}

@Composable
private fun RelationsSheet(
    relations: List<WordRelation>,
    searchResults: List<WordAutocomplete>,
    isSearching: Boolean,
    isMutating: Boolean,
    error: String?,
    onSearch: (String) -> Unit,
    onAdd: (String, String) -> Unit,
    onDelete: (String, String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var relationType by remember { mutableStateOf("RELATED") }
    SheetColumn {
        Text("Related words", style = Typography.titleLarge)
        RelationGroups(relations = relations, isMutating = isMutating, onDelete = onDelete)
        HorizontalDivider(color = QalamSurface2)
        Text("Link another word", style = Typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("RELATED", "SYNONYM", "ANTONYM").forEach { type ->
                AssistChip(onClick = { relationType = type }, label = { Text(type.lowercase()) })
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                onSearch(it)
            },
            label = { Text("Search vocabulary") },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (isSearching) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = QalamPrimary)
                Text("Searching...", style = Typography.bodySmall, color = QalamInk2)
            }
        }
        searchResults.forEach { result ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(result.arabicText, style = Typography.titleLarge.copy(fontFamily = NotoNaskh))
                    result.translation?.let { Text(it, style = Typography.bodySmall, color = QalamInk2) }
                }
                Button(
                    onClick = { onAdd(result.id, relationType) },
                    enabled = !isMutating,
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Text("Link")
                }
            }
        }
        error?.let { Text(it, color = QalamTerra, style = Typography.bodySmall) }
    }
}

@Composable
private fun RelationGroups(
    relations: List<WordRelation>,
    isMutating: Boolean,
    onDelete: (String, String) -> Unit,
) {
    if (relations.isEmpty()) {
        Text("No relations yet.", style = Typography.bodySmall, color = QalamInk2)
        return
    }
    relations.groupBy { it.relationType }.forEach { (type, group) ->
        Text(type.lowercase().replaceFirstChar { it.titlecase() }, style = Typography.labelLarge, color = QalamInk2)
        group.forEach { relation ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(relation.relatedWordArabic, style = Typography.titleLarge.copy(fontFamily = NotoNaskh))
                    relation.relatedWordTranslation?.let { Text(it, style = Typography.bodySmall, color = QalamInk2) }
                }
                TextButton(
                    onClick = { onDelete(relation.relatedWordId, relation.relationType) },
                    enabled = !isMutating,
                ) {
                    Text("Remove")
                }
            }
        }
    }
}

@Composable
private fun EnrichmentSheet(
    suggestion: WordEnrichmentSuggestion?,
    isLoading: Boolean,
    isSaving: Boolean,
    unavailable: Boolean,
    error: String?,
    linkingIndexes: Set<Int>,
    onStart: () -> Unit,
    onSave: (Boolean, String, Boolean, Boolean, Set<Int>) -> Unit,
    onLinkRelation: (Int, AiRelationSuggestion) -> Unit,
) {
    LaunchedEffect(Unit) {
        if (suggestion == null && !isLoading && !unavailable) onStart()
    }

    var acceptNotes by remember(suggestion) { mutableStateOf(!suggestion?.notes.isNullOrBlank()) }
    var notes by remember(suggestion) { mutableStateOf(suggestion?.notes.orEmpty()) }
    var acceptGender by remember(suggestion) { mutableStateOf(suggestion?.gender != null) }
    var acceptPattern by remember(suggestion) { mutableStateOf(suggestion?.verbPattern != null) }
    var acceptedPluralIndexes by remember(suggestion) {
        mutableStateOf(suggestion?.plurals?.indices?.toSet().orEmpty())
    }

    SheetColumn {
        Text("AI enrichment", style = Typography.titleLarge)
        when {
            isLoading -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = QalamPrimary)
                Text("Fetching suggestions...", style = Typography.bodyMedium, color = QalamInk2)
            }
            unavailable -> Text(
                "AI is not configured on the backend.",
                style = Typography.bodySmall,
                color = QalamInk2,
            )
            suggestion == null -> OutlinedButton(onClick = onStart, shape = RoundedCornerShape(16.dp)) {
                Text("Try enrichment")
            }
            else -> {
                suggestion.gender?.let {
                    ToggleRow(checked = acceptGender, onCheckedChange = { acceptGender = it }) {
                        Text("Gender: ${it.lowercase()}", style = Typography.bodyMedium)
                    }
                }
                suggestion.verbPattern?.let {
                    ToggleRow(checked = acceptPattern, onCheckedChange = { acceptPattern = it }) {
                        Text("Verb form: $it", style = Typography.bodyMedium)
                    }
                }
                if (suggestion.plurals.isNotEmpty()) {
                    Text("Plurals", style = Typography.titleMedium)
                    suggestion.plurals.forEachIndexed { index, plural ->
                        ToggleRow(
                            checked = index in acceptedPluralIndexes,
                            onCheckedChange = { checked ->
                                acceptedPluralIndexes = if (checked) {
                                    acceptedPluralIndexes + index
                                } else {
                                    acceptedPluralIndexes - index
                                }
                            },
                        ) {
                            Text(
                                "${plural.pluralForm} · ${plural.pluralType.lowercase().replace('_', ' ')}",
                                style = Typography.bodyMedium.copy(fontFamily = NotoNaskh),
                            )
                        }
                    }
                }
                if (!suggestion.notes.isNullOrBlank()) {
                    ToggleRow(checked = acceptNotes, onCheckedChange = { acceptNotes = it }) {
                        Text("Notes", style = Typography.titleMedium)
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        enabled = acceptNotes,
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                    )
                }
                if (suggestion.relations.isNotEmpty()) {
                    Text("Suggested relations", style = Typography.titleMedium)
                    suggestion.relations.forEachIndexed { index, relation ->
                        SuggestedRelationRow(
                            index = index,
                            relation = relation,
                            isLinking = index in linkingIndexes,
                            onLink = onLinkRelation,
                        )
                    }
                }
                error?.let { Text(it, color = QalamTerra, style = Typography.bodySmall) }
                Button(
                    onClick = {
                        onSave(acceptNotes, notes, acceptGender, acceptPattern, acceptedPluralIndexes)
                    },
                    enabled = !isSaving,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(if (isSaving) "Saving..." else "Save accepted")
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        content()
    }
}

@Composable
private fun SuggestedRelationRow(
    index: Int,
    relation: AiRelationSuggestion,
    isLinking: Boolean,
    onLink: (Int, AiRelationSuggestion) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = QalamSurface2),
        shape = RoundedCornerShape(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(relation.arabicText, style = Typography.titleLarge.copy(fontFamily = NotoNaskh))
                relation.transliteration?.let {
                    Text(it, style = Typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = QalamInk2)
                }
                relation.translation?.let { Text(it, style = Typography.bodySmall, color = QalamInk) }
                Text(relation.relationType.lowercase(), style = Typography.labelSmall, color = QalamInk3)
            }
            OutlinedButton(
                onClick = { onLink(index, relation) },
                enabled = !isLinking,
                shape = RoundedCornerShape(14.dp),
            ) {
                Text(if (isLinking) "Linking..." else "Create/link")
            }
        }
    }
}

private val dictionaryUrlTemplates = mapOf(
    "ALMANY" to "https://www.almaany.com/en/dict/ar-en/{word}",
    "LIVING_ARABIC" to "https://www.livingarabic.com/en/search?q={word}",
    "DERJA_NINJA" to "https://derja.ninja/search?search={word}&script=arabic",
    "REVERSO" to "https://dictionary.reverso.net/arabic-english/{word}",
    "WIKTIONARY" to "https://en.wiktionary.org/wiki/{word}",
    "ARABIC_STUDENT_DICTIONARY" to "https://www.arabicstudentsdictionary.com/search?q={word}",
    "LANGENSCHEIDT" to "https://de.langenscheidt.com/arabisch-deutsch/{word}",
)

private val dictionarySourceLabels = mapOf(
    "ALMANY" to "Almaany",
    "LIVING_ARABIC" to "Living Arabic",
    "DERJA_NINJA" to "Derja Ninja",
    "REVERSO" to "Reverso",
    "WIKTIONARY" to "Wiktionary",
    "ARABIC_STUDENT_DICTIONARY" to "ASD",
    "LANGENSCHEIDT" to "Langenscheidt",
    "CUSTOM" to "Custom",
)

private val dictionarySources = dictionarySourceLabels.keys.toList()

private fun String.formatArabicUrl(arabicText: String): String =
    replace("{word}", URLEncoder.encode(arabicText, Charsets.UTF_8.name()))
