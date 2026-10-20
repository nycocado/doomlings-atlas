package dev.nycocado.atlas.compendium

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.CardThumb
import dev.nycocado.atlas.ui.component.Dock
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.component.RoundButtonStyle
import dev.nycocado.atlas.ui.component.SunsetBand
import dev.nycocado.atlas.ui.component.TraitColorOptions
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.component.signed
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

// Deterministic by position, never random per render (docs/design.md).
private val Tilts = listOf(-2.5f, 2f, 1.5f, -2f)

@Composable
fun CompendiumScreen(
    viewModel: CompendiumViewModel,
    onCardClick: (String) -> Unit,
    onAboutClick: () -> Unit,
    cardOpen: Boolean,
) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    CompendiumContent(filter, results, viewModel::updateFilter, onCardClick, onAboutClick, cardOpen)
}

@Composable
fun CompendiumContent(
    filter: CompendiumFilter,
    results: List<NamedCard>?,
    onFilterChange: (CompendiumFilter) -> Unit,
    onCardClick: (String) -> Unit,
    onAboutClick: () -> Unit,
    // Beside an open card, which has its own way to About.
    cardOpen: Boolean = false,
) {
    val colors = AtlasTheme.colors
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
        scope.launch { collapse.unfold() }
    }
    BoxWithConstraints {
        // Decided by the compendium's own width, not the window's: beside a card on a foldable or a small tablet it
        // has a phone's room, and takes the phone layout.
        val rail = maxWidth >= RailMinWidth
        // The header changes shape, its parts and the dock move to their new places when the layout switches. Only
        // right after that switch: they also move while the header folds as the list scrolls, and must follow that
        // at once.
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
        // The wide header is a single row with nothing to fold, so it doesn't take part in scrolling.
        val folding = if (rail) Modifier else Modifier.nestedScroll(collapse)
        Column(
            Modifier
                .fillMaxSize()
                .background(colors.surface)
                .then(folding)
                .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } },
        ) {
            LookaheadScope {
                Header(
                    filter.query,
                    collapse::fraction,
                    rail,
                    onAboutClick,
                    showAbout = !cardOpen,
                    switchMotion,
                    Modifier.animateBounds(this, boundsTransform = switchMotion),
                ) { onFilterChange(filter.copy(query = it)) }
            }
            Row(Modifier.weight(1f).then(bottom)) {
                // Inside the row, so the header folding above doesn't move anything within this scope: the cards
                // only animate when the layout itself changes, like a card opening beside the list or a filter.
                LookaheadScope {
                    val reflow = this
                    val itemMotion = Modifier.animateBounds(reflow, boundsTransform = switchMotion)
                    if (rail) {
                        Box(
                            Modifier
                                .width(RailWidth)
                                .fillMaxHeight()
                                .padding(start = Space.s5, end = Space.s4, top = Space.s4, bottom = Space.s4),
                            contentAlignment = Alignment.Center,
                        ) { dock(true, itemMotion, itemMotion) }
                    }
                    // With the list on its own, a margin as wide as the rail on the other side centers the content on the
                    // screen like the search. Beside a card the list needs that room.
                    Column(
                        Modifier.weight(1f).padding(end = if (rail && !cardOpen) RailWidth else 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        LazyVerticalGrid(
                            state = grid,
                            columns = GridCells.Adaptive(150.dp),
                            modifier = Modifier.weight(1f).widthIn(max = MaxWidth.wide),
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
                                        Modifier.fillMaxWidth().padding(vertical = Space.s6),
                                        style =
                                            AtlasTypography.bodyMd.copy(
                                                color = colors.onSurfaceVariant,
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
                                    // Moves animate by bounds, which also covers the change of size.
                                    Modifier
                                        .animateItem(
                                            placementSpec = null,
                                        ).animateBounds(reflow, boundsTransform = Reflow),
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

private val CollapseDistance = 96.dp

// Folds the header as the list scrolls down and unfolds it once the list is back at the top. Handwritten:
// Material 3's collapsing app bars come with their own TopAppBar, and the design system is our own (ADR 0006).
@Stable
private class HeaderCollapse(
    private val distance: Float,
    initial: Float = 0f,
) : NestedScrollConnection {
    private var collapsed by mutableFloatStateOf(initial)

    companion object {
        fun saver(distance: Float) = Saver<HeaderCollapse, Float>({ it.collapsed }, { HeaderCollapse(distance, it) })
    }

    fun fraction() = collapsed / distance

    // Scrolling the list from code bypasses nested scrolling, so the header is unfolded explicitly.
    suspend fun unfold() = animate(collapsed, 0f) { value, _ -> collapsed = value }

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (available.y >= 0) return Offset.Zero
        val used = minOf(-available.y, distance - collapsed)
        collapsed += used
        return Offset(0f, -used)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (available.y <= 0) return Offset.Zero
        val used = minOf(available.y, collapsed)
        collapsed -= used
        return Offset(0f, used)
    }

    // A scroll that ends halfway finishes folding or unfolding, whichever is nearer.
    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity {
        if (collapsed > 0f && collapsed < distance) {
            animate(collapsed, if (collapsed < distance / 2) 0f else distance) { value, _ -> collapsed = value }
        }
        return Velocity.Zero
    }
}

// Read at layout, so scrolling skips recomposition.
private fun Modifier.folded(fraction: () -> Float) =
    clipToBounds().layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        val height = (placeable.height * (1 - fraction())).roundToInt()
        layout(placeable.width, height) { placeable.place(0, height - placeable.height) }
    }

@Composable
private fun Header(
    query: String,
    fraction: () -> Float,
    wide: Boolean,
    onAboutClick: () -> Unit,
    // When hidden, the search takes the button's place.
    showAbout: Boolean,
    // Title, search and info button move between the one-row and the stacked header as the same content.
    transform: BoundsTransform,
    modifier: Modifier,
    onQueryChange: (String) -> Unit,
) {
    val colors = AtlasTheme.colors
    val title = remember { movableContentOf { modifier: Modifier -> HeaderTitle(modifier) } }
    val search =
        remember {
            movableContentOf { query: String, onQueryChange: (String) -> Unit, modifier: Modifier ->
                SearchField(query, onQueryChange, modifier)
            }
        }
    val about =
        remember {
            movableContentOf { show: Boolean, onClick: () -> Unit, modifier: Modifier ->
                AnimatedVisibility(
                    show,
                    modifier,
                    enter = fadeIn() + scaleIn() + expandHorizontally(),
                    exit = fadeOut() + scaleOut() + shrinkHorizontally(),
                ) {
                    // The row clips while folding, so it leaves room for the button's shadow.
                    RoundButton(
                        R.drawable.ic_info,
                        R.string.about,
                        onClick,
                        Modifier.padding(end = Shadow.sticker, bottom = Shadow.sticker),
                        style = RoundButtonStyle.QuietOnHeader,
                    )
                }
            }
        }
    Column(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                val radius = 32.dp.toPx() * (1 - fraction())
                shape = RoundedCornerShape(0f, 0f, radius, radius)
                clip = true
            }.background(colors.inverseSurface)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LookaheadScope {
            val moves = Modifier.animateBounds(this, boundsTransform = transform)
            if (wide) {
                CenteredBetween(
                    Modifier.padding(start = Space.s5, end = Space.s5, top = Space.s3, bottom = Space.s4),
                    start = { title(moves) },
                    center = {
                        search(
                            query,
                            onQueryChange,
                            Modifier.animateBounds(
                                this,
                                Modifier.widthIn(max = MaxWidth.compact),
                                boundsTransform = transform,
                            ),
                        )
                    },
                    end = { about(showAbout, onAboutClick, moves) },
                )
            } else {
                Column(
                    Modifier.widthIn(max = MaxWidth.content).padding(start = Space.s5, end = Space.s5, top = Space.s2),
                ) {
                    Row(
                        Modifier.folded(fraction).padding(top = Space.s1, bottom = Space.s4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.weight(1f)) { title(moves) }
                        about(showAbout, onAboutClick, moves)
                    }
                    search(query, onQueryChange, Modifier.padding(bottom = Space.s3).then(moves))
                }
            }
            // Moves with the header's edge as it grows or shrinks, instead of jumping to where it ends up.
            SunsetBand(Modifier.animateBounds(this, Modifier.folded(fraction).padding(top = 6.dp), transform))
        }
    }
}

@Composable
private fun HeaderTitle(modifier: Modifier) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        Image(painterResource(R.drawable.logo_mark_dark), null, Modifier.size(40.dp))
        BasicText(
            stringResource(R.string.app_name),
            style = AtlasTypography.headline.copy(color = AtlasTheme.colors.onInverseSurface),
        )
    }
}

private class LayoutSwitch(
    private var wide: Boolean,
) {
    private var at = 0L

    fun track(wide: Boolean) {
        if (wide != this.wide) {
            this.wide = wide
            at = System.nanoTime()
        }
    }

    fun recent() = System.nanoTime() - at < 700_000_000L
}

// Start and end in the corners, center in the middle of the whole row. Without room for that, the center moves
// toward the end and then narrows, rather than overlap either side. Handwritten: Row can only center a child
// between its neighbors, not on the row itself.
@Composable
private fun CenteredBetween(
    modifier: Modifier,
    start: @Composable () -> Unit,
    center: @Composable () -> Unit,
    end: @Composable () -> Unit,
) = // Each slot is wrapped, so there are always three to measure, even after one animates away.
    Layout({
        Box { start() }
        Box { center() }
        Box { end() }
    }, modifier.fillMaxWidth()) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val first = measurables[0].measure(loose)
        val last = measurables[2].measure(loose)
        val gap = Space.s4.roundToPx()
        val from = first.width + gap
        val to = constraints.maxWidth - last.width - if (last.width > 0) gap else 0
        val middle = measurables[1].measure(loose.copy(maxWidth = (to - from).coerceAtLeast(0)))
        val height = maxOf(first.height, middle.height, last.height)
        layout(constraints.maxWidth, height) {
            first.place(0, (height - first.height) / 2)
            val x = ((constraints.maxWidth - middle.width) / 2).coerceIn(from, (to - middle.width).coerceAtLeast(from))
            middle.place(x, (height - middle.height) / 2)
            last.place(constraints.maxWidth - last.width, (height - last.height) / 2)
        }
    }

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasTheme.colors
    val label = stringResource(R.string.search_label)
    val focusManager = LocalFocusManager.current
    Row(
        modifier
            .fillMaxWidth()
            .height(52.dp)
            .sticker(CircleShape, colors.surfaceSticker, colors.outline)
            .padding(horizontal = Space.s4),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(R.drawable.ic_search),
            null,
            Modifier.size(22.dp),
            colorFilter = ColorFilter.tint(colors.onSurface),
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
            singleLine = true,
            // Results already update while typing: the search key only closes the keyboard.
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            textStyle = AtlasTypography.bodyMd.copy(color = colors.onSurface),
            cursorBrush = SolidColor(colors.onSurface),
            decorationBox = { field ->
                if (query.isEmpty()) {
                    BasicText(
                        stringResource(R.string.search_placeholder),
                        style = AtlasTypography.bodyMd.copy(color = colors.onSurfaceVariant),
                        maxLines = 1,
                    )
                }
                field()
            },
        )
        val clear = stringResource(R.string.clear_search)
        AnimatedVisibility(query.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .size(40.dp)
                    .clickable(role = Role.Button) { onQueryChange("") }
                    .semantics { contentDescription = clear },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(R.drawable.ic_clear),
                    null,
                    Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(colors.onSurface),
                )
            }
        }
    }
}

