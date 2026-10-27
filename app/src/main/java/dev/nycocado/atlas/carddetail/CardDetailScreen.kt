package dev.nycocado.atlas.carddetail

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CARD_RATIO
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardFace
import dev.nycocado.atlas.card.CardFact
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardSection
import dev.nycocado.atlas.card.CardText
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.card.cardImage
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.card.printedText
import dev.nycocado.atlas.cardpane.CardStatusBarIcons
import dev.nycocado.atlas.cardpane.ListDetailLayout
import dev.nycocado.atlas.compendium.CardKind
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.UnofficialFooter
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.component.pointsText
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
import java.util.Locale
import kotlin.random.Random

@Composable
fun CardDetailScreen(
    card: Card,
    translations: CardTranslations,
    chosen: CardLanguage,
    onLanguageChange: (CardLanguage) -> Unit,
    layout: ListDetailLayout,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
) {
    // A new angle each time the card opens, kept while it stays open, rotation included. Never within 1.5° of
    // straight, where the card stops looking like it lies on the table.
    val tilt =
        rememberSaveable(card.id) {
            if (Random.nextBoolean()) lerp(-3f, -1.5f, Random.nextFloat()) else lerp(1.5f, 2.5f, Random.nextFloat())
        }
    // A card without the chosen language shows its printed text; the choice itself stays.
    val languages = (card as? NamedCard)?.let { translations.languages(it) }.orEmpty()
    val language = chosen.takeIf { it in languages } ?: CardLanguage.English
    val text = (card as? NamedCard)?.let { translations.text(it, language) }
    CardDetailContent(card, tilt, onBack, onAboutClick, layout, text, languages, language, onLanguageChange)
}

