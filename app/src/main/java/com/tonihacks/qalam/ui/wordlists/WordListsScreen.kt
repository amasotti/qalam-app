package com.tonihacks.qalam.ui.wordlists

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.RemoveCircleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.domain.model.WordAutocomplete
import com.tonihacks.qalam.domain.model.WordListSummary
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamBg
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamGoldC
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamInk3
import com.tonihacks.qalam.ui.theme.QalamOnPrimary
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamPrimaryC
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.QalamTerraC
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun WordListsScreen(
    onBack: () -> Unit,
    onNavigateToList: (String) -> Unit,
    viewModel: WordListsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateSheet by remember { mutableStateOf(false) }
    val loadError = state.error

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QalamBg),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = QalamInk)
                    }
                    Text("Word lists", style = Typography.displaySmall, color = QalamInk)
                    Spacer(Modifier.height(48.dp))
                }
            }

            when {
                state.isLoading -> item { LoadingBlock() }
                loadError != null -> item { ErrorBlock(loadError, onRetry = viewModel::load) }
                state.lists.isEmpty() -> item { EmptyLists(onCreate = { showCreateSheet = true }) }
                else -> items(state.lists, key = { it.id }) { list ->
                    WordListCard(list = list, onClick = { onNavigateToList(list.id) })
                }
            }
        }

        ExtendedFloatingActionButton(
            onClick = { showCreateSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp),
            containerColor = QalamGold,
            contentColor = QalamOnPrimary,
            icon = { Icon(Icons.Outlined.Add, contentDescription = null) },
            text = { Text("List", style = Typography.labelLarge) },
        )
    }

    if (showCreateSheet) {
        CreateListSheet(
            isSaving = state.isCreating,
            error = state.createError,
            onDismiss = { showCreateSheet = false },
            onCreate = { title, description ->
                viewModel.createList(title, description) { id ->
                    showCreateSheet = false
                    onNavigateToList(id)
                }
            },
        )
    }
}

@Composable
fun WordListDetailScreen(
    listId: String,
    onBack: () -> Unit,
    onNavigateToWord: (String) -> Unit,
    viewModel: WordListDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSuggestSheet by remember { mutableStateOf(false) }
    var showAddWordSheet by remember { mutableStateOf(false) }
    val loadError = state.error
    val detail = state.detail

    LaunchedEffect(listId) {
        viewModel.load(listId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QalamBg),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = QalamInk)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showAddWordSheet = true },
                            enabled = state.detail != null,
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Text("Add", style = Typography.labelLarge)
                        }
                        Button(
                            onClick = {
                                showSuggestSheet = true
                                viewModel.suggestWords()
                            },
                            enabled = state.detail != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = QalamPrimary,
                                contentColor = QalamOnPrimary,
                            ),
                        ) {
                            Icon(Icons.Outlined.AutoAwesome, contentDescription = null)
                            Text("Suggest", style = Typography.labelLarge)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            when {
                state.isLoading -> item { LoadingBlock() }
                loadError != null -> item { ErrorBlock(loadError, onRetry = { viewModel.load(listId) }) }
                detail != null -> {
                    item {
                        Text(detail.title, style = Typography.displaySmall, color = QalamInk)
                        detail.description?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = Typography.bodyLarge, color = QalamInk2)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "${detail.words.size} word${if (detail.words.size == 1) "" else "s"}",
                            style = Typography.labelLarge,
                            color = QalamInk2,
                        )
                        Spacer(Modifier.height(18.dp))
                    }

                    if (detail.words.isEmpty()) {
                        item { EmptyListDetail(onSuggest = { showSuggestSheet = true; viewModel.suggestWords() }) }
                    } else {
                        items(detail.words, key = { it.id }) { word ->
                            WordListWordRow(
                                word = word,
                                onClick = { onNavigateToWord(word.id) },
                                onRemove = { viewModel.removeWord(word.id) },
                            )
                            HorizontalDivider(color = QalamOutline, thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }

    if (showSuggestSheet) {
        SuggestWordsSheet(
            state = state,
            onDismiss = { showSuggestSheet = false },
            onRetry = viewModel::suggestWords,
            onAdd = viewModel::addSuggestion,
        )
    }

    if (showAddWordSheet) {
        AddExistingWordSheet(
            query = state.addQuery,
            results = state.addResults,
            isSearching = state.isSearchingWords,
            error = state.addWordError,
            onQueryChange = viewModel::searchWords,
            onAdd = { wordId ->
                viewModel.addExistingWord(wordId)
                showAddWordSheet = false
            },
            onDismiss = { showAddWordSheet = false },
        )
    }
}

@Composable
private fun WordListCard(list: WordListSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = QalamSurface),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(list.title, style = Typography.titleLarge, color = QalamInk)
                    list.description?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = Typography.bodySmall, color = QalamInk2)
                    }
                }
                Text(
                    "${list.itemCount}",
                    modifier = Modifier
                        .background(QalamPrimaryC, RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    style = Typography.labelLarge,
                    color = QalamPrimary,
                )
            }
        }
    }
}

