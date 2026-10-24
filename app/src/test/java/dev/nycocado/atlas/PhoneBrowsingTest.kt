package dev.nycocado.atlas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

// Card names come from assets/doomlings/cards.json: Classic's green traits, in the compendium's alphabetical order,
// run from Appealing to Woody Stems.
@HiltAndroidTest
class PhoneBrowsingTest : AtlasBehaviorTest() {
    @Test
    fun tappingACardOpensItsDetailAndBackReturnsToTheList() {
        launchPastNotice()

        card("Adorable").performClick()

        compose.onNodeWithText("Adorable").assertIsDisplayed()
        assertAccessible()
        button(R.string.back).performClick()
        text(R.string.search_placeholder).assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun backFromACardKeepsTheFilterAndTheScroll() {
        val scenario = launchPastNotice()
        compose.onNode(isSelectable() and hasText(string(R.string.color_green))).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Woody Stems,", substring = true))
        card("Woody Stems").performClick()
        compose.onNodeWithText("Woody Stems").assertIsDisplayed()

        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

        card("Woody Stems").assertIsDisplayed()
        card("Appealing").assertDoesNotExist()
        // The filters sit at the top of the list: scrolling there is the only way to see them again.
        compose.onNode(hasScrollAction()).performScrollToNode(isSelectable() and hasText(string(R.string.color_green)))
        compose.onNode(isSelectable() and hasText(string(R.string.color_green))).assertIsSelected()
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
        compose.onNodeWithText("Adorable").assertIsDisplayed()
    }
}
