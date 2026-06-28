package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tonihacks.qalam.domain.model.MasteryLevel
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamSurface
import com.tonihacks.qalam.ui.theme.QalamSurface3
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun MasteryCard(level: MasteryLevel, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = QalamSurface),
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mastery", style = Typography.labelMedium, color = QalamInk2)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                MasteryLevel.entries.forEach { seg ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(
                                color = if (seg.ordinal <= level.ordinal) seg.toQalamColor() else QalamSurface3,
                                shape = RoundedCornerShape(5.dp),
                            )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(level.label(), style = Typography.labelSmall, color = level.toQalamColor())
        }
    }
}
