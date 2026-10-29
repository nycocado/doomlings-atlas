package dev.nycocado.atlas.compendium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.cardpane.ListDetailLayout
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.FocusRing
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.trait

// The same columns as the grid around it (Adaptive 150dp, 18dp between cards), so variants line up with the cards
// above and below the band.
private val CellMinWidth = 150.dp
private val CellGap = 18.dp - CardMargin * 2

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
    val description = pluralStringResource(R.plurals.card_group_open, count, group.name, count)
    val interactions = remember { MutableInteractionSource() }
    val label = stringResource(R.string.card_group_collapse)
    Column(
        modifier
            .fillMaxWidth()
            .background(theme.surfaceSunk, Radius.md)
            .border(Stroke.thin, theme.outlineSoft, Radius.md)
            .padding(horizontal = Space.s2, vertical = Space.s2),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(
                    interactions,
                    FocusRing(theme.focusRing, Radius.cardSm),
                    onClickLabel = label,
                    onClick = onCollapse,
                ).padding(horizontal = CardMargin)
                .clearAndSetSemantics {
                    contentDescription = description
                    role = Role.Button
                    onClick(label) { onCollapse().let { true } }
                },
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(R.drawable.ic_collapse),
                null,
                Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(theme.onSurface),
            )
            BasicText(groupTitle(group), style = AtlasTypography.titleMd.copy(color = theme.onSurface))
        }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val columns = ((maxWidth + CellGap) / (CellMinWidth + CellGap)).toInt().coerceAtLeast(1)
            val cell = (maxWidth - CellGap * (columns - 1)) / columns
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(CellGap),
                verticalArrangement = Arrangement.spacedBy(22.dp - CardMargin * 2),
            ) {
                group.variants.forEachIndexed { index, variant ->
                    CompendiumCard(
                        variant,
                        Tilts[index % Tilts.size],
                        { onVariantClick(variant) },
                        Modifier.width(cell),
                        position = index + 1 to count,
                    )
                }
            }
        }
    }
}

internal fun groupTitle(group: CompendiumEntry.Group) = "${group.name} ×${group.variants.size}"

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
