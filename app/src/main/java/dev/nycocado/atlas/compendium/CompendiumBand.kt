package dev.nycocado.atlas.compendium

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Stroke as StrokeWidth

// The grid items of an open card group: the key of the first (its collapse cell), of the last, and all of them.
internal class GroupBand(
    val first: Any,
    val last: Any,
    val keys: Set<Any>,
)

// A cell of a group that is on screen: its row in the grid and where it is, in the grid's own coordinates.
internal data class BandCell(
    val key: Any,
    val row: Int,
    val bounds: Rect,
)

// Where the cells of the open groups are right now, animation included: the grid's layout info only knows where they
// end up. Written as the cells are placed and read only as the bands are drawn, so nothing recomposes as they move.
internal class CellBounds {
    val rects = mutableStateMapOf<Any, Rect>()
}

@Composable
internal fun Modifier.reportBounds(
    key: Any,
    cells: CellBounds,
): Modifier {
    DisposableEffect(key) { onDispose { cells.rects.remove(key) } }
    return onGloballyPositioned { cells.rects[key] = it.boundsInParent() }
}

// The rectangles of a band, one per row of the group that is on screen, inset by `inset` so that bands next to each
// other keep apart. A row runs from the group's first cell or the content's left edge, to its last cell or the
// content's right edge, and reaches down to the next row when the two overlap, so that they fuse; rows that don't
// overlap stay apart. A group whose first or last row is off screen goes on past the viewport.
internal fun bandRects(
    band: GroupBand,
    cells: List<BandCell>,
    contentLeft: Float,
    contentRight: Float,
    viewportTop: Float,
    viewportBottom: Float,
    inset: Float,
    overscan: Float,
): List<Rect> {
    val rows =
        cells
            .groupBy { it.row }
            .toSortedMap()
            .entries
            .toList()
    val spans =
        rows.map { (_, row) ->
            val starts = row.any { it.key == band.first }
            val ends = row.any { it.key == band.last }
            Rect(
                left = if (starts) row.minOf { it.bounds.left } else contentLeft,
                top = row.minOf { it.bounds.top },
                right = if (ends) row.maxOf { it.bounds.right } else contentRight,
                bottom = row.maxOf { it.bounds.bottom },
            )
        }
    val touching =
        spans.indices.map { index ->
            // Whether this row fuses with the next one: it is the next row of the grid, and they overlap.
            val next = spans.getOrNull(index + 1)
            next != null &&
                rows[index + 1].key == rows[index].key + 1 &&
                spans[index].left < next.right &&
                next.left < spans[index].right
        }
    return spans.mapIndexed { index, span ->
        val fusedAbove = index > 0 && touching[index - 1]
        val top =
            when {
                fusedAbove -> span.top
                index == 0 && cells.none { it.key == band.first } -> viewportTop - overscan
                else -> span.top + inset
            }
        val bottom =
            when {
                touching[index] -> spans[index + 1].top
                index == spans.lastIndex && cells.none { it.key == band.last } -> viewportBottom + overscan
                else -> span.bottom - inset
            }
        Rect(span.left + inset, top, span.right - inset, bottom)
    }
}

// What the bands are drawn with, which depends only on the colors and the density, so it is made once.
private class BandLook(
    density: Density,
    fillColor: Color,
) {
    val radius = Radius.md.topStart.toPx(Size.Zero, density)
    private val corners = PathEffect.cornerPathEffect(radius)
    val stroke = Stroke(with(density) { StrokeWidth.thin.toPx() }, pathEffect = corners)

    // A fill takes no path effect, so the corners are rounded on a paint.
    val fill =
        Paint().apply {
            color = fillColor
            pathEffect = corners
        }
}

// The band behind an open group's cells, drawn from the grid because the cells are plain grid items that wrap from row
// to row with the rest. The rects of its rows are united and the outline is stroked once, with every corner rounded,
// the concave ones that a row step makes too.
@Composable
internal fun Modifier.groupBands(
    state: LazyGridState,
    bands: List<GroupBand>,
    cells: CellBounds,
    fill: Color,
    border: Color,
): Modifier {
    val density = LocalDensity.current
    val look = remember(density, fill) { BandLook(density, fill) }
    return drawBehind {
        val padding = GridSidePadding.toPx()
        val onScreen = state.layoutInfo.visibleItemsInfo.associateBy { it.key }
        bands.forEach { band ->
            val inBand =
                band.keys.mapNotNull { key ->
                    onScreen[key]?.let { info ->
                        // The cell as it is placed now, or where the grid puts it before that has been reported.
                        val bounds =
                            cells.rects[key] ?: Rect(
                                padding + info.offset.x,
                                state.layoutInfo.beforeContentPadding + info.offset.y.toFloat(),
                                padding + info.offset.x + info.size.width,
                                state.layoutInfo.beforeContentPadding + info.offset.y + info.size.height.toFloat(),
                            )
                        BandCell(key, info.row, bounds)
                    }
                }
            if (inBand.isNotEmpty()) {
                val shape =
                    bandRects(
                        band,
                        inBand,
                        contentLeft = padding,
                        contentRight = size.width - padding,
                        viewportTop = 0f,
                        viewportBottom = size.height,
                        inset = BandInset.toPx(),
                        // The rounded end is a radius long and its stroke has width: twice the radius puts both
                        // well outside the viewport.
                        overscan = 2 * look.radius,
                    ).map { Path().apply { addRect(it) } }
                        .reduce { all, rect -> Path.combine(PathOperation.Union, all, rect) }
                drawIntoCanvas { it.drawPath(shape, look.fill) }
                drawPath(shape, border, style = look.stroke)
            }
        }
    }
}
