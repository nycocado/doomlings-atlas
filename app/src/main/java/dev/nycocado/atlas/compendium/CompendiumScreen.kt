package dev.nycocado.atlas.compendium

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
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
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.carddetail.LongNameTrait
import dev.nycocado.atlas.cardpane.ListDetailLayout
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.CardThumb
import dev.nycocado.atlas.ui.component.Dock
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.component.pointsText
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.update.AppUpdate
import dev.nycocado.atlas.update.SampleUpdate
import dev.nycocado.atlas.update.UpdateSticker
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

// Starts at the header's side margin, so the rail lines up with the logo above it.
private val RailWidth = Space.s5 + Sizes.dock + Space.s4

private val RailMinWidth = 600.dp

private val MoveSpec = tween<IntOffset>(350, easing = FastOutSlowInEasing)

@Composable
fun CompendiumScreen(
    viewModel: CompendiumViewModel,
    onCardClick: (String) -> Unit,
    onAboutClick: () -> Unit,
    layout: ListDetailLayout,
    update: AppUpdate?,
    onInstallUpdate: () -> Unit,
    onDismissUpdate: () -> Unit,
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val cells by viewModel.cells.collectAsStateWithLifecycle()
    CompendiumContent(
        filter,
        cells,
        viewModel::updateFilter,
        viewModel::toggleGroup,
        onCardClick,
        onAboutClick,
        layout,
        update = update,
        onInstallUpdate = onInstallUpdate,
        onDismissUpdate = onDismissUpdate,
    )
}

