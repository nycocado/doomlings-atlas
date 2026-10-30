package dev.nycocado.atlas

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.annotation.Config

// Classic has 6 Kidneys (assets/doomlings/cards.json).
@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class CardGroupWideWindowTest : AtlasBehaviorTest() {
    private val stack =
        hasContentDescription(context.resources.getQuantityString(R.plurals.card_group_collapsed, 6, "Kidney", 6))
    private val open =
        hasContentDescription(context.resources.getQuantityString(R.plurals.card_group_open, 6, "Kidney", 6))

    @Test
    fun theCompendiumIsAccessibleWithAGroupCollapsedAndOpen() {
        launchPastNotice()
        scrollListTo(stack)
        assertAccessible()

        compose.onNode(stack).performClick()

        compose.onNode(open).assertExists()
        assertAccessible()
    }

    @Test
    fun aVariantOpensBesideTheListWhichKeepsTheGroupOpen() {
        launchPastNotice()
        scrollListTo(stack)
        compose.onNode(stack).performClick()

        compose
            .onNode(
                hasContentDescription("Kidney,", substring = true) and
                    hasContentDescription(
                        context.getString(R.string.card_variant_position, "", 3, 6),
                        substring = true,
                    ),
            ).performClick()

        detailOf("Kidney").assertIsDisplayed()
        compose.onNode(open).assertExists()
        assertAccessible()
    }
}
