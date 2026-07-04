package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.DictionaryLink
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamGoldC
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

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
            word = s.word,
            examples = s.examples,
            dictionaries = s.dictionaries,
            onBack = onBack,
            onNavigateToRoot = onNavigateToRoot,
            onNavigateToWord = onNavigateToWord,
        )
    }
}


@Composable
fun WordDetailContent(
    word: Word,
    examples: List<Example>,
    dictionaries: List<DictionaryLink>,
    onBack: () -> Unit,
    onNavigateToRoot: (String) -> Unit,
    onNavigateToWord: (String) -> Unit,
) {
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
                IconButton(onClick = { /* stub */ }) {
                    Icon(Icons.Outlined.BookmarkBorder, contentDescription = "Bookmark")
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

        // 5. Examples
        if (examples.isNotEmpty()) {
            item {
                Text(
                    "EXAMPLES",
                    style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                    modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 8.dp),
                    color = QalamInk2,
                )
            }
            items(examples) { ex -> ExampleCard(ex) }
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
        word.notes?.let { notes ->
            item {
                Text(
                    "NOTES",
                    style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                    modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 8.dp),
                    color = QalamInk2,
                )
                SelectionContainer {
                    Text(
                        notes,
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
        if (allLinks.isNotEmpty()) {
            item {
                Text(
                    "DICTIONARIES",
                    style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                    modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 8.dp),
                    color = QalamInk2,
                )
            }
            items(allLinks, key = { it.id }) { link ->
                DictionaryRow(name = link.source, url = link.url)
            }
        }

        // Same root section — Phase 4
    }
}