@Composable
fun CardDetailContent(
    card: Card,
    tilt: Float,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    layout: ListDetailLayout = ListDetailLayout.Single,
    text: CardText? = (card as? NamedCard)?.printedText(),
    languages: List<CardLanguage> = listOf(CardLanguage.English),
    language: CardLanguage = CardLanguage.English,
    onLanguageChange: (CardLanguage) -> Unit = {},
) {
    val theme = AtlasTheme.colors
    if (card !is NamedCard || text == null) return
    val face = card.face(text) ?: return
    // Ages and catastrophes have no color: their panel stays neutral, so it can't be read as a trait color.
    val colored = face.colors.isNotEmpty()
    val (panel, dots) =
        if (colored) {
            theme.trait(face.colors.first()).let { it.fill to it.onFill }
        } else {
            theme.surfaceSunk to theme.outlineSoft
        }
    // Status bar icons take the color the design puts on the panel, unless the list's header shares the bar.
    val onPanel = if (colored) dots else theme.onSurface
    CardStatusBarIcons(layout, onPanel.luminance() < 0.5f)

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
                face.faceValue?.let {
                    PointsBadge(it, Modifier.align(Alignment.BottomEnd).offset(x = (-64).dp, y = 30.dp))
                }
            }
            // Keyed by the card, so opening another card starts the animations fresh instead of animating from the last.
            key(card.id) {
                Column(
                    Modifier.padding(start = Space.s5, end = Space.s5, top = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Column {
                        BasicText(
                            tagged(card.name, CardLanguage.English),
                            Modifier.semantics { heading() },
                            style = AtlasTypography.display.copy(color = theme.onSurface),
                        )
                        AnimatedContent(
                            text.name.takeIf { language != CardLanguage.English }?.let { it to language },
                            transitionSpec = LanguageChange,
                            label = "translated name",
                        ) { shown ->
                            shown?.let { (name, shownLanguage) ->
                                BasicText(
                                    tagged(name, shownLanguage),
                                    style = AtlasTypography.titleLg.copy(color = theme.onSurfaceVariant),
                                )
                            }
                        }
                    }
                    Pills(face, language)
                    val selector: (@Composable () -> Unit)? =
                        if (languages.size > 1) {
                            { LanguageSelector(languages, language, onLanguageChange) }
                        } else {
                            null
                        }
                    val effect = face.section<CardSection.Effect>()
                    val flavor = face.section<CardSection.Flavor>()
                    val worldsEnd = face.section<CardSection.WorldsEnd>()
                    val hasBubble = effect != null || flavor != null
                    // Without a text bubble to hang on, the selector takes its own row where the bubble would be.
                    if (face.sections.isEmpty()) {
                        selector?.let { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { it() } }
                    }
                    if (hasBubble) Bubble(effect, flavor, language, selector = selector)
                    worldsEnd?.let {
                        Bubble(
                            it,
                            null,
                            language,
                            label = cardString(TraitIcon.WorldsEnd.label, language),
                            selector = selector.takeUnless { hasBubble },
                        )
                    }
                }
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
        // After the card, so a card picked up and dragged passes under it.
        RoundButton(R.drawable.ic_back, R.string.back, onBack, Modifier.padding(start = Space.s4, top = Space.s5))
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
    val description = LocalResources.current.pointsText(points)
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

private inline fun <reified T : CardSection> CardFace.section(): String? =
    sections.filterIsInstance<T>().firstOrNull()?.text

@Composable
private fun Pills(
    face: CardFace,
    language: CardLanguage,
) {
    val color = face.colors.firstOrNull()
    val theme = AtlasTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
        verticalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        face.facts.forEach { fact ->
            when (fact) {
                is CardFact.Color -> {
                    val palette = theme.trait(fact.color)
                    Pill(
                        stringResource(colorName(fact.color)),
                        palette.fill,
                        palette.onFill,
                    ) { TraitGlyph(fact.color, palette.onFill, 18.dp) }
                }

                is CardFact.Type -> {
                    Pill(stringResource(fact.kind.label), theme.surfaceSticker, theme.onSurface)
                }

                CardFact.Dominant -> {
                    Pill(
                        cardString(R.string.dominant, language),
                        theme.highlight,
                        theme.onHighlight,
                        language = language,
                    ) {
                        Symbol(R.drawable.ic_symbol_dominant)
                    }
                }

                is CardFact.Icon -> {
                    Pill(
                        cardString(fact.icon.label, language),
                        theme.surfaceSticker,
                        theme.onSurface,
                        language = language,
                        glyph =
                            fact.icon.symbol?.let {
                                { Symbol(it, actionColor = theme.trait(color).fill) }
                            },
                    )
                }

                // The coined -ling names stay English whatever the card language.
                is CardFact.Subspecies -> {
                    Pill(fact.name, theme.surfaceSticker, theme.onSurface, language = CardLanguage.English)
                }
            }
        }
    }
}

private val CardKind.label: Int
    @StringRes get() =
        when (this) {
            CardKind.Trait -> R.string.type_trait
            CardKind.Age -> R.string.type_age
            CardKind.Catastrophe -> R.string.type_catastrophe
        }

@Composable
private fun Pill(
    text: String,
    fill: Color,
    content: Color,
    language: CardLanguage? = null,
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
        BasicText(
            language?.let { tagged(text, it) } ?: AnnotatedString(text),
            style = AtlasTypography.labelLg.copy(color = content),
        )
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

// What a bubble shows, as one value so a language change crossfades all of it.
private data class BubbleText(
    val label: String?,
    val text: String?,
    val flavor: String?,
    val language: CardLanguage,
)

@Composable
private fun Bubble(
    text: String?,
    flavor: String?,
    language: CardLanguage,
    label: String? = null,
    selector: (@Composable () -> Unit)? = null,
) {
    val theme = AtlasTheme.colors
    // The selector hangs above the bubble, so the bubble keeps it as far from the pills as the pills are from each other.
    Box(Modifier.padding(top = 6.dp + if (selector != null) Space.s2 else 0.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .sticker(Radius.md, theme.surfaceSticker, theme.outline, shadow = Shadow.stickerLg)
                .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = Space.s4),
        ) {
            // The bubble wraps this, so it resizes with the crossfade.
            AnimatedContent(
                BubbleText(label, text, flavor, language),
                transitionSpec = LanguageChange,
                label = "bubble text",
            ) { shown ->
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.s3)) {
                    shown.label?.let {
                        BasicText(
                            tagged(it.uppercase(), shown.language),
                            style = AtlasTypography.labelSm.copy(color = theme.onSurfaceVariant),
                        )
                    }
                    shown.text?.let {
                        BasicText(
                            tagged(it, shown.language),
                            style = AtlasTypography.bodyLg.copy(color = theme.onSurface),
                        )
                    }
                    shown.flavor?.let {
                        BasicText(
                            tagged(it, shown.language),
                            style =
                                AtlasTypography.bodyMd.copy(
                                    color = theme.onSurfaceVariant,
                                    fontStyle = FontStyle.Italic,
                                ),
                        )
                    }
                }
            }
        }
        // Hangs on the bubble's top-right edge, 18dp above it and 14dp in, as the design sketch has it.
        selector?.let { Box(Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = (-18).dp)) { it() } }
    }
}

// Switching the card language is the player's own action, so it animates, with the duration and easing of the cards
// moving in the compendium list. Compose scales it to zero when Android's animations are removed.
private const val LANGUAGE_CHANGE_MILLIS = 350

private val LanguageChange: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    fadeIn(tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing)) togetherWith
        fadeOut(tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing)) using
        SizeTransform(clip = false) { _, _ -> tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing) }
}

// Each option names its language in itself, so it needs no string resource.
private val CardLanguage.endonym: String
    get() =
        when (this) {
            CardLanguage.English -> "English"
            CardLanguage.Portuguese -> "Português"
            CardLanguage.Spanish -> "Español"
        }

