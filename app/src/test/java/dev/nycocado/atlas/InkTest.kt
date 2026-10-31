package dev.nycocado.atlas

import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.ui.theme.DayColors
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Heat Vision and Hyper-Intelligence with the ink marks of testInks, in the Day theme Robolectric starts in.
@HiltAndroidTest
class InkTest : AtlasBehaviorTest() {
    private fun open(name: String) {
        scrollListTo(cardNamed(name))
        card(name).performClick()
        detailOf(name).assertIsDisplayed()
    }

    private fun effect(words: String): AnnotatedString =
        compose
            .onNode(hasText(words, substring = true))
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)!!
            .single()

    private fun AnnotatedString.stylesOver(words: String) =
        text.indexOf(words).let { start ->
            spanStyles.filter { it.start <= start && it.end >= start + words.length }.map { it.item }
        }

    @Test
    fun aColorWordIsInItsTraitInkAndTalkBackReadsTheWordsOnly() {
        launchPastNotice()
        open("Heat Vision")

        val text = effect("+1 for each red trait in your trait pile.")
        assertFalse(text.text.contains('['))
        assertTrue(text.stylesOver("red").any { it.color == DayColors.traitRed.ink })
        assertTrue(text.stylesOver("trait pile").none { it.color == DayColors.traitRed.ink })
        assertAccessible()
    }

    @Test
    fun aRainbowSpanIsAGradientAndTalkBackReadsTheWordsOnly() {
        launchPastNotice()
        open("Hyper-Intelligence")

        val text = effect("Choose a color. Opponents discard 1 trait of that color")
        assertFalse(text.text.contains('['))
        assertTrue(text.stylesOver("Choose a color.").any { it.brush is ShaderBrush })
        assertTrue(text.stylesOver("Opponents").none { it.brush != null })
        assertAccessible()
    }
}
