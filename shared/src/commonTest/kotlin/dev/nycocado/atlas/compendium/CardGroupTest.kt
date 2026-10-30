package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardColor.Blue
import dev.nycocado.atlas.card.CardColor.Green
import dev.nycocado.atlas.card.CardColor.Red
import dev.nycocado.atlas.card.CardLibrary
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

// Counts from the Classic card list: 6 Kidneys and 5 Swarms.
class CardGroupTest {
    private fun trait(
        name: String,
        set: String,
        number: Int,
        vararg colors: CardColor,
    ) = Trait(
        "$set/traits/${name.lowercase()}-$number",
        set,
        "$name$number.webp",
        name,
        null,
        emptyList(),
        null,
        1,
        colors.toList(),
        false,
        emptyList(),
        emptyList(),
    )

    private fun variants(
        name: String,
        count: Int,
        set: String = "classic",
        color: CardColor = Red,
    ) = (1..count).map { trait(name, set, it, color) }

    private val cards: List<NamedCard> =
        variants("Kidney", 6) + variants("Swarm", 5, color = Green) + trait("Adorable", "classic", 1, Blue)

    private fun List<CompendiumEntry>.groups() = filterIsInstance<CompendiumEntry.Group>()

    private fun entries(
        cards: List<NamedCard>,
        filter: CompendiumFilter = CompendiumFilter(),
    ) = CardLibrary.of(cards).groupByName(cards.filterCompendium(filter))

    @Test
    fun cardsSharingANameFormOneGroupOfAllTheirVariants() {
        val groups = entries(cards).groups()

        assertEquals(listOf("Kidney", "Swarm"), groups.map { it.name })
        assertEquals(listOf(6, 5), groups.map { it.variants.size })
    }

    @Test
    fun aUniqueNameStaysASingleCard() {
        val adorable = entries(cards).first()

        assertIs<CompendiumEntry.Single>(adorable)
        assertEquals("Adorable", adorable.card.name)
    }

    @Test
    fun aFilterLeavingOneVariantShowsItAsASingleCard() {
        val mixed = variants("Kidney", 5) + trait("Kidney", "classic", 6, Blue)

        val result = entries(mixed, CompendiumFilter(colors = setOf(Blue)))

        assertEquals("classic/traits/kidney-6", assertIs<CompendiumEntry.Single>(result.single()).card.id)
    }

    @Test
    fun aFilterKeepsOnlyTheVariantsThatMatch() {
        val mixed = variants("Kidney", 4) + variants("Kidney", 2, color = Blue).map { it.copy(id = "${it.id}-b") }

        val group = entries(mixed, CompendiumFilter(colors = setOf(Blue))).groups().single()

        assertEquals(2, group.variants.size)
    }

    @Test
    fun aSearchMatchingNoVariantShowsNoGroup() {
        assertEquals(emptyList(), entries(cards, CompendiumFilter(query = "zzz")))
    }

    @Test
    fun cardsWithTheSameNameFromTwoSetsFormOneGroup() {
        val twoSets = variants("Kidney", 6) + variants("Kidney", 3, set = "overlush")

        val group = entries(twoSets).groups().single()

        assertEquals(9, group.variants.size)
        assertEquals(setOf("classic", "overlush"), group.variants.map { it.set }.toSet())
    }

    @Test
    fun variantsKeepTheirOrder() {
        val group = entries(cards).groups().first()

        assertEquals((1..6).map { "classic/traits/kidney-$it" }, group.variants.map { it.id })
    }
}
