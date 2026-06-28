package com.tonihacks.qalam.ui.words

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.tonihacks.qalam.domain.model.Example
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.QalamSurface2
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun ExampleCard(example: Example) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = QalamSurface2),
        shape = RoundedCornerShape(14.dp),
    ) {
        SelectionContainer {
            Column(modifier = Modifier.padding(14.dp)) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        example.arabicText,
                        style = Typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                example.transliteration?.let {
                    Text(it, style = Typography.bodyMedium.copy(fontStyle = FontStyle.Italic), color = QalamInk2)
                }
                example.translation?.let {
                    Text(it, style = Typography.bodyMedium, color = QalamInk)
                }
            }
        }
    }
}