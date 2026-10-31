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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
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
import dev.nycocado.atlas.ui.theme.pressSink
import dev.nycocado.atlas.ui.theme.sinkingSticker

// The first cell of an open card group, where its stack was: a round button that collapses it, with the group's name
// and count under it, centered in a cell the size of a card's art, so the variants after it keep the grid's columns.
@Composable
internal fun GroupCollapse(
    cell: GridCell.Collapse,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    val count = cell.count
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
                    pluralStringResource(R.plurals.card_group_open, count, cell.name, count),
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
            groupTitle(cell.name, count),
            Modifier.clearAndSetSemantics {},
            style = AtlasTypography.titleMd.copy(color = theme.onSurface, textAlign = TextAlign.Center),
        )
    }
}

// The first variant's art and colors stand for the group.
@Composable
internal fun GroupStack(
    cell: GridCell.Stack,
    tilt: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val first = cell.variants.first()
    val colors = first.face()?.colors.orEmpty()
    CardStack(
        first.image,
        groupTitle(cell.name, cell.variants.size),
        colors,
        pluralStringResource(R.plurals.card_group_collapsed, cell.variants.size, cell.name, cell.variants.size),
        tilt,
        onClick,
        stringResource(R.string.card_group_expand),
        modifier.padding(CardMargin),
    )
}

@Composable
private fun groupTitle(
    name: String,
    count: Int,
) = stringResource(R.string.card_group_title, name, count)

@PreviewLightDark
@Composable
internal fun CardGroupCollapsedPreview() = GroupSample(open = false)

@PreviewLightDark
@Composable
internal fun CardGroupOpenPreview() = GroupSample(open = true)

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
        ) = Trait(
            id,
            "classic",
            "",
            name,
            null,
            emptyList(),
            null,
            null,
            listOf(CardColor.Red),
            false,
            emptyList(),
            emptyList(),
        )
        CompendiumContent(
            CompendiumFilter(colors = setOf(CardColor.Red)),
            listOf(
                CompendiumEntry.Group("Kidney", (1..variants).map { trait("kidney-$it", "Kidney") }),
                CompendiumEntry.Single(trait("impatience", "Impatience")),
                CompendiumEntry.Single(trait("late", "Late")),
            ).toGridCells(if (open) setOf("Kidney") else emptySet()),
            {},
            {},
            {},
            {},
            ListDetailLayout.Single,
        )
    }
}
