package dev.nycocado.atlas.compendium

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.CardThumb
import dev.nycocado.atlas.ui.component.Dock
import dev.nycocado.atlas.ui.component.TraitColorOptions
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait
import kotlin.math.roundToInt

// Header, filters and dock stop growing here on tablets and unfolded foldables; the grid adds columns instead.
private val ContentMaxWidth = 640.dp

// Deterministic by position, never random per render (docs/design.md).
private val Tilts = listOf(-2.5f, 2f, 1.5f, -2f)

@Composable
fun CompendiumScreen(viewModel: CompendiumViewModel = hiltViewModel()) {
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    CompendiumContent(filter, results, viewModel::updateFilter)
}

// results is null while the cards load.
@Composable
fun CompendiumContent(
    filter: CompendiumFilter,
    results: List<Card>?,
    onFilterChange: (CompendiumFilter) -> Unit,
) {
    val colors = AtlasTheme.colors
    val distance = with(LocalDensity.current) { CollapseDistance.toPx() }
    val collapse = remember(distance) { HeaderCollapse(distance) }
    Column(Modifier.fillMaxSize().background(colors.surface).nestedScroll(collapse)) {
        Header(filter.query, collapse::fraction) { onFilterChange(filter.copy(query = it)) }
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = Space.s5, end = Space.s5, top = Space.s4, bottom = Space.s5),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            fullWidth { Filters(filter, onFilterChange) }
            if (results?.isEmpty() == true) {
                fullWidth {
                    BasicText(
                        stringResource(R.string.no_results),
                        Modifier.padding(vertical = Space.s6),
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
                CompendiumCard(card, tilt)
            }
        }
        Column(
            Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = Space.s4),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BasicText(
                stringResource(R.string.unofficial_notice),
                Modifier.padding(vertical = Space.s2),
                style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant, textAlign = TextAlign.Center),
            )
            Dock(Modifier.padding(bottom = Space.s4))
        }
    }
}

private fun LazyGridScope.fullWidth(content: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.widthIn(max = ContentMaxWidth)) { content() }
        }
    }

// How far the list scrolls while the title and sunset band fold away.
private val CollapseDistance = 96.dp

// Folds the header as the list scrolls down and unfolds it once the list is back at the top. Hand-written:
// Material 3's collapsing app bars come with their own TopAppBar, and the design system is our own (ADR 0006).
@Stable
private class HeaderCollapse(
    private val distance: Float,
) : NestedScrollConnection {
    private var collapsed by mutableFloatStateOf(0f)

    fun fraction() = collapsed / distance

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
}

// Shows only the unfolded share of the content's height, cut from the top. Read at layout, so scrolling skips
// recomposition.
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
    onQueryChange: (String) -> Unit,
) {
    val colors = AtlasTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            // Rounded base while open, straight once folded.
            .graphicsLayer {
                val radius = 32.dp.toPx() * (1 - fraction())
                shape = RoundedCornerShape(0f, 0f, radius, radius)
                clip = true
            }.background(colors.inverseSurface)
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(Modifier.widthIn(max = ContentMaxWidth).padding(start = Space.s5, end = Space.s5, top = Space.s2)) {
            Row(
                Modifier.folded(fraction).padding(top = Space.s1, bottom = Space.s4),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Space.s3),
            ) {
                Image(painterResource(R.drawable.logo_mark_dark), null, Modifier.size(40.dp))
                BasicText(
                    stringResource(R.string.app_name),
                    // The prototype's header title is 28sp, a step below the headline token.
                    style =
                        AtlasTypography.headline.copy(
                            color = colors.onInverseSurface,
                            fontSize = 28.sp,
                            lineHeight = 32.sp,
                        ),
                )
            }
            SearchField(query, onQueryChange, Modifier.padding(bottom = Space.s3))
        }
        SunsetBand(Modifier.folded(fraction).padding(top = 6.dp))
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
            textStyle = AtlasTypography.bodyMd.copy(color = colors.onSurface, fontSize = 16.sp),
            cursorBrush = SolidColor(colors.onSurface),
            decorationBox = { field ->
                if (query.isEmpty()) {
                    BasicText(
                        stringResource(R.string.search_placeholder),
                        style = AtlasTypography.bodyMd.copy(color = colors.onSurfaceVariant, fontSize = 16.sp),
                        maxLines = 1,
                    )
                }
                field()
            },
        )
    }
}

