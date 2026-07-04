package com.tonihacks.qalam.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tonihacks.qalam.ui.theme.QalamGold
import com.tonihacks.qalam.ui.theme.QalamInk
import com.tonihacks.qalam.ui.theme.QalamInk2
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = QalamInk,
    paragraphStyle: TextStyle = Typography.bodyMedium.copy(lineHeight = 25.sp),
) {
    val blocks = markdown.trim().lines()

    SelectionContainer {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            blocks.forEach { rawLine ->
                val line = rawLine.trim()
                when {
                    line.isBlank() -> Spacer(Modifier.height(2.dp))
                    line.startsWithMarkdownHeading() -> MarkdownHeading(line = line, color = color)
                    line.startsWith("- ") || line.startsWith("* ") -> MarkdownBullet(
                        text = line.drop(2).trim(),
                        color = color,
                        paragraphStyle = paragraphStyle,
                    )
                    line.matches(OrderedListRegex) -> MarkdownBullet(
                        text = line.substringAfter(".").trim(),
                        color = color,
                        paragraphStyle = paragraphStyle,
                    )
                    else -> Text(
                        text = line.toMarkdownAnnotatedString(),
                        style = paragraphStyle,
                        color = color,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun MarkdownHeading(line: String, color: Color) {
    val level = line.takeWhile { it == '#' }.length
    val text = line.drop(level).trim()
    val style = if (level <= 2) Typography.headlineSmall else Typography.titleMedium

    Text(
        text = text.toMarkdownAnnotatedString(),
        style = style,
        color = color,
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    )
}

@Composable
private fun MarkdownBullet(
    text: String,
    color: Color,
    paragraphStyle: TextStyle,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text("•", style = paragraphStyle, color = QalamGold)
        Spacer(Modifier.width(8.dp))
        Text(
            text = text.toMarkdownAnnotatedString(),
            style = paragraphStyle,
            color = color,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun String.startsWithMarkdownHeading(): Boolean =
    startsWith("#") && dropWhile { it == '#' }.startsWith(" ")

private fun String.toMarkdownAnnotatedString(): AnnotatedString = buildAnnotatedString {
    appendMarkdownInline(this@toMarkdownAnnotatedString)
}

private fun AnnotatedString.Builder.appendMarkdownInline(source: String) {
    var index = 0
    while (index < source.length) {
        val nextToken = MarkdownToken.entries
            .mapNotNull { token ->
                source.indexOf(token.marker, startIndex = index)
                    .takeIf { it >= 0 }
                    ?.let { it to token }
            }
            .minByOrNull { it.first }

        if (nextToken == null) {
            append(source.substring(index))
            return
        }

        val (start, token) = nextToken
        append(source.substring(index, start))

        val contentStart = start + token.marker.length
        val end = source.indexOf(token.marker, startIndex = contentStart)
        if (end < 0) {
            append(source.substring(start))
            return
        }

        withStyle(token.style) {
            append(source.substring(contentStart, end))
        }
        index = end + token.marker.length
    }
}

private enum class MarkdownToken(
    val marker: String,
    val style: SpanStyle,
) {
    Bold("**", SpanStyle(fontWeight = FontWeight.Bold)),
    Code("`", SpanStyle(color = QalamInk2, fontWeight = FontWeight.Medium)),
    Italic("*", SpanStyle(fontStyle = FontStyle.Italic)),
}

private val OrderedListRegex = Regex("""^\d+\.\s+.+""")