@Composable
fun CompendiumContent(
    filter: CompendiumFilter,
    cells: List<GridCell>?,
    onFilterChange: ((CompendiumFilter) -> CompendiumFilter) -> Unit,
    onGroupToggle: (String) -> Unit,
    onCardClick: (String) -> Unit,
    onAboutClick: () -> Unit,
    layout: ListDetailLayout = ListDetailLayout.Single,
    collapsedAtStart: Float = 0f,
    update: AppUpdate? = null,
    onInstallUpdate: () -> Unit = {},
    onDismissUpdate: () -> Unit = {},
) {
    val theme = AtlasTheme.colors
    val distance = with(LocalDensity.current) { CollapseDistance.toPx() }
    // Saveable like the grid's position, so coming back from a card restores both together.
    val collapse =
        rememberSaveable(distance, saver = HeaderCollapse.saver(distance)) {
            HeaderCollapse(
                distance,
                collapsedAtStart * distance,
            )
        }
    // The text lives here, in Compose state, so no character typed waits for the ViewModel's round trip; the ViewModel
    // only receives it.
    val search = rememberTextFieldState(filter.query)
    val latestOnFilterChange by rememberUpdatedState(onFilterChange)
    LaunchedEffect(search) {
        snapshotFlow { search.text.toString() }.collect {
            latestOnFilterChange { filter -> if (filter.query == it) filter else filter.copy(query = it) }
        }
    }
    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    // A text field keeps focus until something takes it, so the search lets go when the user moves on: tapping
    // outside it, scrolling the list or opening a card.
    val focusManager = LocalFocusManager.current
    val openCard: (String) -> Unit = {
        focusManager.clearFocus()
        onCardClick(it)
    }
    val toggleGroup: (String) -> Unit = {
        focusManager.clearFocus()
        onGroupToggle(it)
    }
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
        // On the rail layout nothing sits below the grid, so it keeps clear of the navigation bar itself.
        val bottom = if (rail) Modifier.windowInsetsPadding(WindowInsets.navigationBars) else Modifier
        SideEffect {
            collapse.layout(
                rail,
                listAtTop =
                    grid.firstVisibleItemIndex == 0 && grid.firstVisibleItemScrollOffset == 0,
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .background(theme.surface)
                .nestedScroll(collapse)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            Header(search, collapse, rail, onAboutClick, showAbout = layout.showAbout)
            // Above the list rather than in it: the grid keeps its first visible item in place, so a banner arriving
            // after the list showed would land out of sight.
            if (update != null) {
                Box(Modifier.fillMaxWidth(), Alignment.TopCenter) {
                    UpdateSticker(
                        update,
                        onInstallUpdate,
                        Modifier
                            .widthIn(max = MaxWidth.content)
                            .padding(start = Space.s5, end = Space.s5, top = Space.s4),
                        onDismiss = onDismissUpdate,
                    )
                }
            }
            ListAndDock(
                rail,
                centered = rail && layout.centersOnRail,
                Modifier.weight(1f).then(bottom),
                list = {
                    LazyVerticalGrid(
                        state = grid,
                        columns = GridCells.Adaptive(CellMinWidth),
                        // The grid's own clip leaves out the items fading out after a filter.
                        modifier = Modifier.clipScrollableContainer(Orientation.Vertical),
                        contentPadding =
                            PaddingValues(
                                start = GridSidePadding,
                                end = GridSidePadding,
                                top = Space.s4,
                                bottom = Space.s5 - CardMargin,
                            ),
                        horizontalArrangement = Arrangement.spacedBy(CellGapAcross),
                        verticalArrangement = Arrangement.spacedBy(CellGapDown),
                    ) {
                        fullWidth("filters") { Filters(filter, onFilterChange) }
                        if (cells?.isEmpty() == true) {
                            fullWidth("empty") {
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
                        items(cells.orEmpty(), key = { it.gridKey }) { cell ->
                            val modifier = Modifier.animateItem(placementSpec = MoveSpec)
                            when (cell) {
                                is GridCell.Single -> {
                                    CompendiumCard(
                                        cell.card,
                                        tiltAt(cell.index),
                                        { openCard(cell.card.id) },
                                        modifier,
                                    )
                                }

                                is GridCell.Stack -> {
                                    GroupStack(cell, tiltAt(cell.index), { toggleGroup(cell.name) }, modifier)
                                }

                                is GridCell.Collapse -> {
                                    GroupCollapse(cell, { toggleGroup(cell.name) }, modifier.padding(CardMargin))
                                }

                                is GridCell.Variant -> {
                                    CompendiumCard(
                                        cell.card,
                                        tiltAt(cell.position - 1),
                                        { openCard(cell.card.id) },
                                        modifier,
                                        cell,
                                    )
                                }
                            }
                        }
                    }
                },
                dock = {
                    Dock(
                        onCardsClick,
                        if (rail) {
                            Modifier
                        } else {
                            Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(start = Space.s4, end = Space.s4, top = Space.s3, bottom = Space.s4)
                        },
                        vertical = rail,
                    )
                },
            )
        }
    }
}

// The list with the dock as a rail beside it or a bar below it.
@Composable
private fun ListAndDock(
    rail: Boolean,
    // With the list on its own, a margin as wide as the rail on the other side centers the content on the screen like
    // the search. Beside a card the list needs that room.
    centered: Boolean,
    modifier: Modifier,
    list: @Composable () -> Unit,
    dock: @Composable () -> Unit,
) {
    val capped: @Composable (Modifier) -> Unit = {
        Box(it, Alignment.TopCenter) {
            Box(Modifier.widthIn(max = MaxWidth.wide).fillMaxSize(), propagateMinConstraints = true) { list() }
        }
    }
    if (rail) {
        Row(modifier) {
            Box(
                Modifier
                    .width(
                        RailWidth,
                    ).fillMaxHeight()
                    .padding(start = Space.s5, end = Space.s4, top = Space.s4, bottom = Space.s4),
                Alignment.Center,
            ) { dock() }
            capped(Modifier.weight(1f).fillMaxHeight())
            if (centered) Spacer(Modifier.width(RailWidth))
        }
    } else {
        Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
            capped(Modifier.weight(1f).fillMaxWidth())
            dock()
        }
    }
}

private fun LazyGridScope.fullWidth(
    key: String,
    content: @Composable () -> Unit,
) = item(key = key, span = { GridItemSpan(maxLineSpan) }) {
    Box(
        Modifier
            .fillMaxWidth()
            .animateItem(placementSpec = null)
            .padding(start = CardMargin, end = CardMargin, bottom = CardMargin),
    ) {
        content()
    }
}

@Composable
private fun CompendiumCard(
    card: NamedCard,
    tilt: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: GridCell.Variant? = null,
) {
    val face = card.face() ?: return
    val description =
        buildList {
            add(card.name)
            face.colors.forEach { add(stringResource(colorName(it))) }
            face.faceValue?.let { add(LocalResources.current.pointsText(it)) }
        }.joinToString(", ").let { described ->
            variant?.let { stringResource(R.string.card_variant_position, described, it.position, it.count) }
                ?: described
        }
    CardThumb(card.image, card.name, face.colors, description, tilt, onClick, modifier.padding(CardMargin))
}

@PreviewLightDark
@Composable
internal fun CompendiumPreview() = CompendiumSample(ListDetailLayout.Single)

@PreviewLightDark
@Composable
internal fun CompendiumHalfCollapsedPreview() = CompendiumSample(ListDetailLayout.Single, collapsedAtStart = 0.5f)

@PreviewLightDark
@Composable
internal fun CompendiumBesideCardPreview() = CompendiumSample(ListDetailLayout.BesideCard)

@PreviewLightDark
@Composable
internal fun CompendiumUpdatePreview() = CompendiumSample(ListDetailLayout.Single, update = SampleUpdate)

@PreviewLightDark
@Composable
internal fun CompendiumLongNamePreview() = CompendiumSample(ListDetailLayout.Single, longName = true)

@Composable
private fun CompendiumSample(
    layout: ListDetailLayout,
    longName: Boolean = false,
    collapsedAtStart: Float = 0f,
    update: AppUpdate? = null,
) = AtlasTheme {
    BlankCardImages {
        val cards =
            listOf(CardColor.Blue, CardColor.Green, CardColor.Purple, CardColor.Red)
                .map {
                    Trait(
                        "$it",
                        "classic",
                        "",
                        "${it.name} trait",
                        null,
                        emptyList(),
                        null,
                        1,
                        listOf(it),
                        false,
                        emptyList(),
                        emptyList(),
                    )
                }.let { if (longName) listOf(LongNameTrait) + it else it }
        CompendiumContent(
            CompendiumFilter(colors = setOf(CardColor.Green)),
            cards.map(CompendiumEntry::Single).toGridCells(emptySet()),
            {},
            {},
            {},
            {},
            layout,
            collapsedAtStart,
            update,
        )
    }
}
