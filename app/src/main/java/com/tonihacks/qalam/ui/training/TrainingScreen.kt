package com.tonihacks.qalam.ui.training

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.domain.model.TrainingWord
import com.tonihacks.qalam.domain.model.TrainingWordRelation
import com.tonihacks.qalam.ui.theme.Amiri
import com.tonihacks.qalam.ui.theme.QalamBg
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamInk3
import com.tonihacks.qalam.ui.theme.QalamOnPrimary
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamPrimaryC
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.QalamSurface3
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.QalamTerraC
import com.tonihacks.qalam.ui.theme.Typography
import kotlin.math.roundToInt

@Composable
fun TrainingRoute(
    onClose: () -> Unit,
    viewModel: TrainingViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (state.session == null) viewModel.startSession()
    }

    LaunchedEffect(state.isComplete) {
        if (state.isComplete && state.summary == null) {
            viewModel.completeSession()
        }
    }

    TrainingScreen(
        state = state,
        onClose = onClose,
        onReveal = viewModel::revealAnswer,
        onGrade = viewModel::submitCurrentResult,
    )
}

@Composable
fun TrainingScreen(
    state: TrainingUiState,
    onClose: () -> Unit,
    onReveal: () -> Unit,
    onGrade: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QalamBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        when {
            state.isLoading -> TrainingLoading()
            state.error != null && state.session == null -> TrainingError(state.error, onClose)
            state.session == null -> TrainingLoading()
            state.session.words.isEmpty() -> TrainingEmpty(onClose)
            state.isComplete -> TrainingComplete(state, onClose)
            else -> TrainingDeck(state, onClose, onReveal, onGrade)
        }
    }
}

@Composable
private fun TrainingDeck(
    state: TrainingUiState,
    onClose: () -> Unit,
    onReveal: () -> Unit,
    onGrade: (Boolean) -> Unit,
) {
    val session = state.session ?: return
    val word = state.currentWord ?: return
    val total = session.words.size
    val progress = ((state.currentIndex + 1).toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        TrainingTopBar(
            current = state.currentIndex + 1,
            total = total,
            progress = progress,
            onClose = onClose,
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            NextCardPeek()
            TrainingCard(
                word = word,
                isRevealed = state.isRevealed,
                onReveal = onReveal,
                onGrade = onGrade,
            )
        }

        TrainingControls(
            isRevealed = state.isRevealed,
            onReveal = onReveal,
            onGrade = onGrade,
        )
    }
}

@Composable
private fun TrainingTopBar(
    current: Int,
    total: Int,
    progress: Float,
    onClose: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Outlined.Close, contentDescription = "Close training", tint = QalamInk)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = QalamPrimary,
            trackColor = QalamSurface3,
        )
        Text(
            "$current / $total",
            style = Typography.labelLarge,
            color = QalamInk2,
        )
    }
}

@Composable
private fun NextCardPeek() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .offset(y = 20.dp)
            .graphicsLayer {
                scaleX = 0.93f
                scaleY = 0.93f
            },
        colors = CardDefaults.cardColors(containerColor = QalamSurface2),
        shape = RoundedCornerShape(8.dp),
    ) {}
}

@Composable
private fun TrainingCard(
    word: TrainingWord,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onGrade: (Boolean) -> Unit,
) {
    var dragX by remember(word.id) { mutableFloatStateOf(0f) }
    val animatedDragX by animateFloatAsState(targetValue = dragX, label = "card_drag_x")
    val thresholdPx = with(LocalDensity.current) { 90.dp.toPx() }
    val knewOpacity = (animatedDragX / thresholdPx).coerceIn(0f, 1f)
    val againOpacity = (-animatedDragX / thresholdPx).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(430.dp)
            .graphicsLayer {
                translationX = animatedDragX
                rotationZ = animatedDragX * 0.04f
            }
            .pointerInput(word.id, isRevealed) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragX += dragAmount.x
                    },
                    onDragEnd = {
                        when {
                            dragX > thresholdPx -> onGrade(true)
                            dragX < -thresholdPx -> onGrade(false)
                        }
                        dragX = 0f
                    },
                    onDragCancel = { dragX = 0f },
                )
            },
        colors = CardDefaults.cardColors(containerColor = QalamSurface),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            SwipeBadge(
                text = "KNEW IT",
                color = QalamPrimary,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(18.dp)
                    .alpha(knewOpacity),
            )
            SwipeBadge(
                text = "AGAIN",
                color = QalamTerra,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(18.dp)
                    .alpha(againOpacity),
            )
            TrainingCardContent(
                word = word,
                isRevealed = isRevealed,
                onReveal = onReveal,
            )
        }
    }
}

@Composable
private fun SwipeBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier
            .border(2.dp, color, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = Typography.labelLarge,
        color = color,
    )
}

