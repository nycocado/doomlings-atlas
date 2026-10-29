package dev.nycocado.atlas.compendium

import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import androidx.compose.ui.graphics.drawscope.Stroke as DrawStroke

// A variant's place in its card group, 1-based.
internal data class VariantPosition(
    val number: Int,
    val count: Int,
)

// The first cell of an open card group, where its stack was: the button that collapses it.
@Composable
internal fun GroupCollapse(
    group: CompendiumEntry.Group,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    val count = group.variants.size
    Column(
        modifier
            .fillMaxWidth()
            .aspectRatio(CARD_RATIO)
            .sticker(Radius.cardSm, theme.surfaceSticker, theme.outline, shadow = Shadow.sticker)
            .describedButton(
                pluralStringResource(R.plurals.card_group_open, count, group.name, count),
                remember { MutableInteractionSource() },
                stringResource(R.string.card_group_collapse),
                onCollapse,
            ).padding(Space.s2),
        verticalArrangement = Arrangement.spacedBy(Space.s2, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // The back chevron turned to point up.
        Image(
            painterResource(R.drawable.ic_back),
            null,
            Modifier.size(28.dp).rotate(90f),
            colorFilter = ColorFilter.tint(theme.onSurface),
        )
        BasicText(
            groupTitle(group),
            style = AtlasTypography.titleMd.copy(color = theme.onSurface, textAlign = TextAlign.Center),
        )
    }
}

// The grid items of an open card group, by key: the collapse cell first, then the variants.
internal class GroupBand(
    val keys: List<Any>,
)

// The band behind an open group's cells, drawn from the grid because the cells are plain grid items that wrap from row
// to row with the rest: one rectangle per row, rounded only where the group starts and ends, and running off the grid's
// edge where it goes on to the next row, so no border closes it there. No composable lays out a band behind cells of a
// lazy grid.
internal fun Modifier.groupBands(
    state: LazyGridState,
    bands: List<GroupBand>,
    fill: Color,
    border: Color,
): Modifier =
    drawBehind {
        val corner = CornerRadius(22.dp.toPx())
        val bleed = 24.dp.toPx()
        val seam = CellGapDown.toPx() / 2 + 1
        val visible = state.layoutInfo.visibleItemsInfo
        bands.forEach { band ->
            val rows = visible.filter { it.key in band.keys }.groupBy { it.row }.values
            val shape =
                rows
                    .map { row ->
                        val starts = row.any { it.key == band.keys.first() }
                        val ends = row.any { it.key == band.keys.last() }
                        val left = if (starts) row.minOf { it.offset.x }.toFloat() else -bleed
                        val right =
                            if (ends) {
                                row.maxOf { it.offset.x + it.size.width }.toFloat()
                            } else {
                                size.width +
                                    bleed
                            }
                        val top = row.minOf { it.offset.y } - if (starts) 0f else seam
                        val bottom = row.maxOf { it.offset.y + it.size.height } + if (ends) 0f else seam
                        Path().apply {
                            addRoundRect(
                                RoundRect(
                                    left,
                                    top,
                                    right,
                                    bottom,
                                    topLeftCornerRadius = if (starts) corner else CornerRadius.Zero,
                                    topRightCornerRadius = if (starts && ends) corner else CornerRadius.Zero,
                                    bottomRightCornerRadius = if (ends) corner else CornerRadius.Zero,
                                    bottomLeftCornerRadius = if (starts && ends) corner else CornerRadius.Zero,
                                ),
                            )
                        }
                    }.reduceOrNull { all, row -> Path.combine(PathOperation.Union, all, row) }
            if (shape != null) {
                // Item offsets leave out the grid's content padding.
                translate(GridSidePadding.toPx(), state.layoutInfo.beforeContentPadding.toFloat()) {
                    drawPath(shape, fill)
                    drawPath(shape, border, style = DrawStroke(Stroke.thin.toPx()))
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

@Composable
private fun GroupSample(open: Boolean) =
    AtlasTheme {
        BlankCardImages {
            fun trait(
                id: String,
                name: String,
            ) = Trait(id, "classic", "", name, null, null, null, listOf(CardColor.Red), false, emptyList(), emptyList())
            CompendiumContent(
                CompendiumFilter(colors = setOf(CardColor.Red)),
                listOf(
                    CompendiumEntry.Group("Kidney", (1..3).map { trait("kidney-$it", "Kidney") }),
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
