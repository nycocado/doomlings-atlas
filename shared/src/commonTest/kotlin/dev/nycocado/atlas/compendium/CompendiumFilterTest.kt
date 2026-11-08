package dev.nycocado.atlas.compendium

import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.BirthOfLife
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardColor.Blue
import dev.nycocado.atlas.card.CardColor.Green
import dev.nycocado.atlas.card.CardColor.Red
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Effect
import dev.nycocado.atlas.card.EffectKind
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.decodeTranslations
import dev.nycocado.atlas.card.kind
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class CompendiumFilterTest {
    private fun trait(
        name: String,
        vararg colors: CardColor,
        effect: String? = null,
    ) = Trait(
        name,
        "classic",
        "$name.webp",
        name,
        null,
        listOfNotNull(
            effect?.let {
                Effect(text = it)
            },
        ),
        null,
        1,
        colors.toList(),
        false,
        emptyList(),
        emptyList(),
    )

    private val cards =
        listOf(
            trait("Venomous", Red, effect = "Steal 1 card."),
            trait("Camouflage", Blue),
            trait("Brave", Blue, Green),
            trait("Prickly"),
            Age(
                "age-of-dracula",
                "classic",
                "a.webp",
                "Age of Dracula",
                listOf(Effect(text = "If Vampirism is in your pile...")),
            ),
            Catastrophe(
                "ai-takeover",
                "classic",
                "c.webp",
                "AI Takeover",
                listOf(Effect(EffectKind.WorldsEnd, "Colorless traits worth 2.")),
                -1,
            ),
            BirthOfLife(
                "birth",
                "classic",
                "b.webp",
                "The Birth of Life",
                listOf(Effect(text = "Set your Gene Pool to 5.")),
            ),
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
                trait("Gainer", effect = "Score +1 per card."),
                trait("Loser", effect = "Score -1 per card."),
                trait("Drawer", effect = "Draw 1 card."),
            )
        assertEquals(
            listOf("Gainer", "Drawer", "Loser"),
            cards.filterCompendium(CompendiumFilter(query = "+1")).map { it.name },
        )
    }

    @Test
    fun searchFindsWordsOfAnyEffectButNotATokensName() {
        val cards =
            listOf(
                Trait(
                    "Pearl",
                    "classic",
                    "p.webp",
                    "Pearl",
                    null,
                    listOf(
                        Effect(EffectKind.Action, "Ignore actions ({action})."),
                        Effect(EffectKind.WorldsEnd, "Add 2 for each Shell ({shell})."),
                        Effect(null, "Play 1 with this symbol: {worlds-end}."),
                    ),
                    null,
                    1,
                    emptyList(),
                    false,
                    emptyList(),
                    emptyList(),
                ),
            )

        fun found(query: String) = cards.filterCompendium(CompendiumFilter(query = query)).map { it.name }

        assertEquals(listOf("Pearl"), found("ignore"))
        assertEquals(listOf("Pearl"), found("for each shell"))
        assertEquals(listOf("Pearl"), found("symbol"))
        assertEquals(emptyList(), found("worlds-end"))
        assertEquals(emptyList(), found("{"))
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

    private val cardsOfTwoSets =
        listOf(
            trait("Classic Trait").copy(id = "classic-trait"),
            trait("Overlush Trait").copy(id = "overlush-trait", set = "overlush"),
            Age("classic-age", "classic", "a.webp", "Classic Age", emptyList()),
            Age("overlush-age", "overlush", "b.webp", "Overlush Age", emptyList()),
            Catastrophe("classic-cat", "classic", "c.webp", "Classic Catastrophe", emptyList(), -1),
            Catastrophe("overlush-cat", "overlush", "d.webp", "Overlush Catastrophe", emptyList(), -1),
        )

    private fun idsOfTwoSets(filter: CompendiumFilter) = cardsOfTwoSets.filterCompendium(filter).map { it.id }

    @Test
    fun filteringByOneSetKeepsOnlyItsCardsInEveryKind() {
        val overlush = setOf("overlush")
        assertEquals(listOf("overlush-trait"), idsOfTwoSets(CompendiumFilter(sets = overlush)))
        assertEquals(listOf("overlush-age"), idsOfTwoSets(CompendiumFilter(CardKind.Age, sets = overlush)))
        assertEquals(listOf("overlush-cat"), idsOfTwoSets(CompendiumFilter(CardKind.Catastrophe, sets = overlush)))
    }

    @Test
    fun noSetChosenKeepsEveryCard() {
        assertEquals(listOf("classic-trait", "overlush-trait"), idsOfTwoSets(CompendiumFilter()))
        assertEquals(listOf("classic-age", "overlush-age"), idsOfTwoSets(CompendiumFilter(CardKind.Age)))
    }

    @Test
    fun survivesSavingWithSetsChosen() {
        val filter = CompendiumFilter(sets = setOf("overlush", "classic"))
        assertEquals(filter, Json.decodeFromString<CompendiumFilter>(Json.encodeToString(filter)))
    }

    private val translations =
        CardTranslations(
            mapOf(
                CardLanguage.Portuguese to
                    decodeTranslations(
                        """{"Venomous": {"name": "Venenoso", "effects": ["Roube 1 carta."]},
                           "ai-takeover": {"name": "IA", "effects": ["Incolores valem 2."]}}""",
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

    private fun found(
        cards: List<Trait>,
        query: String,
    ) = cards.filterCompendium(CompendiumFilter(query = query)).map { it.name }

    @Test
    fun longNamesAllowTwoErrorsAndShortOnesNone() {
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "camuflagge")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "camufllagee")))
        val short = listOf(trait("Fur"))
        assertEquals(listOf("Fur"), found(short, "fur"))
        assertEquals(emptyList(), found(short, "fyr"))
        assertEquals(emptyList(), found(short, "fru"))
    }

    @Test
    fun theShorterWordSetsTheErrorLimit() {
        val short = listOf(trait("Fur"))
        assertEquals(listOf("Fur"), found(short, "fu"))
        assertEquals(emptyList(), found(short, "furr"))
    }

    @Test
    fun twoErrorsFindANameWordButNotAnEffectWord() {
        assertEquals(listOf("Hibernation"), found(listOf(trait("Hibernation")), "hibrenatoin"))
        assertEquals(emptyList(), found(listOf(trait("Aaa", effect = "Hibernation.")), "hibrenatoin"))
        assertEquals(listOf("Aaa"), found(listOf(trait("Aaa", effect = "Hibernation.")), "hibrenation"))
    }

    @Test
    fun theQueryMayMixTheNameAndTheEffect() {
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "venomous steal")))
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "venomuos steel")))
        assertEquals(listOf("Venomous"), names(CompendiumFilter(query = "card ven")))
    }

    @Test
    fun theWordsOfAQueryMatchInOneLanguage() {
        assertEquals(listOf("Venomous"), found("venenoso roube", CardLanguage.Portuguese))
        assertEquals(listOf("Venomous"), found("venomous steal", CardLanguage.Portuguese))
        assertEquals(emptyList(), found("venenoso steal", CardLanguage.Portuguese))
    }

    @Test
    fun symbolsAndSpacesOnlySeparateWords() {
        val cards = listOf(trait("Spiky", effect = "Don't discard cards."), trait("Cold-blooded"))
        assertEquals(listOf("Spiky"), found(cards, "don't discrad"))
        assertEquals(listOf("Spiky"), found(cards, "don't  discard"))
        assertEquals(listOf("Cold-blooded"), found(cards, "cold blooded"))
        assertEquals(listOf("Cold-blooded"), found(cards, "cold bloded"))
        assertEquals(emptyList(), found(cards, "+"))
    }

    @Test
    fun numbersMatchExactly() {
        val cards = listOf(trait("Aaa", effect = "Draw 12 cards."))
        assertEquals(listOf("Aaa"), found(cards, "12 cards"))
        assertEquals(emptyList(), found(cards, "1"))
        assertEquals(emptyList(), found(cards, "1 cards"))
        assertEquals(emptyList(), found(cards, "2 cards"))
        assertEquals(emptyList(), found(cards, "13 cards"))
    }

    @Test
    fun aNumberStillMatchesAfterALongerOne() {
        val cards = listOf(trait("Aaa", effect = "Draw 12 cards, then 2 cards."))
        assertEquals(listOf("Aaa"), found(cards, "2 cards"))
    }

    @Test
    fun aPieceInsideAWordOnlyCountsForTheWholeQuery() {
        assertEquals(listOf("Camouflage"), names(CompendiumFilter(query = "oufla")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "oufla steal")))
        assertEquals(emptyList(), names(CompendiumFilter(query = "ouflage card")))
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
                trait("Bbb", effect = "Card to draw."),
                trait("Ccc", effect = "Draw card."),
            )
        assertEquals(listOf("Ccc", "Bbb", "Aaa"), found(cards, "draw card"))
    }

    @Test
    fun typosIgnoreCaseAndAccentsInTheTranslation() {
        assertEquals(listOf("Venomous"), found("ROÚBR", CardLanguage.Portuguese))
        assertEquals(listOf("Venomous"), found("VENÉNOOS", CardLanguage.Portuguese))
        assertEquals(emptyList(), found("roubr", CardLanguage.English))
    }
}
