package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

// Every citation found in the real card data of every set, written out so a pull request shows what it opens. Cited
// as the card detail cites, in each run of words of the card face: a name across an ink mark's edge is not found.
class CitationDataTest {
    private val library = CardLibrary.decode(CardAssets.packagedCardsJson)

    @Test
    fun citationsOfTheCardTextsAreTheReviewedOnes() {
        val found =
            library.cards!!.filterIsInstance<NamedCard>().flatMap { card ->
                val effects =
                    card
                        .face()
                        ?.blocks
                        .orEmpty()
                        .mapNotNull { it.content as? BlockContent.Effect }
                effects.flatMapIndexed { index, effect ->
                    effect.text.filterIsInstance<TextPart.Words>().flatMap { words ->
                        library.citations(card, words.text).map {
                            "${card.id}.effects[$index]: ${words.text.substring(it.range)} -> ${it.card.id}"
                        }
                    }
                }
            }
        assertEquals(expected, found)
    }

    private val expected =
        listOf(
            "classic/ages/age-of-dracula.effects[1]: Vampirism -> classic/traits/vampirism",
            "classic/ages/birth-of-a-hero.effects[0]: Heroic -> classic/traits/heroic",
            "classic/traits/denial.effects[0]: The Four Horsemen -> classic/catastrophes/the-four-horsemen",
            "overlush/ages/the-legend-of-mokoko-village.effects[0]: Heroic -> classic/traits/heroic",
            "legends-of-enderas/ages/thousand-year-slumber.effects[0]: Slumbering -> legends-of-enderas/traits/slumbering",
            "mokoko-village/traits/swarm-of-kidneys.effects[0]: Kidney -> classic/traits/kidney",
        )
}
