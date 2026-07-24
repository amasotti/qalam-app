package com.tonihacks.qalam.ui.productionpractice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.ProductionPracticeWord
import com.tonihacks.qalam.ui.components.MarkdownText
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamBg
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamOnPrimary
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamPrimaryC
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun ProductionPracticeRoute(
    onClose: () -> Unit,
    viewModel: ProductionPracticeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductionPracticeScreen(
        state = state,
        onClose = onClose,
        onReloadPrompt = viewModel::loadPrompt,
        onSentenceChange = viewModel::updateSentence,
        onToggleUsedWord = viewModel::toggleUsedWord,
        onSubmit = viewModel::submit,
    )
}

@Composable
private fun ProductionPracticeScreen(
    state: ProductionPracticeUiState,
    onClose: () -> Unit,
    onReloadPrompt: () -> Unit,
    onSentenceChange: (String) -> Unit,
    onToggleUsedWord: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QalamBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Header(onClose)
        when {
            state.isLoadingPrompt && state.prompt == null -> Loading()
            state.prompt == null -> PromptError(state.error, onReloadPrompt)
            else -> PracticeContent(
                state = state,
                onReloadPrompt = onReloadPrompt,
                onSentenceChange = onSentenceChange,
                onToggleUsedWord = onToggleUsedWord,
                onSubmit = onSubmit,
            )
        }
    }
}

@Composable
private fun Header(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Text("Sentence practice", style = Typography.headlineMedium, color = QalamInk)
        IconButton(modifier = Modifier.align(Alignment.CenterEnd), onClick = onClose) {
            Icon(Icons.Outlined.Close, contentDescription = "Close sentence practice", tint = QalamInk)
        }
    }
}

@Composable
private fun Loading() {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = QalamPrimary)
    }
}

@Composable
private fun PromptError(error: String?, onReloadPrompt: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(error ?: "Could not load a sentence prompt", color = QalamTerra, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onReloadPrompt) { Text("Try again") }
    }
}

@Composable
private fun PracticeContent(
    state: ProductionPracticeUiState,
    onReloadPrompt: () -> Unit,
    onSentenceChange: (String) -> Unit,
    onToggleUsedWord: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val prompt = state.prompt ?: return
    Spacer(Modifier.height(16.dp))
    Text("Write one Arabic sentence using at least two targets.", style = Typography.bodyMedium, color = QalamInk2)
    Spacer(Modifier.height(20.dp))
    Text("Target words", style = Typography.labelLarge, color = QalamInk2)
    Spacer(Modifier.height(8.dp))
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        prompt.words.forEach { word ->
            TargetWordCard(
                word = word,
                selected = word.id in state.usedWordIds,
                onClick = { onToggleUsedWord(word.id) },
            )
        }
    }
    Spacer(Modifier.height(20.dp))
    Text("Your sentence", style = Typography.labelLarge, color = QalamInk2)
    Spacer(Modifier.height(8.dp))
    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        TextField(
            value = state.sentence,
            onValueChange = onSentenceChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = Typography.bodyLarge.copy(fontFamily = NotoNaskh, fontSize = 24.sp, textAlign = TextAlign.Start),
            placeholder = { Text("اكتب جملة بالعربية", fontFamily = NotoNaskh, fontSize = 24.sp) },
            minLines = 4,
        )
    }
    if (state.error != null) {
        Spacer(Modifier.height(10.dp))
        Text(state.error, style = Typography.bodySmall, color = QalamTerra)
    }
    if (state.isAiUnavailable) {
        Spacer(Modifier.height(10.dp))
        Text("AI feedback is unavailable right now.", style = Typography.bodySmall, color = QalamTerra)
    }
    Spacer(Modifier.height(18.dp))
    Button(
        onClick = onSubmit,
        enabled = !state.isReviewing,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = QalamPrimary, contentColor = QalamOnPrimary),
    ) {
        Text(if (state.isReviewing) "Reviewing…" else "Get AI feedback", style = Typography.labelLarge)
    }
    Spacer(Modifier.height(12.dp))
    OutlinedButton(onClick = onReloadPrompt, enabled = !state.isLoadingPrompt, modifier = Modifier.fillMaxWidth()) {
        Text("New words", style = Typography.labelLarge)
    }
    state.reviewMarkdown?.let { markdown ->
        Spacer(Modifier.height(24.dp))
        Text("Feedback", style = Typography.headlineSmall, color = QalamInk)
        Spacer(Modifier.height(10.dp))
        MarkdownText(markdown = markdown)
    }
}

@Composable
private fun TargetWordCard(
    word: ProductionPracticeWord,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (selected) QalamPrimary else QalamOutline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = if (selected) QalamPrimaryC else QalamSurface),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(12.dp)) {
            androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(word.arabicText, fontFamily = NotoNaskh, fontSize = 26.sp, color = QalamInk)
            }
            Text(
                listOfNotNull(word.translation, word.partOfSpeech.lowercase()).joinToString(" · "),
                style = Typography.bodySmall,
                color = QalamInk2,
            )
        }
    }
}
