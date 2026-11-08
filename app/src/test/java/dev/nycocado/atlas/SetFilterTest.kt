package dev.nycocado.atlas

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardSet
import dev.nycocado.atlas.compendium.SetsSheet
import dev.nycocado.atlas.ui.theme.AtlasTheme
import org.junit.Assert.assertTrue
import org.junit.Test

// The sets are made up in TestCardRepositoryModule: Adorable is alone in "Test Expansion", Altruistic is in "Test Base".
@HiltAndroidTest
class SetFilterTest : AtlasBehaviorTest() {
    private val chosenChip get() = context.getString(R.string.sets_filter_chosen, 1)

    private fun setRow(name: String) = compose.onNode(isToggleable() and hasText(name))

    private fun chooseExpansion() {
        text(R.string.sets_filter).performClick()
        setRow(TEST_EXPANSION_SET).performClick()
        compose.waitForIdle()
    }

    @Test
    fun choosingASetNarrowsTheList() {
        launchPastNotice()
        card("Altruistic").assertIsDisplayed()

        chooseExpansion()

        setRow(TEST_EXPANSION_SET).assertIsOn()
        card("Adorable").assertExists()
        card("Altruistic").assertDoesNotExist()
        compose.onNodeWithText(chosenChip).assertExists()
    }

    @Test
    fun choosingASetWithNoCardOfTheOpenKindOpensTraits() {
        launchPastNotice()
        text(R.string.kind_ages).performClick()
        text(R.string.all_ages).assertExists()

        chooseExpansion()

        text(R.string.all_traits).assertExists()
        text(R.string.kind_ages).assertDoesNotExist()
        card("Adorable").assertExists()
    }

    @Test
    fun theChoiceSurvivesRecreation() {
        val scenario = launchPastNotice()
        chooseExpansion()

        scenario.recreate()

        compose.onNodeWithText(chosenChip).assertExists()
        card("Adorable").assertExists()
        card("Altruistic").assertDoesNotExist()
    }

    @Test
    fun theChipSharesTheTitleRowAtItsEnd() {
        launchPastNotice()

        val title = text(R.string.all_traits).getBoundsInRoot()
        val chip = text(R.string.sets_filter).getBoundsInRoot()

        assertTrue(chip.top < title.bottom && title.top < chip.bottom)
        assertTrue(title.right < chip.left)
    }

    // Ten rows are taller than half of the test's window, where a partially expanded sheet would stop.
    @Test
    fun theSheetOpensTallEnoughToShowEverySet() {
        val sets = (1..10).map { CardSet("set-$it", "Test Set $it", 2024) }
        launchPastNotice().onActivity {
            it.setContent {
                AtlasTheme { SetsSheet(sets, emptySet(), onToggle = {}, onDismiss = {}) }
            }
        }

        sets.forEach { setRow(it.name.orEmpty()).assertIsDisplayed() }
    }

    @Test
    fun theSheetAndTheCompendiumWithSetsChosenAreAccessible() {
        launchPastNotice()
        chooseExpansion()

        assertAccessible()
    }

    @Test
    fun theCardDetailShowsTheSetName() {
        launchPastNotice()

        card("Adorable").performClick()

        compose.onNodeWithText(TEST_EXPANSION_SET).assertIsDisplayed()
    }
}
