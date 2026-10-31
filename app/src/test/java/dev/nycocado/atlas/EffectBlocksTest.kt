package dev.nycocado.atlas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

// Self-Replicating (assets/doomlings/classic/cards.json): an Action effect, "Play 1 card from the discard pile. Ignore its
// action ({action}).", then a plain one; in made-up Português as well (testPortuguese).
@HiltAndroidTest
class EffectBlocksTest : AtlasBehaviorTest() {
    private fun openSelfReplicating() {
        scrollListTo(cardNamed("Self-Replicating"))
        card("Self-Replicating").performClick()
        detailOf("Self-Replicating").assertIsDisplayed()
    }

    @Test
    fun talkBackReadsTheKindsNameAndTheTokensNameInTheText() {
        launchPastNotice()
        openSelfReplicating()

        compose.onNode(hasText(string(R.string.icon_action).uppercase())).assertExists()
        compose.onNode(hasText("Ignore its action (${string(R.string.icon_action)}).", substring = true)).assertExists()
        assertAccessible()
    }

    @Test
    fun theKindsNameAndTheTokensNameFollowTheCardLanguage() {
        launchPastNotice()
        openSelfReplicating()
        compose.onNode(isSelectable() and hasText("Português")).performClick()

        compose.onNode(hasText("AÇÃO")).assertExists()
        compose.onNode(hasText("Ignore a ação dela (Ação).", substring = true)).assertExists()
    }
}
