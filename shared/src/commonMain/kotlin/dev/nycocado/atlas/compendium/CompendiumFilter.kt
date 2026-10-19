package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Trait

// The compendium's segments. Other card types (Gene Pool, Birth of Life) are not listed.
enum class CardKind { Trait, Age, Catastrophe }

data class CompendiumFilter(
    val kind: CardKind = CardKind.Trait,
    val query: String = "",
    // Traits only. null stands for colorless; empty shows every color.
    val colors: Set<CardColor?> = emptySet(),
)

fun List<Card>.filterCompendium(filter: CompendiumFilter): List<Card> {
    val query = normalize(filter.query.trim())
    return mapNotNull { card ->
        val (name, text) =
            when {
                card is Trait && filter.kind == CardKind.Trait -> {
                    card.name to card.effect
                }

                card is Age && filter.kind == CardKind.Age -> {
                    card.name to card.effect
                }

                card is Catastrophe && filter.kind == CardKind.Catastrophe -> {
                    card.name to
                        "${card.effect} ${card.worldsEnd}"
                }

                else -> {
                    return@mapNotNull null
                }
            }
        val colorMatches =
            filter.colors.isEmpty() || card !is Trait ||
                card.colors.ifEmpty { listOf(null) }.any { it in filter.colors }
        val queryMatches = query.isEmpty() || query in normalize(name) || query in normalize(text.orEmpty())
        if (colorMatches && queryMatches) name to card else null
    }.sortedBy { normalize(it.first) }
        .map { it.second }
}

// Lowercase and without accents, so "Catástrofe" matches "catastrofe".
internal fun normalize(text: String): String = stripAccents(text.lowercase())

// Each platform has its own Unicode normalizer.
internal expect fun stripAccents(text: String): String
