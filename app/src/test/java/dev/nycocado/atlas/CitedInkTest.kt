package dev.nycocado.atlas

import androidx.activity.compose.setContent
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasText
import androidx.compose.ui.text.AnnotatedString
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// No card prints a cited name inside an inked span yet, so a made-up trait cites the real Heat Vision in one, drawn in
// place of the app's screen, in the Day theme Robolectric starts in.
@HiltAndroidTest
class CitedInkTest : AtlasBehaviorTest() {
    private val inkedCitation =
        Trait(
            "test/traits/inked-citation",
            "test",
            "",
            "Inked Citation",
            null,
            listOf(Effect(text = "Discard [red]Heat Vision and every red trait[/red].")),
            null,
            0,
            listOf(CardColor.Red),
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
    private fun AnnotatedString.redOver(words: String) =
        text.indexOf(words).let { start ->
            spanStyles.any {
                it.start < start + words.length && it.end > start &&
                    it.item.color == DayColors.traitRed.ink
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
                        null,
                        library,
                    )
                }
            }
        }

        val text =
            compose
                .onNode(hasText("Heat Vision and every red trait", substring = true))
                .fetchSemanticsNode()
                .config
                .getOrNull(SemanticsProperties.Text)!!
                .single()
        val cited = text.text.indexOf("Heat Vision")

        assertTrue(text.getLinkAnnotations(cited, cited + "Heat Vision".length).isNotEmpty())
        assertFalse(text.redOver("Heat Vision"))
        assertTrue(text.redOver("and every red trait"))
    }
}
