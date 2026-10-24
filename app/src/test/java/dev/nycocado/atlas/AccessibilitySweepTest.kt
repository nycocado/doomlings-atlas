package dev.nycocado.atlas

import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertThrows
import org.junit.Test

// The sweep is only worth running if it can fail: this is its proof.
@HiltAndroidTest
class AccessibilitySweepTest : AtlasBehaviorTest() {
    @Test
    fun aClickableWithoutALabelFailsTheSweep() {
        launchPastNotice().onActivity { activity ->
            val view = ComposeView(activity).apply { setContent { Box(Modifier.size(48.dp).clickable {}) } }
            val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
            activity.addContentView(view, ViewGroup.LayoutParams(wrap, wrap))
        }
        compose.waitForIdle()

        assertThrows(IllegalStateException::class.java) { assertAccessible() }
    }
}
