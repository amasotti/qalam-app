package com.tonihacks.qalam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tonihacks.qalam.domain.model.WordListSummary
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamInk3
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamPrimaryC
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerraC
import com.tonihacks.qalam.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScopePicker(
    lists: List<WordListSummary>,
    selectedIds: Set<String>,
    isLoading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    onSelectedIdsChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSheetOpen by remember { mutableStateOf(false) }
    val selectedLists = lists.filter { it.id in selectedIds }
    val totalSelectedWords = selectedLists.sumOf { it.itemCount }
    val title = if (selectedIds.isEmpty()) "All vocabulary" else "${selectedIds.size} list${plural(selectedIds.size)}"
    val description = if (selectedIds.isEmpty()) {
        "Using the whole practice pool"
    } else {
        "$totalSelectedWords word${plural(totalSelectedWords)} selected"
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(QalamSurface, RoundedCornerShape(8.dp))
                .border(1.dp, QalamOutline, RoundedCornerShape(8.dp))
                .clickable(enabled = !isLoading && error == null, onClick = { isSheetOpen = true })
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = Typography.titleMedium, color = QalamInk)
                Spacer(Modifier.height(2.dp))
                Text(description, style = Typography.bodySmall, color = QalamInk2)
            }
            Text("Change", style = Typography.labelLarge, color = QalamPrimary)
        }

        when {
            isLoading -> Text("Loading lists...", style = Typography.bodySmall, color = QalamInk2)
            error != null -> Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QalamTerraC, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(error, modifier = Modifier.weight(1f), style = Typography.bodySmall, color = QalamInk)
                OutlinedButton(onClick = onRetry) { Text("Retry", style = Typography.labelLarge) }
            }
            lists.isEmpty() -> Text("No word lists yet.", style = Typography.bodySmall, color = QalamInk2)
        }
    }

    if (isSheetOpen) {
        WordListScopeSheet(
            lists = lists,
            selectedIds = selectedIds,
            onDismiss = { isSheetOpen = false },
            onSelectedIdsChange = onSelectedIdsChange,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WordListScopeSheet(
    lists: List<WordListSummary>,
    selectedIds: Set<String>,
    onDismiss: () -> Unit,
    onSelectedIdsChange: (Set<String>) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filteredLists = remember(lists, query) {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isEmpty()) {
            lists
        } else {
            lists.filter { list ->
                list.title.contains(normalizedQuery, ignoreCase = true) ||
                    list.description.orEmpty().contains(normalizedQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = QalamSurface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Practice scope", style = Typography.headlineSmall, color = QalamInk)
                TextButton(onClick = onDismiss) {
                    Text("Done", style = Typography.labelLarge, color = QalamPrimary)
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = QalamInk2) },
                placeholder = { Text("Search word lists", style = Typography.bodyMedium, color = QalamInk3) },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            ScopeSheetRow(
                title = "All vocabulary",
                description = "Use every available word",
                selected = selectedIds.isEmpty(),
                enabled = true,
                onClick = { onSelectedIdsChange(emptySet()) },
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(filteredLists, key = { it.id }) { list ->
                    ScopeSheetRow(
                        title = list.title,
                        description = "${list.itemCount} word${plural(list.itemCount)}",
                        selected = list.id in selectedIds,
                        enabled = list.itemCount > 0,
                        onClick = {
                            val nextIds = if (list.id in selectedIds) {
                                selectedIds - list.id
                            } else {
                                selectedIds + list.id
                            }
                            onSelectedIdsChange(nextIds)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeSheetRow(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val container = when {
        !enabled -> QalamSurface2
        selected -> QalamPrimaryC
        else -> QalamSurface
    }
    val border = if (selected) QalamPrimary else QalamOutline
    val iconTint = when {
        selected -> QalamPrimary
        enabled -> QalamInk3
        else -> QalamInk3
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(8.dp))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = iconTint,
        )
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = Typography.titleMedium,
                color = if (enabled) QalamInk else QalamInk3,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
            Text(description, style = Typography.bodySmall, color = if (enabled) QalamInk2 else QalamInk3)
        }
    }
}

private fun plural(count: Int): String = if (count == 1) "" else "s"
