package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.BirthOfLife
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardColor.Blue
import dev.nycocado.atlas.card.CardColor.Green
import dev.nycocado.atlas.card.CardColor.Red
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Trait
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CompendiumFilterTest {
    private fun trait(
        name: String,
        vararg colors: CardColor,
        effect: String? = null,
    ) = Trait(name, "classic", "$name.webp", name, null, effect, 1, colors.toList(), false, emptyList(), emptyList())

    private val cards =
        listOf(
            trait("Venomous", Red, effect = "Steal 1 card."),
            trait("Camouflage", Blue),
            trait("Brave", Blue, Green),
            trait("Prickly"),
            Age("age-of-dracula", "classic", "a.webp", "Age of Dracula", "If Vampirism is in your pile..."),
            Catastrophe("ai-takeover", "classic", "c.webp", "AI Takeover", "-1 Gene Pool", "Colorless traits worth 2."),
            BirthOfLife("birth", "classic", "b.webp", "The Birth of Life", "Set your Gene Pool to 5."),
        )

    private fun names(filter: CompendiumFilter) = cards.filterCompendium(filter).map { (it as? Trait)?.name ?: it.id }

    @Test
    fun showsOnlyTheSelectedKindSortedByName() {
        assertEquals(listOf("Brave", "Camouflage", "Prickly", "Venomous"), names(CompendiumFilter()))
        assertEquals(listOf("age-of-dracula"), names(CompendiumFilter(kind = CardKind.Age)))
        assertEquals(listOf("ai-takeover"), names(CompendiumFilter(kind = CardKind.Catastrophe)))
    }

    @Test
    fun searchesNameAndEffectIgnoringCaseAndAccents() {
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "CAMOUFLÁGE")))
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "steal")))
        assertEquals(listOf("age-of-dracula"), names(CompendiumFilter(kind = CardKind.Age, query = "vampirism")))
    }

    @Test
    fun selectedColorsMatchAnyColorOfTheTrait() {
        assertEquals(listOf("Brave", "Camouflage"), names(CompendiumFilter(colors = setOf(Blue))))
        assertEquals(listOf("Brave", "Venomous"), names(CompendiumFilter(colors = setOf(Green, Red))))
    }

    @Test
    fun searchesTheWorldsEndOfCatastrophes() {
        assertEquals(listOf("ai-takeover"), names(CompendiumFilter(kind = CardKind.Catastrophe, query = "worth 2")))
    }

    @Test
    fun cardsOutsideTheCompendiumHaveNoKind() {
        assertEquals(
            listOf(
                CardKind.Trait,
                CardKind.Trait,
                CardKind.Trait,
                CardKind.Trait,
                CardKind.Age,
                CardKind.Catastrophe,
                null,
            ),
            cards.map { it.kind },
        )
    }

    @Test
    fun colorlessIsAColorOfItsOwn() {
        assertEquals(listOf("Prickly"), names(CompendiumFilter(colors = setOf(null))))
    }

    @Test
    fun normalizesAccentsInAnyLanguage() {
        assertEquals("acao catastrofe nino creme brulee ol", normalize("Ação Catástrofe Niño Crème Brûlée Öl"))
    }

    @Test
    fun searchOfOnlySpacesListsEverything() {
        assertEquals(listOf("Brave", "Camouflage", "Prickly", "Venomous"), names(CompendiumFilter(query = "   ")))
    }

    @Test
    fun searchesSymbolsInTheEffect() {
        val cards =
            listOf(
                trait("Gainer", effect = "+1 Gene Pool."),
                trait("Loser", effect = "-1 Gene Pool."),
                trait("Drawer", effect = "Draw 1 card."),
            )
        assertEquals(listOf("Gainer"), cards.filterCompendium(CompendiumFilter(query = "+1")).map { it.name })
    }

    @Test
    fun cardsSharingANameAreAllListedInTheirDataOrder() {
        val first = trait("Kidney", Red).copy(id = "kidney-red")
        val second = trait("Kidney", Blue).copy(id = "kidney-blue")
        val other = trait("Brave", Green)
        assertEquals(
            listOf("Brave", "kidney-red", "kidney-blue"),
            listOf(first, second, other).filterCompendium(CompendiumFilter()).map { it.id },
        )
        assertEquals(
            listOf("Brave", "kidney-blue", "kidney-red"),
            listOf(second, first, other).filterCompendium(CompendiumFilter()).map { it.id },
        )
    }

    @Test
    fun colorsSurviveSwitchingToAgesAndBack() {
        val backToTraits = CompendiumFilter(colors = setOf(Blue)).copy(kind = CardKind.Age).copy(kind = CardKind.Trait)
        assertEquals(listOf("Brave", "Camouflage"), names(backToTraits))
    }

    @Test
    fun colorsNeverHideAgesOrCatastrophes() {
        assertEquals(listOf("age-of-dracula"), names(CompendiumFilter(CardKind.Age, colors = setOf(Red))))
        assertEquals(listOf("ai-takeover"), names(CompendiumFilter(CardKind.Catastrophe, colors = setOf(Green, null))))
    }

    @Test
    fun survivesSavingWithColorlessSelected() {
        val filter = CompendiumFilter(CardKind.Trait, "drac", setOf(null))
        assertEquals(filter, Json.decodeFromString<CompendiumFilter>(Json.encodeToString(filter)))
    }
}
