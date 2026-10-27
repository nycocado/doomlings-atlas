package dev.nycocado.atlas.card

import dev.nycocado.atlas.compendium.CardKind
import dev.nycocado.atlas.compendium.kind

// What a card shows, decided once for the card detail, the compendium's card thumb and its search.
data class CardFace(
    val facts: List<CardFact>,
    // null for a compass star and for cards without one.
    val faceValue: Int?,
    // Only the sections the card has, in the order it shows them.
    val sections: List<CardSection>,
    // What the compendium search matches: the name and every section but the flavor.
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
    ) : CardSection

    data class Flavor(
        override val text: String,
    ) : CardSection

    data class WorldsEnd(
        override val text: String,
    ) : CardSection
}

// The text is the printed text or a translation of this card. null for a card type without a card kind, which the app
// doesn't show.
fun NamedCard.face(text: CardText = printedText()): CardFace? {
    val kind = kind ?: return null
    val trait = this as? Trait
    val sections =
        listOfNotNull(
            text.effect?.let(CardSection::Effect),
            text.flavor?.let(CardSection::Flavor),
            text.worldsEnd?.let(CardSection::WorldsEnd),
        )
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
        listOf(text.name) + sections.filterNot { it is CardSection.Flavor }.map { it.text },
    )
}
