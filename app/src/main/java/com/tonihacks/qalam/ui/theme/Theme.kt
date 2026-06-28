package com.tonihacks.qalam.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val QalamColorScheme = lightColorScheme(
    primary = QalamPrimary,
    onPrimary = QalamOnPrimary,
    primaryContainer   = QalamPrimaryC,
    onPrimaryContainer = QalamOnPrimaryC,
    background         = QalamBg,
    onBackground       = QalamInk,
    surface            = QalamSurface,
    onSurface          = QalamInk,
    surfaceVariant     = QalamSurface2,
    onSurfaceVariant   = QalamInk2,
    outline            = QalamOutline,
    error              = QalamTerra,
    errorContainer     = QalamTerraC,
)

@Composable
fun QalamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = QalamColorScheme,
        typography = Typography,
        content = content
    )
}