package dev.nycocado.atlas.carddetail

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.CARD_RATIO
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.CardText
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Effect
import dev.nycocado.atlas.card.EffectKind
import dev.nycocado.atlas.card.GenePool
import dev.nycocado.atlas.card.MeaningOfLife
import dev.nycocado.atlas.card.Merchant
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.card.Treasure
import dev.nycocado.atlas.card.cardImage
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.cardpane.CardStatusBarIcons
import dev.nycocado.atlas.cardpane.ListDetailLayout
import dev.nycocado.atlas.ui.component.BlankCardImages
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.component.UnofficialFooter
import dev.nycocado.atlas.ui.component.pointsText
import dev.nycocado.atlas.ui.component.shownName
import dev.nycocado.atlas.ui.component.signed
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.inkOver
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait
import dev.nycocado.atlas.ui.theme.traitPaint
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
    library: CardLibrary,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onCardClick: (String) -> Unit,
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
    CardDetailContent(
        card = card,
        tilt = tilt,
        onBack = onBack,
        onAboutClick = onAboutClick,
        layout = layout,
        translations = translations,
        languages = languages,
        language = language,
        onLanguageChange = onLanguageChange,
        library = library,
        onCardClick = onCardClick,
    )
}

@Composable
fun CardDetailContent(
    card: Card,
    tilt: Float,
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    layout: ListDetailLayout = ListDetailLayout.Single,
    translations: CardTranslations = CardTranslations(),
    languages: List<CardLanguage> = listOf(CardLanguage.English),
    language: CardLanguage = CardLanguage.English,
    onLanguageChange: (CardLanguage) -> Unit = {},
    library: CardLibrary = CardLibrary.loading,
    onCardClick: (String) -> Unit = {},
) {
    val theme = AtlasTheme.colors
    val face = translations.face(card, language)
    // Only traits have a color: every other card's panel stays neutral, so it can't be read as a trait color.
    val colored = face.colors.isNotEmpty()
    val fills = if (colored) face.colors.map { theme.trait(it).fill } else listOf(theme.surfaceSunk)
    val dots = if (colored) theme.inkOver(face.colors) else theme.outlineSoft
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
                Panel(card.image, tilt, fills, dots, onBack)
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
                            if (card is NamedCard) {
                                tagged(
                                    card.name,
                                    CardLanguage.English,
                                )
                            } else {
                                AnnotatedString(card.shownName())
                            },
                            Modifier.semantics { heading() },
                            style = AtlasTypography.display.copy(color = theme.onSurface),
                        )
                        AnimatedContent(
                            face.name.takeIf { language != CardLanguage.English }?.let { it to language },
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
                    Pills(face, language, library.setName(card))
                    val selector: (@Composable () -> Unit)? =
                        if (languages.size > 1) {
                            { LanguageSelector(languages, language, onLanguageChange) }
                        } else {
                            null
                        }
                    // Without a text bubble to hang on, the selector takes its own row where the bubble would be.
                    if (face.blocks.isEmpty()) {
                        selector?.let { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) { it() } }
                    }
                    face.blocks.forEach { block ->
                        BlockBubble(
                            block,
                            language,
                            card,
                            face.colors,
                            library,
                            onCardClick,
                            selector.takeIf { block.selector },
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
    fills: List<Color>,
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
            .drawBehind {
                val step = 18.dp.toPx()
                val radius = 1.6.dp.toPx()
                drawRect(traitPaint(fills, Offset(size.width, size.height)))
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
            .size(Sizes.pointsBadge)
            .sticker(CircleShape, theme.highlight, theme.outline, Stroke.strong, Shadow.sticker)
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        BasicText(signed(points), style = AtlasTypography.numeralLg.copy(color = theme.onHighlight))
    }
}

// Switching the card language is the player's own action, so it animates, with the duration and easing of the cards
// moving in the compendium list. Compose scales it to zero when Android's animations are removed.
internal const val LANGUAGE_CHANGE_MILLIS = 350

internal val LanguageChange: AnimatedContentTransitionScope<*>.() -> ContentTransform = {
    fadeIn(tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing)) togetherWith
        fadeOut(tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing)) using
        SizeTransform(clip = false) { _, _ -> tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing) }
}

// A string in the card language, not the interface's: the labels follow the card they sit beside. AppCompatDelegate and
// LocaleManager would change the whole app's locale, which the card language must not. It only reads a resource, so the
// lint rule is for bundles with language splits, and the app ships as an APK.
@SuppressLint("AppBundleLocaleChanges")
@Composable
internal fun cardString(
    @StringRes id: Int,
    language: CardLanguage,
    vararg args: Any,
): String {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    // A preview renders in the locale its @Preview names and ignores any other, so it shows that locale's label.
    if (LocalInspectionMode.current) return stringResource(id, *args)
    return remember(context, configuration, language, args.toList()) {
        context
            .createConfigurationContext(
                Configuration(configuration).apply { setLocale(Locale.forLanguageTag(language.code)) },
            ).getString(id, *args)
    }
}

