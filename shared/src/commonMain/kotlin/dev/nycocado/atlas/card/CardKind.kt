package dev.nycocado.atlas.card

import kotlinx.serialization.Serializable

// The compendium's segments, in their order.
@Serializable
enum class CardKind { Trait, Age, Catastrophe, GenePool, MeaningOfLife, Treasure, Trinket, Merchant }

val Card.kind: CardKind
    get() =
        when (this) {
            is Trait -> CardKind.Trait
            is Age, is BirthOfLife -> CardKind.Age
            is Catastrophe -> CardKind.Catastrophe
            is GenePool -> CardKind.GenePool
            is MeaningOfLife -> CardKind.MeaningOfLife
            is Treasure -> CardKind.Treasure
            is Trinket -> CardKind.Trinket
            is Merchant -> CardKind.Merchant
        }

// The kinds with a card in this list, in the compendium's order.
fun List<Card>.offeredKinds(): List<CardKind> = map { it.kind }.toSet().sorted()
