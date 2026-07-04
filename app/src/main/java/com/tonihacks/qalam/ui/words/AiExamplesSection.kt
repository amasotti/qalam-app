package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonihacks.qalam.domain.model.AiExample
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun AiExamplesSection(
    aiExamples: List<AiExample>,
    isGenerating: Boolean,
    isSaving: Boolean,
    error: String?,
    unavailable: Boolean,
    onGenerate: () -> Unit,
    onUse: (AiExample) -> Unit,
    onDiscard: (AiExample) -> Unit,
    onDismiss: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 8.dp)) {
        when {
            isGenerating -> Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = QalamPrimary)
                Text("Generating examples…", style = Typography.bodyMedium, color = QalamInk2)
            }

            unavailable -> {
                Text(
                    "AI not configured — set OPENROUTER_API_KEY on the backend to enable this.",
                    style = Typography.bodySmall,
                    color = QalamInk2,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) { Text("Close") }
            }

            error != null -> {
                Text(error, style = Typography.bodySmall, color = QalamTerra)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(14.dp)) { Text("Close") }
                    OutlinedButton(onClick = onGenerate, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.size(6.dp))
                        Text("Try again")
                    }
                }
            }

            aiExamples.isNotEmpty() -> {
                aiExamples.forEach { ex ->
                    AiExampleCard(example = ex, enabled = !isSaving, onUse = { onUse(ex) }, onDiscard = { onDiscard(ex) })
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(onClick = onGenerate, enabled = !isGenerating, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Generate again")
                }
            }

            else -> OutlinedButton(onClick = onGenerate, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Outlined.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Generate examples")
            }
        }
    }
}

@Composable
private fun AiExampleCard(
    example: AiExample,
    enabled: Boolean,
    onUse: () -> Unit,
    onDiscard: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = QalamSurface2),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SelectionContainer {
                Column {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(example.arabic, style = Typography.titleLarge, modifier = Modifier.fillMaxWidth())
                    }
                    example.transliteration?.let {
                        Text(it, style = Typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = QalamInk2)
                    }
                    example.translation?.let {
                        Text(it, style = Typography.bodyMedium, color = QalamInk)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onUse,
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Use", fontSize = 14.sp)
                }
                OutlinedButton(
                    onClick = onDiscard,
                    enabled = enabled,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QalamInk2),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Discard", fontSize = 14.sp)
                }
            }
        }
    }
}
