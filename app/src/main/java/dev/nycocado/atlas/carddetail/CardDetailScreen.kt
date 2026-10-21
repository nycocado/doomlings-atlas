package dev.nycocado.atlas.carddetail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CARD_RATIO
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.card.cardImage
import dev.nycocado.atlas.compendium.CardKind
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.compendium.kind
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.UnofficialFooter
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.component.signed
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.StatusBarIcons
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait
import dev.nycocado.atlas.ui.theme.wideWindow
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun CardDetailScreen(
    viewModel: CompendiumViewModel,
    id: String,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val cards by viewModel.cards.collectAsStateWithLifecycle()
    // A new angle each time the card opens, kept while it stays open, rotation included. Never within 1.5° of
    // straight, where the card stops looking like it lies on the table.
    val tilt =
        rememberSaveable(id) {
            if (Random.nextBoolean()) lerp(-3f, -1.5f, Random.nextFloat()) else lerp(1.5f, 2.5f, Random.nextFloat())
        }
    val card = cards?.find { it.id == id }
    // A back stack restored after an update can name a card the data no longer has.
    if (cards != null && card == null) LaunchedEffect(id) { onBack() }
    card?.let { CardDetailContent(it, tilt, onBack, onAboutClick) }
}

@Composable
fun CardDetailContent(
    card: Card,
    tilt: Float,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val theme = AtlasTheme.colors
    if (card !is NamedCard) return
    val type =
        when (card.kind) {
            CardKind.Trait -> R.string.type_trait
            CardKind.Age -> R.string.type_age
            CardKind.Catastrophe -> R.string.type_catastrophe
            null -> return
        }
    val trait = card as? Trait
    // Ages and catastrophes have no color: their panel stays neutral, so it can't be read as a trait color.
    val (panel, dots) =
        if (card is Trait) {
            theme.trait(card.colors.firstOrNull()).let { it.fill to it.onFill }
        } else {
            theme.surfaceSunk to theme.outlineSoft
        }
    // Status bar icons take the color the design puts on the panel.
    val onPanel = if (card is Trait) dots else theme.onSurface
    // Not beside the list, where the status bar also spans the list's dark header.
    if (!wideWindow()) StatusBarIcons(onPanel.luminance() < 0.5f)

    BoxWithConstraints(Modifier.fillMaxSize().background(theme.surface)) {
        // At least screen-tall, so the notice sits at the bottom of short cards and right after long ones.
        Column(
            Modifier
                // Back to the top for each card, as the pane stays when another card opens.
                .verticalScroll(rememberSaveable(card.id, saver = ScrollState.Saver) { ScrollState(0) })
                .heightIn(min = maxHeight),
        ) {
            Box {
                Panel(card.image, tilt, panel, dots, onBack)
                trait?.points?.let {
                    PointsBadge(it, Modifier.align(Alignment.BottomEnd).offset(x = (-64).dp, y = 30.dp))
                }
            }
            Column(
                Modifier.padding(start = Space.s5, end = Space.s5, top = 22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BasicText(
                    card.name,
                    Modifier.semantics { heading() },
                    style = AtlasTypography.display.copy(color = theme.onSurface),
                )
                Pills(trait, type)
                if (card.effect != null || trait?.flavor != null) Bubble(card.effect, trait?.flavor)
                (card as? Catastrophe)?.let { Bubble(it.worldsEnd, null, label = R.string.worlds_end) }
            }
            Spacer(Modifier.weight(1f))
            UnofficialFooter(
                onAboutClick,
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = Space.s2, vertical = Space.s4),
            )
        }
    }
}

private val CardWidth = 228.dp
private val PanelShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)

