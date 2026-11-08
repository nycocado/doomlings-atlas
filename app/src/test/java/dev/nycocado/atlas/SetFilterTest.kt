package dev.nycocado.atlas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
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
