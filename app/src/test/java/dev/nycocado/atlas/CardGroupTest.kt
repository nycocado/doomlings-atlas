package dev.nycocado.atlas

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionCollection
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

// Classic has 6 Kidneys and 5 Swarms (assets/doomlings/cards.json); the other names in these tests are unique.
@HiltAndroidTest
class CardGroupTest : AtlasBehaviorTest() {
    private val kidneyStack get() = collapsedStack("Kidney", 6)
    private val swarmStack get() = collapsedStack("Swarm", 5)

    @Test
    fun aGroupShowsAsOneStackWithItsVariantCount() {
        launchPastNotice()
        scrollListTo(kidneyStack)

        compose.onNode(kidneyStack).assertIsDisplayed()
        variants("Kidney", 6).assertCountEquals(0)
        assertAccessible()
    }

    @Test
    fun tappingTheStackShowsItsVariantsAndCollapsingHidesThem() {
        launchPastNotice()
        scrollListTo(kidneyStack)

        compose.onNode(kidneyStack).performClick()

        compose.onNode(kidneyStack).assertDoesNotExist()
        compose.onNode(openGroup("Kidney", 6)).assertExists()
        variants("Kidney", 6).assertCountEquals(6)
        scrollListTo(openGroup("Kidney", 6))
        compose.waitForIdle()
        compose.onNode(openGroup("Kidney", 6)).performClick()
        compose.waitForIdle()
        compose.onNode(kidneyStack).assertExists()
        variants("Kidney", 6).assertCountEquals(0)
    }

    @Test
    fun tappingAVariantOpensItsCardDetail() {
        launchPastNotice()
        scrollListTo(kidneyStack)
        compose.onNode(kidneyStack).performClick()

        scrollListTo(variant("Kidney", 2, 6))
        compose.onNode(variant("Kidney", 2, 6)).performClick()

        detailOf("Kidney").assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun twoGroupsStayOpenAtOnce() {
        launchPastNotice()
        scrollListTo(kidneyStack)
        compose.onNode(kidneyStack).performClick()
        scrollListTo(swarmStack)
        compose.onNode(swarmStack).performClick()

        variants("Swarm", 5).assertCountEquals(5)
        scrollListTo(openGroup("Kidney", 6))
        variants("Kidney", 6).assertCountEquals(6)
    }

    @Test
    fun openGroupsSurviveActivityRecreation() {
        val scenario = launchPastNotice()
        scrollListTo(kidneyStack)
        compose.onNode(kidneyStack).performClick()

        scenario.recreate()

        scrollListTo(openGroup("Kidney", 6))
        variants("Kidney", 6).assertCountEquals(6)
    }

    @Test
    fun aFreshStartHasEveryGroupCollapsed() {
        launchPastNotice().close()

        launchPastNotice()

        scrollListTo(kidneyStack)
        compose.onNode(kidneyStack).assertIsDisplayed()
    }

    private fun collapsedStack(
        name: String,
        count: Int,
    ) = hasContentDescription(context.resources.getQuantityString(R.plurals.card_group_collapsed, count, name, count))

    private fun openGroup(
        name: String,
        count: Int,
    ) = hasContentDescription(context.resources.getQuantityString(R.plurals.card_group_open, count, name, count))

    // A variant is described as the card is, followed by its position in the group.
    private fun variant(
        name: String,
        position: Int,
        count: Int,
    ) = hasContentDescription("$name,", substring = true) and
        hasContentDescription(context.getString(R.string.card_variant_position, "", position, count), substring = true)

    private fun variants(
        name: String,
        count: Int,
    ): SemanticsNodeInteractionCollection =
        compose.onAllNodes(
            SemanticsMatcher("is a variant of $name") { node ->
                val description =
                    node.config
                        .getOrNull(SemanticsProperties.ContentDescription)
                        ?.firstOrNull()
                        .orEmpty()
                description.startsWith("$name,") &&
                    (1..count).any {
                        description.endsWith(
                            context.getString(R.string.card_variant_position, "", it, count),
                        )
                    }
            },
        )
}
