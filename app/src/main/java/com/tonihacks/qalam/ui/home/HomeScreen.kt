package com.tonihacks.qalam.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.domain.model.Word
import com.tonihacks.qalam.ui.theme.HankenGrotesk
import com.tonihacks.qalam.ui.theme.NewsReader
import com.tonihacks.qalam.ui.theme.NotoNaskh
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamInk3
import com.tonihacks.qalam.ui.theme.QalamLapis
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamSurface3
import com.tonihacks.qalam.ui.theme.QalamTerra
import com.tonihacks.qalam.ui.theme.Typography
import com.tonihacks.qalam.ui.words.label
import com.tonihacks.qalam.ui.words.toQalamColor
import java.time.LocalTime

@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    onStartTraining: () -> Unit = {},
    onNavigateToWords: () -> Unit = {},
    onNavigateToRoots: () -> Unit = {},
    onNavigateToTexts: () -> Unit = {},
    onNavigateToWord: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val baseUrl by viewModel.baseUrl.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.ping()
        }
    }

    val hasData = uiState.totalWords > 0 || uiState.totalRoots > 0 || uiState.totalTexts > 0

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 14.dp, bottom = 110.dp),
        ) {
            HomeHeader(
                connectionState = connectionState,
                baseUrl = baseUrl,
                onNavigateToSettings = onNavigateToSettings,
            )
            Spacer(Modifier.height(22.dp))
            DueForReviewCard(dueCount = uiState.dueCount, onClick = onStartTraining)
            Spacer(Modifier.height(22.dp))
            MasteryOverviewCard(totalWords = uiState.totalWords, masteryCounts = uiState.masteryCounts)
            Spacer(Modifier.height(14.dp))
            QuickStatsRow(
                totalWords = uiState.totalWords,
                totalRoots = uiState.totalRoots,
                totalTexts = uiState.totalTexts,
                onWords = onNavigateToWords,
                onRoots = onNavigateToRoots,
                onTexts = onNavigateToTexts,
            )
            if (uiState.recentWords.isNotEmpty()) {
                Spacer(Modifier.height(26.dp))
                JumpBackInSection(
                    words = uiState.recentWords,
                    onAllWords = onNavigateToWords,
                    onWordClick = onNavigateToWord,
                )
            }
        }

        if (uiState.isLoading && !hasData) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = QalamPrimary)
            }
        } else if (uiState.error != null && !hasData) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.error ?: "Failed to load",
                    color = QalamTerra,
                    style = Typography.bodyMedium,
                    modifier = Modifier.padding(32.dp),
                )
            }
        }
    }
}

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..17 -> "Good afternoon"
    else -> "Good evening"
}

