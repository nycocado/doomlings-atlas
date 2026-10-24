package dev.nycocado.atlas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.annotation.Config

// From 840dp wide the card opens beside the list. Card names come from assets/doomlings/cards.json.
@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class WideWindowTest : AtlasBehaviorTest() {
    @Test
    fun aCardOpensBesideTheListWhichStaysVisible() {
        val scenario = launchPastNotice()

        card("Adorable").performClick()

        compose.onNodeWithText("Adorable").assertIsDisplayed()
        card("Appealing").assertIsDisplayed()
        text(R.string.search_placeholder).assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun anotherCardReplacesTheOpenOneAndOneBackReturnsToTheList() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()

        card("Appealing").performClick()

        compose.onNodeWithText("Appealing").assertIsDisplayed()
        compose.onNodeWithText("Adorable").assertDoesNotExist()
        scenario.onActivity { a ->
            listOf(0, 1).forEach { a.dispatchKeyEvent(android.view.KeyEvent(it, android.view.KeyEvent.KEYCODE_BACK)) }
        }
        text(R.string.unofficial_notice).assertDoesNotExist()
        text(R.string.search_placeholder).assertIsDisplayed()
        button(R.string.about).assertIsDisplayed()
    }

    @Test
    fun backClosesTheCardBesideTheList() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()
        compose.waitForIdle()
        scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        // The card's pane slides out.
        compose.mainClock.advanceTimeBy(1000)

        text(R.string.search_placeholder).assertIsDisplayed()
        text(R.string.unofficial_notice).assertDoesNotExist()
        button(R.string.about).assertIsDisplayed()
    }

    @Test
    fun aboutOpensFromTheCardBesideTheListAndBackReturns() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()

        button(R.string.about).assertDoesNotExist()
        text(R.string.unofficial_notice).performClick()

        text(R.string.about).assertIsDisplayed()
        assertAccessible()
        scenario.onActivity { a ->
            listOf(0, 1).forEach { a.dispatchKeyEvent(android.view.KeyEvent(it, android.view.KeyEvent.KEYCODE_BACK)) }
        }
        compose.onNodeWithText("Adorable").assertIsDisplayed()
        text(R.string.search_placeholder).assertIsDisplayed()
    }
}
