package dev.nycocado.atlas.carddetail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.BlockContent
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardBlock
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.Effect
import dev.nycocado.atlas.card.EffectKind
import dev.nycocado.atlas.card.Ink
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.TextPart
import dev.nycocado.atlas.card.Token
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.card.Trinket
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.ui.component.signed
import dev.nycocado.atlas.ui.theme.AtlasColors
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait

// What a bubble shows, as one value so a language change crossfades all of it.
private data class BubbleText(
    val block: CardBlock,
    val language: CardLanguage,
)

@Composable
internal fun BlockBubble(
    block: CardBlock,
    language: CardLanguage,
    card: Card,
    // The card's first color, which its action symbols are printed in; null for a colorless trait or a card without color.
    color: CardColor?,
    library: CardLibrary = CardLibrary.loading,
    onCardClick: (String) -> Unit = {},
    selector: (@Composable () -> Unit)? = null,
) {
    val theme = AtlasTheme.colors
    val actionColor = theme.trait(color).fill
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
                BubbleText(block, language),
                transitionSpec = LanguageChange,
                label = "bubble text",
            ) { shown ->
                val shownLanguage = shown.language
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(Space.s3)) {
                    when (val content = shown.block.content) {
                        is BlockContent.GenePool -> {
                            Line(
                                cardString(R.string.gene_pool_line, shownLanguage, signedPlus(content.genePool)),
                                shownLanguage,
                            )
                        }

                        is BlockContent.Value -> {
                            Line(cardString(R.string.value_line, shownLanguage, signed(content.value)), shownLanguage)
                        }

                        is BlockContent.Effect -> {
                            content.kind?.title?.let {
                                KindHeading(it, content.kind?.symbol, shownLanguage, actionColor, content.ink)
                            }
                            EffectText(
                                content.text,
                                note = content.kind == EffectKind.Note,
                                language = shownLanguage,
                                card = card,
                                library = library,
                                onCardClick = onCardClick,
                                actionColor = actionColor,
                            )
                        }

                        null -> {}
                    }
                    shown.block.flavor?.let {
                        BasicText(
                            tagged(it, shownLanguage),
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

@Composable
private fun Line(
    text: String,
    language: CardLanguage,
) = BasicText(tagged(text, language), style = AtlasTypography.bodyLg.copy(color = AtlasTheme.colors.onSurface))

// The printed line is "+N Gene Pool", and "+0" when it only restates the pool.
private fun signedPlus(n: Int) = if (n < 0) signed(n) else "+$n"

@Composable
private fun KindHeading(
    @StringRes title: Int,
    @DrawableRes symbol: Int?,
    language: CardLanguage,
    actionColor: Color,
    ink: Ink?,
) {
    val theme = AtlasTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        symbol?.let { Symbol(it, Modifier.height(16.dp), actionColor) }
        BasicText(
            tagged(cardString(title, language).uppercase(), language),
            style =
                ink?.let { AtlasTypography.labelSm.copy(brush = theme.brush(it)) }
                    ?: AtlasTypography.labelSm.copy(color = theme.onSurfaceVariant),
        )
    }
}

// The name of a kind the card detail shows above its text; a note shows none, as its small type sets it apart.
private val EffectKind.title: Int?
    @StringRes get() =
        when (this) {
            EffectKind.Action -> R.string.icon_action
            EffectKind.PlayWhen -> R.string.icon_play_when
            EffectKind.WorldsEnd -> R.string.worlds_end
            EffectKind.Note -> null
            EffectKind.AwesomePower -> R.string.effect_awesome_power
            EffectKind.PocketObjective -> R.string.effect_pocket_objective
            EffectKind.Rush -> R.string.effect_rush
            EffectKind.Mutate -> R.string.effect_mutate
            EffectKind.Echo -> R.string.effect_echo
            EffectKind.Cloak -> R.string.effect_cloak
            EffectKind.Adapt -> R.string.effect_adapt
            EffectKind.Exchange -> R.string.effect_exchange
            EffectKind.Buy -> R.string.effect_buy
            EffectKind.Collect -> R.string.effect_collect
            EffectKind.Bargain -> R.string.effect_bargain
        }

// Labels have no symbol on the card.
private val EffectKind.symbol: Int?
    @DrawableRes get() =
        when (this) {
            EffectKind.Action -> R.drawable.ic_symbol_action
            EffectKind.PlayWhen -> R.drawable.ic_symbol_play_when
            EffectKind.WorldsEnd -> R.drawable.ic_symbol_worlds_end
            else -> null
        }

// A token's name in the card language: what TalkBack reads for its symbol, and what shows while the app has no symbol
// for it (#148).
@Composable
private fun Token.title(language: CardLanguage): String =
    when (this) {
        Token.Action -> cardString(R.string.icon_action, language)

        Token.Attachment -> cardString(R.string.icon_attachment, language)

        Token.DropOfLife -> cardString(R.string.icon_drop_of_life, language)

        Token.WorldsEnd -> cardString(R.string.worlds_end, language)

        Token.Compass -> cardString(R.string.icon_compass, language)

        Token.Coral -> cardString(R.string.icon_coral, language)

        Token.Fish -> cardString(R.string.icon_fish, language)

        Token.Shell -> cardString(R.string.icon_shell, language)

        // The coined -ling names stay English whatever the card language.
        Token.Mythling -> "Mythling"

        Token.Dinoling -> "Dinoling"

        Token.Techling -> "Techling"

        Token.Deepling -> "Deepling"

        Token.Moonling -> "Moonling"

        Token.Glitterling -> "Glitterling"
    }

// Only the symbols the enabled sets use are converted; the rest have none yet.
private val Token.symbol: Int?
    @DrawableRes get() =
        when (this) {
            Token.Action -> R.drawable.ic_symbol_action
            Token.DropOfLife -> R.drawable.ic_symbol_drop_of_life
            Token.WorldsEnd -> R.drawable.ic_symbol_worlds_end
            else -> null
        }

// 1.3em high and 1.15em for the action, as docs/design.md has it, at the drawable's own width to height: the other
// symbols are square.
private val Token.placeholder: Placeholder
    get() =
        if (this == Token.Action) {
            Placeholder((1.15f * 65 / 31).em, 1.15.em, PlaceholderVerticalAlign.Center)
        } else {
            Placeholder(1.3.em, 1.3.em, PlaceholderVerticalAlign.Center)
        }

// An effect's text with its tokens as symbols in the line, tagged with its language, and the card names it cites as
// links. A note is in smaller type.
@Composable
private fun EffectText(
    parts: List<TextPart>,
    note: Boolean,
    language: CardLanguage,
    card: Card,
    library: CardLibrary,
    onCardClick: (String) -> Unit,
    actionColor: Color,
) {
    val theme = AtlasTheme.colors
    val tokens = parts.filterIsInstance<TextPart.Icon>().map { it.token }.distinct()
    val titles = tokens.associateWith { it.title(language) }
    // Kept across recompositions, so an unchanged text stays equal and isn't laid out again.
    val brushes = remember(parts, theme) { parts.map { (it as? TextPart.Words)?.ink?.let(theme::brush) } }
    val rainbows = mutableListOf<Pair<IntRange, RainbowBrush>>()
    val text =
        buildAnnotatedString {
            withStyle(SpanStyle(localeList = LocaleList(language.code))) {
                parts.forEachIndexed { i, part ->
                    when (part) {
                        is TextPart.Words -> {
                            val start = length
                            append(part.text)
                            val citations = library.citations(card, part.text)
                            brushes[i]?.let { brush ->
                                val span = start until length
                                val style = SpanStyle(brush = brush)
                                if (brush is RainbowBrush) rainbows += span to brush
                                // A cited name keeps its citation style, which an ink drawn over it would hide.
                                val cited = citations.map { (range, _) -> start + range.first..start + range.last }
                                span.without(cited).forEach { addStyle(style, it.first, it.last + 1) }
                            }
                            for ((range, cited) in citations) {
                                val link =
                                    TextLinkStyles(
                                        citedStyle(cited, theme).copy(textDecoration = TextDecoration.Underline),
                                    )
                                addLink(
                                    LinkAnnotation.Clickable(cited.id, link) { onCardClick(cited.id) },
                                    start + range.first,
                                    start + range.last + 1,
                                )
                            }
                        }

                        // The name is the inline content's alternate text, which TalkBack reads in its place.
                        is TextPart.Icon -> {
                            val title = titles.getValue(part.token)
                            when (part.token.symbol) {
                                null -> append(title)
                                else -> appendInlineContent(part.token.code, title)
                            }
                        }
                    }
                }
            }
        }
    val inline =
        tokens
            .mapNotNull { token ->
                token.symbol?.let { symbol ->
                    token.code to
                        InlineTextContent(token.placeholder) { Symbol(symbol, Modifier.fillMaxSize(), actionColor) }
                }
            }.toMap()
    BasicText(
        text,
        style = (if (note) AtlasTypography.bodyMd else AtlasTypography.bodyLg).copy(color = theme.onSurface),
        onTextLayout = { layout ->
            rainbows.forEach { (span, brush) ->
                brush.extent =
                    span.minOf { layout.getBoundingBox(it).left }..span.maxOf { layout.getBoundingBox(it).right }
            }
        },
        inlineContent = inline,
    )
}

// The colors an ink is drawn with: the rainbow's are the four trait inks in the order of the card, red, green, blue,
// purple.
internal fun AtlasColors.inkColors(ink: Ink): List<Color> =
    when (ink) {
        Ink.Red -> listOf(traitRed.ink)
        Ink.Green -> listOf(traitGreen.ink)
        Ink.Blue -> listOf(traitBlue.ink)
        Ink.Purple -> listOf(traitPurple.ink)
        Ink.Colorless -> listOf(traitColorless.ink)
        Ink.Rainbow -> listOf(traitRed.ink, traitGreen.ink, traitBlue.ink, traitPurple.ink)
        Ink.Yellow -> listOf(yellowInk)
    }

private fun AtlasColors.brush(ink: Ink): Brush =
    inkColors(ink).let { if (ink == Ink.Rainbow) RainbowBrush(it) else SolidColor(it.single()) }

// A brush in a span spreads over the whole text, so this one draws its gradient across the extent of its span, from
// its leftmost to its rightmost letter, which the text's layout gives it before the text is drawn. Until then, across
// the whole text.
private class RainbowBrush(
    private val colors: List<Color>,
) : ShaderBrush() {
    // A state, so the shader the span keeps is made again once the layout sets it.
    var extent: ClosedFloatingPointRange<Float>? by mutableStateOf(null)

    override fun createShader(size: Size): Shader {
        val (start, end) = extent?.let { it.start to it.endInclusive } ?: (0f to size.width)
        return LinearGradientShader(Offset(start, 0f), Offset(end, 0f), colors)
    }
}

// The parts of a range outside the given ones, which don't overlap: the standard library has no range subtraction.
private fun IntRange.without(holes: List<IntRange>): List<IntRange> =
    buildList {
        var at = first
        for (hole in holes.sortedBy { it.first }) {
            if (hole.first > at) add(at until hole.first)
            at = maxOf(at, hole.last + 1)
        }
        if (at <= last) add(at..last)
    }

// A cited trait's name in its first color, like a highlighter. Trait colors only mean a card's color, so an age or a
// catastrophe gets the dots of its own top panel instead.
private fun citedStyle(
    cited: NamedCard,
    theme: AtlasColors,
): SpanStyle =
    (cited as? Trait)
        ?.let { theme.trait(it.colors.firstOrNull()) }
        ?.let { SpanStyle(background = it.fill, color = it.onFill) }
        ?: SpanStyle(background = theme.outlineSoft, color = theme.onSurface)

// Anxiety Elixir as in assets/doomlings/imaginary-ends/cards.json. The app doesn't show Trinkets yet, so its blocks are
// the ones shared decides for it, written out.
private val AnxietyElixir =
    Trinket(
        "imaginary-ends/trinkets/anxiety-elixir",
        "imaginary-ends",
        "",
        "Anxiety Elixir",
        emptyList(),
        2,
        "This causes stress. Can't stress that enough.",
    )

private val AnxietyElixirBlocks =
    listOf(
        CardBlock(
            BlockContent.Effect(
                EffectKind.AwesomePower,
                listOf(TextPart.Words("Start of Turn: Discard dominants from hand.")),
            ),
        ),
        CardBlock(
            BlockContent.Effect(
                EffectKind.PocketObjective,
                listOf(TextPart.Words("Reveal: 2 reds in hand.", Ink.Yellow)),
                Ink.Yellow,
            ),
        ),
        CardBlock(BlockContent.Value(2), flavor = "This causes stress. Can't stress that enough."),
    )

@PreviewLightDark
@Composable
internal fun TrinketBlocksPreview() =
    AtlasTheme {
        Column(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s5),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AnxietyElixirBlocks.forEach { BlockBubble(it, CardLanguage.English, AnxietyElixir, color = null) }
        }
    }

// Hyper-Intelligence (assets/doomlings/classic/cards.json) and Sour Grapes (overlush), verbatim, ink marks included: a
// rainbow "Choose a color.", and blue and red words with a rainbow "color".
private val HyperIntelligence =
    Trait(
        "classic/traits/hyper-intelligence",
        "classic",
        "",
        "Hyper-Intelligence",
        "E=mc stop staring at my head.",
        listOf(
            Effect(
                EffectKind.WorldsEnd,
                "[rainbow]Choose a color.[/rainbow] Opponents discard 1 trait of that color from their trait pile at " +
                    "random.",
            ),
        ),
        null,
        4,
        listOf(CardColor.Red),
        true,
        emptyList(),
        listOf(TraitIcon.WorldsEnd),
    )

private val SourGrapes =
    Trait(
        "overlush/traits/sour-grapes",
        "overlush",
        "",
        "Sour Grapes",
        "A source of bad whine.",
        listOf(
            Effect(
                text =
                    "+1 for each [blue]blue[/blue] or each [red]red[/red] trait in your trait pile, whichever is the " +
                        "lower [rainbow]color[/rainbow] count.\n\n(If there's a tie, pick 1!)",
            ),
        ),
        null,
        1,
        listOf(CardColor.Blue, CardColor.Red),
        false,
        emptyList(),
        listOf(TraitIcon.DropOfLife),
    )

@Composable
internal fun InkBlocksSample() =
    Column(
        Modifier.background(AtlasTheme.colors.surface).padding(Space.s5),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        listOf(HyperIntelligence, SourGrapes).forEach { trait ->
            trait.face()?.blocks?.forEach { BlockBubble(it, CardLanguage.English, trait, trait.colors.firstOrNull()) }
        }
    }

@PreviewLightDark
@Composable
internal fun InkBlocksPreview() = AtlasTheme { InkBlocksSample() }
