package dev.nycocado.atlas

import android.content.Context
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dev.nycocado.atlas.ui.component.pointsText
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// The real app on the JVM: the real activity, Hilt and the card data packaged into the assets. Every test runs a
// phone-sized window.
// Hilt wants @HiltAndroidTest on each concrete test class, so a subclass carries it.
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
abstract class AtlasBehaviorTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    // Empty, so a test can launch the activity more than once.
    @get:Rule(order = 1)
    val compose = createEmptyComposeRule()

    protected val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun inject() {
        hilt.inject()
    }

    protected fun launch(): ActivityScenario<MainActivity> = ActivityScenario.launch(MainActivity::class.java)

    protected fun launchPastNotice(): ActivityScenario<MainActivity> {
        context
            .getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(NOTICE_SEEN, true)
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
    protected fun cardNamed(name: String): SemanticsMatcher = hasContentDescription("$name,", substring = true)

    protected fun card(name: String): SemanticsNodeInteraction = compose.onNode(cardNamed(name))

    protected fun detailOf(name: String): SemanticsNodeInteraction = compose.onNodeWithText(name)

    // The green color filter of the compendium.
    protected val green: SemanticsMatcher get() = isSelectable() and hasText(string(R.string.color_green))

    // The points badge of the open card: its description is the points as the resources write them.
    protected val pointsBadge: SemanticsMatcher
        get() {
            val all = (-20..20).map { context.resources.pointsText(it) }
            return SemanticsMatcher("is a points badge") { node ->
                node.config
                    .getOrNull(SemanticsProperties.ContentDescription)
                    .orEmpty()
                    .any { it in all }
            }
        }

    protected fun scrollListTo(matcher: SemanticsMatcher) {
        compose.onNode(hasScrollAction()).performScrollToNode(matcher)
    }

    // The back button or gesture, as the system dispatches it: a key down and a key up, once the screen has settled.
    protected fun pressBack(scenario: ActivityScenario<MainActivity>) =
        pressBack { event -> scenario.onActivity { it.dispatchKeyEvent(event) } }

    protected fun pressBack(dispatch: (KeyEvent) -> Unit) {
        compose.waitForIdle()
        dispatch(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
        dispatch(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        compose.waitForIdle()
    }

    // Compose's own accessibility checks report NOT_RUN on Robolectric, so this sweeps what is on screen: everything that can
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
