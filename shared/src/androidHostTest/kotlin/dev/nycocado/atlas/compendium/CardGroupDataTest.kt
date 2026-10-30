package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardAssets
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.decodeCards
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

// Grouping on the real card data of assets/doomlings: Classic has 6 Kidneys and 5 Swarms (all red and all green), and
// the other sets repeat both names.
class CardGroupDataTest {
    private val all = decodeCards(CardAssets.cardsJson)
    private val classic = all.filter { it.set == "classic" }

    private fun entries(
        filter: CompendiumFilter = CompendiumFilter(),
        from: List<Card> = classic,
    ) = from.filterCompendium(filter).groupByName()

    private fun List<CompendiumEntry>.group(name: String) =
        filterIsInstance<CompendiumEntry.Group>().single { it.name == name }

    @Test
    fun classicKidneyFormsAGroupOfSixAndSwarmOfFive() {
        val entries = entries()

        assertEquals(6, entries.group("Kidney").variants.size)
        assertEquals(5, entries.group("Swarm").variants.size)
    }

    @Test
    fun aTraitWithAUniqueNameStaysSingle() {
        val adorable = entries().filterIsInstance<CompendiumEntry.Single>().single { it.card.name == "Adorable" }

        assertEquals("classic/traits/adorable", adorable.card.id)
    }

    // Of all the Kidneys, only one of Legends of Enderas is purple.
    @Test
    fun aFilterLeavingOneKidneyShowsItAsASingleCard() {
        val kidneys = entries(CompendiumFilter(colors = setOf(CardColor.Purple)), all).filter { it.name() == "Kidney" }

        assertEquals("legends-of-enderas/traits/kidney-2", assertIs<CompendiumEntry.Single>(kidneys.single()).card.id)
    }

    @Test
    fun aSearchMatchingNoVariantShowsNoGroup() {
        assertEquals(emptyList(), entries(CompendiumFilter(query = "no card says this")))
    }

    @Test
    fun theSameNameFromSeveralSetsFormsOneGroup() {
        val kidney = entries(from = all).group("Kidney")

        assertEquals(15, kidney.variants.size)
        assertTrue(setOf("classic", "overlush", "legends-of-enderas").all { it in kidney.variants.map { v -> v.set } })
    }

    private fun CompendiumEntry.name() =
        when (this) {
            is CompendiumEntry.Single -> card.name
            is CompendiumEntry.Group -> name
        }
}
