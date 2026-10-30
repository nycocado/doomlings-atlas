package dev.nycocado.atlas.card

import dev.nycocado.atlas.compendium.CardKind
import dev.nycocado.atlas.compendium.kind

// What a card shows, decided once for the card detail, the compendium's card thumb and its search.
data class CardFace(
    val facts: List<CardFact>,
    // null for a compass star and for cards without one.
    val faceValue: Int?,
    // Only the sections the card has, in the order it shows them: the effects, then the flavor.
    val sections: List<CardSection>,
    // The "+N Gene Pool" line, for the app to write in the card language. A translation has it in its effect text.
    val genePool: Int?,
    // A Trinket's Value, for the app to write in the card language.
    val value: Int?,
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

    data class Type(
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

sealed interface CardSection {
    val text: String

    data class Effect(
        override val text: String,
        val kind: EffectKind? = null,
    ) : CardSection

    data class Flavor(
        override val text: String,
    ) : CardSection
}

private val token = Regex("""\s*\(\{[^}]*\}\)|\{[^}]*\}""")

// A token is an icon, not a word to search for. A parenthesized one goes with its parentheses and the space before them.
fun withoutTokens(text: String) = text.replace(token, "")

// The text is the printed text or a translation of this card. null for a card type without a card kind, which the app
// doesn't show.
fun NamedCard.face(text: CardText = printedText()): CardFace? {
    val kind = kind ?: return null
    val trait = this as? Trait
    val trinket = this as? Trinket
    // A translation's flat text is one plain effect, and a Catastrophe's World's End another.
    val effects =
        text.effects
            ?: listOfNotNull(
                text.effect?.let { Effect(text = it) },
                text.worldsEnd?.let { Effect(EffectKind.WorldsEnd, it) },
            )
    val sections =
        effects.map { CardSection.Effect(it.text, it.kind) } + listOfNotNull(text.flavor?.let(CardSection::Flavor))
    return CardFace(
        buildList {
            trait?.colors?.ifEmpty { listOf(null) }?.forEach { add(CardFact.Color(it)) }
            add(CardFact.Type(kind))
            if (trait?.dominant == true) add(CardFact.Dominant)
            trait?.icons?.forEach { add(CardFact.Icon(it)) }
            trait?.subspecies?.forEach { add(CardFact.Subspecies(it)) }
        },
        trait?.points,
        sections,
        text.genePool,
        trinket?.value,
        listOf(text.name) + sections.filterIsInstance<CardSection.Effect>().map { withoutTokens(it.text) },
    )
}
