package dev.nycocado.atlas.compendium

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.CardThumb
import dev.nycocado.atlas.ui.component.Dock
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.component.signed
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.trait
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

// Room inside each grid cell for the card's tilt and shadow, which draw past its bounds. Item animations draw the
// card into a layer the size of its cell, so without this room they cut the corners and the shadow.
private val CardMargin = 8.dp

// Starts at the header's side margin, so the rail lines up with the logo above it.
private val RailWidth = Space.s5 + 64.dp + Space.s4

private val RailMinWidth = 600.dp

// One duration and curve for everything that moves when the layout changes, so it all starts and lands together
// instead of each part settling at its own pace.
private val ReflowSpec = tween<Rect>(350, easing = FastOutSlowInEasing)
private val Reflow = BoundsTransform { _, _ -> ReflowSpec }
private val MoveSpec = tween<IntOffset>(350, easing = FastOutSlowInEasing)

// Deterministic by position, never random per render (docs/design.md).
private val Tilts = listOf(-2.5f, 2f, 1.5f, -2f)

@Composable
fun CompendiumScreen(
    viewModel: CompendiumViewModel,
    onCardClick: (String) -> Unit,
    onAboutClick: () -> Unit,
    besideCard: Boolean,
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    CompendiumContent(filter, results, viewModel::updateFilter, onCardClick, onAboutClick, besideCard)
}

