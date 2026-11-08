package dev.nycocado.atlas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.compendium.CompendiumViewModel
import org.junit.Test
import org.robolectric.annotation.Config

// Only Classic is enabled: it has Gene Pools and The Birth of Life, and none of the expansions' kinds.
@HiltAndroidTest
class CardKindsTest : AtlasBehaviorTest() {
    @Test
    fun theOpenKindGoingAwayOpensTraits() {
        val scenario = launchPastNotice()
        card("Adorable").assertIsDisplayed()

        openKind(scenario, CardKind.Merchant)

        text(R.string.all_traits).assertIsDisplayed()
        text(R.string.kind_traits).assertIsSelected()
        text(R.string.kind_merchants).assertDoesNotExist()
    }

    @Test
    fun aGenePoolShowsItsTypeWhereACardShowsItsName() {
        launchPastNotice()

        text(R.string.kind_gene_pools).performClick()
        compose.onAllNodes(hasContentDescription(string(R.string.type_gene_pool)))[0].performClick()

        compose.onAllNodesWithText(string(R.string.type_gene_pool)).onFirst().assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun theBirthOfLifeIsAmongTheAges() {
        launchPastNotice()

        text(R.string.kind_ages).performClick()

        scrollListTo(hasContentDescription("The Birth of Life"))
        compose.onNode(hasContentDescription("The Birth of Life")).assertIsDisplayed()
    }

    @Test
    fun theKindsAreAccessibleOnAPhone() {
        launchPastNotice()
        card("Adorable").assertIsDisplayed()

        assertAccessible()
    }

    private fun openKind(
        scenario: ActivityScenario<MainActivity>,
        kind: CardKind,
    ) {
        scenario.onActivity { activity ->
            ViewModelProvider(activity)[CompendiumViewModel::class.java].updateFilter { it.copy(kind = kind) }
        }
        compose.waitForIdle()
    }
}

@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class CardKindsWideWindowTest : AtlasBehaviorTest() {
    @Test
    fun theKindsAreAccessibleOnAWideWindow() {
        launchPastNotice()
        card("Adorable").assertIsDisplayed()

        assertAccessible()
    }
}