@Composable
private fun Panel(
    image: String,
    tilt: Float,
    fill: Color,
    dots: Color,
    onBack: () -> Unit,
) {
    val theme = AtlasTheme.colors
    Box(
        Modifier
            .fillMaxWidth()
            .bleed(Stroke.strong)
            // A dragged card can leave the panel, but only shows inside its frame.
            .clip(PanelShape)
            .background(fill)
            .drawBehind {
                val step = 18.dp.toPx()
                val radius = 1.6.dp.toPx()
                var y = step / 2
                while (y < size.height) {
                    var x = step / 2
                    while (x < size.width) {
                        drawCircle(dots, radius, Offset(x, y))
                        x += step
                    }
                    y += step
                }
            }.border(Stroke.strong, theme.outline, PanelShape)
            .padding(top = Stroke.strong, start = Stroke.strong, end = Stroke.strong)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(420.dp),
    ) {
        RoundButton(R.drawable.ic_back, R.string.back, onBack, Modifier.padding(start = Space.s4, top = Space.s5))
        val scope = rememberCoroutineScope()
        val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
        val lift = remember { Animatable(0f) }
        val drop = {
            scope.launch { drag.animateTo(Offset.Zero, spring(Spring.DampingRatioMediumBouncy)) }
            scope.launch { lift.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
        }
        AsyncImage(
            model = cardImage(image),
            contentDescription = null,
            modifier =
                Modifier
                    .align(Alignment.Center)
                    // Easter egg: long press picks the card up off the table, drag moves it, release puts it back.
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                scope.launch {
                                    lift.animateTo(
                                        1f,
                                        spring(Spring.DampingRatioMediumBouncy),
                                    )
                                }
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                scope.launch { drag.snapTo(drag.value + amount) }
                            },
                            onDragEnd = { drop() },
                            onDragCancel = { drop() },
                        )
                    }.offset { drag.value.round() }
                    .graphicsLayer {
                        scaleX = 1f + 0.08f * lift.value
                        scaleY = 1f + 0.08f * lift.value
                        rotationZ = tilt * (1f - lift.value)
                    }.width(CardWidth)
                    .aspectRatio(CARD_RATIO)
                    .sticker(
                        Radius.cardLg,
                        theme.surfaceSticker,
                        theme.outline,
                        Stroke.strong,
                        Shadow.stickerLg + 6.dp * lift.value,
                    ).clip(Radius.cardLg),
        )
    }
}

// The panel's outline should only show along its rounded base, so the panel reaches past the screen's sides and top.
private fun Modifier.bleed(amount: Dp) =
    layout { measurable, constraints ->
        val px = amount.roundToPx()
        val width = constraints.maxWidth + px * 2
        val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
        layout(constraints.maxWidth, placeable.height - px) { placeable.place(-px, -px) }
    }

@Composable
private fun PointsBadge(
    points: Int,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    val description = pluralStringResource(R.plurals.card_points, points, signed(points))
    val scope = rememberCoroutineScope()
    val spin = remember { Animatable(0f) }
    Box(
        modifier
            // Easter egg: a tap flips the badge around like a coin.
            .pointerInput(Unit) {
                detectTapGestures {
                    scope.launch {
                        spin.animateTo(
                            spin.targetValue + 360f,
                            spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
                        )
                    }
                }
            }.graphicsLayer { rotationZ = 8f + spin.value }
            .size(68.dp)
            .sticker(CircleShape, theme.highlight, theme.outline, Stroke.strong, Shadow.sticker)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(signed(points), style = AtlasTypography.numeralLg.copy(color = theme.onHighlight))
    }
}

@Composable
private fun Pills(
    trait: Trait?,
    @StringRes type: Int,
) {
    val theme = AtlasTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
        verticalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        trait?.colors?.ifEmpty { listOf(null) }?.forEach { color ->
            val palette = theme.trait(color)
            Pill(
                stringResource(colorName(color)),
                palette.fill,
                palette.onFill,
            ) { TraitGlyph(color, palette.onFill, 18.dp) }
        }
        Pill(stringResource(type), theme.surfaceSticker, theme.onSurface)
        if (trait?.dominant == true) {
            Pill(stringResource(R.string.dominant), theme.highlight, theme.onHighlight) {
                Symbol(R.drawable.ic_symbol_dominant)
            }
        }
        trait?.icons?.forEach { icon ->
            Pill(
                stringResource(icon.label),
                theme.surfaceSticker,
                theme.onSurface,
                icon.symbol?.let {
                    { Symbol(it, actionColor = theme.trait(trait.colors.firstOrNull()).fill) }
                },
            )
        }
        trait?.subspecies?.forEach { Pill(it, theme.surfaceSticker, theme.onSurface) }
    }
}

@Composable
private fun Pill(
    text: String,
    fill: Color,
    content: Color,
    glyph: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier
            .heightIn(min = 32.dp)
            .sticker(CircleShape, fill, AtlasTheme.colors.outline, Stroke.thin)
            .padding(horizontal = Space.s3),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        glyph?.invoke()
        BasicText(text, style = AtlasTypography.labelLg.copy(color = content))
    }
}

