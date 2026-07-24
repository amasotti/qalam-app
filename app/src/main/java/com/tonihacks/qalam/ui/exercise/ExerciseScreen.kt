package com.tonihacks.qalam.ui.exercise

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.domain.model.ExerciseAnswer
import com.tonihacks.qalam.domain.model.ExerciseItem
import com.tonihacks.qalam.domain.model.ExerciseOption
import com.tonihacks.qalam.domain.model.ExercisePromptKind
import com.tonihacks.qalam.domain.model.ExerciseResult
import com.tonihacks.qalam.domain.model.ExerciseSessionSummary
import com.tonihacks.qalam.domain.model.ExerciseType
import com.tonihacks.qalam.ui.components.WordListScopePicker
import com.tonihacks.qalam.ui.theme.NotoNaskh
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
fun ExerciseRoute(
    onClose: () -> Unit,
    viewModel: ExerciseViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete && state.summary == null) {
            viewModel.completeSession()
        }
    }

    ExerciseScreen(
        state = state,
        onClose = onClose,
        onStart = viewModel::startSession,
        onRetryWordLists = viewModel::loadWordLists,
        onAnswer = viewModel::answer,
        onNext = viewModel::nextItem,
        onPracticeAgain = viewModel::resetSession,
    )
}

@Composable
fun ExerciseScreen(
    state: ExerciseUiState,
    onClose: () -> Unit,
    onStart: (String, Int, List<String>, List<ExerciseType>) -> Unit,
    onRetryWordLists: () -> Unit,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
    onPracticeAgain: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(QalamBg)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        when {
            state.isLoading -> ExerciseLoading()
            state.error != null && state.session == null -> ExerciseError(state.error, onClose)
            state.session == null -> ExerciseSetupScreen(
                state = state,
                onClose = onClose,
                onStart = onStart,
                onRetryWordLists = onRetryWordLists,
            )
            state.session.items.isEmpty() -> ExerciseEmpty(onClose)
            state.isComplete -> ExerciseComplete(state.summary, state.answers, onClose, onPracticeAgain)
            else -> ExercisePractice(state, onClose, onAnswer, onNext)
        }
    }
}

private enum class ExerciseModeOption(
    val value: String,
    val title: String,
    val description: String,
) {
    Mixed("MIXED", "Mixed", "Blend the queue"),
    New("NEW", "New", "Fresh words"),
    Learning("LEARNING", "Learning", "Build recall"),
    Known("KNOWN", "Known", "Keep them honest"),
}

private val ExerciseSizeOptions = listOf(5, 10, 15, 20)

private enum class ExerciseTypeOption(
    val type: ExerciseType,
    val title: String,
    val description: String,
) {
    Meaning(
        ExerciseType.MULTIPLE_CHOICE_MEANING,
        "Arabic → meaning",
        "Read Arabic, choose its meaning",
    ),
    Arabic(
        ExerciseType.MULTIPLE_CHOICE_ARABIC,
        "Meaning → Arabic",
        "Read meaning, choose Arabic",
    ),
    ConfusableMeaning(
        ExerciseType.CONFUSABLE_MEANING,
        "Confusable meanings",
        "Arabic with deliberately similar meanings",
    ),
    ConfusableArabic(
        ExerciseType.CONFUSABLE_ARABIC,
        "Confusable Arabic",
        "Meaning with deliberately similar Arabic words",
    ),
}

