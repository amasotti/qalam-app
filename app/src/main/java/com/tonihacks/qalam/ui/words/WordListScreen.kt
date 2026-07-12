package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    onNavigateToWord: (String) -> Unit,
    onNavigateToLists: () -> Unit,
    viewModel: WordListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showAddSheet by remember { mutableStateOf(false) }

    val reachedBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisible != null && lastVisible.index >= uiState.items.size - 3
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom) viewModel.loadMore()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text("Words", style = Typography.displaySmall)
                    Spacer(Modifier.width(8.dp))
                    if (uiState.items.isNotEmpty()) {
                        Text(
                            "${uiState.items.size}+ entries",
                            style = Typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = QalamInk2,
                        )
                    }
                }
                OutlinedButton(onClick = onNavigateToLists) {
                    Icon(Icons.AutoMirrored.Outlined.LibraryBooks, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Lists", style = Typography.labelLarge)
                }
            }

            OutlinedTextField(
                value = uiState.query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                placeholder = { Text("Search", color = QalamInk3) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )

            Spacer(Modifier.height(12.dp))

            val filters = listOf(
                null to "All",
                MasteryLevel.NEW to "Unseen",
                MasteryLevel.LEARNING to "Learning",
                MasteryLevel.KNOWN to "Reviewing",
                MasteryLevel.MASTERED to "Mastered",
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filters) { (level, label) ->
                    FilterChip(
                        selected = uiState.activeFilter == level,
                        onClick = { viewModel.onFilterChange(level) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = level.toQalamColor().copy(alpha = 0.2f),
                            selectedLabelColor = level.toQalamColor(),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    items(uiState.items, key = { it.id }) { word ->
                        WordRow(word = word, onClick = { onNavigateToWord(word.id) })
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

        // FAB — gold pill, bottom right (above bottom nav)
        ExtendedFloatingActionButton(
            onClick = { showAddSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp),
            containerColor = QalamGold,
            contentColor = QalamOnPrimary,
        ) {
            Text("+ Word")
        }
    }

    if (showAddSheet) {
        AddWordSheet(
            isSaving = uiState.isCreating,
            errorMessage = uiState.createWordError,
            lookupItems = uiState.lookupItems,
            isLookingUp = uiState.isLookingUp,
            lookupError = uiState.lookupError,
            onLookup = viewModel::lookupWord,
            duplicateCandidates = uiState.duplicateCandidates,
            isCheckingDuplicates = uiState.isCheckingDuplicates,
            onCheckDuplicates = viewModel::checkDuplicates,
            onSelectExisting = { existing ->
                showAddSheet = false
                viewModel.clearDuplicateCandidates()
                onNavigateToWord(existing.id)
            },
            onDismiss = {
                showAddSheet = false
                viewModel.clearLookup()
                viewModel.clearDuplicateCandidates()
            },
            onSave = { draft ->
                viewModel.createWord(draft) { showAddSheet = false }
            },
        )
    }
}

@Composable
private fun WordRow(word: Word, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(word.masteryLevel.toQalamColor(), CircleShape)
                )
                Text(
                    word.masteryLevel.label(),
                    style = Typography.labelSmall,
                    color = QalamInk2,
                )
                Text("·", color = QalamInk3)
                Text(word.partOfSpeech, style = Typography.labelSmall, color = QalamInk2)
            }
            word.transliteration?.let {
                Text(
                    it,
                    style = Typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = QalamInk2,
                )
            }
            word.translation?.let {
                Text(it, style = Typography.bodyMedium, color = QalamInk)
            }
            word.updatedAt?.toFormattedDate()?.let {
                Text(it, style = Typography.labelSmall, color = QalamInk3)
            }
        }
        // Arabic text right-aligned — Amiri large
        Text(
            text = word.arabicText,
            style = Typography.headlineLarge,
        )
    }
}

private val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

private fun String.toFormattedDate(): String? = runCatching {
    DATE_FMT.format(Instant.parse(this).atZone(ZoneId.systemDefault()).toLocalDate())
}.getOrNull()

// Helper extensions — keep near the composables that use them
fun MasteryLevel?.toQalamColor() = when (this) {
    MasteryLevel.NEW -> MasteryUnseen
    MasteryLevel.LEARNING -> MasteryLearning
    MasteryLevel.KNOWN -> MasteryReviewing
    MasteryLevel.MASTERED -> MasteryMastered
    null -> QalamInk3
}

fun MasteryLevel.label() = when (this) {
    MasteryLevel.NEW -> "Unseen"
    MasteryLevel.LEARNING -> "Learning"
    MasteryLevel.KNOWN -> "Reviewing"
    MasteryLevel.MASTERED -> "Mastered"
}
