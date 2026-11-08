package dev.nycocado.atlas

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.Effect
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.carddetail.BlockBubble
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.DayColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// No card prints a cited name inside an inked span yet, so a made-up trait cites the real Heat Vision, a red trait, in a
// blue one, drawn in place of the app's screen, in the Day theme Robolectric starts in.
@HiltAndroidTest
class CitedInkTest : AtlasBehaviorTest() {
    private val inkedCitation =
        Trait(
            "test/traits/inked-citation",
            "test",
            "",
            "Inked Citation",
            null,
            listOf(Effect(text = "Discard [blue]Heat Vision and every blue trait[/blue].")),
            null,
            0,
            listOf(CardColor.Blue),
            false,
            emptyList(),
            listOf(TraitIcon.DropOfLife),
        )

    private val library =
        CardLibrary.decode(
            context.assets
                .open("cards/cards.json")
                .bufferedReader()
                .use { it.readText() },
        )

    // A solid ink's brush becomes the span's color.
    private fun AnnotatedString.inkOver(
        words: String,
        ink: Color,
    ) = text.indexOf(words).let { start ->
        spanStyles.any { it.start < start + words.length && it.end > start && it.item.color == ink }
    }

    // Across every bubble width some line ends between Heat and Vision, or before the arrow, unless the name is kept whole.
    // Below 150dp the name and its arrow don't fit on one line at all.
    @Test
    fun aCitedNameWithASpaceIsNeverSplitAcrossLines() {
        var width by mutableStateOf(150.dp)
        launchPastNotice().onActivity {
            it.setContent {
                AtlasTheme {
                    Box(Modifier.width(width)) {
                        BlockBubble(
                            inkedCitation.face()!!.blocks.single(),
                            CardLanguage.English,
                            inkedCitation,
                            inkedCitation.colors,
                            library,
                        )
                    }
                }
            }
        }
        val effect = compose.onNode(hasText("and every blue trait", substring = true))

        for (step in 0..40) {
            width = (150 + step * 5).dp
            compose.waitForIdle()
            val layouts = mutableListOf<TextLayoutResult>()
            effect.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action!!(layouts)
            val layout = layouts.single()
            val heat = layout.layoutInput.text.indexOf("Heat")
            val vision = layout.layoutInput.text.indexOf("Vision")
            // After the word joiner.
            val arrow = vision + "Vision".length + 1
            assertEquals("at $width", layout.getLineForOffset(heat), layout.getLineForOffset(vision))
            assertEquals("at $width", layout.getLineForOffset(heat), layout.getLineForOffset(arrow))
        }
    }

    @Test
    fun aCitedNameInsideAnInkKeepsItsCitationStyle() {
        launchPastNotice().onActivity {
            it.setContent {
                AtlasTheme {
                    BlockBubble(
                        inkedCitation.face()!!.blocks.single(),
                        CardLanguage.English,
                        inkedCitation,
                        inkedCitation.colors,
                        library,
                    )
                }
            }
        }

        val text =
            compose
                .onNode(hasText("and every blue trait", substring = true))
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.Text)!!
                .single()
        val cited = text.text.indexOf(HEAT_VISION)

        assertTrue(text.getLinkAnnotations(cited, cited + HEAT_VISION.length).isNotEmpty())
        assertTrue(text.inkOver(HEAT_VISION, DayColors.traitRed.ink))
        assertFalse(text.inkOver(HEAT_VISION, DayColors.traitBlue.ink))
        assertTrue(text.inkOver("and every blue trait", DayColors.traitBlue.ink))
    }
}

// A cited name is displayed with a no-break space, so the name stays on one line.
private const val HEAT_VISION = "Heat\u00A0Vision"