// Text tagged with its language, so TalkBack reads a translation in its own language, not the interface's.
internal fun tagged(
    text: String,
    language: CardLanguage,
) = buildAnnotatedString { withStyle(SpanStyle(localeList = LocaleList(language.code))) { append(text) } }

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
        listOf(Effect(text = "Effect text goes here.")),
        null,
        -1,
        listOf(CardColor.Blue),
        true,
        emptyList(),
        listOf(TraitIcon.Action, TraitIcon.DropOfLife, TraitIcon.PlayWhen, TraitIcon.WorldsEnd),
    )

// The longest Classic card name in assets/doomlings/classic/cards.json, verbatim.
internal val LongNameTrait =
    Trait(
        "classic/traits/random-fertilization",
        "classic",
        "",
        "Random Fertilization",
        "Pick a number between 1 and 64 trillion.",
        listOf(Effect(text = "Value is equal to the size of your Gene Pool.")),
        null,
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
            translations =
                CardTranslations(
                    mapOf(
                        CardLanguage.Portuguese to
                            mapOf(
                                LongNameTrait.id to
                                    CardText(
                                        "Fertilização Aleatória",
                                        listOf("O valor é igual ao tamanho do seu Gene Pool."),
                                        "Escolha um número entre 1 e 64 trilhões.",
                                    ),
                            ),
                    ),
                ),
            languages = Both,
            language = CardLanguage.Portuguese,
        )
    }

@PreviewLightDark
@Composable
internal fun TranslatedTraitDetailPreview() = AtlasTheme { TranslatedTraitSample() }

// AI Takeover as in assets/doomlings/classic/cards.json, with its Gene Pool and World's End.
private val AiTakeover =
    Catastrophe(
        "classic/catastrophes/ai-takeover",
        "classic",
        "",
        "AI Takeover",
        listOf(
            Effect(text = "Discard all but 1 card from your hand."),
            Effect(
                EffectKind.WorldsEnd,
                "Each colorless trait is now worth 2. Ignore all colorless trait effects.\n\n(Excluding dominants.)",
            ),
        ),
        -1,
    )

@PreviewLightDark
@Composable
internal fun TranslatedCatastropheDetailPreview() =
    AtlasTheme {
        val catastrophe = AiTakeover
        BlankCardImages {
            CardDetailContent(
                catastrophe,
                tilt = -3f,
                onBack = {},
                onAboutClick = {},
                translations =
                    CardTranslations(
                        mapOf(
                            CardLanguage.Portuguese to
                                mapOf(
                                    catastrophe.id to
                                        CardText(
                                            "Domínio da IA",
                                            listOf(
                                                "Descarte todas as cartas da sua mão, menos 1.",
                                                "Cada trait incolor agora vale 2. " +
                                                    "Ignore todos os efeitos de traits incolores.\n\n(Exceto dominantes.)",
                                            ),
                                        ),
                                ),
                        ),
                    ),
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
                AiTakeover,
                tilt = -3f,
                onBack = {},
                onAboutClick = {},
            )
        }
    }

// Age of Dracula and Vampirism as in assets/doomlings/classic/cards.json, so Age of Dracula cites Vampirism.
@PreviewLightDark
@Composable
internal fun CitedCardDetailPreview() =
    AtlasTheme {
        BlankCardImages {
            CardDetailContent(
                Age(
                    "classic/ages/age-of-dracula",
                    "classic",
                    "",
                    "Age of Dracula",
                    listOf(
                        Effect(text = "Discard 1 random card from your hand."),
                        Effect(
                            text = "If Vampirism is in your trait pile, steal 1 random card from an opponent's hand.",
                        ),
                    ),
                ),
                tilt = -3f,
                onBack = {},
                onAboutClick = {},
                library =
                    CardLibrary.decode(
                        """[{"id": "classic/traits/vampirism", "type": "trait", "set": "classic", "name": "Vampirism",
                        "flavor": null, "effects": [], "genePool": null, "points": 3, "colors": ["purple"], "dominant": true,
                        "subspecies": [], "icons": [], "image": ""}]""",
                    ),
            )
        }
    }

// Magic Beans as in assets/doomlings/imaginary-ends/cards.json: an Action, then a note with a token.
private val MagicBeans =
    Trait(
        "imaginary-ends/traits/magic-beans",
        "imaginary-ends",
        "",
        "Magic Beans",
        "Who knows what these could grow into?",
        listOf(
            Effect(EffectKind.Action, "Draw 1. If its face value is 1, play it and repeat."),
            Effect(EffectKind.Note, "Ignore actions ({action})."),
        ),
        null,
        1,
        listOf(CardColor.Green),
        false,
        listOf("Glitterling"),
        listOf(TraitIcon.Action),
    )

@PreviewLightDark
@Composable
internal fun ActionNoteTokenDetailPreview() = AtlasTheme { CardDetailSample(card = MagicBeans) }

