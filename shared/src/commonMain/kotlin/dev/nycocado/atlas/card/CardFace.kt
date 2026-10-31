package dev.nycocado.atlas.card

// What a card shows, decided once for the card detail, the compendium's card thumb and its search.
data class CardFace(
    val name: String,
    val facts: List<CardFact>,
    // null for a compass star and for cards without one.
    val faceValue: Int?,
    // The card detail's blocks in order; none when the card has no text, flavor or Gene Pool.
    val blocks: List<CardBlock>,
    // What the compendium search matches: the name and the text of every effect, without tokens.
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

// One block of the card detail's text, with the lines the card hangs on it. The app writes the numbers and the kind's
// label in the card language.
data class CardBlock(
    // null for the empty block of a card with only a Gene Pool or a flavor, and for a plain effect.
    val kind: EffectKind?,
    val display: KindDisplay,
    // Without tokens. Null for the empty block.
    val text: String?,
    // The first block's "+N Gene Pool" line.
    val genePool: Int? = null,
    // The last block's Trinket Value.
    val value: Int? = null,
    // The last block's flavor.
    val flavor: String? = null,
    // The block the card detail hangs the card language selector on.
    val selector: Boolean = false,
)

enum class KindDisplay {
    // Above the text, as World's End.
    Heading,

    // Written into the text as "Label:" before it.
    Label,
    None,
}

private val token = Regex("""\s*\(\{[^}]*\}\)|\{[^}]*\}""")

// A token is an icon, not a word to search for. A parenthesized one goes with its parentheses and the space before them.
fun withoutTokens(text: String) = text.replace(token, "")

// null for a card type without a card kind, which the app doesn't show.
fun NamedCard.face(): CardFace? = face(name, effects, flavor, genePool)

// Each translated text takes the kind of the card's effect at its place.
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
        listOf(name) + effects.map { withoutTokens(it.text) },
    )
}

// Apart from face() because the app doesn't show Trinkets yet, but their labels and Value are decided here already.
internal fun NamedCard.blocks(
    effects: List<Effect>,
    flavor: String?,
    genePool: Int?,
): List<CardBlock> {
    val shown = effects.ifEmpty { if (flavor != null || genePool != null) listOf(null) else emptyList() }
    return shown.mapIndexed { index, effect ->
        val last = index == shown.lastIndex
        CardBlock(
            effect?.kind,
            when {
                effect?.kind == EffectKind.WorldsEnd -> KindDisplay.Heading
                effect?.kind?.labelOf != null -> KindDisplay.Label
                else -> KindDisplay.None
            },
            effect?.let { withoutTokens(it.text) },
            genePool = genePool.takeIf { index == 0 },
            value = (this as? Trinket)?.value.takeIf { last },
            flavor = flavor.takeIf { last },
            selector = index == 0,
        )
    }
}
