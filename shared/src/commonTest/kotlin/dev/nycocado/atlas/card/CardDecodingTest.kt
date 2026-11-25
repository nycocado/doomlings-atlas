package dev.nycocado.atlas.card

import dev.nycocado.atlas.scoring.ScoringRule
import dev.nycocado.atlas.scoring.Zone
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardDecodingTest {
    @Test
    fun decodesTrait() {
        val trait =
            decodeCards(
                """
                [{"id": "classic/traits/adorable", "type": "trait", "set": "classic", "name": "Adorable",
                  "flavor": "Rub my belly.", "effects": [], "genePool": null, "points": 4, "colors": ["purple"],
                  "dominant": false, "subspecies": [], "icons": [],
                  "image": "classic/traits/adorable.webp"}]
                """,
            ).single() as Trait

        assertEquals(4, trait.points)
        assertEquals(listOf(CardColor.Purple), trait.colors)
        assertTrue(trait.effectless)
    }

    @Test
    fun decodesEffectsWithKindTokenAndGenePool() {
        val trait =
            decodeCards(
                """
                [{"id": "t", "type": "trait", "set": "classic", "name": "T", "flavor": null,
                  "effects": [
                    {"kind": "action", "text": "Ignore actions ({action})."},
                    {"text": "Draw 1. [small](Unless restricted.)[/small]"}],
                  "genePool": -1, "points": 1, "colors": [], "dominant": false, "subspecies": [], "icons": [],
                  "image": "t.webp"}]
                """,
            ).single() as Trait

        assertEquals(
            listOf(
                Effect(EffectKind.Action, "Ignore actions ({action})."),
                Effect(null, "Draw 1. [small](Unless restricted.)[/small]"),
            ),
            trait.effects,
        )
        assertEquals(-1, trait.genePool)
        assertFalse(trait.effectless)
    }

    @Test
    fun aTraitWithOnlyAGenePoolIsNotEffectless() {
        val trait =
            decodeCards(
                """
                [{"id": "t", "type": "trait", "set": "classic", "name": "T", "flavor": null, "effects": [],
                  "genePool": 1, "points": 1, "colors": [], "dominant": false, "subspecies": [], "icons": [],
                  "image": "t.webp"}]
                """,
            ).single() as Trait

        assertFalse(trait.effectless)
    }

    @Test
    fun decodesATrinketWithItsLabelKindsAndValue() {
        val trinket =
            decodeCards(
                """
                [{"id": "k", "type": "trinket", "set": "imaginary-ends", "name": "K",
                  "effects": [{"kind": "awesome-power", "text": "Draw 1."},
                              {"kind": "pocket-objective", "text": "Reveal: Win."}],
                  "value": 3, "flavor": "Shiny", "image": "k.webp"}]
                """,
            ).single() as Trinket

        assertEquals(3, trinket.value)
        assertEquals(listOf(EffectKind.AwesomePower, EffectKind.PocketObjective), trinket.effects.map { it.kind })
    }

    @Test
    fun rejectsUnknownEffectKind() {
        assertFailsWith<SerializationException> {
            decodeCards(
                """
                [{"id": "a", "type": "age", "set": "classic", "name": "Age",
                  "effects": [{"kind": "sparkles", "text": "Draw."}], "image": "a.webp"}]
                """,
            )
        }
    }

    @Test
    fun rejectsALabelKindOnAnotherCardType() {
        assertFailsWith<IllegalArgumentException> {
            decodeCards(
                """
                [{"id": "a", "type": "age", "set": "classic", "name": "Age",
                  "effects": [{"kind": "rush", "text": "Draw."}], "image": "a.webp"}]
                """,
            )
        }
    }

    @Test
    fun rejectsAnEffectWithoutText() {
        assertFailsWith<SerializationException> {
            decodeCards(
                """
                [{"id": "a", "type": "age", "set": "classic", "name": "Age",
                  "effects": [{"kind": "action"}], "image": "a.webp"}]
                """,
            )
        }
    }

    @Test
    fun decodesCardWithoutName() {
        val card =
            decodeCards(
                """[{"id": "classic/gene-pools/blue", "type": "gene-pool", "set": "classic", "image": "b.jpg"}]""",
            )

        assertTrue(card.single() is GenePool)
    }

    @Test
    fun rejectsMissingRequiredField() {
        assertFailsWith<SerializationException> {
            decodeCards("""[{"id": "a", "type": "age", "set": "classic", "name": "Age", "image": "a.webp"}]""")
        }
    }

    @Test
    fun rejectsUnknownIcon() {
        assertFailsWith<SerializationException> {
            decodeCards(
                """
                [{"id": "t", "type": "trait", "set": "classic", "name": "T", "flavor": null, "effects": [], "genePool": null,
                  "points": 1, "colors": [], "dominant": false, "subspecies": [], "icons": ["sparkles"],
                  "image": "t.webp"}]
                """,
            )
        }
    }

    @Test
    fun rejectsUnknownType() {
        assertFailsWith<SerializationException> {
            decodeCards("""[{"id": "x", "type": "playmat", "set": "classic", "image": "x.jpg"}]""")
        }
    }

    private fun traitWithRule(rule: String) =
        decodeCards(
            """
            [{"id": "classic/traits/kidney", "type": "trait", "set": "classic", "name": "Kidney", "flavor": null,
              "effects": [], "genePool": null, "points": null, "colors": ["red"], "dominant": false,
              "subspecies": [], "icons": [], "image": "k.webp", "scoring": $rule}]
            """,
        ).single() as Trait

    @Test
    fun decodesScoringRule() {
        assertEquals(
            ScoringRule.CountByName(Zone.OwnPile),
            traitWithRule("""{"fn": "countByName", "zone": "ownPile"}""").scoring,
        )
        assertEquals(ScoringRule.HandSize, traitWithRule("""{"fn": "handSize"}""").scoring)
    }

    @Test
    fun rejectsUnknownScoringFunction() {
        assertFailsWith<SerializationException> { traitWithRule("""{"fn": "perEachKidney"}""") }
    }

    @Test
    fun rejectsBadScoringParameters() {
        assertFailsWith<SerializationException> { traitWithRule("""{"fn": "countByName", "zone": "hand"}""") }
        assertFailsWith<SerializationException> { traitWithRule("""{"fn": "countByName"}""") }
        assertFailsWith<SerializationException> { traitWithRule("""{"fn": "handSize", "amount": 2}""") }
    }
}
