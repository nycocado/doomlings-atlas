package dev.nycocado.atlas

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// The real app on the JVM: the real activity, Hilt and the card data packaged into the assets. Every test runs a
// phone-sized window with Compose's accessibility checks on.
// Hilt wants @HiltAndroidTest on each concrete test class, so a subclass carries it.
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
@OptIn(ExperimentalTestApi::class)
abstract class AtlasBehaviorTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    // Empty, so a test can launch the activity more than once.
    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun enableChecks() {
        hilt.inject()
        compose.enableAccessibilityChecks()
    }

    protected fun launch(): ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

    // A launch after the player accepted the unofficial notice.
    protected fun launchPastNotice(): ActivityScenario<MainActivity> {
        context
            .getSharedPreferences("atlas", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("notice_seen", true)
            .commit()
        return launch()
    }

    protected fun string(
        @StringRes id: Int,
    ) = context.getString(id)

    protected fun text(
        @StringRes id: Int,
    ): SemanticsNodeInteraction = compose.onNodeWithText(string(id))

    protected fun button(
        @StringRes id: Int,
    ): SemanticsNodeInteraction = compose.onNodeWithContentDescription(string(id))

    // A card in the list is described as "Name, color, points": matched by the name it starts with.
    protected fun card(name: String): SemanticsNodeInteraction =
        compose.onNode(hasContentDescription("$name,", substring = true))

    // Compose's own accessibility checks don't run on Robolectric, so this sweeps what is on screen: everything that can
    // be clicked must say what it is. Touch size isn't checked (Compose widens every touch area to 48dp) nor contrast.
    protected fun assertAccessible() {
        val unlabeled =
            compose
                .onAllNodes(hasClickAction())
                .fetchSemanticsNodes()
                // Scrolled out of the window: nothing for a player to hit.
                .filterNot { it.touchBoundsInRoot.isEmpty }
                .filter { node ->
                    node.config
                        .getOrNull(SemanticsProperties.Text)
                        .orEmpty()
                        .isEmpty() &&
                        node.config
                            .getOrNull(SemanticsProperties.ContentDescription)
                            .orEmpty()
                            .isEmpty()
                }
        check(unlabeled.isEmpty()) {
            "Clickable without a label:\n" +
                unlabeled.joinToString("\n") { it.config.toString() }
        }
    }
}
