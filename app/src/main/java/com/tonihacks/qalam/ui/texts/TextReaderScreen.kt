package com.tonihacks.qalam.ui.texts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.DictionaryLookupItem
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.domain.model.TextSentence
import com.tonihacks.qalam.domain.model.TextToken
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.ui.words.AddWordSheet
import com.tonihacks.qalam.ui.theme.NewsReader
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamLapisC
import com.tonihacks.qalam.ui.theme.QalamPaper
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

private enum class ReaderMode { INTERLINEAR, PLAIN }

@Composable
fun TextReaderScreen(
    textId: String,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
    viewModel: TextReaderViewModel = hiltViewModel(),
) {
    LaunchedEffect(textId) { viewModel.load(textId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val s = uiState) {
        is TextReaderUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = QalamPrimary)
        }

        is TextReaderUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(s.message, color = QalamTerra, modifier = Modifier.padding(20.dp))
        }

        is TextReaderUiState.Success -> TextReaderContent(
            text = s.text,
            sentences = s.sentences,
            isLinkingWord = s.isLinkingWord,
            linkWordError = s.linkWordError,
            lookupItems = s.lookupItems,
            isLookingUp = s.isLookingUp,
            lookupError = s.lookupError,
            duplicateCandidates = s.duplicateCandidates,
            isCheckingDuplicates = s.isCheckingDuplicates,
            onBack = onBack,
            onNavigateToWord = onNavigateToWord,
            onAddToVocabulary = viewModel::addTokenToVocabulary,
            onClearLinkError = viewModel::clearLinkError,
            onLookupWord = viewModel::lookupWord,
            onClearLookup = viewModel::clearLookup,
            onCheckDuplicates = viewModel::checkDuplicates,
            onClearDuplicateCandidates = viewModel::clearDuplicateCandidates,
            onLinkToExistingWord = viewModel::linkTokenToExistingWord,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TextReaderContent(
    text: TextPassage,
    sentences: List<TextSentence>,
    isLinkingWord: Boolean,
    linkWordError: String?,
    lookupItems: List<DictionaryLookupItem>,
    isLookingUp: Boolean,
    lookupError: String?,
    duplicateCandidates: List<WordAutocomplete>,
    isCheckingDuplicates: Boolean,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
    onAddToVocabulary: (TextToken, WordDraft, () -> Unit) -> Unit,
    onClearLinkError: () -> Unit,
    onLookupWord: (String) -> Unit,
    onClearLookup: () -> Unit,
    onCheckDuplicates: (String) -> Unit,
    onClearDuplicateCandidates: () -> Unit,
    onLinkToExistingWord: (TextToken, WordAutocomplete, () -> Unit) -> Unit,
) {
    var mode by remember { mutableStateOf(ReaderMode.INTERLINEAR) }
    var selectedToken by remember { mutableStateOf<TextToken?>(null) }
    var showAddWordFor by remember { mutableStateOf<TextToken?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // 2. Title + comments + chips
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
                    Text(
                        text.title,
                        style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 24.sp, lineHeight = 32.sp),
                        color = QalamInk,
                    )
                    text.comments?.let { notes ->
                        Spacer(Modifier.height(6.dp))
                        Text(
                            notes,
                            style = Typography.bodyMedium,
                            color = QalamInk2,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(dialectLabel(text.dialect)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = QalamLapisC,
                                labelColor = QalamLapis,
                            ),
                        )
                        SuggestionChip(
                            onClick = {},
                            label = { Text(difficultyLabel(text.difficulty)) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = QalamSurface2,
                                labelColor = QalamInk2,
                            ),
                        )
                    }
                }
            }

            // 3. Interlinear / Plain toggle
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp),
                ) {
                    SegmentedButton(
                        selected = mode == ReaderMode.INTERLINEAR,
                        onClick = { mode = ReaderMode.INTERLINEAR },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = QalamPrimary,
                            activeContentColor = QalamPaper,
                        ),
                    ) { Text("Interlinear") }
                    SegmentedButton(
                        selected = mode == ReaderMode.PLAIN,
                        onClick = { mode = ReaderMode.PLAIN },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = QalamPrimary,
                            activeContentColor = QalamPaper,
                        ),
                    ) { Text("Plain") }
                }
            }

            // 4/5. Body
            if (mode == ReaderMode.INTERLINEAR) {
                items(sentences, key = { it.id }) { sentence ->
                    InterlinearSentence(
                        sentence = sentence,
                        onTokenClick = { selectedToken = it },
                    )
                }
            } else {
                item {
                    SelectionContainer {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                text = text.body.replace("\n", " "),
                                style = TextStyle(fontFamily = NotoNaskh, fontSize = 34.sp, lineHeight = 52.sp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                            )
                        }
                    }
                }
            }

            // 6. Translation card
            text.translation?.let { translation ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(22.dp),
                        colors = CardDefaults.cardColors(containerColor = QalamSurface2),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        SelectionContainer {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Translation", style = Typography.labelMedium, color = QalamInk2)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    translation,
                                    style = TextStyle(fontFamily = NewsReader, fontSize = 16.sp, lineHeight = 24.sp),
                                    color = QalamInk,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedToken?.let { token ->
        TokenBottomSheet(
            token = token,
            onDismiss = { selectedToken = null },
            onViewEntry = { wordId ->
                selectedToken = null
                onNavigateToWord(wordId)
            },
            onAddToVocabulary = {
                selectedToken = null
                onClearLinkError()
                showAddWordFor = token
            },
        )
    }

    showAddWordFor?.let { token ->
        AddWordSheet(
            isSaving = isLinkingWord,
            errorMessage = linkWordError,
            lookupItems = lookupItems,
            isLookingUp = isLookingUp,
            lookupError = lookupError,
            onLookup = onLookupWord,
            duplicateCandidates = duplicateCandidates,
            isCheckingDuplicates = isCheckingDuplicates,
            onCheckDuplicates = onCheckDuplicates,
            onSelectExisting = { existing ->
                onLinkToExistingWord(token, existing) { showAddWordFor = null }
            },
            onDismiss = {
                showAddWordFor = null
                onClearLinkError()
                onClearLookup()
                onClearDuplicateCandidates()
            },
            onSave = { draft ->
                onAddToVocabulary(token, draft) { showAddWordFor = null }
            },
            initialArabic = token.arabic,
            initialTranslation = token.translation.orEmpty(),
            initialTransliteration = token.transliteration.orEmpty(),
            initialDialect = text.dialect,
        )
    }
}

@Composable
private fun InterlinearSentence(sentence: TextSentence, onTokenClick: (TextToken) -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sentence.tokens.forEach { token -> TokenCell(token = token, onClick = { onTokenClick(token) }) }
        }
    }
}

