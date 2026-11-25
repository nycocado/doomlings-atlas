package dev.nycocado.atlas.scoring

import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.Trait

// The final state of a game, after the table resolved every World's End that moves cards.
data class FinishedGame(
    val players: List<Player>,
    // In the order they came; the 3rd one's World's End counts.
    val catastrophes: List<Catastrophe>,
) {
    init {
        require(players.size in 2..6) { "a game has 2 to 6 players, not ${players.size}" }
        require(catastrophes.size == 3) { "a game has 3 catastrophes, not ${catastrophes.size}" }
    }

    fun score(): List<PlayerScore> = players.map { PlayerScore(it, breakdown(it)) }

    private fun breakdown(player: Player) =
        Breakdown(
            FaceValues(player.pile.size, player.pile.sumOf { it.faceValue }),
            player.pile.mapNotNull { card -> card.scoring?.count(card, player, this) },
            player.manualAdjustment,
        )
}

data class Player(
    val name: String,
    val pile: List<Trait>,
    val hand: List<Trait>,
    val genePool: Int,
    // Signed, for cards missing from the data and house rules.
    val manualAdjustment: Int,
) {
    init {
        require(genePool in 1..8) { "a Gene Pool is 1 to 8, not $genePool" }
    }
}

data class PlayerScore(
    val player: Player,
    val breakdown: Breakdown,
) {
    val score: Int get() = breakdown.faceValues.total + breakdown.rules.sumOf { it.value } + breakdown.manualAdjustment
}

data class Breakdown(
    val faceValues: FaceValues,
    // One per card with a rule, in pile order.
    val rules: List<RuleValue>,
    val manualAdjustment: Int,
)

data class FaceValues(
    val traits: Int,
    val total: Int,
)

private val Trait.faceValue: Int get() = points ?: 0

// Players by score, tied players in one group. A group breaks by the drawn traits' face values only when every tied
// player has one; otherwise the tie stays.
fun ranking(
    scores: List<PlayerScore>,
    drawn: Map<Player, Trait> = emptyMap(),
): List<List<PlayerScore>> =
    scores
        .groupBy { it.score }
        .entries
        .sortedByDescending { it.key }
        .map { it.value }
        .flatMap { tied ->
            val draws = tied.map { drawn[it.player] }
            if (tied.size == 1 || null in draws) {
                listOf(tied)
            } else {
                tied
                    .groupBy {
                        drawn
                            .getValue(
                                it.player,
                            ).faceValue
                    }.entries
                    .sortedByDescending { it.key }
                    .map { it.value }
            }
        }
