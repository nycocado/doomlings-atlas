package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

class CardTranslationTest {
    private val trait =
        Trait(
            "t",
            "classic",
            "t.webp",
            "Venomous",
            "Ouch.",
            "Steal 1 card.",
            1,
            emptyList(),
            false,
            emptyList(),
            emptyList(),
        )
    private val plain =
        Trait("p", "classic", "p.webp", "Banal", null, null, 1, emptyList(), false, emptyList(), emptyList())
    private val catastrophe = Catastrophe("c", "classic", "c.webp", "AI Takeover", "-1 Gene Pool", "Colorless worth 2.")

    private val translations =
        CardTranslations(
            mapOf(
                CardLanguage.Portuguese to
                    decodeTranslations(
                        """
                        {
                          "t": {"name": "Venenoso", "flavor": "Ai.", "effect": "Roube 1 carta."},
                          "c": {"name": "Tomada pela IA", "effect": "-1 Reserva", "worldsEnd": "Incolores valem 2."}
                        }
                        """,
                    ),
            ),
        )

    @Test
    fun aTranslatedCardGivesItsTranslatedText() {
        assertEquals(
            CardText("Venenoso", "Roube 1 carta.", "Ai.", null),
            translations.text(trait, CardLanguage.Portuguese),
        )
    }

    @Test
    fun aCatastrophesWorldsEndIsTranslated() {
        assertEquals("Incolores valem 2.", translations.text(catastrophe, CardLanguage.Portuguese).worldsEnd)
    }

    @Test
    fun aCardWithoutThatLanguageGivesItsPrintedTextAndDoesNotListTheLanguage() {
        assertEquals(CardText("Banal", null, null, null), translations.text(plain, CardLanguage.Portuguese))
        assertEquals(
            CardText("Venomous", "Steal 1 card.", "Ouch.", null),
            translations.text(trait, CardLanguage.Spanish),
        )
        assertEquals(listOf(CardLanguage.English), translations.languages(plain))
        assertEquals(listOf(CardLanguage.English, CardLanguage.Portuguese), translations.languages(trait))
    }

    @Test
    fun englishGivesThePrintedTextEvenWhenATranslationExists() {
        assertEquals("AI Takeover", translations.text(catastrophe, CardLanguage.English).name)
        assertEquals("Colorless worth 2.", translations.text(catastrophe, CardLanguage.English).worldsEnd)
    }
}
