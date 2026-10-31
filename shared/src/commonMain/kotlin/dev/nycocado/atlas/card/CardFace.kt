package dev.nycocado.atlas.card

// What a card shows, decided once for the card detail, the compendium's card thumb and its search.
data class CardFace(
    val name: String,
    val facts: List<CardFact>,
    // null for a compass star and for cards without one.
    val faceValue: Int?,
    // The card detail's blocks in order; none when the card has no text, flavor or Gene Pool.
    val blocks: List<CardBlock>,
    // What the compendium search matches: the name and the text of every effect, without tokens or ink marks.
    val searchable: List<String>,
) {
    // Empty for cards without color; null stands for colorless.
    val colors: List<CardColor?> get() = facts.filterIsInstance<CardFact.Color>().map { it.color }
}

// The card detail's pills, in their order.
sealed interface CardFact {
    // null stands for colorless.
    data class Color(
        val color: CardColor?,
    ) : CardFact

    data class Kind(
        val kind: CardKind,
    ) : CardFact

    data object Dominant : CardFact

    data class Icon(
        val icon: TraitIcon,
    ) : CardFact

    data class Subspecies(
        val name: String,
    ) : CardFact
}

// One block of the card detail's text: the Gene Pool, an effect or a Trinket's Value, in that order. The app writes the
// numbers and the kind's name in the card language.
data class CardBlock(
    // null for the block of a card with only a flavor.
    val content: BlockContent?,
    // The last block's flavor.
    val flavor: String? = null,
    // The block the card detail hangs the card language selector on.
    val selector: Boolean = false,
)

sealed interface BlockContent {
    data class GenePool(
        val genePool: Int,
    ) : BlockContent

    // No kind is a plain effect.
    data class Effect(
        val kind: EffectKind?,
        val text: List<TextPart>,
        val yellow: Boolean = false,
    ) : BlockContent

    data class Value(
        val value: Int,
    ) : BlockContent
}

sealed interface TextPart {
    // null for words without an ink.
    data class Words(
        val text: String,
        val ink: Ink? = null,
    ) : TextPart

    data class Icon(
        val token: Token,
    ) : TextPart
}

// The tokens of the card data, by the name in braces.
enum class Token(
    val code: String,
) {
    Action("action"),
    Attachment("attachment"),
    DropOfLife("drop-of-life"),
    WorldsEnd("worlds-end"),
    Compass("compass"),
    Coral("coral"),
    Fish("fish"),
    Shell("shell"),
    Mythling("mythling"),
    Dinoling("dinoling"),
    Techling("techling"),
    Deepling("deepling"),
    Moonling("moonling"),
    Glitterling("glitterling"),
}

// The inks a span of an effect is printed in, by the name in brackets: `[red]red[/red]`.
enum class Ink(
    val code: String,
) {
    Red("red"),
    Blue("blue"),
    Green("green"),
    Purple("purple"),
    Colorless("colorless"),
    Rainbow("rainbow"),
}

// The closing brace is escaped too: Android's ICU regex rejects a lone "}", which the JVM's accepts.
private val braces = Regex("""\{([^}]*)\}""")

// A span runs to the closing mark of its own ink; the card data test rules out nested and unclosed spans.
private val inkedSpan = Regex("""\[([^\]/]*)\]([\s\S]*?)\[/\1\]""")

// An unknown ink or token stays as written; the card data test rules them out.
private fun parts(text: String): List<TextPart> =
    buildList {
        var at = 0
        for (match in inkedSpan.findAll(text)) {
            val ink = Ink.entries.find { it.code == match.groupValues[1] } ?: continue
            addAll(tokenParts(text.substring(at, match.range.first), null))
            addAll(tokenParts(match.groupValues[2], ink))
            at = match.range.last + 1
        }
        addAll(tokenParts(text.substring(at), null))
    }

private fun tokenParts(
    text: String,
    ink: Ink?,
): List<TextPart> =
    buildList {
        var at = 0
        for (match in braces.findAll(text)) {
            val token = Token.entries.find { it.code == match.groupValues[1] } ?: continue
            if (match.range.first > at) add(TextPart.Words(text.substring(at, match.range.first), ink))
            add(TextPart.Icon(token))
            at = match.range.last + 1
        }
        if (at < text.length) add(TextPart.Words(text.substring(at), ink))
    }

private val token = Regex("""\s*\(\{[^}]*\}\)|\{[^}]*\}""")

private val inkMark = Regex("""\[/?[^\]/]*\]""")

// The words of an effect: a token is an icon, not a word to search for, and an ink mark only colors its words. A
// parenthesized token goes with its parentheses and the space before them.
fun plainText(text: String) = text.replace(token, "").replace(inkMark, "")

// null for a card type without a card kind, which the app doesn't show.
fun NamedCard.face(): CardFace? = face(name, effects, flavor, genePool)

// Each translated text takes the kind of the card's effect at its place. zip drops effects past the shorter list: the
// translation data test keeps the counts equal.
internal fun NamedCard.face(text: CardText): CardFace? =
    face(
        text.name,
        effects.zip(text.effects) { effect, translated -> effect.copy(text = translated) },
        text.flavor,
        genePool,
    )

private fun NamedCard.face(
    name: String,
    effects: List<Effect>,
    flavor: String?,
    genePool: Int?,
): CardFace? {
    val kind = kind ?: return null
    val trait = this as? Trait
    val blocks = blocks(effects, flavor, genePool)
    return CardFace(
        name,
        buildList {
            trait?.colorsOrColorless?.forEach { add(CardFact.Color(it)) }
            add(CardFact.Kind(kind))
            if (trait?.dominant == true) add(CardFact.Dominant)
            trait?.icons?.forEach { add(CardFact.Icon(it)) }
            trait?.subspecies?.forEach { add(CardFact.Subspecies(it)) }
        },
        trait?.points,
        blocks,
        listOf(name) + effects.map { plainText(it.text) },
    )
}

// Apart from face() because the app doesn't show Trinkets yet, but their labels and Value are decided here already.
internal fun NamedCard.blocks(
    effects: List<Effect>,
    flavor: String?,
    genePool: Int?,
): List<CardBlock> {
    val contents =
        listOfNotNull(genePool?.let(BlockContent::GenePool)) +
            effects.map { BlockContent.Effect(it.kind, parts(it.text), printsInYellow(it.kind)) } +
            listOfNotNull((this as? Trinket)?.value?.let(BlockContent::Value))
    val shown: List<BlockContent?> = contents.ifEmpty { if (flavor != null) listOf(null) else emptyList() }
    return shown.mapIndexed { index, content ->
        CardBlock(content, flavor.takeIf { index == shown.lastIndex }, selector = index == 0)
    }
}

// A trait prints its World's End in the normal text color.
private fun NamedCard.printsInYellow(kind: EffectKind?) =
    kind == EffectKind.PocketObjective || (this is Catastrophe && kind == EffectKind.WorldsEnd)
