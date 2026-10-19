package dev.nycocado.atlas.card

import kotlinx.serialization.json.Json

// Strict on purpose: a missing field, unknown key or unknown value fails instead of being skipped.
fun decodeCards(json: String): List<Card> = Json.decodeFromString(json)
