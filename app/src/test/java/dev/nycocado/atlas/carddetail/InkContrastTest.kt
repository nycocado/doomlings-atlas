package dev.nycocado.atlas.carddetail

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import dev.nycocado.atlas.ui.theme.AtlasColors
import dev.nycocado.atlas.ui.theme.DayColors
import dev.nycocado.atlas.ui.theme.GameNightColors
import org.junit.Assert.assertTrue
import org.junit.Test

// docs/design.md asks for 4.5:1 for text; the bubble is surface-sticker in both themes.
class InkContrastTest {
    // The WCAG contrast ratio, which Compose has no function for.
    private fun contrast(
        a: Color,
        b: Color,
    ): Float {
        val (light, dark) = listOf(a.luminance(), b.luminance()).sortedDescending()
        return (light + 0.05f) / (dark + 0.05f)
    }

    // The gradient's shader blends in sRGB, so its colors between two inks are sampled the same way.
    private fun blend(
        a: Color,
        b: Color,
        t: Float,
    ) = Color(a.red + (b.red - a.red) * t, a.green + (b.green - a.green) * t, a.blue + (b.blue - a.blue) * t)

    private fun AtlasColors.inksOnTheBubble(): List<Pair<Color, Float>> {
        val inks = listOf(traitRed, traitGreen, traitBlue, traitPurple, traitColorless).map { it.ink }
        val gradient =
            rainbowInks.zipWithNext().flatMap { (a, b) -> (0..10).map { blend(a, b, it / 10f) } }
        return (inks + gradient).map { it to contrast(it, surfaceSticker) }
    }

    @Test
    fun everyInkAndTheWholeRainbowReach4_5To1OnTheBubbleByDay() {
        val weak = DayColors.inksOnTheBubble().filter { it.second < 4.5f }
        assertTrue("$weak", weak.isEmpty())
    }

    @Test
    fun everyInkAndTheWholeRainbowReach4_5To1OnTheBubbleAtNight() {
        val weak = GameNightColors.inksOnTheBubble().filter { it.second < 4.5f }
        assertTrue("$weak", weak.isEmpty())
    }
}
