package dev.nycocado.atlas.carddetail

import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.component.BackButton
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.UnofficialFooter
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.component.signed
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait
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
    cards?.find { it.id == id }?.let { CardDetailContent(it, tilt, onBack, onAboutClick) }
}

@Composable
fun CardDetailContent(
    card: Card,
    tilt: Float,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
) {
    val colors = AtlasTheme.colors
    val (name, type) =
        when (card) {
            is Trait -> card.name to R.string.type_trait
            is Age -> card.name to R.string.type_age
            is Catastrophe -> card.name to R.string.type_catastrophe
            else -> return
        }
    val trait = card as? Trait
    // Ages and catastrophes have no color: their panel stays neutral, so it can't be read as a trait color.
    val (panel, dots) =
        if (card is Trait) {
            colors.trait(card.colors.firstOrNull()).let { it.fill to it.onFill }
        } else {
            colors.surfaceSunk to colors.outlineSoft
        }

    BoxWithConstraints(Modifier.fillMaxSize().background(colors.surface)) {
        // At least screen-tall, so the notice sits at the bottom of short cards and right after long ones.
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
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
                    name,
                    Modifier.semantics { heading() },
                    style = AtlasTypography.display.copy(color = colors.onSurface),
                )
                Pills(trait, type)
                val effect = (card as? Trait)?.effect ?: (card as? Age)?.effect ?: (card as? Catastrophe)?.effect
                if (effect != null || trait?.flavor != null) Bubble(effect, trait?.flavor)
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
private const val CARD_RATIO = 541f / 756f

private val PanelShape = RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp)

@Composable
private fun Panel(
    image: String,
    tilt: Float,
    fill: Color,
    dots: Color,
    onBack: () -> Unit,
) {
    val colors = AtlasTheme.colors
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
            }.border(Stroke.strong, colors.outline, PanelShape)
            .padding(top = Stroke.strong, start = Stroke.strong, end = Stroke.strong)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(420.dp),
    ) {
        BackButton(onBack, Modifier.padding(start = Space.s4, top = Space.s5))
        val scope = rememberCoroutineScope()
        val drag = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
        val lift = remember { Animatable(0f) }
        val drop = {
            scope.launch { drag.animateTo(Offset.Zero, spring(Spring.DampingRatioMediumBouncy)) }
            scope.launch { lift.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
        }
        AsyncImage(
            model = "file:///android_asset/cards/$image",
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
                        colors.surfaceSticker,
                        colors.outline,
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
    val colors = AtlasTheme.colors
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
            .sticker(CircleShape, colors.highlight, colors.outline, Stroke.strong, Shadow.sticker)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(signed(points), style = AtlasTypography.numeral.copy(color = colors.onHighlight, fontSize = 32.sp))
    }
}

@Composable
private fun Pills(
    trait: Trait?,
    @StringRes type: Int,
) {
    val colors = AtlasTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
        verticalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        trait?.colors?.ifEmpty { listOf(null) }?.forEach { color ->
            val palette = colors.trait(color)
            Pill(
                stringResource(colorName(color)),
                palette.fill,
                palette.onFill,
            ) { TraitGlyph(color, palette.onFill, 18.dp) }
        }
        Pill(stringResource(type), colors.surfaceSticker, colors.onSurface)
        if (trait?.dominant == true) Pill(stringResource(R.string.dominant), colors.highlight, colors.onHighlight)
        trait?.icons?.forEach { Pill(stringResource(it.label), colors.surfaceSticker, colors.onSurface) }
        trait?.subspecies?.forEach { Pill(it, colors.surfaceSticker, colors.onSurface) }
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
            .height(32.dp)
            .sticker(CircleShape, fill, AtlasTheme.colors.outline, Stroke.thin)
            .padding(horizontal = Space.s3),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        glyph?.invoke()
        BasicText(text, style = AtlasTypography.labelLg.copy(color = content, fontSize = 14.sp))
    }
}

@Composable
private fun Bubble(
    text: String?,
    flavor: String?,
    @StringRes label: Int? = null,
) {
    val colors = AtlasTheme.colors
    Column(
        Modifier
            .padding(top = 6.dp)
            .fillMaxWidth()
            .sticker(Radius.md, colors.surfaceSticker, colors.outline, shadow = Shadow.stickerLg)
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = Space.s4),
        verticalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        label?.let {
            BasicText(
                stringResource(it).uppercase(),
                style = AtlasTypography.labelSm.copy(color = colors.onSurfaceVariant),
            )
        }
        text?.let { BasicText(it, style = AtlasTypography.bodyLg.copy(color = colors.onSurface)) }
        flavor?.let {
            BasicText(
                it,
                style =
                    AtlasTypography.bodyMd.copy(
                        color = colors.onSurfaceVariant,
                        fontStyle = FontStyle.Italic,
                        fontSize = 14.sp,
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

@Composable
fun DetailPlaceholder() {
    val colors = AtlasTheme.colors
    Box(Modifier.fillMaxSize().background(colors.surface).padding(Space.s6), contentAlignment = Alignment.Center) {
        BasicText(
            stringResource(R.string.detail_placeholder),
            style = AtlasTypography.bodyMd.copy(color = colors.onSurfaceVariant, textAlign = TextAlign.Center),
        )
    }
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
                    listOf(TraitIcon.Action),
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
