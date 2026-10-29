package dev.nycocado.atlas.compendium

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CARD_RATIO
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.cardpane.ListDetailLayout
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.CardStack
import dev.nycocado.atlas.ui.component.describedButton
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.pressSink
import dev.nycocado.atlas.ui.theme.sinkingSticker
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

// A variant's place in its card group, 1-based.
internal data class VariantPosition(
    val number: Int,
    val count: Int,
)

// The first cell of an open card group, where its stack was: a round button that collapses it, with the group's name
// and count under it, centered in a cell the size of a card's art, so the variants after it keep the grid's columns.
@Composable
internal fun GroupCollapse(
    group: CompendiumEntry.Group,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    val count = group.variants.size
    val interactions = remember { MutableInteractionSource() }
    val sink = pressSink(interactions, Shadow.sticker)
    Column(
        modifier.fillMaxWidth().aspectRatio(CARD_RATIO),
        verticalArrangement = Arrangement.spacedBy(Space.s2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(48.dp)
                .describedButton(
                    pluralStringResource(R.plurals.card_group_open, count, group.name, count),
                    interactions,
                    stringResource(R.string.card_group_collapse),
                    CircleShape,
                    onCollapse,
                ),
        ) {
            Box(
                Modifier
                    .matchParentSize()
                    .sinkingSticker(sink, CircleShape, theme.surfaceSticker, theme.outline, Shadow.sticker),
                contentAlignment = Alignment.Center,
            ) {
                // The back chevron turned to point up.
                Image(
                    painterResource(R.drawable.ic_back),
                    null,
                    Modifier.size(22.dp).rotate(90f),
                    colorFilter = ColorFilter.tint(theme.onSurface),
                )
            }
        }
        // The button already says all of this.
        BasicText(
            groupTitle(group),
            Modifier.clearAndSetSemantics {},
            style = AtlasTypography.titleMd.copy(color = theme.onSurface, textAlign = TextAlign.Center),
        )
    }
}

// The grid items of an open card group, by key: the collapse cell first, then the variants.
internal class GroupBand(
    val keys: List<Any>,
)

// The band behind an open group's cells, drawn from the grid because the cells are plain grid items that wrap from row
// to row with the rest. One bar per row, each its own shape: rounded and closed only where the group starts and ends,
// and open, with no border, where it goes on to the next row, running to the edge of the grid's content. No composable
// lays out a band behind cells of a lazy grid.
internal fun Modifier.groupBands(
    state: LazyGridState,
    bands: List<GroupBand>,
    fill: Color,
    border: Color,
): Modifier =
    drawBehind {
        val radius = 22.dp.toPx()
        val corner = CornerRadius(radius)
        val stroke = DrawStroke(Stroke.thin.toPx())
        val contentRight = size.width - 2 * GridSidePadding.toPx()
        val visible = state.layoutInfo.visibleItemsInfo
        // Item offsets leave out the grid's content padding.
        translate(GridSidePadding.toPx(), state.layoutInfo.beforeContentPadding.toFloat()) {
            bands.forEach { band ->
                visible.filter { it.key in band.keys }.groupBy { it.row }.values.forEach { row ->
                    val starts = row.any { it.key == band.keys.first() }
                    val ends = row.any { it.key == band.keys.last() }
                    val left = if (starts) row.minOf { it.offset.x }.toFloat() else 0f
                    val right = if (ends) row.maxOf { it.offset.x + it.size.width }.toFloat() else contentRight
                    val top = row.minOf { it.offset.y }.toFloat()
                    val bottom = row.maxOf { it.offset.y + it.size.height }.toFloat()
                    val start = if (starts) corner else CornerRadius.Zero
                    val end = if (ends) corner else CornerRadius.Zero
                    val bar = RoundRect(left, top, right, bottom, start, end, end, start)
                    drawPath(Path().apply { addRoundRect(bar) }, fill)
                    drawPath(
                        Path().apply {
                            val rl = start.x
                            val rr = end.x
                            moveTo(left + rl, top)
                            lineTo(right - rr, top)
                            if (ends) {
                                arcTo(Rect(right - 2 * radius, top, right, top + 2 * radius), -90f, 90f, false)
                                lineTo(right, bottom - radius)
                                arcTo(Rect(right - 2 * radius, bottom - 2 * radius, right, bottom), 0f, 90f, false)
                            } else {
                                moveTo(right, bottom)
                            }
                            lineTo(left + rl, bottom)
                            if (starts) {
                                arcTo(Rect(left, bottom - 2 * radius, left + 2 * radius, bottom), 90f, 90f, false)
                                lineTo(left, top + radius)
                                arcTo(Rect(left, top, left + 2 * radius, top + 2 * radius), 180f, 90f, false)
                            }
                        },
                        border,
                        style = stroke,
                    )
                }
            }
        }
    }

// The first variant's art and colors stand for the group.
@Composable
internal fun GroupStack(
    group: CompendiumEntry.Group,
    tilt: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val first = group.variants.first()
    val colors = first.face()?.colors.orEmpty()
    CardStack(
        first.image,
        groupTitle(group),
        colors,
        pluralStringResource(R.plurals.card_group_collapsed, group.variants.size, group.name, group.variants.size),
        tilt,
        onClick,
        stringResource(R.string.card_group_expand),
        modifier.padding(CardMargin),
    )
}

@Composable
internal fun groupTitle(group: CompendiumEntry.Group) =
    stringResource(R.string.card_group_title, group.name, group.variants.size)

@PreviewLightDark
@Composable
internal fun CardGroupCollapsedPreview() = GroupSample(open = false)

@PreviewLightDark
@Composable
internal fun CardGroupOpenPreview() = GroupSample(open = true)

// Enough variants to wrap on a wide window too.
@PreviewLightDark
@Composable
internal fun CardGroupOpenWidePreview() = GroupSample(open = true, variants = 9)

@Composable
private fun GroupSample(
    open: Boolean,
    variants: Int = 3,
) = AtlasTheme {
    BlankCardImages {
        fun trait(
            id: String,
            name: String,
        ) = Trait(id, "classic", "", name, null, null, null, listOf(CardColor.Red), false, emptyList(), emptyList())
        CompendiumContent(
            CompendiumFilter(colors = setOf(CardColor.Red)),
            listOf(
                CompendiumEntry.Group("Kidney", (1..variants).map { trait("kidney-$it", "Kidney") }),
                CompendiumEntry.Single(trait("impatience", "Impatience")),
                CompendiumEntry.Single(trait("late", "Late")),
            ),
            if (open) setOf("Kidney") else emptySet(),
            {},
            {},
            {},
            {},
            ListDetailLayout.Single,
        )
    }
}
