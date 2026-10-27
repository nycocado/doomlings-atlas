package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

class CardLibraryTest {
    private val library =
        CardLibrary.decode(
            """
            [{"id": "classic/traits/adorable", "type": "trait", "set": "classic", "name": "Adorable",
              "flavor": "Rub my belly.", "effect": null, "points": 4, "colors": ["purple"],
              "dominant": false, "subspecies": [], "icons": [],
              "image": "classic/traits/adorable.webp"},
             {"id": "classic/gene-pools/blue", "type": "gene-pool", "set": "classic", "image": "b.jpg"}]
            """,
        )

    @Test
    fun findsKnownCard() {
        val found = library.lookup("classic/traits/adorable") as CardLookup.Found

        assertEquals("Adorable", (found.card as Trait).name)
    }

    @Test
    fun missesUnknownCard() {
        assertEquals(CardLookup.Missing, library.lookup("classic/traits/retired"))
    }

    @Test
    fun stillLoadingBeforeCardsLoad() {
        assertEquals(CardLookup.Loading, CardLibrary.loading.lookup("classic/traits/adorable"))
    }
}
