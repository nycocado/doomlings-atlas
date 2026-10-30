package dev.nycocado.atlas.card

import dev.nycocado.atlas.compendium.CompendiumEntry
import dev.nycocado.atlas.compendium.groupByName
import kotlin.test.Test
import kotlin.test.assertEquals

class CardLibraryTest {
    private val library =
        CardLibrary.decode(
            """
            [{"id": "classic/traits/adorable", "type": "trait", "set": "classic", "name": "Adorable",
              "flavor": "Rub my belly.", "effects": [], "genePool": null, "points": 4, "colors": ["purple"],
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

    private val names =
        CardLibrary.decode(
            """
            [{"id": "classic/traits/kidney", "type": "trait", "set": "classic", "name": "Kidney",
              "flavor": "", "effects": [], "genePool": null, "points": 1,
              "colors": ["red"], "dominant": false, "subspecies": [], "icons": [], "image": "k1.webp"},
             {"id": "classic/traits/swarm-of-kidneys", "type": "trait", "set": "classic", "name": "Swarm of Kidneys",
              "flavor": "", "effects": [], "genePool": null, "points": 1,
              "colors": ["green"], "dominant": false, "subspecies": [], "icons": [], "image": "s.webp"},
             {"id": "overlush/traits/kidney", "type": "trait", "set": "overlush", "name": "Kidney",
              "flavor": "", "effects": [], "genePool": null, "points": 1,
              "colors": ["red"], "dominant": false, "subspecies": [], "icons": [], "image": "k2.webp"}]
            """,
        )

    private fun named(id: String) = names.cards!!.filterIsInstance<NamedCard>().first { it.id == id }

    @Test
    fun aNameWithVariantsResolvesToAllOfThemInDataOrder() {
        assertEquals(listOf("classic/traits/kidney", "overlush/traits/kidney"), names.variants("Kidney").map { it.id })
        assertEquals(emptyList(), names.variants("Nothing"))
    }

    @Test
    fun aCitationOfANameWithVariantsOpensTheFirst() {
        val citations = names.citations(named("classic/traits/swarm-of-kidneys"), "Kidney")

        assertEquals(listOf("classic/traits/kidney"), citations.map { it.card.id })
    }

    @Test
    fun aCardCitingItsOwnNameGetsNoCitation() {
        val text = "Swarm of Kidneys and a Kidney."

        assertEquals(
            listOf("Swarm of Kidneys"),
            names.citations(named("classic/traits/kidney"), text).map {
                text.substring(it.range)
            },
        )
        assertEquals(emptyList(), names.citations(named("overlush/traits/kidney"), "A Kidney."))
        assertEquals(emptyList(), names.citations(named("classic/traits/swarm-of-kidneys"), "Swarm of Kidneys"))
    }

    @Test
    fun groupingAfterAFilterKeepsOnlyTheFilteredVariants() {
        val entries =
            names.groupByName(
                listOf(named("overlush/traits/kidney"), named("classic/traits/swarm-of-kidneys")),
            )

        assertEquals(
            listOf("overlush/traits/kidney", "classic/traits/swarm-of-kidneys"),
            entries.map {
                (it as CompendiumEntry.Single).card.id
            },
        )
    }

    @Test
    fun groupingKeepsTheFilterOrderOfGroupsAndVariants() {
        val entries = names.groupByName(names.cards!!.filterIsInstance<NamedCard>().reversed())

        assertEquals(
            listOf("overlush/traits/kidney", "classic/traits/kidney"),
            (entries.first() as CompendiumEntry.Group).variants.map { it.id },
        )
    }
}