// Carnivore as in assets/doomlings/legends-of-enderas/cards.json, with its Dinoling token.
private val Carnivore =
    Trait(
        "legends-of-enderas/traits/carnivore",
        "legends-of-enderas",
        "",
        "Carnivore",
        "A healthy, balanced meal includes meat with two sides of meat.",
        listOf(
            Effect(EffectKind.Action, "Discard all Dinolings ({dinoling}) from your trait pile."),
            Effect(EffectKind.Note, "(Excluding this one.)"),
        ),
        null,
        6,
        listOf(CardColor.Green),
        false,
        listOf("Dinoling"),
        listOf(TraitIcon.Action),
    )

@PreviewLightDark
@Composable
internal fun SubspeciesTokenDetailPreview() = AtlasTheme { CardDetailSample(card = Carnivore) }

// Fish Collector as in assets/doomlings/castle-glass/cards.json: blue and purple, whose on-colors differ.
internal val FishCollector =
    Trait(
        "castle-glass/traits/fish-collector",
        "castle-glass",
        "",
        "Fish Collector",
        "I hope I can find the other boot.",
        listOf(Effect(EffectKind.Action, "Steal a face value 1 from up to two trait piles.")),
        null,
        0,
        listOf(CardColor.Blue, CardColor.Purple),
        false,
        emptyList(),
        listOf(TraitIcon.Action),
    )

@PreviewLightDark
@Composable
internal fun DualColorDetailPreview() = AtlasTheme { CardDetailSample(card = FishCollector) }

// Awesome! as in assets/doomlings/imaginary-ends/cards.json, one of the traits of all four colors.
private val Awesome =
    Trait(
        "imaginary-ends/traits/awesome",
        "imaginary-ends",
        "",
        "Awesome!",
        "Fear me! Tehee!",
        listOf(
            Effect(EffectKind.WorldsEnd, "Stabilize to 2."),
            Effect(text = "Add 2 face values in your hand to your score.\n\n(Max +17)"),
        ),
        null,
        1,
        listOf(CardColor.Blue, CardColor.Green, CardColor.Purple, CardColor.Red),
        true,
        listOf("Glitterling"),
        listOf(TraitIcon.DropOfLife, TraitIcon.WorldsEnd),
    )

@PreviewLightDark
@Composable
internal fun FourColorDetailPreview() = AtlasTheme { CardDetailSample(card = Awesome) }

// Eloquence as in assets/doomlings/classic/cards.json: a trait prints its World's End in the normal text color.
private val Eloquence =
    Trait(
        "classic/traits/eloquence",
        "classic",
        "",
        "Eloquence",
        "Say the right thing, then stop talking.",
        listOf(Effect(EffectKind.WorldsEnd, "Play another trait. Ignore its action ({action}).")),
        null,
        1,
        emptyList(),
        false,
        emptyList(),
        listOf(TraitIcon.WorldsEnd),
    )

@PreviewLightDark
@Composable
internal fun TraitWorldsEndDetailPreview() = AtlasTheme { CardDetailSample(card = Eloquence) }

// The cards below are as in assets/doomlings/<set>/cards.json.
private val BlueGenePool = GenePool("classic/gene-pools/blue", "classic", "")

private val ArmoredMelon =
    MeaningOfLife(
        "upgrade/meaning-of-life/the-armored-melon",
        "upgrade",
        "",
        "The Armored Melon",
        listOf(Effect(text = "+3 if you have 3-5 cards in hand.\n\n+6 if you have 6 or more.")),
        null,
    )

private val ArdentGrip =
    Treasure(
        "legends-of-enderas/treasures/ardent-grip",
        "legends-of-enderas",
        "",
        "Ardent Grip",
        listOf(Effect(EffectKind.Rush, "Set your Gene Pool to 5."), Effect(EffectKind.Rush, "Draw 1.")),
    )

private val Bon =
    Merchant(
        "imaginary-ends/merchants/bon",
        "imaginary-ends",
        "",
        "Bon",
        listOf(Effect(EffectKind.Bargain, "Trash 1 trinket, then gain 1 trinket.\nYou may repeat once.")),
    )

@PreviewLightDark
@Composable
internal fun BirthOfLifeDetailPreview() = AtlasTheme { CardDetailSample(card = TheBirthOfLife) }

@PreviewLightDark
@Composable
internal fun GenePoolDetailPreview() = AtlasTheme { CardDetailSample(card = BlueGenePool) }

@PreviewLightDark
@Composable
internal fun MeaningOfLifeDetailPreview() = AtlasTheme { CardDetailSample(card = ArmoredMelon) }

@PreviewLightDark
@Composable
internal fun TreasureDetailPreview() = AtlasTheme { CardDetailSample(card = ArdentGrip) }

@PreviewLightDark
@Composable
internal fun TrinketDetailPreview() = AtlasTheme { CardDetailSample(card = AnxietyElixir) }

@PreviewLightDark
@Composable
internal fun MerchantDetailPreview() = AtlasTheme { CardDetailSample(card = Bon) }
