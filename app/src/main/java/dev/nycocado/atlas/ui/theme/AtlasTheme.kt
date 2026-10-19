package dev.nycocado.atlas.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAtlasColors = staticCompositionLocalOf { DayColors }

@Composable
fun AtlasTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (dark) GameNightColors else DayColors
    CompositionLocalProvider(
        LocalAtlasColors provides colors,
        LocalIndication provides FocusRing(colors.focusRing),
        content = content,
    )
}

object AtlasTheme {
    val colors: AtlasColors
        @Composable @ReadOnlyComposable
        get() = LocalAtlasColors.current
}
