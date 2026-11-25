package dev.nycocado.atlas.scoring

import dev.nycocado.atlas.card.Trait
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

// A card's `scoring` entry in the card data (ADR 0005): the serial name is the function's `fn`, the properties its
// parameters. Decoding is strict, so an unknown function or a bad parameter fails.
@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("fn")
sealed interface ScoringRule {
    // What the rule reads from a player beyond the trait piles, so the app asks only for that.
    val needs: Set<PlayerInput> get() = emptySet()

    fun count(
        card: Trait,
        player: Player,
        game: FinishedGame,
    ): RuleValue

    // A card's own name, so a variant of any set counts as one (GLOSSARY, Variant).
    @Serializable
    @SerialName("countByName")
    data class CountByName(
        val zone: Zone,
    ) : ScoringRule {
        override fun count(
            card: Trait,
            player: Player,
            game: FinishedGame,
        ): RuleValue {
            val piles =
                when (zone) {
                    Zone.OwnPile -> listOf(player.pile)
                    Zone.AllPiles -> game.players.map { it.pile }
                }
            return RuleValue(
                card,
                piles.sumOf { pile ->
                    pile.count { it.name == card.name }
                },
                Counted.SameName,
            )
        }
    }

    @Serializable
    @SerialName("genePoolSize")
    data object GenePoolSize : ScoringRule {
        override val needs = setOf(PlayerInput.GenePoolSize)

        override fun count(
            card: Trait,
            player: Player,
            game: FinishedGame,
        ) = RuleValue(card, player.genePoolSize, Counted.GenePoolSize)
    }

    @Serializable
    @SerialName("handSize")
    data object HandSize : ScoringRule {
        override val needs = setOf(PlayerInput.Hand)

        override fun count(
            card: Trait,
            player: Player,
            game: FinishedGame,
        ) = RuleValue(card, player.hand.size, Counted.CardsInHand)
    }

    @Serializable
    @SerialName("handWithEffects")
    data object HandWithEffects : ScoringRule {
        override val needs = setOf(PlayerInput.Hand)

        override fun count(
            card: Trait,
            player: Player,
            game: FinishedGame,
        ) = RuleValue(card, player.hand.count { !it.effectless }, Counted.CardsInHandWithEffects)
    }

    // A dual-color trait counts for both colors, a colorless one for none.
    @Serializable
    @SerialName("differentColors")
    data object DifferentColors : ScoringRule {
        override fun count(
            card: Trait,
            player: Player,
            game: FinishedGame,
        ) = RuleValue(
            card,
            player.pile
                .flatMap { it.colors }
                .distinct()
                .size,
            Counted.Colors,
        )
    }
}

@Serializable
enum class Zone {
    @SerialName("ownPile")
    OwnPile,

    @SerialName("allPiles")
    AllPiles,
}

enum class PlayerInput { Hand, GenePoolSize }

// What a rule counted, as data: the app writes it in the interface language ("4 Kidneys").
sealed interface Counted {
    // Traits named as the rule's card, which the app writes in the card language.
    data object SameName : Counted

    data object GenePoolSize : Counted

    data object CardsInHand : Counted

    data object CardsInHandWithEffects : Counted

    data object Colors : Counted
}

// A compass star's value is its count; a Drop of Life bonus will give a value apart from it.
data class RuleValue(
    val card: Trait,
    val count: Int,
    val counted: Counted,
    val value: Int = count,
)

fun needsOf(pile: List<Trait>): Set<PlayerInput> = pile.flatMapTo(mutableSetOf()) { it.scoring?.needs.orEmpty() }

// The sets whose every scoring card has its rule; a game's cards come only from these.
val scoredSets: Set<String> = setOf("classic")
