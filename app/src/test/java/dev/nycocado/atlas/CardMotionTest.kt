package dev.nycocado.atlas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeWithVelocity
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.annotation.Config

// An unfolded foldable: wide enough for a card beside the list, which then has room for two columns of cards and four
// on its own. Card names come from assets/doomlings/classic/cards.json.
@OptIn(ExperimentalTestApi::class)
@HiltAndroidTest
@Config(qualifiers = "w852dp-h883dp-xhdpi")
class CardMotionTest : AtlasBehaviorTest() {
    @Test
    fun cardsStayPutAfterScrollingOnceTheCardBesideTheListCloses() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()
        pressBack(scenario)
        compose.waitUntilDoesNotExist(hasText(string(R.string.unofficial_notice)))
        compose.waitForIdle()

        compose.mainClock.autoAdvance = false
        val before = positions()
        dragListUp()
        check(positions() != before) { "The list didn't scroll" }

        val moving = cardsMovingOnTheirOwn()
        check(moving.isEmpty()) { "Moved after the scroll: ${moving.keys}" }
    }

    // A card in the list is described as "Name, color, points".
    private val isCard =
        SemanticsMatcher("is a card in the list") { node ->
            node.config
                .getOrNull(SemanticsProperties.ContentDescription)
                .orEmpty()
                .any { ", " in it }
        }

    // A finger dragging the list up a few rows and lifting still, so it doesn't fling.
    private fun dragListUp() {
        compose.onNode(scrollingList).performTouchInput {
            swipeWithVelocity(center, center.copy(y = center.y - height * 0.4f), endVelocity = 0f, durationMillis = 300)
        }
        compose.mainClock.advanceTimeByFrame()
    }

    private fun cardsMovingOnTheirOwn(): Map<String, Offset> {
        val start = positions()
        repeat(30) { compose.mainClock.advanceTimeByFrame() }
        return positions().filter { (name, at) -> start[name]?.let { (at - it).getDistance() > 0.5f } == true }
    }

    private fun positions(): Map<String, Offset> =
        compose.onAllNodes(isCard).fetchSemanticsNodes().associate { node ->
            node.config[SemanticsProperties.ContentDescription].first() to node.positionInRoot
        }
}
