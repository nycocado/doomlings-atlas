package dev.nycocado.atlas

import androidx.activity.BackEventCompat
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performFirstLinkClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.core.view.WindowCompat
import androidx.test.core.app.ActivityScenario
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
        // The citing effect is its own block now, short and low on the screen: the swipe goes over the whole card.
        compose.onRoot().performTouchInput { swipeUp(startY = bottom * 0.8f, endY = top, durationMillis = 100) }
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

    // Light icons are the dark header's; a card with a light panel asks for dark ones while it is open.
    private fun ActivityScenario<MainActivity>.darkIcons(): Boolean {
        var dark = false
        onActivity {
            dark =
                WindowCompat
                    .getInsetsController(
                        it.window,
                        it.window.decorView,
                    ).isAppearanceLightStatusBars
        }
        return dark
    }

    @Test
    fun theCiterKeepsItsStatusBarIconsAfterBackFromTheCitedCard() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        compose.waitForIdle()
        val citer = scenario.darkIcons()
        assertTrue("Age of Dracula's cream panel asks for dark icons", citer)

        tapVampirism()
        pressBack(scenario)
        compose.waitUntilDoesNotExist(hasText("Vampirism"))

        detailOf("Age of Dracula").assertIsDisplayed()
        assertEquals(citer, scenario.darkIcons())
    }

    @Test
    fun aCancelledBackGestureKeepsTheCardsStatusBarIcons() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        compose.waitForIdle()
        assertTrue(scenario.darkIcons())

        scenario.onActivity {
            val back = it.onBackPressedDispatcher
            back.dispatchOnBackStarted(BackEventCompat(0.1f, 0.5f, 0.2f, BackEventCompat.EDGE_LEFT))
            back.dispatchOnBackProgressed(BackEventCompat(0.3f, 0.5f, 0.5f, BackEventCompat.EDGE_LEFT))
        }
        compose.waitForIdle()
        scenario.onActivity { it.onBackPressedDispatcher.dispatchOnBackCancelled() }
        compose.waitForIdle()

        detailOf("Age of Dracula").assertIsDisplayed()
        assertTrue(scenario.darkIcons())
    }

    @Test
    fun theListHasLightStatusBarIconsAfterBackFromACard() {
        val scenario = launchPastNotice()
        openAgeOfDracula()
        pressBack(scenario)

        text(R.string.search_placeholder).assertIsDisplayed()
        assertEquals(false, scenario.darkIcons())
    }

    @Test
    fun tappingBackTwiceWhileACitedCardFadesOutLeavesTheCiterOpen() {
        launchPastNotice()
        openAgeOfDracula()
        tapVampirism()
        detailOf("Vampirism").assertIsDisplayed()

        tapBackAgainMidFade()

        detailOf("Age of Dracula").assertIsDisplayed()
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
