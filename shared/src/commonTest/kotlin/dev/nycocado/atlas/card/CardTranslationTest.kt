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
            listOf(Effect(text = "Discard."), Effect(EffectKind.WorldsEnd, "Colorless worth 2.")),
            -1,
        )

    private val translations =
        CardTranslations(
            mapOf(
                CardLanguage.Portuguese to
                    decodeTranslations(
                        """
                        {
                          "t": {"name": "Venenoso", "flavor": "Ai.", "effects": ["Roube 1 carta."]},
                          "c": {"name": "Tomada pela IA", "effects": ["Descarte.", "Incolores valem 2."]}
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
            listOf(CardBlock(null, KindDisplay.None, "Roube 1 carta.", flavor = "Ai.", selector = true)),
            face?.blocks,
        )
    }

    @Test
    fun aTranslatedEffectKeepsTheKindOfItsCardsEffectAndTheCardsGenePool() {
        assertEquals(
            listOf(
                CardBlock(null, KindDisplay.None, "Descarte.", genePool = -1, selector = true),
                CardBlock(EffectKind.WorldsEnd, KindDisplay.Heading, "Incolores valem 2."),
            ),
            translations.face(catastrophe, CardLanguage.Portuguese)?.blocks,
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
        assertEquals(
            listOf(
                CardBlock(null, KindDisplay.None, "Discard.", genePool = -1, selector = true),
                CardBlock(EffectKind.WorldsEnd, KindDisplay.Heading, "Colorless worth 2."),
            ),
            face?.blocks,
        )
    }
}
