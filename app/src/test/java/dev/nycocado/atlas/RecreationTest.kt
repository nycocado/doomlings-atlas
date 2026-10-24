package dev.nycocado.atlas

import android.os.Bundle
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
import dev.nycocado.atlas.cardpane.CardKey
import org.junit.Test
import org.robolectric.Robolectric

// Card names come from assets/doomlings/cards.json, as in PhoneBrowsingTest.
@HiltAndroidTest
class RecreationTest : AtlasBehaviorTest() {
    private val green get() = isSelectable() and hasText(string(R.string.color_green))

    @Test
    fun recreatingKeepsTheOpenCard() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()

        scenario.recreate()

        compose.onNodeWithText("Adorable").assertIsDisplayed()
    }

    @Test
    fun recreatingKeepsTheFilterAndTheListPosition() {
        val scenario = launchPastNotice()
        compose.onNode(green).performClick()
        compose.onNode(hasScrollAction()).performScrollToNode(hasContentDescription("Woody Stems,", substring = true))

        scenario.recreate()

        card("Woody Stems").assertIsDisplayed()
        card("Appealing").assertDoesNotExist()
        compose.onNode(hasScrollAction()).performScrollToNode(green)
        compose.onNode(green).assertIsSelected()
    }

    @Test
    fun processDeathBringsBackTheOpenCardAndTheFilter() {
        val saved = saveAfterOpening("Appealing", filterGreen = true)

        restoreInNewActivity(saved)

        compose.onNodeWithText("Appealing").assertIsDisplayed()
        button(R.string.back).performClick()
        compose.onNode(green).assertIsSelected()
        card("Adorable").assertDoesNotExist()
    }

    @Test
    fun aRestoredCardTheDataDoesNotHaveLandsOnTheList() {
        val saved = saveAfterOpening("Adorable", filterGreen = false)
        renameSavedCards(saved, "classic/traits/no-longer-in-the-data")

        restoreInNewActivity(saved)

        text(R.string.search_placeholder).assertIsDisplayed()
        card("Adorable").assertIsDisplayed()
    }

    // The state the system keeps when it kills the app in the background.
    private fun saveAfterOpening(
        name: String,
        filterGreen: Boolean,
    ): Bundle {
        launchPastNotice().close()
        val saved = Bundle()
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup()
        if (filterGreen) compose.onNode(green).performClick()
        card(name).performClick()
        activity
            .pause()
            .saveInstanceState(saved)
            .stop()
            .destroy()
        return saved
    }

    // A new activity has a new ViewModel store: only the saved bundle can bring the state back.
    private fun restoreInNewActivity(saved: Bundle) {
        Robolectric
            .buildActivity(MainActivity::class.java)
            .create(saved)
            .start()
            .restoreInstanceState(saved)
            .resume()
            .visible()
    }

    // The back stack is saved as a list of bundles, each a key's class name and its fields (CardKey holds the id).
    private fun renameSavedCards(
        bundle: Bundle,
        id: String,
    ) {
        if (bundle.getString("type") == CardKey::class.java.name) bundle.getBundle("value")!!.putString("id", id)
        bundle.keySet().forEach { key ->
            when (val value = bundle.get(key)) {
                is Bundle -> renameSavedCards(value, id)
                is List<*> -> value.filterIsInstance<Bundle>().forEach { renameSavedCards(it, id) }
            }
        }
    }
}
