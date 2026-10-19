package dev.nycocado.atlas.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

// Color tokens from design/tokens.json; usage notes live there. Keep both files in sync.
@Immutable
data class AtlasColors(
    val surface: Color,
    val surfaceSunk: Color,
    val surfaceSticker: Color,
    val surfaceStickerHigh: Color,
    val inverseSurface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineSoft: Color,
    val primary: Color,
    val onPrimary: Color,
    val error: Color,
    val focusRing: Color,
    val traitBlue: TraitColors,
    val traitGreen: TraitColors,
    val traitPurple: TraitColors,
    val traitRed: TraitColors,
    val traitColorless: TraitColors,
    // Same in both themes.
    val onInverseSurface: Color = Color(0xFFFFF4E8),
    val onInverseSurfaceVariant: Color = Color(0xFFD9CFEE),
    val highlight: Color = Color(0xFFF7C443),
    val onHighlight: Color = Color(0xFF23173B),
    val sunsetRed: Color = Color(0xFFF26B78),
    val sunsetOrange: Color = Color(0xFFF08A3C),
    val sunsetYellow: Color = Color(0xFFF7C443),
    val sunsetSea: Color = Color(0xFF3AA0D8),
    val onSunset: Color = Color(0xFF23173B),
)

// fill and onFill are the exact card color, the same in both themes.
@Immutable
data class TraitColors(
    val fill: Color,
    val onFill: Color,
    val ink: Color,
    val container: Color,
)

internal val DayColors =
    AtlasColors(
        surface = Color(0xFFFFF4E8),
        surfaceSunk = Color(0xFFFBE9D6),
        surfaceSticker = Color(0xFFFFFFFF),
        surfaceStickerHigh = Color(0xFFFFFFFF),
        inverseSurface = Color(0xFF23173B),
        onSurface = Color(0xFF23173B),
        onSurfaceVariant = Color(0xFF5E5172),
        outline = Color(0xFF23173B),
        outlineSoft = Color(0xFFE3D3C2),
        primary = Color(0xFF23173B),
        onPrimary = Color(0xFFFFF4E8),
        error = Color(0xFFB3261E),
        focusRing = Color(0xFF23173B),
        traitBlue = TraitColors(Color(0xFF0192D0), Color(0xFF23173B), Color(0xFF00658F), Color(0xFFDCF1FB)),
        traitGreen = TraitColors(Color(0xFF00AC4D), Color(0xFF23173B), Color(0xFF00703A), Color(0xFFDAF5E4)),
        traitPurple = TraitColors(Color(0xFFA23F97), Color(0xFFFFFFFF), Color(0xFF8A2F80), Color(0xFFF7E1F3)),
        traitRed = TraitColors(Color(0xFFEB4244), Color(0xFF140C24), Color(0xFFB5202A), Color(0xFFFDE2E1)),
        traitColorless = TraitColors(Color(0xFFAFAFAF), Color(0xFF23173B), Color(0xFF5C6068), Color(0xFFDCDFE4)),
    )

internal val GameNightColors =
    AtlasColors(
        surface = Color(0xFF1B1230),
        surfaceSunk = Color(0xFF23173B),
        surfaceSticker = Color(0xFF2C1F48),
        surfaceStickerHigh = Color(0xFF382A57),
        inverseSurface = Color(0xFF2C1F48),
        onSurface = Color(0xFFFFF4E8),
        onSurfaceVariant = Color(0xFFD9CFEE),
        outline = Color(0xFFD9CFEE),
        outlineSoft = Color(0xFF4A3B6B),
        primary = Color(0xFFF7C443),
        onPrimary = Color(0xFF23173B),
        error = Color(0xFFFFB4AB),
        focusRing = Color(0xFFF7C443),
        traitBlue = TraitColors(Color(0xFF0192D0), Color(0xFF23173B), Color(0xFF7FCDF5), Color(0xFF0D3447)),
        traitGreen = TraitColors(Color(0xFF00AC4D), Color(0xFF23173B), Color(0xFF6FDC98), Color(0xFF0D3A22)),
        traitPurple = TraitColors(Color(0xFFA23F97), Color(0xFFFFFFFF), Color(0xFFEBA3E0), Color(0xFF42173C)),
        traitRed = TraitColors(Color(0xFFEB4244), Color(0xFF140C24), Color(0xFFFFAAA6), Color(0xFF4A1517)),
        traitColorless = TraitColors(Color(0xFFAFAFAF), Color(0xFF23173B), Color(0xFFC9CCD2), Color(0xFF2C3038)),
    )
