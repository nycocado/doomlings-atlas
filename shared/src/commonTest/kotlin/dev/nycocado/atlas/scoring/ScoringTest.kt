package dev.nycocado.atlas.scoring

import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Effect
import dev.nycocado.atlas.card.Trait
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

// Classic cards as printed; expected scores are worked out by hand from their text and the rulesheet.
private fun trait(
    id: String,
    name: String,
    points: Int?,
    colors: List<CardColor> = emptyList(),
    effect: String? = null,
    scoring: ScoringRule? = null,
) = Trait(
    id = "classic/traits/$id",
    set = "classic",
    image = "classic/traits/$id.webp",
    name = name,
    flavor = null,
    effects = listOfNotNull(effect?.let { Effect(text = it) }),
    genePool = null,
    points = points,
    colors = colors,
    dominant = false,
    subspecies = emptyList(),
    icons = emptyList(),
    scoring = scoring,
)

private val adorable = trait("adorable", "Adorable", 4, listOf(CardColor.Purple))
private val antlers = trait("antlers", "Antlers", 3, listOf(CardColor.Red))
private val bark = trait("bark", "Bark", 2, listOf(CardColor.Green))
private val fangs = trait("fangs", "Fangs", 1, listOf(CardColor.Red))
private val confusion = trait("confusion", "Confusion", -2)
private val bad = trait("bad", "Bad", 1, listOf(CardColor.Red), "Opponents discard 2 cards from their hand at random.")

// No effects but a Gene Pool line, so not effectless (GLOSSARY, Effect): one of Boredom's "cards with effects".
private val teeth = trait("teeth", "Teeth", 1, listOf(CardColor.Purple)).copy(genePool = 1)
private val coldBlood =
    trait("cold-blood", "Cold Blood", 1, listOf(CardColor.Blue), "Draw 3 cards.\nYou may play 1 now.")

private fun kidney(id: String) =
    trait(
        id,
        "Kidney",
        null,
        listOf(CardColor.Red),
        "Value is equal to the number of Kidneys in your trait pile.\n(Including this one.)",
        ScoringRule.CountByName(Zone.OwnPile),
    )

private fun swarm(id: String) =
    trait(
        id,
        "Swarm",
        null,
        listOf(CardColor.Green),
        "Value is equal to the number of Swarm traits in all trait piles.\n(Including this one.)",
        ScoringRule.CountByName(Zone.AllPiles),
    )

private val altruistic =
    trait(
        "altruistic",
        "Altruistic",
        null,
        effect = "Value is equal to the size of your Gene Pool.",
        scoring = ScoringRule.GenePoolSize,
    )
private val randomFertilization =
    trait(
        "random-fertilization",
        "Random Fertilization",
        null,
        listOf(CardColor.Green),
        "Value is equal to the size of your Gene Pool.",
        ScoringRule.GenePoolSize,
    )
private val fortunate =
    trait(
        "fortunate",
        "Fortunate",
        null,
        listOf(CardColor.Green),
        "Value is equal to the number of cards in your hand.",
        ScoringRule.HandSize,
    )
private val boredom =
    trait(
        "boredom",
        "Boredom",
        null,
        effect = "Value is equal to the number of cards in your hand with effects.",
        scoring = ScoringRule.HandWithEffects,
    )
private val gratitude =
    trait(
        "gratitude",
        "Gratitude",
        null,
        effect = "Value is equal to the number of different [rainbow]colors[/rainbow] in your trait pile.",
        scoring = ScoringRule.DifferentColors,
    )

private fun catastrophe(
    id: String,
    name: String,
) = Catastrophe("classic/catastrophes/$id", "classic", "classic/catastrophes/$id.webp", name, emptyList(), null)

private val catastrophes =
    listOf(
        catastrophe("ai-takeover", "AI Takeover"),
        catastrophe("deus-ex-machina", "Deus Ex Machina"),
        catastrophe("glacial-meltdown", "Glacial Meltdown"),
    )