@Composable
private fun TokenCell(token: TextToken, onClick: () -> Unit) {
    val linked = token.wordId != null
    Column(
        modifier = Modifier
            .let { if (linked) it.background(QalamLapisC, RoundedCornerShape(8.dp)) else it }
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(token.arabic, style = TextStyle(fontFamily = NotoNaskh, fontSize = 30.sp), textAlign = TextAlign.Center)
        token.transliteration?.let {
            Text(
                it,
                style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 12.5.sp),
                color = QalamLapis,
            )
        }
        token.translation?.let {
            Text(it, style = TextStyle(fontFamily = NewsReader, fontSize = 12.sp), color = QalamInk2)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TokenBottomSheet(
    token: TextToken,
    onDismiss: () -> Unit,
    onViewEntry: (String) -> Unit,
    onAddToVocabulary: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = QalamPaper,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = token.arabic,
                    style = TextStyle(fontFamily = NotoNaskh, fontSize = 52.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(8.dp))
            token.transliteration?.let {
                Text(
                    it,
                    style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 16.sp),
                    color = QalamLapis,
                )
            }
            token.translation?.let {
                Text(it, style = Typography.titleMedium, color = QalamInk)
            }
            if (token.wordId != null) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { onViewEntry(token.wordId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) { Text("View full entry") }
            } else {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onAddToVocabulary,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                ) { Text("Add to vocabulary") }
            }
        }
    }
}
