package dev.nycocado.atlas.compendium

import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.ui.theme.Space

// What the list's grid and an open card group's band share, so their cells line up.

// Room inside each grid cell for the card's tilt and shadow, which draw past its bounds. Item animations draw the
// card into a layer the size of its cell, so without this room they cut the corners and the shadow.
internal val CardMargin = 8.dp

// The grid's side padding, where the first cell starts: the cards sit at the screen margin.
internal val GridSidePadding = Space.s5 - CardMargin

internal val CellMinWidth = 150.dp

// Between two cells; the cards' own margins add the rest of the 18dp across and 22dp down.
internal val CellGapAcross = 18.dp - CardMargin * 2
internal val CellGapDown = 22.dp - CardMargin * 2

// Deterministic by position, never random per render (docs/design.md).
private val Tilts = listOf(-2.5f, 2f, 1.5f, -2f)

internal fun tiltAt(index: Int) = Tilts[index % Tilts.size]

internal val CompendiumEntry.Group.gridKey get() = "group:$name"
