package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json

// Strict on purpose: a missing field, unknown key or unknown value fails instead of being skipped, and so does a label
// kind on a card type that doesn't have it.
fun decodeCards(json: String): List<Card> =
    Json.decodeFromString<List<Card>>(json).onEach { card ->
        (card as? NamedCard)?.effects?.forEach {
            require(
                it.kind?.allowedOn(card) != false,
            ) { "${card.id}: ${it.kind} is not a kind of ${card::class.simpleName}" }
        }
    }
