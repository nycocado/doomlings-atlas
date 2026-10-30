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
            listOf(Effect(text = "Steal 1 card.")),
            null,
            1,
            emptyList(),
            false,
            emptyList(),
            emptyList(),
        )
    private val plain =
        Trait(
            "p",
            "classic",
            "p.webp",
            "Banal",
            null,
            emptyList(),
            null,
            1,
            emptyList(),
            false,
            emptyList(),
            emptyList(),
        )
    private val catastrophe =
        Catastrophe(
            "c",
            "classic",
            "c.webp",
            "AI Takeover",
            listOf(Effect(EffectKind.WorldsEnd, "Colorless worth 2.")),
            -1,
        )

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
        assertEquals(plain.printedText(), translations.text(plain, CardLanguage.Portuguese))
        assertEquals(trait.printedText(), translations.text(trait, CardLanguage.Spanish))
        assertEquals(listOf(CardLanguage.English), translations.languages(plain))
        assertEquals(listOf(CardLanguage.English, CardLanguage.Portuguese), translations.languages(trait))
    }

    @Test
    fun englishGivesThePrintedTextEvenWhenATranslationExists() {
        assertEquals("AI Takeover", translations.text(catastrophe, CardLanguage.English).name)
        assertEquals(catastrophe.effects, translations.text(catastrophe, CardLanguage.English).effects)
        assertEquals(-1, translations.text(catastrophe, CardLanguage.English).genePool)
    }
}