@Composable
private fun WordListWordRow(
    word: Word,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(word.translation.orEmpty(), style = Typography.titleMedium, color = QalamInk)
            word.transliteration?.let {
                Text(
                    it,
                    style = Typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = QalamInk2,
                )
            }
        }
        Text(
            text = word.arabicText,
            modifier = Modifier.padding(horizontal = 12.dp),
            style = Typography.headlineMedium.copy(fontFamily = NotoNaskh, fontSize = 30.sp),
            color = QalamInk,
            textAlign = TextAlign.End,
        )
        IconButton(onClick = onRemove) {
            Icon(Icons.Outlined.RemoveCircleOutline, contentDescription = "Remove from list", tint = QalamTerra)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateListSheet(
    isSaving: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = QalamSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("New word list", style = Typography.headlineMedium, color = QalamInk)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Description") },
                minLines = 3,
                shape = RoundedCornerShape(16.dp),
            )
            error?.let { Text(it, style = Typography.bodySmall, color = QalamTerra) }
            Button(
                onClick = { onCreate(title, description) },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QalamPrimary,
                    contentColor = QalamOnPrimary,
                ),
            ) {
                Text(if (isSaving) "Creating..." else "Create list", style = Typography.labelLarge)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExistingWordSheet(
    query: String,
    results: List<WordAutocomplete>,
    isSearching: Boolean,
    error: String?,
    onQueryChange: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = QalamSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add existing word", style = Typography.headlineMedium, color = QalamInk)
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search Arabic or translation") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
            )
            when {
                isSearching -> LoadingBlock()
                error != null -> Text(error, style = Typography.bodySmall, color = QalamTerra)
                query.length < 2 -> Text("Type at least two characters.", style = Typography.bodySmall, color = QalamInk2)
                results.isEmpty() -> Text("No matching words outside this list.", style = Typography.bodySmall, color = QalamInk2)
                else -> results.forEach { word ->
                    WordSearchResultRow(word = word, onAdd = { onAdd(word.id) })
                }
            }
        }
    }
}

@Composable
private fun WordSearchResultRow(word: WordAutocomplete, onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QalamSurface2, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                word.arabicText,
                style = Typography.headlineMedium.copy(fontFamily = NotoNaskh, fontSize = 28.sp),
                color = QalamInk,
            )
            word.translation?.let { Text(it, style = Typography.bodySmall, color = QalamInk2) }
        }
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(
                containerColor = QalamPrimary,
                contentColor = QalamOnPrimary,
            ),
        ) {
            Text("Add", style = Typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuggestWordsSheet(
    state: WordListDetailUiState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onAdd: (Int) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = QalamSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("AI suggestions", style = Typography.headlineMedium, color = QalamInk)
            Text(
                "Candidates stay as previews until you add them.",
                style = Typography.bodySmall,
                color = QalamInk2,
            )

            when {
                state.isSuggesting -> LoadingBlock()
                state.isAiUnavailable -> Text(
                    "AI is not configured on the backend.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(QalamGoldC, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    style = Typography.bodyLarge,
                    color = QalamInk,
                )
                state.suggestionError != null -> ErrorBlock(state.suggestionError, onRetry)
                state.suggestions.isEmpty() -> Text("No suggestions yet.", style = Typography.bodyLarge, color = QalamInk2)
                else -> state.suggestions.forEachIndexed { index, item ->
                    SuggestionRow(index = index, item = item, onAdd = onAdd)
                }
            }

            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Done", style = Typography.labelLarge, color = QalamPrimary)
            }
        }
    }
}

@Composable
private fun SuggestionRow(index: Int, item: SuggestionUiItem, onAdd: (Int) -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = QalamSurface2),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    item.suggestion.arabicText,
                    style = Typography.headlineMedium.copy(fontFamily = NotoNaskh, fontSize = 30.sp),
                    color = QalamInk,
                )
                item.suggestion.translation?.let {
                    Text(it, style = Typography.titleMedium, color = QalamInk)
                }
                item.suggestion.transliteration?.let {
                    Text(it, style = Typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = QalamInk2)
                }
            }
            when (item.status) {
                SuggestionStatus.Checking -> CircularProgressIndicator(modifier = Modifier.padding(12.dp), color = QalamPrimary)
                SuggestionStatus.Adding -> Text("Adding...", style = Typography.labelLarge, color = QalamInk2)
                SuggestionStatus.Added -> Text("Added", style = Typography.labelLarge, color = QalamPrimary)
                SuggestionStatus.Error -> Text("Failed", style = Typography.labelLarge, color = QalamTerra)
                SuggestionStatus.Exists -> OutlinedButton(onClick = { onAdd(index) }) {
                    Text("Add", style = Typography.labelLarge)
                }
                SuggestionStatus.Missing -> Button(
                    onClick = { onAdd(index) },
                    enabled = !item.suggestion.translation.isNullOrBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QalamPrimary,
                        contentColor = QalamOnPrimary,
                    ),
                ) {
                    Text("Create", style = Typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun EmptyLists(onCreate: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = QalamSurface),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("No lists yet", style = Typography.headlineSmall, color = QalamInk)
            Text("Create a focused set like colors, family, or food, then train from that pool.", style = Typography.bodyLarge, color = QalamInk2)
            Button(onClick = onCreate, colors = ButtonDefaults.buttonColors(containerColor = QalamPrimary)) {
                Text("Create first list", color = QalamOnPrimary, style = Typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EmptyListDetail(onSuggest: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = QalamSurface),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("This list is empty", style = Typography.headlineSmall, color = QalamInk)
            Text("Use AI suggestions to seed a useful practice set.", style = Typography.bodyLarge, color = QalamInk2)
            Button(onClick = onSuggest, colors = ButtonDefaults.buttonColors(containerColor = QalamPrimary)) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, tint = QalamOnPrimary)
                Text("Suggest words", color = QalamOnPrimary, style = Typography.labelLarge)
            }
        }
    }
}

@Composable
private fun LoadingBlock() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = QalamPrimary)
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(QalamTerraC, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(message, style = Typography.bodyLarge, color = QalamInk)
        OutlinedButton(onClick = onRetry) {
            Text("Try again", style = Typography.labelLarge)
        }
    }
}
