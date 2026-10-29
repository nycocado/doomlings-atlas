package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.card.printedText
import kotlinx.serialization.Serializable

// The compendium's segments. Other card types (Gene Pool, Birth of Life) are not listed.
@Serializable
enum class CardKind { Trait, Age, Catastrophe }

@Serializable
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

// The query matches the printed text and the translation in the card language; the list stays sorted by printed name.
fun List<Card>.filterCompendium(
    filter: CompendiumFilter,
    translations: CardTranslations = CardTranslations(),
    language: CardLanguage = CardLanguage.English,
): List<NamedCard> {
    val query = normalize(filter.query.trim())
    return filterIsInstance<NamedCard>()
        .filter { card ->
            val colorMatches =
                filter.colors.isEmpty() || card !is Trait ||
                    card.colors.ifEmpty { listOf(null) }.any { it in filter.colors }
            val queryMatches =
                query.isEmpty() ||
                    listOf(card.printedText(), translations.text(card, language)).any { text ->
                        card
                            .face(text)
                            ?.searchable
                            .orEmpty()
                            .any { query in normalize(it) }
                    }
            card.kind == filter.kind && colorMatches && queryMatches
        }.sortedBy { normalize(it.name) }
}

internal fun normalize(text: String): String = stripAccents(text.lowercase())

internal expect fun stripAccents(text: String): String
