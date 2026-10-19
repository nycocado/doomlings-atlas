package dev.nycocado.atlas.card

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

// Checks the real assets/doomlings/cards.json, every set. Decoding is strict, so it also checks required fields.
class CardDataIntegrityTest {
    private val dir = File(System.getProperty("cards.dir"))
    private val cards = decodeCards(dir.resolve("cards.json").readText())

    @Test
    fun idsAreUnique() {
        assertEquals(
            emptyList(),
            cards
                .groupBy { it.id }
                .filterValues { it.size > 1 }
                .keys
                .toList(),
        )
    }

    @Test
    fun everyCardHasItsImage() {
        assertEquals(emptyList(), cards.map { it.image }.filterNot { dir.resolve(it).isFile })
    }
}
