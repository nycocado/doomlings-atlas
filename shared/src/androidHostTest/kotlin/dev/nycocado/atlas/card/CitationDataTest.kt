package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

// Every citation found in the real card data of every set, written out so a pull request shows what it opens.
class CitationDataTest {
    private val library = CardLibrary.decode(CardAssets.cardsJson)

    @Test
    fun citationsOfTheCardTextsAreTheReviewedOnes() {
        val found =
            library.cards!!.filterIsInstance<NamedCard>().flatMap { card ->
                card.effects.flatMapIndexed { index, effect ->
                    library.citations(card, effect.text).map {
                        "${card.id}.effects[$index]: ${effect.text.substring(it.range)} -> ${it.cardId}"
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
            "legends-of-enderas/ages/thousand-year-slumber.effects[0]: Slumbering -> legends-of-enderas/traits/slumbering",
            "mokoko-village/traits/swarm-of-kidneys.effects[0]: Kidney -> classic/traits/kidney",
            "overlush/ages/the-legend-of-mokoko-village.effects[0]: Heroic -> classic/traits/heroic",
        )
}
