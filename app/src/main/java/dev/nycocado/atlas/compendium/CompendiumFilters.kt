package dev.nycocado.atlas.compendium

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.card.CardSet
import dev.nycocado.atlas.ui.component.TraitColorOptions
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait

@Composable
internal fun Filters(
    filter: CompendiumFilter,
    sets: List<CardSet>,
    onFilterChange: ((CompendiumFilter) -> CompendiumFilter) -> Unit,
) {
    val theme = AtlasTheme.colors
    var showSets by rememberSaveable { mutableStateOf(false) }
    if (showSets) {
        SetsSheet(
            sets,
            filter.sets,
            onToggle = { id -> onFilterChange { it.copy(sets = if (id in it.sets) it.sets - id else it.sets + id) } },
            onDismiss = { showSets = false },
        )
    }
    Column(Modifier.fillMaxWidth()) {
        // The controls center on wide screens; the title below stays in line with the first column of cards.
        Column(Modifier.widthIn(max = MaxWidth.content).align(Alignment.CenterHorizontally)) {
            AnimatedVisibility(
                visible = filter.kind == CardKind.Trait,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = Space.s4).selectableGroup(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TraitColorOptions.forEach { color ->
                        val selected = color in filter.colors
                        // Narrower than their 64dp only when the row can't fit them, as beside a card on a foldable.
                        ColorDisc(color, selected, Modifier.weight(1f, fill = false)) {
                            onFilterChange { it.copy(colors = if (color in it.colors) emptySet() else setOf(color)) }
                        }
                    }
                }
            }
            KindSegments(filter.kind) { kind -> onFilterChange { it.copy(kind = kind) } }
            // No set to offer while none has a confirmed name.
            if (sets.isNotEmpty()) {
                SetsChip(filter.sets.size, Modifier.padding(top = Space.s3).align(Alignment.End)) { showSets = true }
            }
        }
        Row(
            Modifier.padding(top = Space.s4),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            BasicText(
                stringResource(filter.kind.title),
                Modifier.semantics { heading() },
                style = AtlasTypography.titleLg.copy(color = theme.onSurface),
            )
            AnimatedVisibility(
                visible = filter.kind == CardKind.Trait && filter.colors.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                BasicText(
                    stringResource(R.string.color_hint),
                    Modifier.padding(bottom = 3.dp),
                    style = AtlasTypography.bodySm.copy(color = theme.onSurfaceVariant),
                )
            }
        }
    }
}

// The smallest a label shrinks to fit, fixed in dp: a 10sp minimum grows with the font scale, and at 200% long names
// (Colorless, Vermelho, Catastrophes) don't fit even at that minimum.
private val minLabelSize: TextUnit
    @Composable get() = with(LocalDensity.current) { 10.dp.toSp() }

// One color at a time for now: Classic has no two-color traits.
@Composable
private fun ColorDisc(
    color: CardColor?,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val theme = AtlasTheme.colors
    val name = stringResource(colorName(color))
    Column(
        modifier
            .width(64.dp)
            .selectable(selected, role = Role.RadioButton, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val ring by animateFloatAsState(
            if (selected) 1f else 0f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "ring",
        )
        Box(Modifier.size(60.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 0.8f + 0.2f * ring
                        scaleY = 0.8f + 0.2f * ring
                        alpha = ring.coerceIn(0f, 1f)
                    }.border(Stroke.strong, theme.outline, CircleShape),
            )
            Box(
                Modifier.size(48.dp).sticker(CircleShape, theme.trait(color).fill, theme.outline),
                contentAlignment = Alignment.Center,
            ) { TraitGlyph(color, theme.trait(color).onFill) }
        }
        BasicText(
            name,
            style = AtlasTypography.labelSm.copy(color = theme.onSurface),
            maxLines = 1,
            autoSize =
                TextAutoSize.StepBased(
                    minFontSize = minLabelSize,
                    maxFontSize = AtlasTypography.labelSm.fontSize,
                ),
        )
    }
}

private val CardKind.label: Int
    @StringRes get() =
        when (this) {
            CardKind.Trait -> R.string.kind_traits
            CardKind.Age -> R.string.kind_ages
            CardKind.Catastrophe -> R.string.kind_catastrophes
        }

private val CardKind.title: Int
    @StringRes get() =
        when (this) {
            CardKind.Trait -> R.string.all_traits
            CardKind.Age -> R.string.all_ages
            CardKind.Catastrophe -> R.string.all_catastrophes
        }

@Composable
private fun KindSegments(
    selected: CardKind,
    onSelect: (CardKind) -> Unit,
) {
    val theme = AtlasTheme.colors
    val gap = 6.dp
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .sticker(CircleShape, theme.surfaceSticker, theme.outline, stroke = Stroke.sticker)
            .padding(horizontal = Space.s1),
    ) {
        val count = CardKind.entries.size
        val width = (maxWidth - gap * (count - 1)) / count
        val offset by animateDpAsState((width + gap) * selected.ordinal, label = "segment")
        // Drawn, so it follows the segments' height with large text.
        Row(
            Modifier
                .drawBehind {
                    val start = offset.toPx()
                    val x = if (layoutDirection == LayoutDirection.Ltr) start else size.width - width.toPx() - start
                    val inset = Space.s1.toPx()
                    val height = size.height - inset * 2
                    drawRoundRect(theme.primary, Offset(x, inset), Size(width.toPx(), height), CornerRadius(height / 2))
                }.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(gap),
        ) {
            CardKind.entries.forEach { kind ->
                val active = kind == selected
                val textColor by animateColorAsState(
                    if (active) theme.onPrimary else theme.onSurface,
                    label = "segment text",
                )
                Box(
                    Modifier
                        .weight(1f)
                        // Taller than the selected segment's 40dp, up to the minimum touch target.
                        .heightIn(min = Sizes.touchTarget)
                        .selectable(active, role = Role.RadioButton) { onSelect(kind) },
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        stringResource(kind.label),
                        Modifier.padding(horizontal = Space.s2),
                        style = AtlasTypography.labelLg.copy(color = textColor),
                        maxLines = 1,
                        autoSize =
                            TextAutoSize.StepBased(
                                minFontSize = minLabelSize,
                                maxFontSize = AtlasTypography.labelLg.fontSize,
                            ),
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun FiltersPreview() = FiltersSample(CompendiumFilter())

@PreviewLightDark
@Composable
internal fun FiltersGreenPreview() = FiltersSample(CompendiumFilter(colors = setOf(CardColor.Green)))

@PreviewLightDark
@Composable
internal fun FiltersAgesPreview() = FiltersSample(CompendiumFilter(kind = CardKind.Age))

@PreviewLightDark
@Composable
internal fun ColorDiscPreview() =
    AtlasTheme {
        Row(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            horizontalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            ColorDisc(CardColor.Blue, selected = false, Modifier) {}
            ColorDisc(CardColor.Green, selected = true, Modifier) {}
        }
    }

@PreviewLightDark
@Composable
internal fun KindSegmentsPreview() =
    AtlasTheme {
        Column(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            CardKind.entries.forEach { KindSegments(it) {} }
        }
    }

@PreviewLightDark
@Composable
internal fun FiltersSetsChosenPreview() =
    FiltersSample(CompendiumFilter(sets = setOf("castle-glass", "mokoko-village")), SampleSets)

@Composable
private fun FiltersSample(
    filter: CompendiumFilter,
    sets: List<CardSet> = emptyList(),
) = AtlasTheme {
    Box(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) { Filters(filter, sets) {} }
}
