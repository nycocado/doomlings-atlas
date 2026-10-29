package dev.nycocado.atlas

import android.os.Bundle
import android.os.Parcel
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import androidx.savedstate.read
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.cardpane.CardKey
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.Robolectric

// Card names come from assets/doomlings/cards.json, as in PhoneBrowsingTest.
@HiltAndroidTest
class RecreationTest : AtlasBehaviorTest() {
    @Test
    fun recreatingKeepsTheOpenCard() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()

        scenario.recreate()

        detailOf("Adorable").assertIsDisplayed()
    }

    @Test
    fun recreatingKeepsTheFilterAndTheListPosition() {
        val scenario = launchPastNotice()
        compose.onNode(green).performClick()
        scrollListTo(cardNamed("Woody Stems"))

        scenario.recreate()

        card("Woody Stems").assertIsDisplayed()
        card("Appealing").assertDoesNotExist()
        scrollListTo(green)
        compose.onNode(green).assertIsSelected()
    }

    @Test
    fun processDeathBringsBackTheOpenCardAndTheFilter() {
        val saved = saveAfterOpening("Appealing", filterGreen = true)

        restoreInNewActivity(saved)

        detailOf("Appealing").assertIsDisplayed()
        button(R.string.back).performClick()
        compose.onNode(green).assertIsSelected()
        card("Adorable").assertDoesNotExist()
    }

    @Test
    fun aRestoredCardTheDataDoesNotHaveLandsOnTheList() {
        val saved = saveAfterOpening("Adorable", filterGreen = false)
        val renamed = renameSavedCards(saved, "classic/traits/no-longer-in-the-data")
        assertTrue("The saved state holds no card to rename", renamed > 0)

        restoreInNewActivity(saved)

        text(R.string.search_placeholder).assertIsDisplayed()
        card("Adorable").assertIsDisplayed()
    }

    @Test
    fun processDeathBringsBackTheOpenGroups() {
        val stack =
            hasContentDescription(context.resources.getQuantityString(R.plurals.card_group_collapsed, 6, "Kidney", 6))
        val saved =
            saveAfter {
                scrollListTo(stack)
                compose.onNode(stack).performClick()
            }

        restoreInNewActivity(saved)

        scrollListTo(
            hasContentDescription(context.resources.getQuantityString(R.plurals.card_group_open, 6, "Kidney", 6)),
        )
    }

    private fun saveAfterOpening(
        name: String,
        filterGreen: Boolean,
    ): Bundle =
        saveAfter {
            if (filterGreen) compose.onNode(green).performClick()
            card(name).performClick()
        }

    // The state the system keeps when it kills the app in the background.
    private fun saveAfter(interaction: () -> Unit): Bundle {
        launchPastNotice().close()
        val saved = Bundle()
        val activity = Robolectric.buildActivity(MainActivity::class.java).setup()
        interaction()
        activity
            .pause()
            .saveInstanceState(saved)
            .stop()
            .destroy()
        return saved
    }

    // A new activity has a new ViewModel store: only the saved bundle can bring the state back. It goes through a Parcel,
    // as when the system writes it out, so nothing survives by object identity.
    private fun restoreInNewActivity(saved: Bundle) {
        val parcel = Parcel.obtain()
        val restored =
            try {
                parcel.writeBundle(saved)
                parcel.setDataPosition(0)
                parcel.readBundle(MainActivity::class.java.classLoader)!!
            } finally {
                parcel.recycle()
            }
        Robolectric
            .buildActivity(MainActivity::class.java)
            .create(restored)
            .start()
            .restoreInstanceState(restored)
            .resume()
            .visible()
    }

    // The back stack is saved as a list of bundles, each a key's class name and its fields (CardKey holds the id).
    // Returns how many card ids it renamed.
    private fun renameSavedCards(
        bundle: Bundle,
        id: String,
    ): Int {
        var renamed = 0
        if (bundle.getString("type") == CardKey::class.java.name) {
            bundle.getBundle("value")!!.putString("id", id)
            renamed++
        }
        bundle.read { toMap() }.values.forEach { value ->
            when (value) {
                is Bundle -> renamed += renameSavedCards(value, id)
                is List<*> -> renamed += value.filterIsInstance<Bundle>().sumOf { renameSavedCards(it, id) }
            }
        }
        return renamed
    }
}