@Composable
private fun Filters(
    filter: CompendiumFilter,
    onFilterChange: (CompendiumFilter) -> Unit,
) {
    val colors = AtlasTheme.colors
    Column(Modifier.fillMaxWidth()) {
        // The controls center on wide screens; the title below stays in line with the first column of cards.
        Column(Modifier.widthIn(max = MaxWidth.content).align(Alignment.CenterHorizontally)) {
            AnimatedVisibility(
                visible = filter.kind == CardKind.Trait,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(bottom = Space.s4),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TraitColorOptions.forEach { color ->
                        val selected = color in filter.colors
                        // Narrower than their 64dp only when the row can't fit them, as beside a card on a foldable.
                        ColorDisc(color, selected, Modifier.weight(1f, fill = false)) {
                            onFilterChange(filter.copy(colors = if (selected) emptySet() else setOf(color)))
                        }
                    }
                }
            }
            KindSegments(filter.kind) { onFilterChange(filter.copy(kind = it)) }
        }
        Row(
            Modifier.padding(top = Space.s4),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
        ) {
            BasicText(stringResource(filter.kind.title), style = AtlasTypography.titleLg.copy(color = colors.onSurface))
            AnimatedVisibility(
                visible = filter.kind == CardKind.Trait && filter.colors.isEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                BasicText(
                    stringResource(R.string.color_hint),
                    Modifier.padding(bottom = 3.dp),
                    style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant),
                )
            }
        }
    }
}

