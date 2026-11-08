package dev.nycocado.atlas.carddetail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardColor.Blue
import dev.nycocado.atlas.card.CardColor.Green
import dev.nycocado.atlas.card.CardColor.Purple
import dev.nycocado.atlas.card.CardColor.Red
import dev.nycocado.atlas.card.Ink
import dev.nycocado.atlas.ui.theme.AtlasColors
import dev.nycocado.atlas.ui.theme.DayColors
import dev.nycocado.atlas.ui.theme.GameNightColors
import dev.nycocado.atlas.ui.theme.inkOver
import dev.nycocado.atlas.ui.theme.trait
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// docs/design.md asks for 4.5:1 for text; the bubble is surface-sticker in both themes.
// Robolectric, as ColorUtils reads android.graphics.Color, which is a stub on the plain JVM.
@RunWith(RobolectricTestRunner::class)
class InkContrastTest {
    private fun contrast(
        a: Color,
        b: Color,
    ): Double = ColorUtils.calculateContrast(a.toArgb(), b.toArgb())

    // The gradient's shader blends in sRGB, so its colors between two inks are sampled the same way.
    private fun blend(
        a: Color,
        b: Color,
        t: Float,
    ) = Color(a.red + (b.red - a.red) * t, a.green + (b.green - a.green) * t, a.blue + (b.blue - a.blue) * t)

    // Every ink's colors, and for a gradient the colors between them too.
    private fun AtlasColors.inksOnTheBubble(): List<Pair<Color, Double>> =
        Ink.entries
            .flatMap { ink ->
                inkColors(ink).let { colors ->
                    colors + colors.zipWithNext().flatMap { (a, b) -> (1..9).map { blend(a, b, it / 10f) } }
                }
            }.map { it to contrast(it, surfaceSticker) }

    @Test
    fun everyInkAndEveryGradientReach4_5To1OnTheBubbleByDay() {
        val weak = DayColors.inksOnTheBubble().filter { it.second < 4.5f }
        assertTrue("$weak", weak.isEmpty())
    }

    @Test
    fun everyInkAndEveryGradientReach4_5To1OnTheBubbleAtNight() {
        val weak = GameNightColors.inksOnTheBubble().filter { it.second < 4.5f }
        assertTrue("$weak", weak.isEmpty())
    }

    // docs/design.md asks for 3:1 for glyphs, which the panel's dots are. The multi-color combinations the card data has today.
    private val combinations =
        listOf(
            listOf(Blue, Green),
            listOf(Green, Red),
            listOf(Purple, Red),
            listOf(Blue, Red),
            listOf(Blue, Purple),
            listOf(Green, Purple),
            listOf(Blue, Green, Purple, Red),
        )

    private fun AtlasColors.worstContrast(colors: List<CardColor>): Double =
        colors
            .map { trait(it).fill }
            .zipWithNext()
            .flatMap { (a, b) -> (0..100).map { blend(a, b, it / 100f) } }
            .minOf { contrast(it, inkOver(colors)) }

    @Test
    fun theInkOverEveryMultiColorGradientReaches3To1EverywhereOnIt() {
        val weak = combinations.filter { DayColors.worstContrast(it) < 3f }
        assertTrue("$weak", weak.isEmpty())
    }

    // Contrast worked out by hand: over blue to purple white keeps 3.49:1, more than the 3.25:1 of the darkest on-color, Red's.
    @Test
    fun theInkOverBlueToPurpleIsWhite() {
        assertEquals(Color.White, DayColors.inkOver(listOf(Blue, Purple)))
    }

    @Test
    fun aSingleColorKeepsItsOwnInk() {
        assertEquals(DayColors.traitPurple.onFill, DayColors.inkOver(listOf(Purple)))
        assertEquals(DayColors.traitColorless.onFill, DayColors.inkOver(listOf(null)))
    }
}
