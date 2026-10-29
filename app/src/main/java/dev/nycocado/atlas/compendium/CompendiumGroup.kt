package dev.nycocado.atlas.compendium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.cardpane.ListDetailLayout
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.CardStack
import dev.nycocado.atlas.ui.component.describedButton
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke

// A variant's place in its card group, 1-based.
internal data class VariantPosition(
    val number: Int,
    val count: Int,
)

// An open card group: the stack gives way to its variants in a band that starts with the button that collapses it.
@Composable
internal fun OpenGroup(
    group: CompendiumEntry.Group,
    onCollapse: () -> Unit,
    onVariantClick: (NamedCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    val count = group.variants.size
    Column(
        modifier
            .fillMaxWidth()
            .background(theme.surfaceSunk, Radius.md)
            .border(Stroke.thin, theme.outlineSoft, Radius.md)
            .padding(Space.s2),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .describedButton(
                    pluralStringResource(R.plurals.card_group_open, count, group.name, count),
                    remember { MutableInteractionSource() },
                    stringResource(R.string.card_group_collapse),
                    Radius.md,
                    onCollapse,
                ).padding(horizontal = CardMargin),
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The back chevron turned to point up.
            Image(
                painterResource(R.drawable.ic_back),
                null,
                Modifier.size(22.dp).rotate(90f),
                colorFilter = ColorFilter.tint(theme.onSurface),
            )
            BasicText(groupTitle(group), style = AtlasTypography.titleMd.copy(color = theme.onSurface))
        }
        // Nothing existing fits: the variants must line up with the grid's columns, FlowRow has no adaptive columns,
        // a grid can't nest in the scrolling one, and a band can't be drawn around separate grid items.
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = ((maxWidth + CellGapAcross) / (CellMinWidth + CellGapAcross)).toInt().coerceAtLeast(1)
            val cell = (maxWidth - CellGapAcross * (columns - 1)) / columns
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CellGapAcross),
                verticalArrangement = Arrangement.spacedBy(CellGapDown),
            ) {
                group.variants.forEachIndexed { index, variant ->
                    CompendiumCard(
                        variant,
                        tiltAt(index),
                        { onVariantClick(variant) },
                        Modifier.width(cell),
                        VariantPosition(index + 1, count),
                    )
                }
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
