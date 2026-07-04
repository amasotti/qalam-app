package com.tonihacks.qalam.ui.roots

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.RootListItem
import com.tonihacks.qalam.ui.theme.Amiri
import com.tonihacks.qalam.ui.theme.NewsReader
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun RootListScreen(
    onNavigateToRoot: (String) -> Unit,
    viewModel: RootListViewModel = hiltViewModel(),
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
            Text("Roots", style = Typography.displaySmall)
            Spacer(Modifier.width(8.dp))
            if (uiState.items.isNotEmpty()) {
                Text(
                    "${uiState.items.size}+ roots",
                    style = Typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    color = QalamInk2,
                )
            }
        }

        Text(
            "The skeleton of the language, where families of words share a shape and sense.",
            style = Typography.bodyMedium,
            color = QalamInk2,
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp),
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 20.dp),
        ) {
            items(uiState.items, key = { it.root.id }) { item ->
                RootRow(item = item, onClick = { onNavigateToRoot(item.root.id) })
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

@Composable
private fun RootRow(item: RootListItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                item.root.normalizedForm,
                style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 17.sp),
                color = QalamGold,
            )
            item.root.meaning?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = Typography.bodyMedium, color = QalamInk)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = item.formCount.toString(),
                style = Typography.headlineMedium,
                color = QalamGold,
                textAlign = TextAlign.End,
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = item.root.displayForm,
                    style = TextStyle(
                        fontFamily = Amiri,
                        fontSize = 38.sp,
                        lineHeight = 46.sp,
                        letterSpacing = 6.sp,
                    ),
                    color = QalamInk,
                )
            }
        }
    }
}
