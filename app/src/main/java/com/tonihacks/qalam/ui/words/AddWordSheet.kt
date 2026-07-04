package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection.Companion.Rtl
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonihacks.qalam.domain.model.Dialect
import com.tonihacks.qalam.domain.model.PartOfSpeech
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.ui.theme.Amiri
import com.tonihacks.qalam.ui.theme.QalamPaper
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
                    fontFamily = Amiri,
                    fontSize = 24.sp,
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next,
                ),
                singleLine = true,
            )

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
