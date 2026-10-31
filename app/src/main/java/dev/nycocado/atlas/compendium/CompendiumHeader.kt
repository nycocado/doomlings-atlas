package dev.nycocado.atlas.compendium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.component.RoundButtonStyle
import dev.nycocado.atlas.ui.component.SunsetBand
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker
import kotlin.math.roundToInt

internal val CollapseDistance = 96.dp

// Collapses the header as the list scrolls down and expands it once the list is back at the top. Handwritten:
// Material 3's collapsing app bars come with their own TopAppBar, and the design system is our own (ADR 0006).
@Stable
internal class HeaderCollapse(
    private val distance: Float,
    initial: Float = 0f,
    rail: Boolean = false,
) : NestedScrollConnection {
    private var collapsed by mutableFloatStateOf(initial)
    private var titleHeight = 0

    // The rail layout's header is a single row with nothing to collapse, so it neither collapses nor scrolls along.
    var rail by mutableStateOf(rail)
        private set

    companion object {
        // With the layout it was in, so a rotation out of the rail layout still counts as leaving it.
        fun saver(distance: Float) =
            listSaver<HeaderCollapse, Any>(
                { listOf(it.collapsed, it.rail) },
                { HeaderCollapse(distance, it[0] as Float, it[1] as Boolean) },
            )
    }

    // Given before each frame's layout. The list scrolls without the header in the rail layout, so on leaving it the
    // header takes at once the state the stacked layout keeps: expanded only with the list at the top.
    fun layout(
        rail: Boolean,
        listAtTop: Boolean,
    ) {
        if (this.rail && !rail) collapsed = if (listAtTop) 0f else distance
        this.rail = rail
    }

    fun fraction() = if (rail) 0f else collapsed / distance

    // Scrolling the list from code bypasses nested scrolling, so the header is expanded explicitly.
    suspend fun expand() = animate(collapsed, 0f) { value, _ -> collapsed = value }

    // The stacked header's title row, the part that collapses. Set by the header's layout, read in the same pass.
    internal fun measuredTitle(height: Int) {
        titleHeight = height
    }

    internal fun Modifier.collapsingTitle() = collapsing(::fraction) { titleHeight }

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (rail || available.y >= 0) return Offset.Zero
        val used = minOf(-available.y, distance - collapsed)
        collapsed += used
        return Offset(0f, -used)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (rail || available.y <= 0) return Offset.Zero
        val used = minOf(available.y, collapsed)
        collapsed -= used
        return Offset(0f, used)
    }

    override suspend fun onPostFling(
        consumed: Velocity,
        available: Velocity,
    ): Velocity {
        if (!rail && collapsed > 0f && collapsed < distance) {
            animate(collapsed, if (collapsed < distance / 2) 0f else distance) { value, _ -> collapsed = value }
        }
        return Velocity.Zero
    }
}

// Cuts off the top of the content as the header collapses, sliding the rest up. Read at layout, so scrolling skips
// recomposition.
private fun Modifier.collapsing(
    fraction: () -> Float,
    // The part of the content's height that collapses.
    height: (Int) -> Int = { it },
) = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val hidden = (height(placeable.height) * fraction()).roundToInt()
    layout(placeable.width, placeable.height - hidden) { placeable.place(0, -hidden) }
}

@Composable
internal fun Header(
    search: TextFieldState,
    collapse: HeaderCollapse,
    rail: Boolean,
    onAboutClick: () -> Unit,
    // When hidden, the search takes the button's place.
    showAbout: Boolean,
) {
    val theme = AtlasTheme.colors
    val fraction = collapse::fraction
    Column(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val radius = Radius.lg.bottomStart.toPx(size, this) * (1 - fraction())
                shape = RoundedCornerShape(0f, 0f, radius, radius)
                clip = true
            }.background(theme.inverseSurface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Box(with(collapse) { Modifier.collapsingTitle() }) {
            HeaderParts(
                rail,
                collapse::measuredTitle,
                Modifier.fillMaxWidth(),
                title = { HeaderTitle() },
                search = { SearchField(search) },
                about = { InfoButton(showAbout, onAboutClick) },
            )
        }
        // Right below the content, so it moves with the header's edge without animating on its own.
        SunsetBand(Modifier.collapsing(fraction).padding(top = 6.dp))
    }
}

@Composable
private fun HeaderTitle() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        Image(painterResource(R.drawable.logo_mark_dark), null, Modifier.size(40.dp))
        BasicText(
            stringResource(R.string.app_name),
            Modifier.semantics { heading() },
            style = AtlasTypography.headline.copy(color = AtlasTheme.colors.onInverseSurface),
        )
    }
}