@Composable
private fun HomeHeader(
    connectionState: HomeViewModel.ConnectionState,
    baseUrl: String,
    onNavigateToSettings: () -> Unit,
) {
    val greeting = remember { greetingForHour(LocalTime.now().hour) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column {
            Text(
                text = greeting,
                fontFamily = NewsReader,
                fontStyle = FontStyle.Italic,
                fontSize = 15.sp,
                color = QalamInk2,
            )
            Text(
                text = "قلم",
                fontFamily = NotoNaskh,
                fontSize = 40.sp,
                lineHeight = 40.sp,
                color = QalamInk,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                text = "QALAM · ARABIC",
                fontFamily = HankenGrotesk,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                letterSpacing = 3.sp,
                color = QalamInk3,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
        ConnectionPill(
            state = connectionState,
            baseUrl = baseUrl,
            onClick = onNavigateToSettings,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
private fun ConnectionPill(
    state: HomeViewModel.ConnectionState,
    baseUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dotColor = when (state) {
        HomeViewModel.ConnectionState.Online  -> QalamPrimary
        HomeViewModel.ConnectionState.Offline -> QalamTerra
        HomeViewModel.ConnectionState.Unknown -> QalamInk3
    }

    val label = when (state) {
        HomeViewModel.ConnectionState.Online  -> baseUrl.toUri().host ?: "Online"
        HomeViewModel.ConnectionState.Offline -> "Offline"
        HomeViewModel.ConnectionState.Unknown -> "…"
    }

    val transition = rememberInfiniteTransition(label = "connection_pulse")
    val pulseAlpha by transition.animateFloat(
        initialValue  = 1f,
        targetValue   = 0.3f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dot_alpha",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(QalamSurface)
            .border(1.dp, QalamOutline, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(if (state == HomeViewModel.ConnectionState.Online) pulseAlpha else 1f)
                .background(dotColor, CircleShape)
        )
        Text(text = label, style = Typography.labelMedium, color = QalamInk2)
    }
}

@Composable
private fun DueForReviewCard(dueCount: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF23816B), Color(0xFF155244))))
            .clickable(onClick = onClick)
            .padding(24.dp),
    ) {
        Text(
            text = "ت",
            fontFamily = NotoNaskh,
            fontSize = 170.sp,
            color = Color.White.copy(alpha = 0.07f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp),
        )
        Column {
            Text(
                text = "DUE FOR REVIEW",
                fontFamily = HankenGrotesk,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.8f),
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(top = 6.dp, bottom = 2.dp),
            ) {
                Text(
                    text = "$dueCount",
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.Bold,
                    fontSize = 58.sp,
                    lineHeight = 58.sp,
                    color = Color.White,
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "words waiting",
                    fontFamily = NewsReader,
                    fontStyle = FontStyle.Italic,
                    fontSize = 19.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.16f))
                    .padding(horizontal = 18.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(
                    text = "Start training session",
                    fontFamily = HankenGrotesk,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun MasteryOverviewCard(totalWords: Int, masteryCounts: Map<MasteryLevel, Int>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(QalamSurface)
            .border(1.dp, QalamOutline, RoundedCornerShape(22.dp))
            .padding(18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Mastery", style = Typography.titleMedium, color = QalamInk)
            Text(
                text = "$totalWords words tracked",
                fontFamily = NewsReader,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = QalamInk2,
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(7.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (totalWords == 0) {
                Box(Modifier.weight(1f).fillMaxHeight().background(QalamSurface3))
            } else {
                MasteryLevel.entries.forEach { level ->
                    val count = masteryCounts[level] ?: 0
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .weight(count.toFloat())
                                .fillMaxHeight()
                                .background(level.toQalamColor()),
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MasteryLevel.entries.forEach { level ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                ) {
                    Box(Modifier.size(8.dp).background(level.toQalamColor(), CircleShape))
                    Text(level.label(), style = Typography.labelSmall, color = QalamInk2)
                    Text(
                        text = "${masteryCounts[level] ?: 0}",
                        style = Typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = QalamInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    totalWords: Int,
    totalRoots: Int,
    totalTexts: Int,
    onWords: () -> Unit,
    onRoots: () -> Unit,
    onTexts: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(Icons.Outlined.Translate, QalamPrimary, totalWords, "Words", onWords, Modifier.weight(1f))
        StatCard(Icons.Outlined.AccountTree, QalamGold, totalRoots, "Roots", onRoots, Modifier.weight(1f))
        StatCard(Icons.AutoMirrored.Outlined.MenuBook, QalamLapis, totalTexts, "Texts", onTexts, Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    tint: Color,
    count: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(QalamSurface)
            .border(1.dp, QalamOutline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 16.dp),
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "$count",
            fontFamily = HankenGrotesk,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 26.sp,
            color = QalamInk,
        )
        Spacer(Modifier.height(3.dp))
        Text(label, style = Typography.labelSmall, color = QalamInk2)
    }
}

@Composable
private fun JumpBackInSection(
    words: List<Word>,
    onAllWords: () -> Unit,
    onWordClick: (String) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Jump back in", style = Typography.titleMedium, color = QalamInk)
            Text(
                text = "All words",
                style = Typography.labelLarge,
                color = QalamPrimary,
                modifier = Modifier.clickable(onClick = onAllWords),
            )
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(words, key = { it.id }) { word ->
                RecentWordCard(word = word, onClick = { onWordClick(word.id) })
            }
        }
    }
}

@Composable
private fun RecentWordCard(word: Word, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(142.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(QalamSurface)
            .border(1.dp, QalamOutline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(15.dp),
    ) {
        Box(Modifier.size(8.dp).background(word.masteryLevel.toQalamColor(), CircleShape))
        Text(
            text = word.arabicText,
            fontFamily = NotoNaskh,
            fontSize = 30.sp,
            lineHeight = 34.sp,
            color = QalamInk,
            maxLines = 1,
            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
        )
        word.transliteration?.let {
            Text(
                text = it,
                fontFamily = NewsReader,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = QalamInk2,
                maxLines = 1,
            )
        }
        word.translation?.let {
            Text(
                text = it,
                style = Typography.bodySmall,
                color = QalamInk,
                maxLines = 2,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}
