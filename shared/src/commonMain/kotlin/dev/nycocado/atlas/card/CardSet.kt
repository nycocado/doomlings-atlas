package dev.nycocado.atlas.card

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// One assets/doomlings/<set>/set.json. Name and year are read off the set's cards and never guessed: null where the
// cards don't print them. Both keys are required, so a null is a decision and not a forgotten field.
@Serializable
data class CardSet(
    val id: String,
    val name: String?,
    val year: Int?,
)

// Special's promos span several years, so it goes last whatever its year. A set with no year comes first: Classic,
// which prints none, opens the release order.
internal fun List<CardSet>.inReleaseOrder(): List<CardSet> =
    filter { it.name != null }
        .sortedWith(compareBy({ it.id == SPECIAL_SET }, { it.year }, { it.name }))

internal fun decodeSets(json: String): List<CardSet> = Json.decodeFromString(json)

private const val SPECIAL_SET = "special"