@Composable
fun CompendiumContent(
    filter: CompendiumFilter,
    results: List<NamedCard>?,
    onFilterChange: (CompendiumFilter) -> Unit,
    onCardClick: (String) -> Unit,
    onAboutClick: () -> Unit,
    // The card beside the list has its own way to About.
    besideCard: Boolean = false,
) {
    val theme = AtlasTheme.colors
    val distance = with(LocalDensity.current) { CollapseDistance.toPx() }
    // Saveable like the grid's position, so coming back from a card restores both together.
    val collapse = rememberSaveable(distance, saver = HeaderCollapse.saver(distance)) { HeaderCollapse(distance) }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    // A text field keeps focus until something takes it, so the search lets go when the user moves on: tapping
    // outside it, scrolling the list or opening a card.
    val focusManager = LocalFocusManager.current
    LaunchedEffect(grid) {
        snapshotFlow { grid.isScrollInProgress }.filter { it }.collect { focusManager.clearFocus() }
    }
    // Tapping the current destination goes back to the top of the list.
    val onCardsClick: () -> Unit = {
        scope.launch { grid.animateScrollToItem(0) }
        scope.launch { collapse.expand() }
    }
    BoxWithConstraints {
        // Decided by the compendium's own width, not the window's: beside a card on a foldable or a small tablet it
        // has a phone's room, and takes the phone layout.
        val rail = maxWidth >= RailMinWidth
        // The header changes shape, its parts and the dock move to their new places when the layout switches. Only
        // right after that switch: when the list jumps to a new width without switching, like beside a card on a
        // large tablet, they jump with it.
        val switched = remember { LayoutSwitch(rail) }
        switched.track(rail)
        val switchMotion = BoundsTransform { _, _ -> if (switched.recent()) ReflowSpec else snap() }
        val dock =
            remember {
                movableContentOf { vertical: Boolean, modifier: Modifier, itemModifier: Modifier ->
                    Dock(onCardsClick, modifier, vertical, itemModifier)
                }
            }
        // On the rail layout nothing sits below the grid, so it keeps clear of the navigation bar itself.
        val bottom = if (rail) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier
        // The rail layout's header is a single row with nothing to collapse, so it doesn't take part in scrolling.
        val collapsesOnScroll = if (rail) Modifier else Modifier.nestedScroll(collapse)
        val fraction: () -> Float = if (rail) ({ 0f }) else collapse::fraction
        // Around the whole screen, where the dock stays put while the header collapses and the row it sits in grows.
        LookaheadScope {
            val itemMotion = Modifier.animateBounds(this, boundsTransform = switchMotion)
            Column(
                Modifier
                    .fillMaxSize()
                    .background(theme.surface)
                    .then(collapsesOnScroll)
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
            ) {
                Header(
                    filter.query,
                    fraction,
                    collapse,
                    rail,
                    onAboutClick,
                    showAbout = !besideCard,
                    switchMotion,
                ) { onFilterChange(filter.copy(query = it)) }
                Row(Modifier.weight(1f).then(bottom)) {
                    // Inside the row, so the header collapsing above doesn't move anything within this scope: the cards
                    // only animate when the layout itself changes, like a card opening beside the list or a filter.
                    LookaheadScope {
                        val reflow = this
                        if (rail) {
                            Box(
                                Modifier
                                    .width(RailWidth)
                                    .fillMaxHeight()
                                    .padding(start = Space.s5, end = Space.s4, top = Space.s4, bottom = Space.s4),
                                contentAlignment = Alignment.Center,
                            ) { dock(true, itemMotion, itemMotion) }
                        }
                        // With the list on its own, a margin as wide as the rail on the other side centers the content
                        // on the screen like the search. Beside a card the list needs that room.
                        Column(
                            Modifier.weight(1f).padding(end = if (rail && !besideCard) RailWidth else 0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            LazyVerticalGrid(
                                state = grid,
                                columns = GridCells.Adaptive(150.dp),
                                // The grid's own clip leaves out the items fading out after a filter.
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .widthIn(max = MaxWidth.wide)
                                        .clipScrollableContainer(Orientation.Vertical),
                                contentPadding =
                                    PaddingValues(
                                        start = Space.s5 - CardMargin,
                                        end = Space.s5 - CardMargin,
                                        top = Space.s4,
                                        bottom = Space.s5 - CardMargin,
                                    ),
                                horizontalArrangement = Arrangement.spacedBy(18.dp - CardMargin * 2),
                                verticalArrangement = Arrangement.spacedBy(22.dp - CardMargin * 2),
                            ) {
                                fullWidth("filters", reflow) { Filters(filter, onFilterChange) }
                                if (results?.isEmpty() == true) {
                                    fullWidth("empty", reflow) {
                                        BasicText(
                                            stringResource(R.string.no_results),
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = Space.s6)
                                                // Read out as it appears, since the search field keeps the focus.
                                                .semantics { liveRegion = LiveRegionMode.Polite },
                                            style =
                                                AtlasTypography.bodyMd.copy(
                                                    color = theme.onSurfaceVariant,
                                                    textAlign = TextAlign.Center,
                                                ),
                                        )
                                    }
                                }
                                itemsIndexed(results.orEmpty(), key = { _, card -> card.id }) { index, card ->
                                    val tilt = Tilts[index % Tilts.size]
                                    CompendiumCard(
                                        card,
                                        tilt,
                                        {
                                            focusManager.clearFocus()
                                            onCardClick(card.id)
                                        },
                                        // animateBounds skips moves inside the grid, which places its items as
                                        // scrolling.
                                        Modifier
                                            .animateItem(placementSpec = MoveSpec)
                                            .animateBounds(reflow, boundsTransform = Reflow),
                                    )
                                }
                            }
                            if (!rail) {
                                dock(
                                    false,
                                    // Bounds first, as on the rail, so the same animation carries over between both.
                                    itemMotion
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .padding(start = Space.s4, end = Space.s4, top = Space.s3, bottom = Space.s4),
                                    itemMotion,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun LazyGridScope.fullWidth(
    key: String,
    reflow: LookaheadScope,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) {
    Box(
        Modifier
            .fillMaxWidth()
            .animateItem(placementSpec = null)
            .animateBounds(reflow, boundsTransform = Reflow)
            .padding(start = CardMargin, end = CardMargin, bottom = CardMargin),
    ) {
        content()
    }
}

private class LayoutSwitch(
    private var rail: Boolean,
) {
    private var at = 0L

    fun track(rail: Boolean) {
        if (rail != this.rail) {
            this.rail = rail
            at = System.nanoTime()
        }
    }

    fun recent() = System.nanoTime() - at < 700_000_000L
}

@Composable
private fun CompendiumCard(
    card: NamedCard,
    tilt: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val trait = card as? Trait
    val cardColors = trait?.colors
    val description =
        buildList {
            add(card.name)
            cardColors?.ifEmpty { listOf(null) }?.forEach { add(stringResource(colorName(it))) }
            trait?.points?.let { add(pluralStringResource(R.plurals.card_points, it, signed(it))) }
        }.joinToString(", ")
    CardThumb(card.image, card.name, cardColors, description, tilt, onClick, modifier.padding(CardMargin))
}

@PreviewLightDark
@Composable
internal fun CompendiumPreview() =
    AtlasTheme {
        BlankCardImages {
            val cards =
                listOf(CardColor.Blue, CardColor.Green, CardColor.Purple, CardColor.Red).map {
                    Trait(
                        "$it",
                        "classic",
                        "",
                        "${it.name} trait",
                        null,
                        null,
                        1,
                        listOf(it),
                        false,
                        emptyList(),
                        emptyList(),
                    )
                }
            CompendiumContent(CompendiumFilter(colors = setOf(CardColor.Green)), cards, {}, {}, {})
        }
    }
