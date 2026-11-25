package dev.nycocado.atlas.scoring

import dev.nycocado.atlas.card.CardAssets
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.decodeCards
import kotlin.test.Test
import kotlin.test.assertEquals

// ScoringTest builds each compass star's rule by hand; this pins the real Classic data to the same rules.
class CompassStarDataTest {
    @Test
    fun classicCompassStarsCallTheirRules() {
        val expected =
            mapOf(
                "Kidney" to ScoringRule.CountByName(Zone.OwnPile),
                "Swarm" to ScoringRule.CountByName(Zone.AllPiles),
                "Altruistic" to ScoringRule.GenePoolSize,
                "Random Fertilization" to ScoringRule.GenePoolSize,
                "Fortunate" to ScoringRule.HandSize,
                "Boredom" to ScoringRule.HandWithEffects,
                "Gratitude" to ScoringRule.DifferentColors,
            )
        val classic =
            decodeCards(CardAssets.cardsJson)
                .filterIsInstance<Trait>()
                .filter { it.set == "classic" && it.points == null }

        assertEquals(expected.keys, classic.map { it.name }.toSet())
        classic.forEach { assertEquals(expected[it.name], it.scoring, it.id) }
    }
}
