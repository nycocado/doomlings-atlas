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
    fun aCardWithATranslationShowsItInThatLanguage() {
        val face = translations.face(trait, CardLanguage.Portuguese)

        assertEquals("Venenoso", face?.name)
        assertEquals(
            listOf(CardSection.Effect("Roube 1 carta."), CardSection.Flavor("Ai.")),
            face?.sections,
        )
    }

    @Test
    fun aCatastrophesWorldsEndIsItsOwnSection() {
        assertEquals(
            listOf(
                CardSection.Effect("-1 Reserva"),
                CardSection.Effect("Incolores valem 2.", EffectKind.WorldsEnd),
            ),
            translations.face(catastrophe, CardLanguage.Portuguese)?.sections,
        )
    }

    @Test
    fun aCardWithoutThatLanguageShowsItsPrintedTextAndDoesNotListTheLanguage() {
        assertEquals(plain.face(), translations.face(plain, CardLanguage.Portuguese))
        assertEquals(trait.face(), translations.face(trait, CardLanguage.Spanish))
        assertEquals(listOf(CardLanguage.English), translations.languages(plain))
        assertEquals(listOf(CardLanguage.English, CardLanguage.Portuguese), translations.languages(trait))
    }

    @Test
    fun englishShowsThePrintedTextEvenWhenATranslationExists() {
        val face = translations.face(catastrophe, CardLanguage.English)

        assertEquals("AI Takeover", face?.name)
        assertEquals(listOf(CardSection.Effect("Colorless worth 2.", EffectKind.WorldsEnd)), face?.sections)
        assertEquals(-1, face?.genePool)
    }
}
