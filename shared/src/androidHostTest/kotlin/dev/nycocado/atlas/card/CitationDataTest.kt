package dev.nycocado.atlas.card

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Every citation found in the real assets/doomlings/cards.json, written out so a pull request shows what it opens.
class CitationDataTest {
    private val library = CardLibrary.decode(File(System.getProperty("cards.dir")!!).resolve("cards.json").readText())

    @Test
    fun citationsOfTheCardTextsAreTheReviewedOnes() {
        val found =
            library.cards!!.filterIsInstance<NamedCard>().flatMap { card ->
                val fields = listOf("effect" to card.effect, "worldsEnd" to (card as? Catastrophe)?.worldsEnd)
                fields.flatMap { (field, text) ->
                    library.citations(card, text.orEmpty()).map {
                        "${card.id}.$field: ${text!!.substring(it.range)} -> ${it.cardId}"
                    }
                }
            }
        assertEquals(expected, found)
    }

    private val expected =
        listOf(
            "classic/ages/age-of-dracula.effect: Vampirism -> classic/traits/vampirism",
            "classic/ages/birth-of-a-hero.effect: Heroic -> classic/traits/heroic",
            "classic/traits/denial.effect: The Four Horsemen -> classic/catastrophes/the-four-horsemen",
            "legends-of-enderas/ages/thousand-year-slumber.effect: Slumbering -> legends-of-enderas/traits/slumbering",
            "mokoko-village/traits/swarm-of-kidneys.effect: Kidney -> classic/traits/kidney",
            "overlush/ages/the-legend-of-mokoko-village.effect: Heroic -> classic/traits/heroic",
        )
}
