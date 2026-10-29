package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.NamedCard

// What the compendium lists: a card, or the cards that share its name.
sealed interface CompendiumEntry {
    data class Single(
        val card: NamedCard,
    ) : CompendiumEntry

    // Always two or more variants.
    data class Group(
        val name: String,
        val variants: List<NamedCard>,
    ) : CompendiumEntry
}

// A step after the filter, so a group holds only the variants the filter left. Same-named cards arrive together
// because the filter sorts by name.
fun List<NamedCard>.groupByName(): List<CompendiumEntry> =
    groupBy { it.name }.map { (name, variants) ->
        if (variants.size == 1) CompendiumEntry.Single(variants.single()) else CompendiumEntry.Group(name, variants)
    }
