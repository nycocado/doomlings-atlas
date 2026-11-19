package dev.nycocado.atlas

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.AnnotatedString
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.ui.component.pointsText
import org.junit.Test

// Card names come from assets/doomlings/classic/cards.json: Classic's green traits, in the compendium's alphabetical
// order, run from Appealing to Woody Stems.
@HiltAndroidTest
class PhoneBrowsingTest : AtlasBehaviorTest() {
    @Test
    fun tappingACardOpensItsDetailAndBackReturnsToTheList() {
        launchPastNotice()

        card("Adorable").performClick()

        detailOf("Adorable").assertIsDisplayed()
        assertAccessible()
        button(R.string.back).performClick()
        text(R.string.search_placeholder).assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun tappingBackTwiceWhileACardFadesOutStaysOnTheList() {
        launchPastNotice()
        card("Adorable").performClick()

        tapBackAgainMidFade()

        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun tappingBackTwiceWhileAboutFadesOutStaysOnTheList() {
        launchPastNotice()
        button(R.string.about).performClick()

        tapBackAgainMidFade()

        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun tappingAboutTwiceWhileItFadesInOpensItOnce() {
        val scenario = launchPastNotice()
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        button(R.string.about).performClick()
        button(R.string.about).performClick()
        compose.mainClock.autoAdvance = true
        compose.waitForIdle()

        pressBack(scenario)

        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun theSearchFieldKeepsEveryCharacterTypedBeforeTheListCatchesUp() {
        launchPastNotice()
        val field = compose.onNode(hasSetTextAction())
        val insert = field.fetchSemanticsNode().config[SemanticsActions.InsertTextAtCursor].action!!

        // All in one frame, as a fast keyboard does: nothing reaches the ViewModel in between.
        compose.runOnUiThread { "adorable".forEach { insert(AnnotatedString(it.toString())) } }
        compose.waitForIdle()

        field.assertTextEquals("adorable")
        card("Adorable").assertIsDisplayed()
    }

    // Classic has no trait that is both blue and green.
    @Test
    fun severalColorsMarkTogetherAndColorlessUnmarksThem() {
        launchPastNotice()
        val blue = disc(R.string.color_blue)
        val colorless = disc(R.string.color_colorless)

        compose.onNode(blue).performClick()
        compose.onNode(green).performClick()

        listOf(blue, green).forEach {
            compose.onNode(it).assertIsSelected()
            compose.onNode(it).assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox))
        }
        text(R.string.no_results).assertIsDisplayed()

        compose.onNode(colorless).performClick()

        compose.onNode(blue).assertIsNotSelected()
        compose.onNode(green).assertIsNotSelected()
        compose.onNode(colorless).assertIsSelected()
    }

    private fun disc(name: Int): SemanticsMatcher = isSelectable() and hasText(string(name))

    @Test
    fun backFromACardKeepsTheFilterAndTheScroll() {
        val scenario = launchPastNotice()
        compose.onNode(green).performClick()
        scrollListTo(cardNamed("Woody Stems"))
        card("Woody Stems").performClick()
        detailOf("Woody Stems").assertIsDisplayed()

        pressBack(scenario)

        card("Woody Stems").assertIsDisplayed()
        card("Appealing").assertDoesNotExist()
        // The filters sit at the top of the list: scrolling there is the only way to see them again.
        scrollListTo(green)
        compose.onNode(green).assertIsSelected()
        card("Adorable").assertDoesNotExist()
    }

    @Test
    fun aboutOpensFromTheCompendiumAndBackReturnsToIt() {
        launchPastNotice()

        button(R.string.about).performClick()

        text(R.string.about).assertIsDisplayed()
        assertAccessible()
        button(R.string.back).performClick()
        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun aboutOpensFromACardAndBackReturnsToTheCard() {
        launchPastNotice()
        card("Adorable").performClick()

        text(R.string.unofficial_notice).performClick()

        text(R.string.about).assertIsDisplayed()
        button(R.string.back).performClick()
        detailOf("Adorable").assertIsDisplayed()
    }

    @Test
    fun aTraitWithAFixedValueShowsItsPoints() {
        launchPastNotice()

        card("Adorable").assertContentDescriptionEquals(
            "Adorable, ${string(R.string.color_purple)}, ${context.resources.pointsText(4)}",
        )
    }

    @Test
    fun aTraitWhoseValueDependsOnTheGameShowsNoPoints() {
        launchPastNotice()
        scrollListTo(cardNamed("Fortunate"))

        card("Fortunate").assertContentDescriptionEquals("Fortunate, ${string(R.string.color_green)}")
    }

    @Test
    fun aTraitWithAFixedValueShowsItsPointsBadgeInTheDetail() {
        launchPastNotice()

        card("Adorable").performClick()

        compose.onNode(pointsBadge).assertIsDisplayed()
    }

    @Test
    fun aTraitWhoseValueDependsOnTheGameShowsNoPointsBadgeInTheDetail() {
        launchPastNotice()
        scrollListTo(cardNamed("Fortunate"))

        card("Fortunate").performClick()

        detailOf("Fortunate").assertIsDisplayed()
        compose.onNode(pointsBadge).assertDoesNotExist()
    }

    @Test
    fun aCatastropheDetailIsAccessible() {
        launchPastNotice()
        text(R.string.kind_catastrophes).performClick()

        // A catastrophe has no color or points: its description is just the name.
        compose.onNode(hasContentDescription("AI Takeover")).performClick()

        detailOf("AI Takeover").assertIsDisplayed()
        assertAccessible()
    }
}