// Decorative: the pill's text names it.
@Composable
private fun Symbol(
    @DrawableRes id: Int,
    actionColor: Color = Color.Unspecified,
) {
    val vector = ImageVector.vectorResource(id)
    // The action symbol is printed in the card's color; its drawable names those paths card_color.
    val painter =
        if (id == R.drawable.ic_symbol_action) {
            rememberVectorPainter(remember(vector, actionColor) { vector.withCardColor(actionColor) })
        } else {
            rememberVectorPainter(vector)
        }
    Image(painter, null, Modifier.height(18.dp))
}

// ImageVector has no way to change a path's fill, so the paths are copied with the card_color ones refilled. Converted
// symbols are flat lists of paths, without groups.
private fun ImageVector.withCardColor(color: Color): ImageVector =
    ImageVector
        .Builder(name, defaultWidth, defaultHeight, viewportWidth, viewportHeight)
        .apply {
            root.filterIsInstance<VectorPath>().forEach {
                addPath(
                    it.pathData,
                    it.pathFillType,
                    it.name,
                    if (it.name == "card_color") SolidColor(color) else it.fill,
                    it.fillAlpha,
                    it.stroke,
                    it.strokeAlpha,
                    it.strokeLineWidth,
                    it.strokeLineCap,
                    it.strokeLineJoin,
                    it.strokeLineMiter,
                )
            }
        }.build()

@Composable
private fun Bubble(
    text: String?,
    flavor: String?,
    @StringRes label: Int? = null,
) {
    val theme = AtlasTheme.colors
    Column(
        Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .sticker(Radius.md, theme.surfaceSticker, theme.outline, shadow = Shadow.stickerLg)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = Space.s4),
        verticalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        label?.let {
            BasicText(
                stringResource(it).uppercase(),
                style = AtlasTypography.labelSm.copy(color = theme.onSurfaceVariant),
            )
        }
        text?.let { BasicText(it, style = AtlasTypography.bodyLg.copy(color = theme.onSurface)) }
        flavor?.let {
            BasicText(
                it,
                style =
                    AtlasTypography.bodyMd.copy(
                        color = theme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                    ),
            )
        }
    }
}

private val TraitIcon.label: Int
    @StringRes get() =
        when (this) {
            TraitIcon.Action -> R.string.icon_action
            TraitIcon.Attachment -> R.string.icon_attachment
            TraitIcon.Coral -> R.string.icon_coral
            TraitIcon.Curious -> R.string.icon_curious
            TraitIcon.Dragon -> R.string.icon_dragon
            TraitIcon.DropOfLife -> R.string.icon_drop_of_life
            TraitIcon.Exotic -> R.string.icon_exotic
            TraitIcon.Fish -> R.string.icon_fish
            TraitIcon.PlayWhen -> R.string.icon_play_when
            TraitIcon.Shell -> R.string.icon_shell
            TraitIcon.Spell -> R.string.icon_spell
            TraitIcon.Suppress -> R.string.icon_suppress
            TraitIcon.WorldsEnd -> R.string.worlds_end
        }

// Only the symbols the enabled sets use are converted; the rest have none yet.
private val TraitIcon.symbol: Int?
    @DrawableRes get() =
        when (this) {
            TraitIcon.Action -> R.drawable.ic_symbol_action
            TraitIcon.DropOfLife -> R.drawable.ic_symbol_drop_of_life
            TraitIcon.PlayWhen -> R.drawable.ic_symbol_play_when
            TraitIcon.WorldsEnd -> R.drawable.ic_symbol_worlds_end
            else -> null
        }

@PreviewLightDark
@Composable
internal fun CardDetailPreview() =
    AtlasTheme {
        BlankCardImages {
            CardDetailContent(
                Trait(
                    "blue",
                    "classic",
                    "",
                    "Blue trait",
                    "Flavor text goes here.",
                    "Effect text goes here.",
                    -1,
                    listOf(CardColor.Blue),
                    true,
                    emptyList(),
                    listOf(TraitIcon.Action, TraitIcon.DropOfLife, TraitIcon.PlayWhen, TraitIcon.WorldsEnd),
                ),
                tilt = -3f,
                onBack = {},
                onAboutClick = {},
            )
        }
    }

@PreviewLightDark
@Composable
internal fun CatastropheDetailPreview() =
    AtlasTheme {
        BlankCardImages {
            CardDetailContent(
                Catastrophe("c", "classic", "", "A catastrophe", "Effect text.", "World's End text."),
                tilt = -3f,
                onBack = {},
                onAboutClick = {},
            )
        }
    }
