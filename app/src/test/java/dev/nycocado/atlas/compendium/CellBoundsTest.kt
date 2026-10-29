package dev.nycocado.atlas.compendium

import android.content.ComponentName
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

// The band follows the cells as they move because the bounds they report move with them, frame by frame.
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class CellBoundsTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    // The app's manifest has no activity to host a test composition: this one is declared here.
    @Before
    fun declareHost() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        shadowOf(context.packageManager).addActivityIfNotPresent(ComponentName(context, ComponentActivity::class.java))
    }

    private fun setContent(content: @Composable () -> Unit) {
        ActivityScenario.launch(ComponentActivity::class.java).onActivity { it.setContent(content = content) }
    }

    private val cells = CellBounds()
    private var items by mutableStateOf(listOf("a", "b", "c"))

    @Test
    fun theReportedBoundsMoveFrameByFrameWhileACellAnimatesToItsPlace() {
        setContent {
            LazyVerticalGrid(GridCells.Fixed(1)) {
                items(items, key = { it }) { key ->
                    Box(
                        Modifier
                            .animateItem(placementSpec = tween<IntOffset>(300))
                            .reportBounds(key, cells)
                            .size(100.dp),
                    )
                }
            }
        }
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false
        val before = cells.rects.getValue("c").top

        compose.runOnUiThread {
            items = listOf("a", "c")
            Snapshot.sendApplyNotifications()
        }
        compose.mainClock.advanceTimeBy(150)
        val during = cells.rects.getValue("c").top
        compose.mainClock.advanceTimeBy(600)
        println(
            "DBG " +
                cells.rects.entries
                    .map { it.key to it.value }
                    .toList(),
        )
        val after = cells.rects.getValue("c").top

        assertTrue("c starts at $before and ends at $after, but was at $during", during < before && during > after)
        assertEquals(setOf("a", "c"), cells.rects.keys)
    }
}
