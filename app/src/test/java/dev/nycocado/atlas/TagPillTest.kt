package dev.nycocado.atlas

import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasText
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardText
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.carddetail.CardDetailContent
import dev.nycocado.atlas.ui.theme.AtlasTheme
import org.junit.Test

// Black and Blue as in assets/doomlings/castle-glass/cards.json, with made-up Português for the card's own text.
// TestCardRepositoryModule keeps only Classic, so its card detail is drawn in place of the app's screen.
// The labels are written out, against docs/testing.md: the resources hold the <b> this proves the pill strips.
@HiltAndroidTest
class TagPillTest : AtlasBehaviorTest() {
    private val blackAndBlue =
        Trait(
            "castle-glass/traits/black-and-blue",
            "castle-glass",
            "",
            "Black and Blue",
            "And a little green, too",
            emptyList(),
            null,
            1,
            listOf(CardColor.Blue, CardColor.Green),
            false,
            emptyList(),
            listOf(TraitIcon.Curious, TraitIcon.Fish),
        )

    private fun show(language: CardLanguage) {
        val translations =
            CardTranslations(
                mapOf(
                    CardLanguage.Portuguese to
                        mapOf(blackAndBlue.id to CardText("Preto e Azul", emptyList(), "E verde")),
                ),
            )
        launchPastNotice().onActivity {
            it.setContent {
                AtlasTheme {
                    CardDetailContent(
                        blackAndBlue,
                        tilt = -3f,
                        onBack = {},
                        onAboutClick = {},
                        translations = translations,
                        language = language,
                    )
                }
            }
        }
    }

    @Test
    fun aQualifierAndItsSubjectAreOnePillWithAReadableLabel() {
        show(CardLanguage.English)

        compose.onNode(hasText("Curious Fish")).assertExists()
        compose.onNode(hasText(string(R.string.icon_fish))).assertDoesNotExist()
        assertAccessible()
    }

    @Test
    fun theLabelFollowsTheCardLanguage() {
        show(CardLanguage.Portuguese)

        compose.onNode(hasText("Peixe Curioso")).assertExists()
    }
}