@Composable
private fun SunsetBand(modifier: Modifier = Modifier) {
    val colors = AtlasTheme.colors
    Row(modifier.fillMaxWidth().height(14.dp)) {
        listOf(colors.sunsetRed, colors.sunsetOrange, colors.sunsetYellow, colors.sunsetSea).forEach {
            Spacer(Modifier.weight(1f).height(14.dp).background(it))
        }
    }
}

@Composable
private fun Filters(
    filter: CompendiumFilter,
    onFilterChange: (CompendiumFilter) -> Unit,
) {
    val colors = AtlasTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(Space.s4)) {
        if (filter.kind == CardKind.Trait) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TraitColorOptions.forEach { color ->
                    val selected = color in filter.colors
                    ColorDisc(color, selected) {
                        onFilterChange(filter.copy(colors = if (selected) emptySet() else setOf(color)))
                    }
                }
            }
        }
        KindSegments(filter.kind) { onFilterChange(filter.copy(kind = it)) }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Space.s2)) {
            BasicText(stringResource(filter.kind.title), style = AtlasTypography.titleLg.copy(color = colors.onSurface))
            if (filter.kind == CardKind.Trait && filter.colors.isEmpty()) {
                BasicText(
                    stringResource(R.string.color_hint),
                    Modifier.padding(bottom = 3.dp),
                    style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant),
                )
            }
        }
    }
}

// One color at a time for now; Classic has no two-color traits. Tapping the selected color shows all again.
// The double ring marks selection, so color is never the only cue.
@Composable
private fun ColorDisc(
    color: CardColor?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AtlasTheme.colors
    val name = stringResource(colorName(color))
    Column(
        Modifier
            .width(64.dp)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { this.selected = selected },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        val ring =
            if (selected) {
                Modifier
                    .border(
                        3.dp,
                        colors.outline,
                        CircleShape,
                    ).padding(3.dp)
            } else {
                Modifier.padding(3.dp)
            }
        Box(Modifier.size(60.dp).then(ring).padding(3.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(48.dp).sticker(CircleShape, colors.trait(color).fill, colors.outline),
                contentAlignment = Alignment.Center,
            ) { TraitGlyph(color, colors.trait(color).onFill) }
        }
        BasicText(name, style = AtlasTypography.labelSm.copy(color = colors.onSurface))
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
    Row(
        Modifier
            .fillMaxWidth()
            .sticker(CircleShape, colors.surfaceSticker, colors.outline, stroke = Stroke.sticker)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        CardKind.entries.forEach { kind ->
            val active = kind == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(CircleShape)
                    .background(if (active) colors.primary else colors.surfaceSticker)
                    .clickable(role = Role.RadioButton) { onSelect(kind) }
                    .semantics { this.selected = active },
                contentAlignment = Alignment.Center,
            ) {
                BasicText(
                    stringResource(kind.label),
                    style = AtlasTypography.labelLg.copy(color = if (active) colors.onPrimary else colors.onSurface),
                )
            }
        }
    }
}

@Composable
private fun CompendiumCard(
    card: Card,
    tilt: Float,
) {
    val (name, cardColors, points) =
        when (card) {
            is Trait -> Triple(card.name, card.colors, card.points)
            is Age -> Triple(card.name, null, null)
            is Catastrophe -> Triple(card.name, null, null)
            else -> return
        }
    val description =
        buildList {
            add(name)
            cardColors?.ifEmpty { listOf(null) }?.forEach { add(stringResource(colorName(it))) }
            points?.let { add(pluralStringResource(R.plurals.card_points, it, it)) }
        }.joinToString(", ")
    CardThumb(card.image, name, cardColors, description, tilt)
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
            CompendiumContent(CompendiumFilter(colors = setOf(CardColor.Green)), cards) {}
        }
    }
