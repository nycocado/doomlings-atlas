package dev.nycocado.atlas.compendium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.animateBounds
import androidx.compose.animation.core.animate
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
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
import androidx.compose.ui.layout.LookaheadScope
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.component.RoundButtonStyle
import dev.nycocado.atlas.ui.component.SunsetBand
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Shadow
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
) : NestedScrollConnection {
    private var collapsed by mutableFloatStateOf(initial)

    companion object {
        fun saver(distance: Float) = Saver<HeaderCollapse, Float>({ it.collapsed }, { HeaderCollapse(distance, it) })
    }

    fun fraction() = collapsed / distance

    // Measured by the title row, the part of the header's content that collapses, and read by the header in the same
    // layout pass.
    var titleHeight = 0

    // Scrolling the list from code bypasses nested scrolling, so the header is expanded explicitly.
    suspend fun expand() = animate(collapsed, 0f) { value, _ -> collapsed = value }

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

    // A scroll that ends halfway finishes collapsing or expanding, whichever is nearer.
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

// Cuts off the top of the content as the header collapses, sliding the rest up. Read at layout, so scrolling skips
// recomposition.
private fun Modifier.collapsing(
    fraction: () -> Float,
    // The part of the content's height that collapses, all of it by default.
    height: (Int) -> Int = { it },
) = clipToBounds().layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val hidden = (height(placeable.height) * fraction()).roundToInt()
    layout(placeable.width, placeable.height - hidden) { placeable.place(0, -hidden) }
}

@Composable
internal fun Header(
    query: String,
    fraction: () -> Float,
    collapse: HeaderCollapse,
    rail: Boolean,
    onAboutClick: () -> Unit,
    // When hidden, the search takes the button's place.
    showAbout: Boolean,
    // Title, search and info button move between the one-row and the stacked header as the same content.
    transform: BoundsTransform,
    onQueryChange: (String) -> Unit,
) {
    val theme = AtlasTheme.colors
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
                    // The header clips while collapsing, so it leaves room for the button's shadow.
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
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                val radius = 32.dp.toPx() * (1 - fraction())
                shape = RoundedCornerShape(0f, 0f, radius, radius)
                clip = true
            }.background(theme.inverseSurface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        // Collapses outside the LookaheadScope, which animateBounds would follow a frame or more late: inside it nothing
        // moves or changes size, the content slides up as a whole.
        Box(Modifier.collapsing(fraction) { collapse.titleHeight }) {
            LookaheadScope {
                val moves = Modifier.animateBounds(this, boundsTransform = transform)
                // The header's height glides with its parts when the layout switches.
                Column(moves.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (rail) {
                        CenteredBetween(
                            Modifier.padding(start = Space.s5, end = Space.s5, top = Space.s3, bottom = Space.s4),
                            start = { title(moves) },
                            center = {
                                search(
                                    query,
                                    onQueryChange,
                                    Modifier.animateBounds(
                                        this@LookaheadScope,
                                        Modifier.widthIn(max = MaxWidth.compact),
                                        boundsTransform = transform,
                                    ),
                                )
                            },
                            end = { about(showAbout, onAboutClick, moves) },
                        )
                    } else {
                        Column(
                            Modifier
                                .widthIn(max = MaxWidth.content)
                                .padding(start = Space.s5, end = Space.s5, top = Space.s2),
                        ) {
                            Row(
                                Modifier
                                    .onSizeChanged { collapse.titleHeight = it.height }
                                    .padding(top = Space.s1, bottom = Space.s4),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.weight(1f)) { title(moves) }
                                about(showAbout, onAboutClick, moves)
                            }
                            search(query, onQueryChange, Modifier.padding(bottom = Space.s3).then(moves))
                        }
                    }
                }
            }
        }
        // Right below the content, so it moves with the header's edge without animating on its own.
        SunsetBand(Modifier.collapsing(fraction).padding(top = 6.dp))
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
            Modifier.semantics { heading() },
            style = AtlasTypography.headline.copy(color = AtlasTheme.colors.onInverseSurface),
        )
    }
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
            Modifier.size(22.dp),
            colorFilter = ColorFilter.tint(theme.onSurface),
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f).semantics { contentDescription = label },
            singleLine = true,
            // Results already update while typing: the search key only closes the keyboard.
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            textStyle = AtlasTypography.bodyMd.copy(color = theme.onSurface),
            cursorBrush = SolidColor(theme.onSurface),
            decorationBox = { field ->
                if (query.isEmpty()) {
                    BasicText(
                        stringResource(R.string.search_placeholder),
                        style = AtlasTypography.bodyMd.copy(color = theme.onSurfaceVariant),
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
                    .size(48.dp)
                    .clickable(role = Role.Button) { onQueryChange("") }
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
