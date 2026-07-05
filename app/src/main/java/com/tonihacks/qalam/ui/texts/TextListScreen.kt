package com.tonihacks.qalam.ui.texts

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.Dialect
import com.tonihacks.qalam.domain.model.TextPassage
import com.tonihacks.qalam.ui.theme.NewsReader
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamLapisC
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextListScreen(
    onNavigateToText: (String) -> Unit,
    viewModel: TextListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val reachedBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisible != null && lastVisible.index >= uiState.items.size - 3
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom) viewModel.loadMore()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text("Texts", style = Typography.displaySmall)
            Spacer(Modifier.width(8.dp))
            if (uiState.items.isNotEmpty()) {
                Text(
                    "${uiState.items.size}+ passages",
                    style = Typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    color = QalamInk2,
                )
            }
        }

        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 20.dp),
            ) {
                items(uiState.items, key = { it.id }) { text ->
                    TextRow(text = text, onClick = { onNavigateToText(text.id) })
                    HorizontalDivider(color = QalamOutline, thickness = 0.5.dp)
                }
                if (uiState.isLoading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = QalamPrimary)
                        }
                    }
                }
                uiState.error?.let { err ->
                    item {
                        Text(
                            err,
                            color = QalamTerra,
                            modifier = Modifier.padding(20.dp),
                            style = Typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextRow(text: TextPassage, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
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

        Spacer(Modifier.height(10.dp))

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = text.arabicPreview(),
                style = TextStyle(fontFamily = NotoNaskh, fontSize = 30.sp, lineHeight = 40.sp),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text.title,
            style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 17.sp),
            color = QalamInk,
        )

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Outlined.AutoStories, contentDescription = null, tint = QalamPrimary)
            Text("Read interlinear", style = Typography.labelLarge, color = QalamPrimary)
        }
    }
}

// Helpers shared with TextReaderScreen — same package.

fun TextPassage.arabicPreview(): String = body.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()

fun dialectLabel(raw: String): String = runCatching { Dialect.valueOf(raw) }.getOrNull()?.let {
    when (it) {
        Dialect.MSA -> "MSA"
        Dialect.TUNISIAN -> "Tunisian"
        Dialect.MOROCCAN -> "Moroccan"
        Dialect.EGYPTIAN -> "Egyptian"
        Dialect.GULF -> "Gulf"
        Dialect.LEVANTINE -> "Levantine"
        Dialect.IRAQI -> "Iraqi"
    }
} ?: raw

fun difficultyLabel(raw: String): String = raw.lowercase().replaceFirstChar { it.uppercase() }
