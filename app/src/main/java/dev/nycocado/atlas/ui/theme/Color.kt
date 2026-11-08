package dev.nycocado.atlas.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.util.lerp
import dev.nycocado.atlas.card.CardColor

// The tokens in ui/theme mirror design/tokens.json, where their usage is described. Keep them in sync.
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
    val yellowInk: Color,
    // The wonder ink's stops: orange, teal, blue.
    val wonderInk: List<Color>,
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

@Immutable
data class TraitColors(
    val fill: Color,
    val onFill: Color,
    val ink: Color,
    val container: Color,
)

fun AtlasColors.trait(color: CardColor?): TraitColors =
    when (color) {
        CardColor.Blue -> traitBlue
        CardColor.Green -> traitGreen
        CardColor.Purple -> traitPurple
        CardColor.Red -> traitRed
        null -> traitColorless
    }

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
        yellowInk = Color(0xFF8A6100),
        wonderInk = listOf(Color(0xFF9A5D04), Color(0xFF0D6D61), Color(0xFF046476)),
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
        yellowInk = Color(0xFFF7C443),
        wonderInk = listOf(Color(0xFFFAA325), Color(0xFF16B5A1), Color(0xFF07A7C4)),
        traitBlue = TraitColors(Color(0xFF0192D0), Color(0xFF23173B), Color(0xFF7FCDF5), Color(0xFF0D3447)),
        traitGreen = TraitColors(Color(0xFF00AC4D), Color(0xFF23173B), Color(0xFF6FDC98), Color(0xFF0D3A22)),
        traitPurple = TraitColors(Color(0xFFA23F97), Color(0xFFFFFFFF), Color(0xFFEBA3E0), Color(0xFF42173C)),
        traitRed = TraitColors(Color(0xFFEB4244), Color(0xFF140C24), Color(0xFFFFAAA6), Color(0xFF4A1517)),
        traitColorless = TraitColors(Color(0xFFAFAFAF), Color(0xFF23173B), Color(0xFFC9CCD2), Color(0xFF2C3038)),
    )

// How a card's colors are painted: one color flat, several blended top-left to bottom-right in data order. end is the
// painted area's bottom-right corner.
internal fun traitPaint(
    colors: List<Color>,
    end: Offset,
): Brush = colors.singleOrNull()?.let(::SolidColor) ?: Brush.linearGradient(colors, Offset.Zero, end)

// What is drawn over traitPaint. Over several colors, the trait on-color that keeps the most contrast over the whole
// blend, as no single color's own on-color holds across it.
internal fun AtlasColors.inkOver(colors: List<CardColor?>): Color {
    colors.singleOrNull()?.let { return trait(it).onFill }
    if (colors.size == 1) return traitColorless.onFill
    val blend =
        colors.map { trait(it).fill }.zipWithNext().flatMap { (a, b) ->
            (0..BLEND_SAMPLES).map { sRgbMix(a, b, it / BLEND_SAMPLES.toFloat()) }
        }
    return listOf(traitBlue, traitGreen, traitPurple, traitRed)
        .map { it.onFill }
        .distinct()
        .maxBy { ink -> blend.minOf { contrast(ink, it) } }
}

private const val BLEND_SAMPLES = 20

// As the gradient's shader blends; Compose's lerp blends in Oklab, which would give other colors.
private fun sRgbMix(
    a: Color,
    b: Color,
    t: Float,
) = Color(lerp(a.red, b.red, t), lerp(a.green, b.green, t), lerp(a.blue, b.blue, t))

// The WCAG contrast ratio: ColorUtils.calculateContrast takes android.graphics colors, not Compose's.
private fun contrast(
    a: Color,
    b: Color,
): Float {
    val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
    return (light + 0.05f) / (dark + 0.05f)
}
