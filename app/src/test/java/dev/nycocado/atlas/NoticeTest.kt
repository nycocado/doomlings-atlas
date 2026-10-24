package dev.nycocado.atlas

import android.view.KeyEvent
import android.view.MotionEvent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.robolectric.shadows.ShadowDialog

@HiltAndroidTest
class NoticeTest : AtlasBehaviorTest() {
    @Test
    fun firstLaunchShowsTheNotice() {
        launch()

        text(R.string.unofficial_explanation).assertIsDisplayed()
        text(R.string.got_it).assertIsDisplayed()
        assertAccessible()
    }

    @Test
    fun backDoesNotDismissIt() {
        launch()

        val dialog = ShadowDialog.getLatestDialog()
        dialog.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
        dialog.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        compose.waitForIdle()

        text(R.string.got_it).assertIsDisplayed()
    }

    @Test
    fun tappingOutsideDoesNotDismissIt() {
        launch()

        val view = ShadowDialog.getLatestDialog().window!!.decorView
        // The dialog's window wraps the notice, so a touch past its edge lands on the dimmed screen behind it.
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach {
            view.dispatchTouchEvent(MotionEvent.obtain(0, 0, it, -200f, -200f, 0))
        }
        compose.waitForIdle()

        text(R.string.got_it).assertIsDisplayed()
    }

    @Test
    fun acceptingItShowsTheCompendium() {
        launch()

        text(R.string.got_it).performClick()

        text(R.string.got_it).assertDoesNotExist()
        text(R.string.search_placeholder).assertIsDisplayed()
    }

    @Test
    fun itNeverShowsAgainAfterANewLaunch() {
        launch().use { text(R.string.got_it).performClick() }

        launch()

        text(R.string.search_placeholder).assertIsDisplayed()
        text(R.string.got_it).assertDoesNotExist()
    }
}