// Title and info button above the search, or all in one row with the search centered on the whole row.
// Handwritten: Row centers a child between its neighbors, not on the row.
@Composable
private fun HeaderParts(
    rail: Boolean,
    onTitleHeight: (Int) -> Unit,
    modifier: Modifier,
    title: @Composable () -> Unit,
    search: @Composable () -> Unit,
    about: @Composable () -> Unit,
) = Layout({
    // Each part is wrapped, so there are always three to measure, even while the info button is hidden.
    Box { title() }
    Box { search() }
    Box { about() }
}, modifier) { (titleSlot, searchSlot, aboutSlot), constraints ->
    val width = constraints.maxWidth
    val side = Space.s5.roundToPx()
    val loose = constraints.copy(minWidth = 0, minHeight = 0, maxWidth = (width - side * 2).coerceAtLeast(0))
    if (rail) {
        val top = Space.s3.roundToPx()
        val gap = Space.s4.roundToPx()
        val title = titleSlot.measure(loose)
        val about = aboutSlot.measure(loose)
        val from = side + title.width + gap
        val to = width - side - about.width - if (about.width > 0) gap else 0
        val searchWidth = minOf(MaxWidth.compact.roundToPx(), to - from).coerceAtLeast(0)
        val search = searchSlot.measure(loose.copy(minWidth = searchWidth, maxWidth = searchWidth))
        val row = maxOf(title.height, search.height, about.height)
        layout(width, top + row + Space.s4.roundToPx()) {
            title.placeRelative(side, top + (row - title.height) / 2)
            val x = ((width - search.width) / 2).coerceIn(from, (to - search.width).coerceAtLeast(from))
            search.placeRelative(x, top + (row - search.height) / 2)
            about.placeRelative(width - side - about.width, top + (row - about.height) / 2)
        }
    } else {
        val outer = minOf(width, MaxWidth.content.roundToPx())
        val start = Alignment.CenterHorizontally.align(outer, width, layoutDirection) + side
        val inner = loose.copy(maxWidth = (outer - side * 2).coerceAtLeast(0))
        val about = aboutSlot.measure(inner)
        val title = titleSlot.measure(inner.copy(maxWidth = (inner.maxWidth - about.width).coerceAtLeast(0)))
        val search = searchSlot.measure(inner.copy(minWidth = inner.maxWidth))
        val row = maxOf(title.height, about.height)
        val rowTop = Space.s2.roundToPx() + Space.s1.roundToPx()
        val titleRow = Space.s1.roundToPx() + row + Space.s4.roundToPx()
        onTitleHeight(titleRow)
        layout(width, Space.s2.roundToPx() + titleRow + search.height + Space.s3.roundToPx()) {
            title.placeRelative(start, rowTop + Alignment.CenterVertically.align(title.height, row))
            about.placeRelative(
                start + inner.maxWidth - about.width,
                rowTop + Alignment.CenterVertically.align(about.height, row),
            )
            search.placeRelative(start, Space.s2.roundToPx() + titleRow)
        }
    }
}

@Composable
private fun InfoButton(
    show: Boolean,
    onClick: () -> Unit,
) {
    if (!show) return
    // The header clips while collapsing, so it leaves room for the button's shadow.
    RoundButton(
        R.drawable.ic_info,
        R.string.about,
        onClick,
        Modifier.padding(end = Shadow.sticker, bottom = Shadow.sticker),
        style = RoundButtonStyle.QuietOnHeader,
    )
}

@Composable
private fun SearchField(
    search: TextFieldState,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    val label = stringResource(R.string.search_label)
    val focusManager = LocalFocusManager.current
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .sticker(CircleShape, theme.surfaceSticker, theme.outline)
            .padding(horizontal = Space.s4),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painterResource(R.drawable.ic_search),
            null,
            Modifier.size(Sizes.icon),
            colorFilter = ColorFilter.tint(theme.onSurface),
        )
        BasicTextField(
            state = search,
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
            lineLimits = TextFieldLineLimits.SingleLine,
            // Results already update while typing: the search key only closes the keyboard.
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            onKeyboardAction = { focusManager.clearFocus() },
            textStyle = AtlasTypography.bodyMd.copy(color = theme.onSurface),
            cursorBrush = SolidColor(theme.onSurface),
            decorator = { field ->
                if (search.text.isEmpty()) {
                    BasicText(
                        stringResource(R.string.search_placeholder),
                        style = AtlasTypography.bodyMd.copy(color = theme.onSurfaceVariant),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
                field()
            },
        )
        val clear = stringResource(R.string.clear_search)
        AnimatedVisibility(search.text.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .size(Sizes.touchTarget)
                    .clickable(role = Role.Button) { search.clearText() }
                    .semantics { contentDescription = clear },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painterResource(R.drawable.ic_clear),
                    null,
                    Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(theme.onSurface),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun HeaderPreview() = HeaderSample("", rail = false)

@PreviewLightDark
@Composable
internal fun HeaderHalfCollapsedPreview() = HeaderSample("", rail = false, collapsed = 0.5f)

@PreviewLightDark
@Composable
internal fun HeaderRailPreview() = HeaderSample("Adorable", rail = true)

@PreviewLightDark
@Composable
internal fun SearchFieldPreview() = AtlasTheme { Frame { SearchField(rememberTextFieldState()) } }

@PreviewLightDark
@Composable
internal fun SearchFieldTypedPreview() = AtlasTheme { Frame { SearchField(rememberTextFieldState("Adorable")) } }

@Composable
private fun Frame(content: @Composable () -> Unit) =
    Box(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) { content() }

@Composable
private fun HeaderSample(
    query: String,
    rail: Boolean,
    collapsed: Float = 0f,
) = AtlasTheme {
    val distance = with(LocalDensity.current) { CollapseDistance.toPx() }
    val collapse = remember { HeaderCollapse(distance, collapsed * distance, rail) }
    Box(Modifier.background(AtlasTheme.colors.surface)) {
        Header(rememberTextFieldState(query), collapse, rail, {}, showAbout = true)
    }
}