private fun player(
    name: String,
    pile: List<Trait>,
    hand: List<Trait> = emptyList(),
    genePoolSize: Int = 5,
    manualAdjustment: Int = 0,
) = Player(name, pile, hand, genePoolSize, manualAdjustment)

private fun scoresOf(vararg players: Player) = FinishedGame(players.toList(), catastrophes).score()

private val filler = player("Filler", listOf(antlers))

class ScoringTest {
    @Test
    fun effectlessPileScoresItsFaceValues() {
        val score = scoresOf(player("Ana", listOf(adorable, antlers, confusion)), filler).first()

        assertEquals(5, score.score)
        assertEquals(FaceValues(traits = 3, total = 5), score.breakdown.faceValues)
        assertEquals(emptyList(), score.breakdown.rules)
    }

    @Test
    fun kidneyCountsTheKidneysInItsOwnPile() {
        val kidney = kidney("kidney")
        val kidney2 = kidney("kidney-2")
        val score =
            scoresOf(player("Ana", listOf(kidney, kidney2, adorable)), player("Ben", listOf(kidney("kidney-3"))))
                .first()

        assertEquals(8, score.score)
        assertEquals(FaceValues(traits = 3, total = 4), score.breakdown.faceValues)
        assertEquals(
            listOf(
                RuleValue(kidney, 2, Counted.SameName),
                RuleValue(kidney2, 2, Counted.SameName),
            ),
            score.breakdown.rules,
        )
    }

    @Test
    fun swarmCountsTheSwarmsInEveryPile() {
        val scores =
            scoresOf(player("Ana", listOf(swarm("swarm"), swarm("swarm-2"))), player("Ben", listOf(swarm("swarm-3"))))

        assertEquals(listOf(6, 3), scores.map { it.score })
        assertEquals(RuleValue(scores[1].player.pile.single(), 3, Counted.SameName), scores[1].breakdown.rules.single())
    }

    @Test
    fun altruisticAndRandomFertilizationAreTheGenePoolSize() {
        val score = scoresOf(player("Ana", listOf(altruistic, randomFertilization), genePoolSize = 6), filler).first()

        assertEquals(12, score.score)
        assertEquals(
            listOf(
                RuleValue(altruistic, 6, Counted.GenePoolSize),
                RuleValue(randomFertilization, 6, Counted.GenePoolSize),
            ),
            score.breakdown.rules,
        )
    }

    @Test
    fun fortunateCountsTheCardsInHand() {
        val score = scoresOf(player("Ana", listOf(fortunate), hand = listOf(adorable, bad, coldBlood)), filler).first()

        assertEquals(3, score.score)
        assertEquals(RuleValue(fortunate, 3, Counted.CardsInHand), score.breakdown.rules.single())
    }

    @Test
    fun boredomCountsTheCardsInHandWithEffects() {
        val hand = listOf(adorable, teeth, bad, coldBlood)
        val score = scoresOf(player("Ana", listOf(boredom), hand = hand), filler).first()

        assertEquals(3, score.score)
        assertEquals(RuleValue(boredom, 3, Counted.CardsInHandWithEffects), score.breakdown.rules.single())
    }

    @Test
    fun gratitudeCountsTheDifferentColorsOfThePileWithoutColorless() {
        val score =
            scoresOf(player("Ana", listOf(gratitude, adorable, antlers, bark, fangs, confusion)), filler).first()

        assertEquals(11, score.score)
        assertEquals(RuleValue(gratitude, 3, Counted.Colors), score.breakdown.rules.single())
    }

    @Test
    fun manualAdjustmentAddsToTheScore() {
        val score = scoresOf(player("Ana", listOf(adorable, antlers, confusion), manualAdjustment = -3), filler).first()

        assertEquals(2, score.score)
        assertEquals(-3, score.breakdown.manualAdjustment)
    }

