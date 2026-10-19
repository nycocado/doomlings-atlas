package dev.nycocado.atlas.card

import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CardDecodingTest {
    @Test
    fun decodesTrait() {
        val trait =
            decodeCards(
                """
                [{"id": "classic/traits/adorable", "type": "trait", "set": "classic", "name": "Adorable",
                  "flavor": "Rub my belly.", "effect": null, "points": 4, "colors": ["purple"],
                  "dominant": false, "subspecies": [], "icons": [],
                  "image": "classic/traits/adorable.webp"}]
                """,
            ).single() as Trait

        assertEquals(4, trait.points)
        assertEquals(listOf(CardColor.Purple), trait.colors)
        assertTrue(trait.effectless)
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
                [{"id": "t", "type": "trait", "set": "classic", "name": "T", "flavor": null, "effect": null,
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
}
