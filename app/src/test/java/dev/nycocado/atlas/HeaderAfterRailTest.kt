package dev.nycocado.atlas

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

private const val UPRIGHT = "w411dp-h891dp-port-xxhdpi"
private const val SIDEWAYS = "w891dp-h411dp-land-xxhdpi"

// Wide enough for a card beside the list, which then has too little room for the rail layout.
private const val UNFOLDED = "w900dp-h1200dp-xhdpi"

// The phone on its side gives the compendium the rail layout, where the list scrolls without the header; back upright,
// the stacked layout's header follows the list. Card names come from assets/doomlings/classic/cards.json.
@HiltAndroidTest
class HeaderAfterRailTest : AtlasBehaviorTest() {
    @Test
    fun backAtTheTopInTheRailLayoutTheHeaderReturnsExpanded() {
        val scenario = launchPastNotice()
        list.performTouchInput { swipeUp() }
        title.assertIsNotDisplayed()

        rotate(scenario, SIDEWAYS)
        list.performScrollToIndex(0)
        rotate(scenario, UPRIGHT)

        title.assertIsDisplayed()
    }

    @Test
    fun scrolledDownInTheRailLayoutTheHeaderReturnsCollapsed() {
        val scenario = launchPastNotice()
        title.assertIsDisplayed()

        rotate(scenario, SIDEWAYS)
        list.performScrollToIndex(10)
        rotate(scenario, UPRIGHT)

        title.assertIsNotDisplayed()
    }

    @Test
    fun aCardOpeningBesideTheScrolledListCollapsesItsHeader() {
        RuntimeEnvironment.setQualifiers(UNFOLDED)
        launchPastNotice()
        scrollListTo(cardNamed("Woody Stems"))
        title.assertIsDisplayed()

        card("Woody Stems").performClick()

        detailOf("Woody Stems").assertIsDisplayed()
        // It collapses as the switch to the stacked layout reaches the screen.
        compose.waitUntil { !title.isDisplayed() }
        title.assertIsNotDisplayed()
    }

    private val list get() = compose.onNode(hasScrollAction())

    // The header's title row, the part that collapses.
    private val title: SemanticsNodeInteraction get() =
        compose.onNode(isHeading() and hasText(string(R.string.app_name)))

    // Recreates the screen, as turning the phone does.
    private fun rotate(
        scenario: ActivityScenario<MainActivity>,
        qualifiers: String,
    ) {
        // Once the screen has settled, so the last scroll is in the saved state.
        compose.waitForIdle()
        RuntimeEnvironment.setQualifiers(qualifiers)
        scenario.recreate()
        compose.waitForIdle()
    }
}
