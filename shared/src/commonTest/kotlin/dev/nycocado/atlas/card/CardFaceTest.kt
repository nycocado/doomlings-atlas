package dev.nycocado.atlas.card

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
                CardFact.Kind(CardKind.Trait),
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
                CardFact.Kind(CardKind.Trait),
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
    fun aCatastropheHasABlockPerEffectAndItsWorldsEndAsAHeading() {
        val face = aiTakeover.face()
        assertEquals(
            listOf(
                CardBlock(
                    null,
                    KindDisplay.None,
                    "Discard all but 1 card from your hand.",
                    genePool = -1,
                    selector = true,
                ),
                CardBlock(
                    EffectKind.WorldsEnd,
                    KindDisplay.Heading,
                    "Each colorless trait is now worth 2. Ignore all colorless trait effects. (Excluding dominants.)",
                ),
            ),
            face?.blocks,
        )
        assertEquals(listOf(CardFact.Kind(CardKind.Catastrophe)), face?.facts)
    }

    private val bruteStrength =
        Trait(
            "classic/traits/brute-strength",
            "classic",
            "classic/traits/brute-strength.webp",
            "Brute Strength",
            "Wreck yourself before you check yourself bro!",
            emptyList(),
            -1,
            4,
            listOf(CardColor.Red),
            false,
            emptyList(),
            emptyList(),
        )

    @Test
    fun aTraitWithAGenePoolAndNoEffectHasOneBlockWithNoText() {
        assertEquals(
            listOf(
                CardBlock(
                    null,
                    KindDisplay.None,
                    null,
                    genePool = -1,
                    flavor = "Wreck yourself before you check yourself bro!",
                    selector = true,
                ),
            ),
            bruteStrength.face()?.blocks,
        )
    }

    @Test
    fun anEffectlessTraitHasOneBlockWithOnlyItsFlavor() {
        assertEquals(
            listOf(
                CardBlock(
                    null,
                    KindDisplay.None,
                    null,
                    flavor = "And you might ask yourself, \"Why does this trait exist?",
                    selector = true,
                ),
            ),
            confusion.face()?.blocks,
        )
    }

    @Test
    fun aCardWithNeitherEffectNorFlavorNorGenePoolHasNoBlocks() {
        val bare = confusion.copy(flavor = null)

        assertEquals(emptyList(), bare.face()?.blocks)
    }

    private val anxietyElixir =
        Trinket(
            "imaginary-ends/trinkets/anxiety-elixir",
            "imaginary-ends",
            "imaginary-ends/trinkets/anxiety-elixir.webp",
            "Anxiety Elixir",
            listOf(
                Effect(EffectKind.AwesomePower, "Start of Turn: Discard dominants from hand."),
                Effect(EffectKind.PocketObjective, "Reveal: 2 reds in hand."),
            ),
            2,
            "This causes stress. Can't stress that enough.",
        )

    @Test
    fun aTrinketsLabelsAreInlineAndItsFlavorAndValueEndTheLastBlock() {
        assertEquals(
            listOf(
                CardBlock(
                    EffectKind.AwesomePower,
                    KindDisplay.Label,
                    "Start of Turn: Discard dominants from hand.",
                    selector = true,
                ),
                CardBlock(
                    EffectKind.PocketObjective,
                    KindDisplay.Label,
                    "Reveal: 2 reds in hand.",
                    value = 2,
                    flavor = "This causes stress. Can't stress that enough.",
                ),
            ),
            anxietyElixir.blocks(anxietyElixir.effects, anxietyElixir.flavor, null),
        )
    }

    private val selfReplicating =
        Trait(
            "classic/traits/self-replicating",
            "classic",
            "classic/traits/self-replicating.webp",
            "Self-Replicating",
            "More me? Copy that.",
            listOf(
                Effect(EffectKind.Action, "Play 1 card from the discard pile. Ignore its action ({action})."),
                Effect(text = "When you play another green trait from your hand, you may return this to your hand."),
            ),
            null,
            0,
            listOf(CardColor.Green),
            false,
            emptyList(),
            listOf(TraitIcon.Action),
        )

    @Test
    fun aCardWithSeveralEffectsEndsItsLastBlockWithItsFlavorAndShowsNoActionLabel() {
        assertEquals(
            listOf(
                CardBlock(
                    EffectKind.Action,
                    KindDisplay.None,
                    "Play 1 card from the discard pile. Ignore its action.",
                    selector = true,
                ),
                CardBlock(
                    null,
                    KindDisplay.None,
                    "When you play another green trait from your hand, you may return this to your hand.",
                    flavor = "More me? Copy that.",
                ),
            ),
            selfReplicating.face()?.blocks,
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
            "Ignore actions and Shells.",
            pearl
                .face()
                ?.blocks
                ?.single()
                ?.text,
        )
    }

    @Test
    fun aTranslationsFlatTextIsOnePlainBlockAndItsWorldsEndAnother() {
        val face =
            CardTranslations(
                mapOf(
                    CardLanguage.Portuguese to
                        mapOf(
                            aiTakeover.id to
                                CardText("Tomada pela IA", "-1 Reserva\n\nDescarte.", worldsEnd = "Incolores valem 2."),
                        ),
                ),
            ).face(aiTakeover, CardLanguage.Portuguese)

        assertEquals(
            listOf(
                CardBlock(null, KindDisplay.None, "-1 Reserva\n\nDescarte.", selector = true),
                CardBlock(EffectKind.WorldsEnd, KindDisplay.Heading, "Incolores valem 2."),
            ),
            face?.blocks,
        )
    }

    @Test
    fun translatedFaceHasNoPrintedFallback() {
        val translations =
            CardTranslations(
                mapOf(CardLanguage.Portuguese to mapOf(aiTakeover.id to CardText("Tomada pela IA"))),
            )

        assertEquals("Tomada pela IA", translations.translatedFace(aiTakeover, CardLanguage.Portuguese)?.name)
        assertEquals(null, translations.translatedFace(confusion, CardLanguage.Portuguese))
        assertEquals(null, translations.translatedFace(aiTakeover, CardLanguage.Spanish))
    }

    @Test
    fun aTranslationFillsTheBlocksAndSearch() {
        val face =
            CardTranslations(
                mapOf(
                    CardLanguage.Portuguese to
                        mapOf(
                            confusion.id to
                                CardText("Confusão", flavor = "E você pode se perguntar: \"Por que este trait existe?"),
                        ),
                ),
            ).face(confusion, CardLanguage.Portuguese)
        assertEquals("Confusão", face?.name)
        assertEquals(
            listOf(
                CardBlock(
                    null,
                    KindDisplay.None,
                    null,
                    flavor = "E você pode se perguntar: \"Por que este trait existe?",
                    selector = true,
                ),
            ),
            face?.blocks,
        )
        assertEquals(listOf("Confusão"), face?.searchable)
    }

    @Test
    fun onlyATrinketHasAValue() {
        assertEquals(
            null,
            confusion
                .face()
                ?.blocks
                ?.single()
                ?.value,
        )
        assertEquals(
            2,
            anxietyElixir.blocks(anxietyElixir.effects, anxietyElixir.flavor, null).last().value,
        )
    }

    @Test
    fun aColorlessTraitHasOneColorlessFact() {
        assertEquals(
            listOf(CardFact.Color(null), CardFact.Kind(CardKind.Trait)),
            confusion.face()?.facts,
        )
    }
}