    @Test
    fun breakdownSumsToTheScore() {
        val pile = listOf(kidney("kidney"), fortunate, adorable)
        val score = scoresOf(player("Ana", pile, hand = listOf(bad), manualAdjustment = 2), filler).first()

        // Adorable 4, Kidney 1, Fortunate 1, adjustment 2.
        assertEquals(8, score.score)
        assertEquals(
            Breakdown(
                FaceValues(traits = 3, total = 4),
                listOf(RuleValue(pile[0], 1, Counted.SameName), RuleValue(fortunate, 1, Counted.CardsInHand)),
                manualAdjustment = 2,
            ),
            score.breakdown,
        )
    }

    @Test
    fun eachRuleDeclaresWhatItNeedsFromThePlayer() {
        assertEquals(setOf(PlayerInput.Hand), fortunate.scoring?.needs)
        assertEquals(setOf(PlayerInput.Hand), boredom.scoring?.needs)
        assertEquals(setOf(PlayerInput.GenePoolSize), altruistic.scoring?.needs)
        assertEquals(emptySet(), kidney("kidney").scoring?.needs)
        assertEquals(
            setOf(PlayerInput.Hand, PlayerInput.GenePoolSize),
            needsOf(listOf(fortunate, altruistic, adorable)),
        )
    }

    @Test
    fun gameNeedsTwoToSixPlayersAndThreeCatastrophes() {
        assertFailsWith<IllegalArgumentException> { FinishedGame(listOf(filler), catastrophes) }
        assertFailsWith<IllegalArgumentException> {
            FinishedGame(List(7) { player("P$it", emptyList()) }, catastrophes)
        }
        assertFailsWith<IllegalArgumentException> {
            FinishedGame(listOf(filler, filler.copy(name = "B")), catastrophes.take(2))
        }
    }

    @Test
    fun genePoolSizeIsBetweenOneAndEight() {
        assertFailsWith<IllegalArgumentException> { player("Ana", emptyList(), genePoolSize = 0) }
        assertFailsWith<IllegalArgumentException> { player("Ana", emptyList(), genePoolSize = 9) }
    }
}

class RankingTest {
    // Ana 4 - 2 + 3 = 5, Ben 4 + 2 + 2 = 8, Cy 2 + 3 = 5, Di 1.
    private val ana = player("Ana", listOf(adorable, confusion, antlers))
    private val ben =
        player("Ben", listOf(adorable, bark, trait("deep-roots", "Deep Roots", 2, listOf(CardColor.Green))))
    private val cy = player("Cy", listOf(bark, antlers))
    private val di = player("Di", listOf(fangs))
    private val scores = scoresOf(ana, ben, cy, di)

    @Test
    fun ordersPlayersByScoreAndKeepsATieWithoutAnOrder() {
        assertEquals(listOf(listOf("Ben"), listOf("Ana", "Cy"), listOf("Di")), ranking(scores).names())
    }

    @Test
    fun tableOrderBreaksATie() {
        assertEquals(
            listOf(listOf("Ben"), listOf("Cy"), listOf("Ana"), listOf("Di")),
            ranking(scores, tieOrder = listOf(2, 0)).names(),
        )
    }

    @Test
    fun tableOrderChangesNoScoreNorAnyoneOutsideTheTie() {
        val ranked = ranking(scores, tieOrder = listOf(3, 2, 1, 0))

        assertEquals(listOf(listOf("Ben"), listOf("Cy"), listOf("Ana"), listOf("Di")), ranked.names())
        assertEquals(listOf(8, 5, 5, 1), ranked.flatten().map { it.score })
    }

    @Test
    fun tieStaysWhenTheOrderLeavesATiedPlayerOut() {
        assertEquals(
            listOf(listOf("Ben"), listOf("Ana", "Cy"), listOf("Di")),
            ranking(scores, tieOrder = listOf(2)).names(),
        )
    }

    @Test
    fun equalPlayersStayApart() {
        val ranked = ranking(scoresOf(ana, ana.copy()), tieOrder = listOf(1, 0))

        assertEquals(2, ranked.size)
    }

    private fun List<List<PlayerScore>>.names() = map { group -> group.map { it.player.name } }
}
