package dev.nycocado.atlas

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performFirstLinkClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

// Age of Dracula cites Vampirism (assets/doomlings/classic/cards.json), in Português as well (testPortuguese).
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
class CitedCardTest : AtlasBehaviorTest() {
    private fun openAgeOfDracula() {
        text(R.string.kind_ages).performClick()
        compose.onNode(hasContentDescription("Age of Dracula")).performClick()
    }

    private fun tapVampirism() = compose.onNode(hasText("Vampirism", substring = true)).performFirstLinkClick()

    @Test
    fun theCitedNameIsALinkInsideTheEffect() {
        launchPastNotice()
        openAgeOfDracula()

        val link =
            compose
                .onNode(
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.LinkTestMarker),
                ).fetchSemanticsNode()

        // TalkBack reads the effect around the link, and announces the name in it as a link.
        val effect =
            link.parent!!
                .config[SemanticsProperties.Text]
                .single()
                .text
        assertTrue("If Vampirism is in your trait pile" in effect)
        assertTrue(SemanticsActions.OnClick in link.config)
    }

    @Test
    @Config(qualifiers = "w360dp-h640dp-xhdpi")
    fun theCiterKeepsItsScrollAfterBack() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        val effect = compose.onNode(hasText("Vampirism", substring = true))
        val unscrolled = effect.fetchSemanticsNode().positionInRoot
        effect.performTouchInput { swipeUp(startY = bottom, endY = top, durationMillis = 100) }
        compose.waitForIdle()
        val before = effect.fetchSemanticsNode().positionInRoot
        effect.assertIsDisplayed()
        assertNotEquals(unscrolled, before)

        tapVampirism()
        detailOf("Vampirism").assertIsDisplayed()
        pressBack(scenario)

        effect.assertIsDisplayed()
        assertEquals(before, effect.fetchSemanticsNode().positionInRoot)
    }

    @Test
    fun aCitedCardOpensAndBackReturnsToTheCiter() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        detailOf("Age of Dracula").assertIsDisplayed()
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
        detailOf("Age of Dracula").assertIsDisplayed()
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
        detailOf("Age of Dracula").assertIsDisplayed()
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
