package com.tonihacks.qalam.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.tonihacks.qalam.ui.theme.QalamBg
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamGoldC
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamOutline
import com.tonihacks.qalam.ui.theme.QalamPrimary
import com.tonihacks.qalam.ui.theme.QalamPrimaryC
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun PracticeScreen(
    onBack: () -> Unit,
    onFlashcards: () -> Unit,
    onMultipleChoice: () -> Unit,
    onSentenceProduction: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QalamBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Practice", style = Typography.headlineMedium, color = QalamInk)
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.Close, contentDescription = "Close practice", tint = QalamInk)
            }
        }
        Spacer(Modifier.height(18.dp))
        Text("Choose how you want to exercise today.", style = Typography.bodyMedium, color = QalamInk2)
        Spacer(Modifier.height(22.dp))
        PracticeModeCard(
            title = "Flashcards",
            description = "Reveal the answer, then self-grade recall.",
            icon = Icons.Filled.Bolt,
            tint = QalamPrimary,
            container = QalamPrimaryC,
            onClick = onFlashcards,
        )
        Spacer(Modifier.height(12.dp))
        PracticeModeCard(
            title = "Multiple choice",
            description = "Tap an option and let Qalam grade the answer.",
            icon = Icons.Outlined.CheckCircle,
            tint = QalamGold,
            container = QalamGoldC,
            onClick = onMultipleChoice,
        )
        Spacer(Modifier.height(12.dp))
        PracticeModeCard(
            title = "Sentence practice",
            description = "Write with target words and get AI feedback.",
            icon = Icons.Outlined.Edit,
            tint = QalamPrimary,
            container = QalamPrimaryC,
            onClick = onSentenceProduction,
        )
    }
}

@Composable
private fun PracticeModeCard(
    title: String,
    description: String,
    icon: ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    container: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(QalamSurface, RoundedCornerShape(8.dp))
            .border(1.dp, QalamOutline, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(container, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint)
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = Typography.titleLarge, color = QalamInk)
            Spacer(Modifier.height(3.dp))
            Text(description, style = Typography.bodySmall, color = QalamInk2)
        }
    }
}
