package dev.nycocado.atlas

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
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