@Composable
private fun ExerciseSetupScreen(
    state: ExerciseUiState,
    onClose: () -> Unit,
    onStart: (String, Int, List<String>, List<ExerciseType>) -> Unit,
    onRetryWordLists: () -> Unit,
) {
    var selectedMode by remember { mutableStateOf(ExerciseModeOption.Mixed) }
    var selectedSize by remember { mutableStateOf(10) }
    var selectedWordListIds by remember { mutableStateOf(emptySet<String>()) }
    var selectedExerciseTypes by remember {
        mutableStateOf(setOf(ExerciseType.MULTIPLE_CHOICE_MEANING))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        TopTitleBar(title = "Exercises", onClose = onClose)

        Spacer(Modifier.height(28.dp))
        Text("Scope", style = Typography.labelLarge, color = QalamInk2)
        Spacer(Modifier.height(10.dp))
        WordListScopePicker(
            lists = state.wordLists,
            selectedIds = selectedWordListIds,
            isLoading = state.isLoadingWordLists,
            error = state.wordListError,
            onRetry = onRetryWordLists,
            onSelectedIdsChange = { selectedWordListIds = it },
        )

        Spacer(Modifier.height(28.dp))
        Text("Mode", style = Typography.labelLarge, color = QalamInk2)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ExerciseModeOption.entries.forEach { mode ->
                SetupOptionButton(
                    title = mode.title,
                    description = mode.description,
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Question style", style = Typography.labelLarge, color = QalamInk2)
        Spacer(Modifier.height(4.dp))
        Text("Choose one or more. Selected styles alternate.", style = Typography.bodySmall, color = QalamInk2)
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ExerciseTypeOption.entries.forEach { option ->
                SetupOptionButton(
                    title = option.title,
                    description = option.description,
                    selected = option.type in selectedExerciseTypes,
                    onClick = {
                        selectedExerciseTypes = when {
                            option.type !in selectedExerciseTypes -> selectedExerciseTypes + option.type
                            selectedExerciseTypes.size > 1 -> selectedExerciseTypes - option.type
                            else -> selectedExerciseTypes
                        }
                    },
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        Text("Questions", style = Typography.labelLarge, color = QalamInk2)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ExerciseSizeOptions.forEach { size ->
                SizeButton(
                    size = size,
                    selected = selectedSize == size,
                    onClick = { selectedSize = size },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Button(
            onClick = {
                onStart(
                    selectedMode.value,
                    selectedSize,
                    selectedWordListIds.toList(),
                    ExerciseTypeOption.entries
                        .filter { it.type in selectedExerciseTypes }
                        .map(ExerciseTypeOption::type),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = QalamPrimary,
                contentColor = QalamOnPrimary,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text("Start ${selectedSize}-question exercise", style = Typography.labelLarge)
        }
    }
}

@Composable
private fun ExercisePractice(
    state: ExerciseUiState,
    onClose: () -> Unit,
    onAnswer: (String) -> Unit,
    onNext: () -> Unit,
) {
    val session = state.session ?: return
    val item = state.currentItem ?: return
    val total = session.items.size
    val progress = ((state.currentIndex + 1).toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        ExerciseTopBar(state.currentIndex + 1, total, progress, onClose)
        ExerciseQuestionCard(
            item = item,
            selectedOptionId = state.selectedOptionId,
            answerFeedback = state.answerFeedback,
            error = state.error,
            onAnswer = onAnswer,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp),
        )
        Button(
            onClick = onNext,
            enabled = state.answerFeedback != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = QalamPrimary,
                contentColor = QalamOnPrimary,
                disabledContainerColor = QalamSurface3,
                disabledContentColor = QalamInk3,
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
        ) {
            Text(if (state.currentIndex == total - 1) "Finish exercise" else "Next question")
        }
    }
}

@Composable
private fun ExerciseQuestionCard(
    item: ExerciseItem,
    selectedOptionId: String?,
    answerFeedback: ExerciseAnswer?,
    error: String?,
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(QalamSurface, RoundedCornerShape(8.dp))
            .border(1.dp, QalamOutline, RoundedCornerShape(8.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = if (item.prompt.kind == ExercisePromptKind.ARABIC_WORD) {
                "Choose the meaning"
            } else {
                "Choose the Arabic"
            },
            style = Typography.labelLarge,
            color = QalamInk2,
        )
        Spacer(Modifier.height(18.dp))
        PromptText(item)
        Spacer(Modifier.height(24.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item.options.forEach { option ->
                ExerciseOptionButton(
                    option = option,
                    promptKind = item.prompt.kind,
                    selectedOptionId = selectedOptionId,
                    answerFeedback = answerFeedback,
                    onClick = { onAnswer(option.id) },
                )
            }
        }
        if (answerFeedback != null) {
            Spacer(Modifier.height(18.dp))
            AnswerFeedback(answerFeedback)
        }
        if (error != null) {
            Spacer(Modifier.height(14.dp))
            Text(error, style = Typography.bodySmall, color = QalamTerra, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PromptText(item: ExerciseItem) {
    if (item.prompt.kind == ExercisePromptKind.ARABIC_WORD) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Text(
                text = item.prompt.text,
                modifier = Modifier.fillMaxWidth(),
                fontFamily = NotoNaskh,
                fontSize = 46.sp,
                lineHeight = 56.sp,
                color = QalamInk,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        Text(
            text = item.prompt.text,
            modifier = Modifier.fillMaxWidth(),
            style = Typography.headlineMedium,
            color = QalamInk,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ExerciseOptionButton(
    option: ExerciseOption,
    promptKind: ExercisePromptKind,
    selectedOptionId: String?,
    answerFeedback: ExerciseAnswer?,
    onClick: () -> Unit,
) {
    val isAnswered = answerFeedback != null
    val isSelected = selectedOptionId == option.id
    val isCorrect = answerFeedback?.correctOptionId == option.id
    val container = when {
        isCorrect -> QalamPrimaryC
        isAnswered && isSelected -> QalamTerraC
        isSelected -> QalamSurface2
        else -> QalamSurface
    }
    val border = when {
        isCorrect -> QalamPrimary
        isAnswered && isSelected -> QalamTerra
        isSelected -> QalamInk3
        else -> QalamOutline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(8.dp))
            .border(1.dp, border, RoundedCornerShape(8.dp))
            .clickable(enabled = !isAnswered && selectedOptionId == null, onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = if (isCorrect) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = border,
        )
        Column(Modifier.weight(1f)) {
            if (promptKind == ExercisePromptKind.TRANSLATION) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = option.arabicText,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = NotoNaskh,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        color = QalamInk,
                    )
                }
                option.transliteration?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = Typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = QalamInk2)
                }
            } else {
                Text(
                    text = option.translation.takeUnless { it.isNullOrBlank() } ?: option.arabicText,
                    style = Typography.titleMedium,
                    color = QalamInk,
                )
            }
        }
    }
}

@Composable
private fun AnswerFeedback(answer: ExerciseAnswer) {
    val correct = answer.result == ExerciseResult.CORRECT
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (correct) QalamPrimaryC else QalamTerraC, RoundedCornerShape(8.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (correct) "Correct" else "Not this one",
            style = Typography.titleMedium,
            color = if (correct) QalamPrimary else QalamTerra,
        )
    }
}

@Composable
private fun ExerciseComplete(
    summary: ExerciseSessionSummary?,
    answers: List<ExerciseAnswer>,
    onClose: () -> Unit,
    onPracticeAgain: () -> Unit,
) {
    val correct = summary?.correct ?: answers.count { it.result == ExerciseResult.CORRECT }
    val total = summary?.totalItems ?: answers.size
    val accuracy = summary?.accuracy ?: if (answers.isEmpty()) 0.0 else correct * 100.0 / answers.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopTitleBar(title = "Complete", onClose = onClose)
        Spacer(Modifier.height(56.dp))
        Box(
            modifier = Modifier
                .background(QalamPrimaryC, CircleShape)
                .padding(34.dp),
        ) {
            Text("${accuracy.roundToInt()}%", style = Typography.headlineLarge, color = QalamPrimary)
        }
        Spacer(Modifier.height(22.dp))
        Text("$correct of $total correct", style = Typography.titleLarge, color = QalamInk)
        Spacer(Modifier.height(6.dp))
        Text(
            text = "${summary?.promotions?.size ?: 0} mastery promotion${if (summary?.promotions?.size == 1) "" else "s"}",
            style = Typography.bodyMedium,
            color = QalamInk2,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onPracticeAgain,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = QalamPrimary,
                contentColor = QalamOnPrimary,
            ),
        ) {
            Icon(Icons.Outlined.Replay, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Practice again", style = Typography.labelLarge)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Back home", style = Typography.labelLarge)
        }
    }
}

@Composable
private fun SetupOptionButton(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val container = when {
        !enabled -> QalamSurface2
        selected -> QalamPrimaryC
        else -> QalamSurface
    }
    val titleColor = when {
        !enabled -> QalamInk3
        selected -> QalamPrimary
        else -> QalamInk
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = container),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(title, style = Typography.titleMedium, color = titleColor)
            Spacer(Modifier.height(2.dp))
            Text(description, style = Typography.bodySmall, color = QalamInk2)
        }
    }
}

@Composable
private fun SizeButton(
    size: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        Button(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.buttonColors(
                containerColor = QalamPrimary,
                contentColor = QalamOnPrimary,
            ),
        ) {
            Text(size.toString(), style = Typography.labelLarge)
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = QalamInk),
        ) {
            Text(size.toString(), style = Typography.labelLarge)
        }
    }
}

@Composable
private fun ExerciseTopBar(
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
            Icon(Icons.Outlined.Close, contentDescription = "Close exercise", tint = QalamInk)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = QalamPrimary,
            trackColor = QalamSurface3,
        )
        Text("$current / $total", style = Typography.labelLarge, color = QalamInk2)
    }
}

@Composable
private fun TopTitleBar(title: String, onClose: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = Typography.headlineMedium, color = QalamInk)
        IconButton(onClick = onClose) {
            Icon(Icons.Outlined.Close, contentDescription = "Close exercise", tint = QalamInk)
        }
    }
}

@Composable
private fun ExerciseLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = QalamPrimary)
    }
}

@Composable
private fun ExerciseError(error: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(error, style = Typography.bodyMedium, color = QalamTerra, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onClose) { Text("Back home", style = Typography.labelLarge) }
    }
}

@Composable
private fun ExerciseEmpty(onClose: () -> Unit) {
    ExerciseError("No exercise items came back for this selection.", onClose)
}
