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
    fun aCatastropheHasItsGenePoolThenABlockPerEffectWithItsKind() {
        val face = aiTakeover.face()
        assertEquals(
            listOf(
                CardBlock(BlockContent.GenePool(-1), selector = true),
                effectBlock(null, "Discard all but 1 card from your hand."),
                effectBlock(
                    EffectKind.WorldsEnd,
                    "Each colorless trait is now worth 2. Ignore all colorless trait effects. (Excluding dominants.)",
                    ink = Ink.Yellow,
                ),
            ),
            face?.blocks,
        )
        assertEquals(listOf(CardFact.Kind(CardKind.Catastrophe)), face?.facts)
    }

    @Test
    fun aTraitsWorldsEndIsNotYellow() {
        val worldsEnd =
            awesome
                .face()
                ?.blocks
                ?.single()
                ?.content as BlockContent.Effect

        assertEquals(null, worldsEnd.ink)
    }

    @Test
    fun aMarkedSpanKeepsItsInkInsideAYellowEffect() {
        val catastrophe =
            Catastrophe(
                "test/catastrophes/marked",
                "test",
                "",
                "Marked",
                listOf(Effect(EffectKind.WorldsEnd, "[red]Red[/red] traits are worth 2.")),
                -1,
            )

        assertEquals(
            listOf(TextPart.Words("Red", Ink.Red), TextPart.Words(" traits are worth 2.", Ink.Yellow)),
            (
                catastrophe
                    .face()
                    ?.blocks
                    ?.last()
                    ?.content as BlockContent.Effect
            ).text,
        )
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
    fun aTraitWithAGenePoolAndNoEffectHasOnlyItsGenePoolBlockWithTheFlavor() {
        assertEquals(
            listOf(
                CardBlock(
                    BlockContent.GenePool(-1),
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
    fun aTrinketsLabelsAreKindsAndItsValueIsTheLastBlockWithTheFlavor() {
        assertEquals(
            listOf(
                effectBlock(EffectKind.AwesomePower, "Start of Turn: Discard dominants from hand.", selector = true),
                effectBlock(EffectKind.PocketObjective, "Reveal: 2 reds in hand.", ink = Ink.Yellow),
                CardBlock(BlockContent.Value(2), flavor = "This causes stress. Can't stress that enough."),
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
    fun aCardWithSeveralEffectsEndsItsLastBlockWithItsFlavorAndKeepsItsTokensInTheText() {
        assertEquals(
            listOf(
                CardBlock(
                    BlockContent.Effect(
                        EffectKind.Action,
                        listOf(
                            TextPart.Words("Play 1 card from the discard pile. Ignore its action ("),
                            TextPart.Icon(Token.Action),
                            TextPart.Words(")."),
                        ),
                    ),
                    selector = true,
                ),
                effectBlock(
                    null,
                    "When you play another green trait from your hand, you may return this to your hand.",
                    flavor = "More me? Copy that.",
                ),
            ),
            selfReplicating.face()?.blocks,
        )
    }

    @Test
    fun everyTokenOfTheCardDataIsAnIconAndAnUnknownOneStaysAsWritten() {
        val tokens =
            listOf(
                "action",
                "attachment",
                "drop-of-life",
                "worlds-end",
                "compass",
                "coral",
                "fish",
                "shell",
                "mythling",
                "dinoling",
                "techling",
                "deepling",
                "moonling",
                "glitterling",
            )
        val age =
            Age("a", "classic", "a.webp", "Age", listOf(Effect(text = tokens.joinToString("") { "{$it}" } + "{x}")))

        assertEquals(
            Token.entries.map { TextPart.Icon(it) } + TextPart.Words("{x}"),
            (
                age
                    .face()
                    ?.blocks
                    ?.single()
                    ?.content as BlockContent.Effect
            ).text,
        )
    }

    private fun Age.textParts() = (face()?.blocks?.single()?.content as BlockContent.Effect).text

    private fun age(text: String) = Age("a", "classic", "a.webp", "Age", listOf(Effect(text = text)))

    @Test
    fun aMarkedSpanIsWordsWithItsInk() {
        assertEquals(
            listOf(
                TextPart.Words("Play a "),
                TextPart.Words("red", Ink.Red),
                TextPart.Words(" or "),
                TextPart.Words("rainbow", Ink.Rainbow),
                TextPart.Words(" trait."),
            ),
            age("Play a [red]red[/red] or [rainbow]rainbow[/rainbow] trait.").textParts(),
        )
    }

    @Test
    fun everyInkOfTheCardDataHasItsName() {
        val inks = listOf("red", "blue", "green", "purple", "colorless", "rainbow", "yellow")

        assertEquals(
            Ink.entries.map { TextPart.Words("x", it) },
            age(inks.joinToString("") { "[$it]x[/$it]" }).textParts(),
        )
    }

    @Test
    fun tokensStillParseInAnEffectWithInks() {
        assertEquals(
            listOf(
                TextPart.Words("Discard 1 "),
                TextPart.Words("blue", Ink.Blue),
                TextPart.Words(" trait ("),
                TextPart.Icon(Token.Action),
                TextPart.Words(")."),
            ),
            age("Discard 1 [blue]blue[/blue] trait ({action}).").textParts(),
        )
    }

    @Test
    fun searchLeavesOutInkMarks() {
        assertEquals(
            listOf("Age", "Discard 1 blue trait."),
            age("Discard 1 [blue]blue[/blue] trait ({action}).").face()?.searchable,
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
    }

    @Test
    fun aTranslationsEffectsTakeTheKindsOfTheCardsEffectsInOrderAndItsSearchLeavesOutTokens() {
        val face =
            CardTranslations(
                mapOf(
                    CardLanguage.Portuguese to
                        mapOf(
                            aiTakeover.id to
                                CardText(
                                    "Tomada pela IA",
                                    listOf("Descarte.", "Incolores valem 2. Ignore ações ({action})."),
                                ),
                        ),
                ),
            ).face(aiTakeover, CardLanguage.Portuguese)

        assertEquals(
            listOf(
                CardBlock(BlockContent.GenePool(-1), selector = true),
                effectBlock(null, "Descarte."),
                CardBlock(
                    BlockContent.Effect(
                        EffectKind.WorldsEnd,
                        listOf(
                            TextPart.Words("Incolores valem 2. Ignore ações (", Ink.Yellow),
                            TextPart.Icon(Token.Action),
                            TextPart.Words(").", Ink.Yellow),
                        ),
                        ink = Ink.Yellow,
                    ),
                ),
            ),
            face?.blocks,
        )
        assertEquals(listOf("Tomada pela IA", "Descarte.", "Incolores valem 2. Ignore ações."), face?.searchable)
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
                    flavor = "E você pode se perguntar: \"Por que este trait existe?",
                    selector = true,
                ),
            ),
            face?.blocks,
        )
        assertEquals(listOf("Confusão"), face?.searchable)
    }

    @Test
    fun aColorlessTraitHasOneColorlessFact() {
        assertEquals(
            listOf(CardFact.Color(null), CardFact.Kind(CardKind.Trait)),
            confusion.face()?.facts,
        )
    }
}

// A block of one effect whose text has no token.
internal fun effectBlock(
    kind: EffectKind?,
    text: String,
    flavor: String? = null,
    selector: Boolean = false,
    ink: Ink? = null,
) = CardBlock(BlockContent.Effect(kind, listOf(TextPart.Words(text, ink)), ink), flavor, selector)
