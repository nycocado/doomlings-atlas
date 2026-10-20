package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait

// The compendium's segments. Other card types (Gene Pool, Birth of Life) are not listed.
enum class CardKind { Trait, Age, Catastrophe }

data class CompendiumFilter(
    val kind: CardKind = CardKind.Trait,
    val query: String = "",
    // Traits only. null stands for colorless; empty shows every color.
    val colors: Set<CardColor?> = emptySet(),
)

val Card.kind: CardKind?
    get() =
        when (this) {
            is Trait -> CardKind.Trait
            is Age -> CardKind.Age
            is Catastrophe -> CardKind.Catastrophe
            else -> null
        }

fun List<Card>.filterCompendium(filter: CompendiumFilter): List<NamedCard> {
    val query = normalize(filter.query.trim())
    return filterIsInstance<NamedCard>()
        .filter { card ->
            val colorMatches =
                filter.colors.isEmpty() || card !is Trait ||
                    card.colors.ifEmpty { listOf(null) }.any { it in filter.colors }
            val text = if (card is Catastrophe) "${card.effect} ${card.worldsEnd}" else card.effect.orEmpty()
            val queryMatches = query.isEmpty() || query in normalize(card.name) || query in normalize(text)
            card.kind == filter.kind && colorMatches && queryMatches
        }.sortedBy { normalize(it.name) }
}

internal fun normalize(text: String): String = stripAccents(text.lowercase())

internal expect fun stripAccents(text: String): String
