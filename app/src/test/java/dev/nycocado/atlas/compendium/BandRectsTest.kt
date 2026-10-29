package dev.nycocado.atlas.compendium

import androidx.compose.ui.geometry.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// A grid of 3 columns of 100 wide with a gap of 2, rows 160 tall with a gap of 6, in a viewport 1000 tall whose content
// runs from x = 10 to x = 322; the band is inset by 1.
class BandRectsTest {
    private val left = 10f
    private val right = 322f
    private val inset = 1f

    private fun cell(
        key: String,
        row: Int,
        column: Int,
    ) = BandCell(key, row, Rect(left + column * 102f, row * 166f, left + column * 102f + 100f, row * 166f + 160f))

    private fun rects(
        band: GroupBand,
        vararg cells: BandCell,
    ) = bandRects(
        band,
        cells.toList(),
        contentLeft = left,
        contentRight = right,
        viewportTop = 0f,
        viewportBottom = 1000f,
        inset = inset,
        overscan = 50f,
    )

    private val band = GroupBand("collapse", "v3", setOf("collapse", "v1", "v2", "v3"))

    @Test
    fun aGroupInOneRowIsOneRectInsetOnAllSides() {
        val single = GroupBand("collapse", "v2", setOf("collapse", "v1", "v2"))

        val result = rects(single, cell("collapse", 0, 0), cell("v1", 0, 1), cell("v2", 0, 2))

        assertEquals(listOf(Rect(11f, 1f, 313f, 159f)), result)
    }

    @Test
    fun aGroupThatEndsMidRowFusesItsRowsOverTheGapBetweenThem() {
        val result = rects(band, cell("collapse", 0, 0), cell("v1", 0, 1), cell("v2", 0, 2), cell("v3", 1, 0))

        // The first row reaches down to the top of the second, which is as wide as its one cell.
        assertEquals(listOf(Rect(11f, 1f, 321f, 166f), Rect(11f, 166f, 109f, 325f)), result)
    }

    @Test
    fun aGroupThatStartsMidRowRunsFromItsFirstCellAndContinuesFromTheContentEdge() {
        val result = rects(band, cell("collapse", 0, 1), cell("v1", 0, 2), cell("v2", 1, 0), cell("v3", 1, 1))

        assertEquals(listOf(Rect(113f, 1f, 321f, 166f), Rect(11f, 166f, 211f, 325f)), result)
    }

    // The collapse cell alone in the last column: the second row ends before the first begins, so there is nothing
    // to fuse and two separate shapes are better than a sliver joining them.
    @Test
    fun rowsThatDoNotOverlapAreNotJoined() {
        val two = GroupBand("collapse", "v2", setOf("collapse", "v1", "v2"))

        val result = rects(two, cell("collapse", 0, 2), cell("v1", 1, 0), cell("v2", 1, 1))

        assertEquals(listOf(Rect(215f, 1f, 321f, 159f), Rect(11f, 167f, 211f, 325f)), result)
    }

    @Test
    fun aGroupThatEndsTheListIsClosedAtItsLastCell() {
        val result = rects(band, cell("collapse", 0, 2), cell("v1", 1, 0), cell("v2", 1, 1), cell("v3", 1, 2))

        assertEquals(325f, result.last().bottom)
        assertEquals(313f, result.last().right)
    }

    @Test
    fun aFirstRowOutsideTheViewportLetsTheBandRunPastItsTop() {
        // Only the second row is on screen: the collapse cell, which starts the group, is not.
        val result = rects(band, cell("v1", 1, 0), cell("v2", 1, 1), cell("v3", 1, 2))

        assertEquals(1, result.size)
        assertTrue(result.single().top < 0f)
        // And it goes on from the content's left edge, not from a cell.
        assertEquals(11f, result.single().left)
    }

    @Test
    fun aLastRowOutsideTheViewportLetsTheBandRunPastItsBottom() {
        val result = rects(band, cell("collapse", 5, 0), cell("v1", 5, 1), cell("v2", 5, 2))

        assertTrue(result.single().bottom > 1000f)
        assertEquals(321f, result.single().right)
    }

    @Test
    fun bandsThatShareARowDoNotTouch() {
        val first = GroupBand("a", "a1", setOf("a", "a1"))
        val second = GroupBand("b", "b1", setOf("b", "b1"))

        val one = rects(first, cell("a", 0, 0), cell("a1", 0, 1))
        val other =
            bandRects(
                second,
                listOf(cell("b", 0, 2), cell("b1", 1, 0)),
                left,
                right,
                viewportTop = 0f,
                viewportBottom = 1000f,
                inset = inset,
                overscan = 50f,
            )

        assertTrue(one.single().right < other.first().left)
        assertTrue(other.first().top >= one.single().top)
    }

    @Test
    fun bandsOnConsecutiveRowsKeepTheirDistance() {
        val upper = GroupBand("a", "a1", setOf("a", "a1"))
        val lower = GroupBand("b", "b1", setOf("b", "b1"))

        val above = rects(upper, cell("a", 0, 0), cell("a1", 0, 1)).single()
        val below = rects(lower, cell("b", 1, 0), cell("b1", 1, 1)).single()

        assertTrue(below.top - above.bottom >= 2 * inset)
    }
}
