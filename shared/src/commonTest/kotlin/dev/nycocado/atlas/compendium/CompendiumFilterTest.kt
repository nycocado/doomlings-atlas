package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.BirthOfLife
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardColor.Blue
import dev.nycocado.atlas.card.CardColor.Green
import dev.nycocado.atlas.card.CardColor.Red
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.decodeTranslations
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
        assertEquals(names(CompendiumFilter()), names(CompendiumFilter(query = "   ")))
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

    private val translations =
        CardTranslations(
            mapOf(
                CardLanguage.Portuguese to
                    decodeTranslations(
                        """{"Venomous": {"name": "Venenoso", "effect": "Roube 1 carta."},
                           "ai-takeover": {"name": "IA", "effect": "-1 Reserva", "worldsEnd": "Incolores valem 2."}}""",
                    ),
            ),
        )

    private fun found(
        query: String,
        language: CardLanguage,
        kind: CardKind = CardKind.Trait,
    ) = cards.filterCompendium(CompendiumFilter(kind = kind, query = query), translations, language).map { it.id }

    @Test
    fun searchFindsAWordOnlyTheTranslationHasAndStillThePrintedText() {
        assertEquals(listOf("Venomous"), found("roube", CardLanguage.Portuguese))
        assertEquals(listOf("Venomous"), found("venenoso", CardLanguage.Portuguese))
        assertEquals(listOf("Venomous"), found("steal", CardLanguage.Portuguese))
        assertEquals(listOf("ai-takeover"), found("incolores", CardLanguage.Portuguese, CardKind.Catastrophe))
    }

    @Test
    fun searchIgnoresTranslationsInOtherLanguages() {
        assertEquals(emptyList(), found("roube", CardLanguage.English))
        assertEquals(emptyList(), found("roube", CardLanguage.Spanish))
    }

    @Test
    fun aTypoInTheNameStillFindsTheCard() {
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "camoufluge")))
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "camouflge")))
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "camouflagge")))
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "venomuos")))
    }

    @Test
    fun longNamesAllowTwoErrorsAndShortOnesNone() {
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "camuflagge")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "camuflagee-x")))
        val short = listOf(trait("Fur"))
        assertEquals(listOf("Fur"), short.filterCompendium(CompendiumFilter(query = "fur")).map { it.name })
        assertEquals(emptyList(), short.filterCompendium(CompendiumFilter(query = "fyr")).map { it.name })
        assertEquals(emptyList(), short.filterCompendium(CompendiumFilter(query = "fru")).map { it.name })
    }

    @Test
    fun aTypoInTheEffectAllowsOneErrorPerWord() {
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "steel")))
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "stael")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "stexl x")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "stxxl")))
    }

    @Test
    fun everyQueryWordMustMatchAWordOfTheSameText() {
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "card steel")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "card camouflage")))
    }

    @Test
    fun exactMatchesRankAbovePrefixAndTypoMatches() {
        val cards =
            listOf(
                trait("Aaa", effect = "Draw a cart."),
                trait("Bbb", effect = "Draw 1 card."),
                trait("Ccc", effect = "Draw cards now."),
                trait("Ddd", effect = "Draw card."),
            )
        assertEquals(
            listOf("Ccc", "Ddd", "Bbb", "Aaa"),
            cards.filterCompendium(CompendiumFilter(query = "draw card")).map { it.name },
        )
        val names = listOf(trait("Arave"), trait("Brave"))
        assertEquals(
            listOf("Brave", "Arave"),
            names.filterCompendium(CompendiumFilter(query = "brave")).map { it.name },
        )
    }

    @Test
    fun typosIgnoreCaseAndAccentsInTheTranslation() {
        assertEquals(listOf("Venomous"), found("ROÚBR", CardLanguage.Portuguese))
        assertEquals(listOf("Venomous"), found("venénoso", CardLanguage.Portuguese))
        assertEquals(emptyList(), found("roubr", CardLanguage.English))
    }
}
