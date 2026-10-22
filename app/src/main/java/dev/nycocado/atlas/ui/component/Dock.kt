package dev.nycocado.atlas.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Space

private enum class Destination(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
) {
    Cards(R.string.dock_cards, R.drawable.ic_dock_cards),
    Score(R.string.dock_score, R.drawable.ic_dock_score),
    Scanner(R.string.dock_scanner, R.drawable.ic_dock_scanner),
    Tutorial(R.string.dock_tutorial, R.drawable.ic_dock_tutorial),
}

// The rail (vertical) shows icons only; labels go to semantics. The pill and each item stay the same nodes in both
// shapes, so a caller can animate one into the other through itemModifier and its own modifier.
@Composable
fun Dock(
    onCardsClick: () -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
    itemModifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    Box(
        modifier
            .then(
                if (vertical) {
                    Modifier.width(64.dp)
                } else {
                    Modifier.widthIn(max = MaxWidth.compact).fillMaxWidth().height(64.dp)
                },
            ).background(theme.inverseSurface, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        DockLine(
            vertical,
            if (vertical) {
                Modifier.padding(
                    vertical = Space.s2,
                )
            } else {
                Modifier.fillMaxWidth().padding(horizontal = Space.s2)
            },
        ) { Destination.entries.forEach { DockItem(it, vertical, onCardsClick, itemModifier) } }
    }
}

// The items as a Column (rail) or a Row (bar) would place them. Handwritten as one layout: moved between two parents,
// a caller's animation can't glide them on their first frames.
@Composable
private fun DockLine(
    vertical: Boolean,
    modifier: Modifier,
    content: @Composable () -> Unit,
) = Layout(content, modifier) { measurables, constraints ->
    val arrangement = if (vertical) Arrangement.spacedBy(Space.s3) else Arrangement.SpaceAround
    val spacing = arrangement.spacing.roundToPx()
    val main = if (vertical) constraints.maxHeight else constraints.maxWidth
    val cross = if (vertical) constraints.maxWidth else constraints.maxHeight
    var used = 0
    var lastSpace = 0
    val items =
        measurables.map {
            val room = if (main == Constraints.Infinity) Constraints.Infinity else (main - used).coerceAtLeast(0)
            val item =
                it.measure(
                    if (vertical) {
                        Constraints(
                            maxWidth = cross,
                            maxHeight = room,
                        )
                    } else {
                        Constraints(maxWidth = room, maxHeight = cross)
                    },
                )
            val size = if (vertical) item.height else item.width
            lastSpace = minOf(spacing, (main - used - size).coerceAtLeast(0))
            used += size + lastSpace
            item
        }
    val sizes = IntArray(items.size) { if (vertical) items[it].height else items[it].width }
    val length = maxOf(used - lastSpace, if (vertical) constraints.minHeight else constraints.minWidth)
    val thickness =
        maxOf(
            items.maxOfOrNull { if (vertical) it.width else it.height } ?: 0,
            if (vertical) constraints.minWidth else constraints.minHeight,
        )
    val positions = IntArray(items.size)
    if (vertical) {
        with(arrangement) { arrange(length, sizes, positions) }
        layout(thickness, length) {
            items.forEachIndexed { i, item ->
                item.place(Alignment.CenterHorizontally.align(item.width, thickness, layoutDirection), positions[i])
            }
        }
    } else {
        with(arrangement) { arrange(length, sizes, layoutDirection, positions) }
        layout(length, thickness) {
            items.forEachIndexed { i, item ->
                item.place(positions[i], Alignment.CenterVertically.align(item.height, thickness))
            }
        }
    }
}

@Composable
private fun DockItem(
    destination: Destination,
    vertical: Boolean,
    onCardsClick: () -> Unit,
    modifier: Modifier,
) {
    val theme = AtlasTheme.colors
    val label = stringResource(destination.label)
    if (destination == Destination.Cards) {
        Row(
            modifier
                .height(48.dp)
                .background(theme.highlight, CircleShape)
                .clickable(onClick = onCardsClick)
                .padding(horizontal = if (vertical) 12.dp else Space.s4)
                .clearAndSetSemantics {
                    contentDescription = label
                    selected = true
                    onClick { onCardsClick().let { true } }
                },
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(destination.icon),
                null,
                Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(theme.onHighlight),
            )
            if (!vertical) {
                BasicText(label, style = AtlasTypography.labelLg.copy(color = theme.onHighlight), maxLines = 1)
            }
        }
    } else {
        val comingSoon = stringResource(R.string.coming_soon, label)
        Box(
            modifier.size(48.dp).clearAndSetSemantics {
                contentDescription = comingSoon
                disabled()
            },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(destination.icon),
                null,
                Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(theme.onInverseSurface.copy(alpha = 0.38f)),
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun DockPreview() =
    AtlasTheme {
        Row(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) {
            Dock({}, Modifier.weight(1f))
            Dock({}, Modifier.padding(start = Space.s4), vertical = true)
        }
    }
