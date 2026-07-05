package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDirection.Companion.Rtl
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonihacks.qalam.domain.model.Dialect
import com.tonihacks.qalam.domain.model.DictionaryLookupItem
import com.tonihacks.qalam.domain.model.PartOfSpeech
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.ui.theme.NewsReader
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamPaper
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordSheet(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (WordDraft) -> Unit,
    initialArabic: String = "",
    initialTranslation: String = "",
    initialTransliteration: String = "",
    initialDialect: String = "MSA",
    lookupItems: List<DictionaryLookupItem> = emptyList(),
    isLookingUp: Boolean = false,
    lookupError: String? = null,
    onLookup: ((String) -> Unit)? = null,
) {
    var arabic by remember { mutableStateOf(initialArabic) }
    var translation by remember { mutableStateOf(initialTranslation) }
    var transliteration by remember { mutableStateOf(initialTransliteration) }
    var partOfSpeech by remember { mutableStateOf("UNKNOWN") }
    var dialect by remember { mutableStateOf(initialDialect) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = QalamPaper,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Add word", style = Typography.titleLarge)

            OutlinedTextField(
                value = arabic,
                onValueChange = { arabic = it },
                label = { Text("Arabic *") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    textDirection = Rtl,
                    fontFamily = NotoNaskh,
                    fontSize = 24.sp,
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                singleLine = true,
            )

            if (onLookup != null) {
                OutlinedButton(
                    onClick = { onLookup(arabic) },
                    enabled = arabic.isNotBlank() && !isLookingUp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    if (isLookingUp) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = QalamLapis)
                    } else {
                        Text("Lookup in ASD")
                    }
                }
            }

            if (lookupItems.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Suggestions",
                        style = Typography.labelSmall,
                        color = QalamInk2,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                    lookupItems.take(5).forEach { item ->
                        LookupResultRow(
                            item = item,
                            onClick = {
                                arabic = item.arabicText
                                translation = item.translation.orEmpty()
                                transliteration = item.transliteration.orEmpty()
                            },
                        )
                        HorizontalDivider(color = QalamSurface2)
                    }
                }
            }

            lookupError?.let {
                Text(it, color = QalamTerra, style = Typography.bodySmall)
            }

            OutlinedTextField(
                value = translation,
                onValueChange = { translation = it },
                label = { Text("Translation *") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                singleLine = true,
            )

            OutlinedTextField(
                value = transliteration,
                onValueChange = { transliteration = it },
                label = { Text("Transliteration") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done,
                ),
                singleLine = true,
            )

            OptionDropdown(
                label = "Part of speech",
                value = partOfSpeech,
                options = PartOfSpeech.entries.map { it.name },
                onValueChange = { partOfSpeech = it },
            )

            OptionDropdown(
                label = "Dialect",
                value = dialect,
                options = Dialect.entries.map { it.name },
                onValueChange = { dialect = it },
            )

            errorMessage?.let {
                Text(
                    text = it,
                    color = QalamTerra,
                    style = Typography.bodySmall,
                )
            }

            Button(
                onClick = {
                    onSave(
                        WordDraft(
                            arabicText = arabic.trim(),
                            translation = translation.trim(),
                            transliteration = transliteration.trim().ifBlank { null },
                            partOfSpeech = partOfSpeech,
                            dialect = dialect,
                        )
                    )
                },
                enabled = !isSaving && arabic.isNotBlank() && translation.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = QalamPaper,
                    )
                } else {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun LookupResultRow(item: DictionaryLookupItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = item.arabicText,
                style = TextStyle(fontFamily = NotoNaskh, fontSize = 22.sp),
                color = if (item.hasExactWordMatch) QalamPrimary else QalamLapis,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            item.transliteration?.let {
                Text(it, style = TextStyle(fontFamily = NewsReader, fontStyle = FontStyle.Italic, fontSize = 13.sp), color = QalamInk2)
            }
            item.translation?.let {
                Text(it, style = Typography.bodySmall)
            }
            item.pluralArabic?.let {
                Text(
                    "pl. $it",
                    style = TextStyle(fontFamily = NotoNaskh, fontSize = 14.sp),
                    color = QalamInk2,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionDropdown(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth(),
            singleLine = true,
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
