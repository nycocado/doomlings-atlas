package dev.nycocado.atlas

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.annotation.Config

// From 840dp wide the card opens beside the list. Card names come from assets/doomlings/cards.json.
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class WideWindowTest : AtlasBehaviorTest() {
    @Test
    fun aCardOpensBesideTheListWhichStaysVisible() {
        launchPastNotice()

        card("Adorable").performClick()

        detailOf("Adorable").assertIsDisplayed()
        card("Appealing").assertIsDisplayed()
        text(R.string.search_placeholder).assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun anotherCardReplacesTheOpenOneAndOneBackReturnsToTheList() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()

        card("Appealing").performClick()

        detailOf("Appealing").assertIsDisplayed()
        detailOf("Adorable").assertDoesNotExist()
        pressBack(scenario)
        text(R.string.unofficial_notice).assertDoesNotExist()
        text(R.string.search_placeholder).assertIsDisplayed()
        button(R.string.about).assertIsDisplayed()
    }

    @Test
    fun backClosesTheCardBesideTheList() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()
        pressBack(scenario)
        // The card's pane fades out.
        compose.waitUntilDoesNotExist(hasText(string(R.string.unofficial_notice)))

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
        pressBack(scenario)
        detailOf("Adorable").assertIsDisplayed()
        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun nothingTakesFocusWhenTheAppOpens() {
        launchPastNotice()

        focused.assertCountEquals(0)
    }

    @Test
    fun nothingTakesFocusWhenTheCardBesideTheListCloses() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()

        pressBack(scenario)
        compose.waitUntilDoesNotExist(hasText(string(R.string.unofficial_notice)))

        focused.assertCountEquals(0)
    }

    @Test
    fun tappingTheSearchFieldFocusesIt() {
        launchPastNotice()

        searchField.performClick()

        searchField.assertIsFocused()
    }

    private val focused get() = compose.onAllNodes(isFocused())

    private val searchField get() = compose.onNode(hasSetTextAction())
}