@Composable
private fun TrainingCardContent(
    word: TrainingWord,
    isRevealed: Boolean,
    onReveal: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = word.arabicText,
                modifier = Modifier.fillMaxWidth(),
                style = Typography.displaySmall.copy(
                    fontFamily = Amiri,
                    fontSize = 42.sp,
                    lineHeight = 56.sp,
                ),
                color = QalamInk,
                textAlign = TextAlign.Center,
            )
        }

        if (!isRevealed) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Tap show answer",
                style = Typography.bodySmall,
                color = QalamInk3,
                textAlign = TextAlign.Center,
            )
            return@Column
        }

        Spacer(Modifier.height(22.dp))
        HorizontalDivider(color = QalamOutline)
        Spacer(Modifier.height(22.dp))

        word.transliteration?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = Typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                color = QalamInk2,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
        }

        word.translation?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = it,
                style = Typography.titleLarge,
                color = QalamInk,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
        }

        CardMetaRow(word)
        ExampleList(word.examples)
        RelationList(word.relations)
    }
}

@Composable
private fun CardMetaRow(word: TrainingWord) {
    val chips = buildList {
        word.root?.takeIf { it.isNotBlank() }?.let { add("Root $it") }
        word.notes?.takeIf { it.isNotBlank() }?.let { add(it) }
        add(word.masteryLevel.name.lowercase().replaceFirstChar { it.uppercase() })
    }

    if (chips.isEmpty()) return

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        chips.take(2).forEach { label ->
            Text(
                text = label,
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .background(QalamSurface2, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                style = Typography.labelSmall,
                color = QalamInk2,
            )
        }
    }
}

@Composable
private fun ExampleList(examples: List<Example>) {
    if (examples.isEmpty()) return

    Spacer(Modifier.height(18.dp))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        examples.take(2).forEach { example ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(QalamBg, RoundedCornerShape(8.dp))
                    .padding(12.dp),
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = example.arabicText,
                        modifier = Modifier.fillMaxWidth(),
                        style = Typography.bodyLarge.copy(fontFamily = Amiri, fontSize = 24.sp),
                        color = QalamInk,
                        textAlign = TextAlign.Start,
                    )
                }
                example.translation?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = Typography.bodySmall, color = QalamInk2)
                }
            }
        }
    }
}

@Composable
private fun RelationList(relations: List<TrainingWordRelation>) {
    if (relations.isEmpty()) return

    Spacer(Modifier.height(14.dp))
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        relations.take(4).forEach { relation ->
            Text(
                text = "${relation.relationType.lowercase()}: ${relation.relatedWordArabic}",
                style = Typography.bodySmall,
                color = QalamInk2,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TrainingControls(
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onGrade: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "Swipe right if you knew it · left to review again",
            style = Typography.bodySmall,
            color = QalamInk3,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        if (!isRevealed) {
            Button(
                onClick = onReveal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = QalamPrimary,
                    contentColor = QalamOnPrimary,
                ),
                contentPadding = PaddingValues(vertical = 14.dp),
            ) {
                Text("Show answer", style = Typography.labelLarge)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { onGrade(false) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = QalamTerra),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Icon(Icons.Outlined.Replay, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Again", style = Typography.labelLarge)
                }
                Button(
                    onClick = { onGrade(true) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = QalamPrimary,
                        contentColor = QalamOnPrimary,
                    ),
                    contentPadding = PaddingValues(vertical = 14.dp),
                ) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Got it", style = Typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun TrainingLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = QalamPrimary)
    }
}

@Composable
private fun TrainingError(
    message: String,
    onClose: () -> Unit,
) {
    TrainingMessage(
        title = "Training unavailable",
        message = message,
        actionLabel = "Close",
        onAction = onClose,
    )
}

@Composable
private fun TrainingEmpty(onClose: () -> Unit) {
    TrainingMessage(
        title = "No cards ready",
        message = "There are no words available for this training session.",
        actionLabel = "Close",
        onAction = onClose,
    )
}

@Composable
private fun TrainingComplete(
    state: TrainingUiState,
    onClose: () -> Unit,
) {
    val summary = state.summary
    val message = if (summary == null) {
        "Saving your session..."
    } else {
        "${summary.correct} knew it · ${summary.incorrect} to review · ${summary.accuracy.roundToInt()}% accuracy"
    }

    TrainingMessage(
        title = "Session complete",
        message = message,
        actionLabel = "Done",
        onAction = onClose,
        showProgress = summary == null,
    )
}

@Composable
private fun TrainingMessage(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    showProgress: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(QalamPrimaryC, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (showProgress) {
                CircularProgressIndicator(color = QalamPrimary, modifier = Modifier.size(32.dp))
            } else {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = QalamPrimary)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text(title, style = Typography.headlineMedium, color = QalamInk, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(message, style = Typography.bodyLarge, color = QalamInk2, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAction,
            enabled = !showProgress,
            colors = ButtonDefaults.buttonColors(
                containerColor = QalamPrimary,
                contentColor = QalamOnPrimary,
            ),
        ) {
            Text(actionLabel, style = Typography.labelLarge)
        }
    }
}