// One color at a time for now: Classic has no two-color traits.
@Composable
private fun ColorDisc(
    color: CardColor?,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val colors = AtlasTheme.colors
    val name = stringResource(colorName(color))
    Column(
        modifier
            .width(64.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected },
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
                    }.border(3.dp, colors.outline, CircleShape),
            )
            Box(
                Modifier.size(48.dp).sticker(CircleShape, colors.trait(color).fill, colors.outline),
                contentAlignment = Alignment.Center,
            ) { TraitGlyph(color, colors.trait(color).onFill) }
        }
        BasicText(name, style = AtlasTypography.labelSm.copy(color = colors.onSurface), maxLines = 1)
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
    val colors = AtlasTheme.colors
    val gap = 6.dp
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .sticker(CircleShape, colors.surfaceSticker, colors.outline, stroke = Stroke.sticker)
            .padding(4.dp),
    ) {
        val count = CardKind.entries.size
        val width = (maxWidth - gap * (count - 1)) / count
        val offset by animateDpAsState((width + gap) * selected.ordinal, label = "segment")
        Box(
            Modifier
                .offset { IntOffset(offset.roundToPx(), 0) }
                .width(width)
                .height(40.dp)
                .background(colors.primary, CircleShape),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            CardKind.entries.forEach { kind ->
                val active = kind == selected
                val textColor by animateColorAsState(
                    if (active) colors.onPrimary else colors.onSurface,
                    label = "segment text",
                )
                Box(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clickable(role = Role.RadioButton) { onSelect(kind) }
                        .semantics { this.selected = active },
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
