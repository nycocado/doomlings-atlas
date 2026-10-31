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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import dev.nycocado.atlas.card.EffectKind
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.TextPart
import dev.nycocado.atlas.card.Token
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.Trinket
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

// One block of the card detail's text, in a bubble.
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
                                KindHeading(
                                    it,
                                    content.kind?.symbol,
                                    shownLanguage,
                                    actionColor,
                                )
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
) {
    val theme = AtlasTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        symbol?.let { Symbol(it, Modifier.height(16.dp), actionColor) }
        BasicText(
            tagged(cardString(title, language).uppercase(), language),
            style = AtlasTypography.labelSm.copy(color = theme.onSurfaceVariant),
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
    val text =
        buildAnnotatedString {
            withStyle(SpanStyle(localeList = LocaleList(language.code))) {
                parts.forEach { part ->
                    when (part) {
                        is TextPart.Words -> {
                            val start = length
                            append(part.text)
                            for ((range, cited) in library.citations(card, part.text)) {
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
                            if (part.token.symbol !=
                                null
                            ) {
                                appendInlineContent(part.token.code, title)
                            } else {
                                append(title)
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
        inlineContent = inline,
    )
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
        CardBlock(BlockContent.Effect(EffectKind.PocketObjective, listOf(TextPart.Words("Reveal: 2 reds in hand.")))),
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
