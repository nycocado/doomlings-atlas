package dev.nycocado.atlas

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performFirstLinkClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.annotation.Config

// Age of Dracula cites Vampirism (assets/doomlings/cards.json), in Português as well (testPortuguese).
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class CitedCardTest : AtlasBehaviorTest() {
    private fun openAgeOfDracula() {
        text(R.string.kind_ages).performClick()
        compose.onNode(hasContentDescription("Age of Dracula")).performClick()
        detailOf("Age of Dracula").assertIsDisplayed()
    }

    private fun tapVampirism() = compose.onNode(hasText("Vampirism", substring = true)).performFirstLinkClick()

    @Test
    fun aCitedCardOpensAndBackReturnsToTheCiter() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        assertAccessible()

        tapVampirism()

        detailOf("Vampirism").assertIsDisplayed()
        assertAccessible()
        pressBack(scenario)
        detailOf("Age of Dracula").assertIsDisplayed()
        pressBack(scenario)
        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun aCitedCardOpensFromTheTranslatedText() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        compose.onNode(isSelectable() and hasText("Português")).performClick()
        compose.onNode(hasText("pilha de traits", substring = true)).assertIsDisplayed()

        tapVampirism()

        detailOf("Vampirism").assertIsDisplayed()
        pressBack(scenario)
        detailOf("Age of Dracula").assertIsDisplayed()
        pressBack(scenario)
        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w1280dp-h800dp-xhdpi")
    fun aCitedCardOpensBesideTheListAndBackReturnsToTheCiter() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        assertAccessible()

        tapVampirism()

        detailOf("Vampirism").assertIsDisplayed()
        assertAccessible()
        pressBack(scenario)
        compose.waitUntilDoesNotExist(hasText("Vampirism"))
        detailOf("Age of Dracula").assertIsDisplayed()
        pressBack(scenario)
        compose.waitUntilDoesNotExist(hasText(string(R.string.unofficial_notice)))
        text(R.string.search_placeholder).assertIsDisplayed()
    }
}
