package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

class CardKindTest {
    private val trait =
        Trait(
            "t",
            "classic",
            "",
            "Trait",
            null,
            emptyList(),
            null,
            1,
            emptyList(),
            false,
            emptyList(),
            emptyList(),
        )
    private val age = Age("a", "classic", "", "Age", emptyList())
    private val birthOfLife = BirthOfLife("b", "classic", "", "The Birth of Life", emptyList())
    private val catastrophe = Catastrophe("c", "classic", "", "Catastrophe", emptyList(), null)
    private val genePool = GenePool("g", "classic", "")
    private val meaningOfLife = MeaningOfLife("m", "upgrade", "", "Meaning", emptyList(), null)
    private val treasure = Treasure("tr", "legends-of-enderas", "", "Treasure", emptyList())
    private val trinket = Trinket("ti", "imaginary-ends", "", "Trinket", emptyList(), 1, "Flavor")
    private val merchant = Merchant("me", "imaginary-ends", "", "Merchant", emptyList())

    @Test
    fun theBirthOfLifeIsAnAge() {
        assertEquals(CardKind.Age, birthOfLife.kind)
    }

    @Test
    fun eachNewTypeHasItsKind() {
        assertEquals(CardKind.GenePool, genePool.kind)
        assertEquals(CardKind.MeaningOfLife, meaningOfLife.kind)
        assertEquals(CardKind.Treasure, treasure.kind)
        assertEquals(CardKind.Trinket, trinket.kind)
        assertEquals(CardKind.Merchant, merchant.kind)
    }

    @Test
    fun theKindsComeInTheCompendiumOrder() {
        assertEquals(
            listOf(
                CardKind.Trait,
                CardKind.Age,
                CardKind.Catastrophe,
                CardKind.GenePool,
                CardKind.MeaningOfLife,
                CardKind.Treasure,
                CardKind.Trinket,
                CardKind.Merchant,
            ),
            listOf(merchant, trinket, treasure, meaningOfLife, genePool, catastrophe, age, trait).offeredKinds(),
        )
    }

    @Test
    fun aKindWithNoCardInTheFilteredSetsIsNotOffered() {
        assertEquals(
            listOf(CardKind.Trait, CardKind.Age, CardKind.GenePool),
            listOf(genePool, birthOfLife, trait).offeredKinds(),
        )
    }
}
