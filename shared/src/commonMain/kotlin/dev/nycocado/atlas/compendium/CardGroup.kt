package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.NamedCard

// What the compendium lists: a card, or the cards that share its name.
sealed interface CompendiumEntry {
    data class Single(
        val card: Card,
    ) : CompendiumEntry

    // Always two or more variants.
    data class Group(
        val name: String,
        val variants: List<NamedCard>,
    ) : CompendiumEntry
}

// A step after the filter, so a group holds only the variants the filter left; groups and variants keep the filter's
// order. `filtered` must come from this library's cards: a name missing from its index would make a group of no
// variants. A card without a name (a Gene Pool) groups with nothing.
fun CardLibrary.groupByName(filtered: List<Card>): List<CompendiumEntry> {
    val position = filtered.withIndex().associate { (index, card) -> card.id to index }
    return filtered.distinctBy { (it as? NamedCard)?.name ?: it.id }.map { card ->
        if (card !is NamedCard) return@map CompendiumEntry.Single(card)
        val variants = variants(card.name).filter { it.id in position }.sortedBy { position.getValue(it.id) }
        if (variants.size == 1) {
            CompendiumEntry.Single(variants.single())
        } else {
            CompendiumEntry.Group(card.name, variants)
        }
    }
}
