package dev.nycocado.atlas.card

import dev.nycocado.atlas.compendium.CardKind
import kotlin.test.Test
import kotlin.test.assertEquals

// Cards verbatim from the card data in assets/doomlings/<set>/cards.json.
class CardFaceTest {
    private val confusion =
        Trait(
            "classic/traits/confusion",
            "classic",
            "classic/traits/confusion.webp",
            "Confusion",
            "And you might ask yourself, \"Why does this trait exist?",
            emptyList(),
            null,
            -2,
            emptyList(),
            false,
            emptyList(),
            emptyList(),
        )

    private val blackAndBlue =
        Trait(
            "castle-glass/traits/black-and-blue",
            "castle-glass",
            "castle-glass/traits/black-and-blue.webp",
            "Black and Blue",
            "And a little green, too",
            emptyList(),
            null,
            1,
            listOf(CardColor.Blue, CardColor.Green),
            false,
            emptyList(),
            listOf(TraitIcon.Curious, TraitIcon.Fish),
        )

    @Test
    fun aTwoColorTraitHasBothColors() {
        assertEquals(
            listOf(
                CardFact.Color(CardColor.Blue),
                CardFact.Color(CardColor.Green),
                CardFact.Type(CardKind.Trait),
                CardFact.Icon(TraitIcon.Curious),
                CardFact.Icon(TraitIcon.Fish),
            ),
            blackAndBlue.face()?.facts,
        )
    }

    private val awesome =
        Trait(
            "imaginary-ends/traits/awesome",
            "imaginary-ends",
            "imaginary-ends/traits/awesome.webp",
            "Awesome!",
            "Fear me! Tehee!",
            listOf(
                Effect(EffectKind.WorldsEnd, "Stabilize to 2. Add 2 face values in your hand to your score. (Max +17)"),
            ),
            null,
            1,
            listOf(CardColor.Blue, CardColor.Green, CardColor.Purple, CardColor.Red),
            true,
            listOf("Glitterling"),
            listOf(TraitIcon.DropOfLife, TraitIcon.WorldsEnd),
        )

    @Test
    fun aDominantTraitHasTheDominantFactAfterItsType() {
        assertEquals(
            listOf(
                CardFact.Color(CardColor.Blue),
                CardFact.Color(CardColor.Green),
                CardFact.Color(CardColor.Purple),
                CardFact.Color(CardColor.Red),
                CardFact.Type(CardKind.Trait),
                CardFact.Dominant,
                CardFact.Icon(TraitIcon.DropOfLife),
                CardFact.Icon(TraitIcon.WorldsEnd),
                CardFact.Subspecies("Glitterling"),
            ),
            awesome.face()?.facts,
        )
    }

    private val altruistic =
        Trait(
            "classic/traits/altruistic",
            "classic",
            "classic/traits/altruistic.webp",
            "Altruistic",
            "Can I borrow a few bucks? I donated all my money to charity.",
            listOf(Effect(text = "Value is equal to the size of your Gene Pool.")),
            null,
            null,
            emptyList(),
            false,
            emptyList(),
            listOf(TraitIcon.DropOfLife),
        )

    @Test
    fun aCompassStarHasNoFaceValue() {
        assertEquals(null, altruistic.face()?.faceValue)
        assertEquals(-2, confusion.face()?.faceValue)
    }

    private val aiTakeover =
        Catastrophe(
            "classic/catastrophes/ai-takeover",
            "classic",
            "classic/catastrophes/ai-takeover.webp",
            "AI Takeover",
            listOf(
                Effect(text = "Discard all but 1 card from your hand."),
                Effect(
                    EffectKind.WorldsEnd,
                    "Each colorless trait is now worth 2. Ignore all colorless trait effects. (Excluding dominants.)",
                ),
            ),
            -1,
        )

    @Test
    fun aCatastropheHasOneSectionPerEffectAndItsGenePool() {
        val face = aiTakeover.face()
        assertEquals(
            listOf(
                CardSection.Effect("Discard all but 1 card from your hand."),
                CardSection.Effect(
                    "Each colorless trait is now worth 2. Ignore all colorless trait effects. (Excluding dominants.)",
                    EffectKind.WorldsEnd,
                ),
            ),
            face?.sections,
        )
        assertEquals(-1, face?.genePool)
        assertEquals(listOf(CardFact.Type(CardKind.Catastrophe)), face?.facts)
    }

    @Test
    fun anEffectlessTraitHasNoEffectSection() {
        assertEquals(
            listOf(CardSection.Flavor("And you might ask yourself, \"Why does this trait exist?")),
            confusion.face()?.sections,
        )
    }

    @Test
    fun searchIsByNameAndEveryEffectButNotFlavor() {
        assertEquals(
            listOf(
                "AI Takeover",
                "Discard all but 1 card from your hand.",
                "Each colorless trait is now worth 2. Ignore all colorless trait effects. (Excluding dominants.)",
            ),
            aiTakeover.face()?.searchable,
        )
        assertEquals(listOf("Confusion"), confusion.face()?.searchable)
    }

    @Test
    fun searchLeavesOutTokens() {
        val pearl =
            Age(
                "a",
                "classic",
                "a.webp",
                "Age",
                listOf(Effect(text = "Ignore actions ({action}) and Shells ({shell}).")),
            )

        assertEquals(listOf("Age", "Ignore actions and Shells."), pearl.face()?.searchable)
        assertEquals(
            listOf(CardSection.Effect("Ignore actions ({action}) and Shells ({shell}).")),
            pearl.face()?.sections,
        )
    }

    @Test
    fun aTranslationsFlatTextIsOnePlainSectionAndItsWorldsEndAnother() {
        val face =
            aiTakeover.face(CardText("Tomada pela IA", "-1 Reserva\n\nDescarte.", worldsEnd = "Incolores valem 2."))

        assertEquals(
            listOf(
                CardSection.Effect("-1 Reserva\n\nDescarte."),
                CardSection.Effect("Incolores valem 2.", EffectKind.WorldsEnd),
            ),
            face?.sections,
        )
        assertEquals(null, face?.genePool)
    }

    @Test
    fun aTranslationFillsTheSectionsAndSearch() {
        val face =
            confusion.face(
                CardText("Confusão", flavor = "E você pode se perguntar: \"Por que este trait existe?"),
            )
        assertEquals(
            listOf(CardSection.Flavor("E você pode se perguntar: \"Por que este trait existe?")),
            face?.sections,
        )
        assertEquals(listOf("Confusão"), face?.searchable)
    }

    @Test
    fun aColorlessTraitHasOneColorlessFact() {
        assertEquals(
            listOf(CardFact.Color(null), CardFact.Type(CardKind.Trait)),
            confusion.face()?.facts,
        )
    }
}
