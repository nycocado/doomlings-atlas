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

// Enough variants to wrap on a wide window too, after cards that make the group start and end in the middle of a row.
@PreviewLightDark
@Composable
internal fun CardGroupOpenWidePreview() = GroupSample(open = true, variants = 9, before = 2)

// Two open groups one after the other.
@PreviewLightDark
@Composable
internal fun CardGroupAdjacentPreview() = GroupSample(open = true, variants = 2, adjacent = true)

@Composable
private fun GroupSample(
    open: Boolean,
    variants: Int = 3,
    before: Int = 0,
    adjacent: Boolean = false,
) = AtlasTheme {
    BlankCardImages {
        fun trait(
            id: String,
            name: String,
        ) = Trait(id, "classic", "", name, null, null, null, listOf(CardColor.Red), false, emptyList(), emptyList())
        CompendiumContent(
            CompendiumFilter(colors = setOf(CardColor.Red)),
            buildList {
                (1..before).forEach { add(CompendiumEntry.Single(trait("before-$it", "Before $it"))) }
                add(CompendiumEntry.Group("Kidney", (1..variants).map { trait("kidney-$it", "Kidney") }))
                if (adjacent) add(CompendiumEntry.Group("Swarm", (1..variants).map { trait("swarm-$it", "Swarm") }))
                add(CompendiumEntry.Single(trait("impatience", "Impatience")))
                add(CompendiumEntry.Single(trait("late", "Late")))
            },
            if (open) setOfNotNull("Kidney", "Swarm".takeIf { adjacent }) else emptySet(),
            {},
            {},
            {},
            {},
            ListDetailLayout.Single,
        )
    }
}
