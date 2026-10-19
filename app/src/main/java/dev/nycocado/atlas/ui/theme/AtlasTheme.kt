package dev.nycocado.atlas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val LocalAtlasColors = staticCompositionLocalOf { DayColors }

// Own design system (ADR 0006): "Day" by default, "Game night" when the system is dark. No dynamic color.
@Composable
fun AtlasTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalAtlasColors provides if (dark) GameNightColors else DayColors, content = content)
}

object AtlasTheme {
    val colors: AtlasColors
        @Composable @ReadOnlyComposable
        get() = LocalAtlasColors.current
}
