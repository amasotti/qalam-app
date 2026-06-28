package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.tonihacks.qalam.domain.model.WordDraft
import com.tonihacks.qalam.ui.theme.QalamPaper
import com.tonihacks.qalam.ui.theme.Typography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWordSheet(
    onDismiss: () -> Unit,
    onSave: (WordDraft) -> Unit,
) {
    var arabic by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var transliteration by remember { mutableStateOf("") }

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

            val AmiriFamily = null
            OutlinedTextField(
                value = arabic,
                onValueChange = { arabic = it },
                label = { Text("Arabic *") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    textDirection = androidx.compose.ui.text.style.TextDirection.Rtl,
                    fontFamily = AmiriFamily,       // from Type.kt
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

            Button(
                onClick = {
                    onSave(
                        WordDraft(
                            arabicText = arabic,
                            translation = translation,
                            transliteration = transliteration.ifBlank { null },
                        )
                    )
                },
                enabled = arabic.isNotBlank() && translation.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Save")
            }
        }
    }
}