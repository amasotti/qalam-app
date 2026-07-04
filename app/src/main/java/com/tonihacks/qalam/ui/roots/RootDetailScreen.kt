package com.tonihacks.qalam.ui.roots

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.Root
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.ui.components.MarkdownText
import com.tonihacks.qalam.ui.theme.Amiri
import com.tonihacks.qalam.ui.theme.NewsReader
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamGoldC
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun RootDetailScreen(
    rootId: String,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
    viewModel: RootDetailViewModel = hiltViewModel(),
) {
    LaunchedEffect(rootId) { viewModel.load(rootId) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is RootDetailUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator(color = QalamPrimary)
        }
        is RootDetailUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) {
            Text(state.message, color = QalamTerra, modifier = Modifier.padding(20.dp))
        }
        is RootDetailUiState.Success -> RootDetailContent(
            root = state.root,
            forms = state.forms,
            onBack = onBack,
            onNavigateToWord = onNavigateToWord,
        )
    }
}

@Composable
private fun RootDetailContent(
    root: Root,
    forms: List<Word>,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
    ) {
        item {
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
        }

        item {
            RootHero(root = root, formCount = forms.size)
        }

        root.analysis?.takeIf { it.isNotBlank() }?.let { analysis ->
            item {
                SemanticNoteCard(analysis = analysis)
            }
        }

        item {
            Text(
                "DERIVATION TREE",
                style = Typography.labelSmall.copy(letterSpacing = 2.sp),
                modifier = Modifier.padding(start = 22.dp, top = 18.dp, bottom = 8.dp),
                color = QalamInk2,
            )
        }

        if (forms.isEmpty()) {
            item {
                Text(
                    "No linked words yet.",
                    style = Typography.bodyMedium,
                    color = QalamInk2,
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                )
            }
        } else {
            items(forms, key = { it.id }) { form ->
                DerivationRow(word = form, onClick = { onNavigateToWord(form.id) })
            }
        }
    }
}

@Composable
private fun RootHero(root: Root, formCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = QalamGoldC),
    ) {
        SelectionContainer {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(QalamGoldC, QalamGoldC.copy(alpha = 0.45f)),
                        ),
                    )
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = root.displayForm,
                        style = TextStyle(
                            fontFamily = Amiri,
                            fontSize = 62.sp,
                            lineHeight = 72.sp,
                            letterSpacing = 10.sp,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = QalamInk,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    root.normalizedForm,
                    style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 18.sp),
                    color = QalamGold,
                )
                root.meaning?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = Typography.titleMedium, color = QalamInk, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(10.dp))
                Text("$formCount linked forms", style = Typography.labelLarge, color = QalamInk2)
            }
        }
    }
}

@Composable
private fun SemanticNoteCard(analysis: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = QalamSurface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = QalamGold)
            Column {
                Text("Semantic note", style = Typography.labelMedium, color = QalamGold)
                Spacer(Modifier.height(6.dp))
                MarkdownText(
                    markdown = analysis,
                    paragraphStyle = TextStyle(fontFamily = NewsReader, fontSize = 16.5.sp, lineHeight = 25.sp),
                )
            }
        }
    }
}

@Composable
private fun DerivationRow(word: Word, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimelineDot()
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = word.arabicText,
                    style = TextStyle(fontFamily = Amiri, fontSize = 30.sp, lineHeight = 38.sp),
                    color = QalamInk,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            word.transliteration?.let {
                Text(
                    it,
                    style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 15.sp),
                    color = QalamGold,
                )
            }
            word.translation?.let {
                Text(it, style = Typography.bodyMedium, color = QalamInk2)
            }
        }
        Icon(
            Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = QalamInk2,
            modifier = Modifier.alpha(0.62f),
        )
    }
}

@Composable
private fun TimelineDot() {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(QalamGold, CircleShape),
    )
}