// A string in the card language, not the interface's: the labels follow the card they sit beside. AppCompatDelegate and
// LocaleManager would change the whole app's locale, which the card language must not. It only reads a resource, so the
// lint rule is for bundles with language splits, and the app ships as an APK.
@SuppressLint("AppBundleLocaleChanges")
@Composable
private fun cardString(
    @StringRes id: Int,
    language: CardLanguage,
): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    // A preview renders in the locale its @Preview names and ignores any other, so it shows that locale's label.
    if (LocalInspectionMode.current) return stringResource(id)
    return remember(context, configuration, language) {
        context
            .createConfigurationContext(
                Configuration(configuration).apply { setLocale(Locale.forLanguageTag(language.code)) },
            ).getString(id)
    }
}

// Text tagged with its language, so TalkBack reads a translation in its own language, not the interface's.
private fun tagged(
    text: String,
    language: CardLanguage,
) = buildAnnotatedString { withStyle(SpanStyle(localeList = LocaleList(language.code))) { append(text) } }

@Composable
private fun LanguageSelector(
    languages: List<CardLanguage>,
    selected: CardLanguage,
    onSelect: (CardLanguage) -> Unit,
) {
    val theme = AtlasTheme.colors
    val label = stringResource(R.string.card_language)
    // The highlight is drawn behind the options and slides between them. The index starts at the selected option, so
    // nothing slides when the card opens.
    val position by animateFloatAsState(
        languages.indexOf(selected).toFloat(),
        tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing),
        label = "language highlight",
    )
    val gap = 4.dp
    // Compose widens the touch target of a 30dp option to the 48dp minimum without changing its layout.
    Row(
        Modifier
            .background(theme.inverseSurface, CircleShape)
            .padding(3.dp)
            .width(IntrinsicSize.Max)
            .drawBehind {
                val option = (size.width - gap.toPx() * (languages.size - 1)) / languages.size
                val start = position * (option + gap.toPx())
                val x = if (layoutDirection == LayoutDirection.Ltr) start else size.width - option - start
                drawRoundRect(theme.highlight, Offset(x, 0f), Size(option, size.height), CornerRadius(size.height / 2))
            }.selectableGroup()
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        languages.forEach { language ->
            val active = language == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(30.dp)
                    .widthIn(min = 44.dp)
                    .clip(CircleShape)
                    .selectable(active, role = Role.RadioButton) { onSelect(language) }
                    .clearAndSetSemantics {
                        text = tagged(language.endonym, language)
                        this.selected = active
                        role = Role.RadioButton
                        onClick {
                            onSelect(language)
                            true
                        }
                    }.padding(horizontal = Space.s3),
                contentAlignment = Alignment.Center,
            ) {
                // TalkBack reads the endonym above, tagged with its language; the code is only for the eye.
                BasicText(
                    language.code.uppercase(),
                    style =
                        AtlasTypography.labelSm.copy(
                            color = if (active) theme.onHighlight else theme.onInverseSurface,
                        ),
                )
            }
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
internal fun CardDetailPreview() = AtlasTheme { CardDetailSample() }

@Composable
internal fun CardDetailSample(
    layout: ListDetailLayout = ListDetailLayout.Single,
    card: Card = SampleTrait,
) = BlankCardImages {
    CardDetailContent(card, tilt = -3f, onBack = {}, onAboutClick = {}, layout = layout)
}

private val SampleTrait =
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
    )

// The longest Classic card name in assets/doomlings/cards.json, verbatim.
internal val LongNameTrait =
    Trait(
        "classic/traits/random-fertilization",
        "classic",
        "",
        "Random Fertilization",
        "Pick a number between 1 and 64 trillion.",
        "Value is equal to the size of your Gene Pool.",
        null,
        listOf(CardColor.Green),
        false,
        emptyList(),
        listOf(TraitIcon.DropOfLife),
    )

@PreviewLightDark
@Composable
internal fun CardDetailLongNamePreview() = AtlasTheme { CardDetailSample(card = LongNameTrait) }

private val Both = listOf(CardLanguage.English, CardLanguage.Portuguese)

@Composable
internal fun TranslatedTraitSample() =
    BlankCardImages {
        CardDetailContent(
            LongNameTrait,
            tilt = -3f,
            onBack = {},
            onAboutClick = {},
            text =
                CardText(
                    "Fertilização Aleatória",
                    "O valor é igual ao tamanho do seu Gene Pool.",
                    "Escolha um número entre 1 e 64 trilhões.",
                    null,
                ),
            languages = Both,
            language = CardLanguage.Portuguese,
        )
    }

@PreviewLightDark
@Composable
internal fun TranslatedTraitDetailPreview() = AtlasTheme { TranslatedTraitSample() }

@PreviewLightDark
@Composable
internal fun TranslatedCatastropheDetailPreview() =
    AtlasTheme {
        BlankCardImages {
            CardDetailContent(
                Catastrophe("c", "classic", "", "A catastrophe", "Effect text.", "World's End text."),
                tilt = -3f,
                onBack = {},
                onAboutClick = {},
                text = CardText("Uma catástrofe", "Texto do efeito.", null, "Texto do Fim do Mundo."),
                languages = Both,
                language = CardLanguage.Portuguese,
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
