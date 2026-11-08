package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.NamedCard

// What the compendium's grid draws, in order. `index` is the cell's place among the entries, which the app turns into
// the card's tilt; a variant restarts at its group, so it carries its 1-based `position` instead.
sealed interface GridCell {
    data class Single(
        val card: Card,
        val index: Int,
    ) : GridCell

    data class Stack(
        val name: String,
        val variants: List<NamedCard>,
        val index: Int,
    ) : GridCell

    data class Collapse(
        val name: String,
        val count: Int,
    ) : GridCell

    data class Variant(
        val card: NamedCard,
        val position: Int,
        val count: Int,
    ) : GridCell
}

// A name in `open` that no group has (a single card, or a group the filter hides) is ignored but stays in `open`, so
// the group opens again when the filter shows it.
fun List<CompendiumEntry>.toGridCells(open: Set<String>): List<GridCell> =
    flatMapIndexed { index, entry ->
        when (entry) {
            is CompendiumEntry.Single -> {
                listOf(GridCell.Single(entry.card, index))
            }

            is CompendiumEntry.Group -> {
                if (entry.name in open) {
                    listOf(GridCell.Collapse(entry.name, entry.variants.size)) +
                        entry.variants.mapIndexed { i, variant ->
                            GridCell.Variant(variant, i + 1, entry.variants.size)
                        }
                } else {
                    listOf(GridCell.Stack(entry.name, entry.variants, index))
                }
            }
        }
    }
