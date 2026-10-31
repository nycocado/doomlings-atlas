package dev.nycocado.atlas.card

import kotlinx.serialization.Serializable

// The compendium's segments. Other card types (Gene Pool, Birth of Life) are not listed.
@Serializable
enum class CardKind { Trait, Age, Catastrophe }

val Card.kind: CardKind?
    get() =
        when (this) {
            is Trait -> CardKind.Trait
            is Age -> CardKind.Age
            is Catastrophe -> CardKind.Catastrophe
            else -> null
        }
