package dev.nycocado.atlas.compendium

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
    kinds: List<CardKind>,
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
            KindSegments(kinds, filter.kind) { kind -> onFilterChange { it.copy(kind = kind) } }
        }
        Row(
            Modifier.fillMaxWidth().padding(top = Space.s4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            // The chip is measured first, so the title keeps what is left and the hint drops below it when that runs out.
            FlowRow(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(Space.s2),
                itemVerticalAlignment = Alignment.Bottom,
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
            // No set to offer while the cards load.
            if (sets.isNotEmpty()) {
                SetsChip(filter.sets.size) { showSets = true }
            }
        }
    }
}

private val scrollFadeWidth = 24.dp

// Compose has no fading edge, unlike the View system. Erases the content rather than painting the track's color over
// it, and reads the state while drawing, where the row's first layout has already settled it.
private fun Modifier.scrollFade(state: LazyListState): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
        drawContent()
        val width = scrollFadeWidth.toPx()
        val ltr = layoutDirection == LayoutDirection.Ltr
        if (if (ltr) state.canScrollBackward else state.canScrollForward) {
            drawRect(
                Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), endX = width),
                size = Size(width, size.height),
                blendMode = BlendMode.DstOut,
            )
        }
        if (if (ltr) state.canScrollForward else state.canScrollBackward) {
            drawRect(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Color.Black),
                    startX = size.width - width,
                    endX = size.width,
                ),
                topLeft = Offset(size.width - width, 0f),
                size = Size(width, size.height),
                blendMode = BlendMode.DstOut,
            )
        }
    }

// The smallest a color's name shrinks to fit, fixed in dp: a 10sp minimum grows with the font scale, and at 200% long
// names (Colorless, Vermelho) don't fit even at that minimum.
private val minLabelSize: TextUnit
    @Composable get() = with(LocalDensity.current) { 10.dp.toSp() }

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
            CardKind.GenePool -> R.string.kind_gene_pools
            CardKind.MeaningOfLife -> R.string.kind_meanings_of_life
            CardKind.Treasure -> R.string.kind_treasures
            CardKind.Trinket -> R.string.kind_trinkets
            CardKind.Merchant -> R.string.kind_merchants
        }

private val CardKind.title: Int
    @StringRes get() =
        when (this) {
            CardKind.Trait -> R.string.all_traits
            CardKind.Age -> R.string.all_ages
            CardKind.Catastrophe -> R.string.all_catastrophes
            CardKind.GenePool -> R.string.all_gene_pools
            CardKind.MeaningOfLife -> R.string.all_meanings_of_life
            CardKind.Treasure -> R.string.all_treasures
            CardKind.Trinket -> R.string.all_trinkets
            CardKind.Merchant -> R.string.all_merchants
        }

// A row that scrolls, with eight kinds that never fit a phone; with room to spare they center.
@Composable
private fun KindSegments(
    kinds: List<CardKind>,
    selected: CardKind,
    onSelect: (CardKind) -> Unit,
) {
    val theme = AtlasTheme.colors
    val row = rememberLazyListState()
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .sticker(CircleShape, theme.surfaceSticker, theme.outline, stroke = Stroke.sticker)
            .padding(horizontal = Space.s1),
    ) {
        // The row's own state, not a BringIntoViewRequester: that request goes on to the compendium's grid, which would
        // jump each time the filters come back into it. Only the player's pick animates (docs/design.md, Motion).
        var shownKind by remember { mutableStateOf(selected) }
        LaunchedEffect(selected, kinds, maxWidth) {
            val picked = selected != shownKind
            shownKind = selected
            kinds.indexOf(selected).takeIf { it >= 0 }?.let {
                if (picked) row.animateScrollToItem(it) else row.scrollToItem(it)
            }
        }
        LazyRow(
            Modifier
                .fillMaxWidth()
                // A segment scrolled halfway out is cut along the track's rounded end.
                .clip(CircleShape)
                .scrollFade(row)
                .selectableGroup(),
            state = row,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        ) {
            items(kinds, key = { it }) { kind ->
                val active = kind == selected
                val textColor by animateColorAsState(
                    if (active) theme.onPrimary else theme.onSurface,
                    label = "segment text",
                )
                val fill by animateColorAsState(
                    if (active) theme.primary else Color.Transparent,
                    label = "segment fill",
                )
                Box(
                    Modifier
                        // Taller than the selected segment's 40dp, up to the minimum touch target.
                        .heightIn(min = Sizes.touchTarget)
                        // Drawn, so it follows the segment's height with large text.
                        .drawBehind {
                            val inset = Space.s1.toPx()
                            val height = size.height - inset * 2
                            drawRoundRect(fill, Offset(0f, inset), Size(size.width, height), CornerRadius(height / 2))
                        }.selectable(active, role = Role.RadioButton) { onSelect(kind) }
                        .padding(horizontal = Space.s4),
                    contentAlignment = Alignment.Center,
                ) {
                    BasicText(
                        stringResource(kind.label),
                        style = AtlasTypography.labelLg.copy(color = textColor),
                        maxLines = 1,
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

// Only some kinds offered, as with Classic alone.
@PreviewLightDark
@Composable
internal fun KindSegmentsClassicPreview() =
    AtlasTheme {
        Box(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) {
            KindSegments(
                listOf(CardKind.Trait, CardKind.Age, CardKind.Catastrophe, CardKind.GenePool),
                CardKind.GenePool,
            ) {}
        }
    }

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
            CardKind.entries.forEach { KindSegments(CardKind.entries, it) {} }
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
    Box(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) { Filters(filter, CardKind.entries, sets) {} }
}
