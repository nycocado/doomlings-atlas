package dev.nycocado.atlas.card

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json

// Language codes are the file names of the translations in each set folder of assets/doomlings (pt.json) and of
// docs/glossary.
enum class CardLanguage(
    val code: String,
) {
    English("en"),
    Portuguese("pt"),
    Spanish("es"),
}

// A card's name and the text fields it has in one language. In a translation file it is complete or absent: every text
// field its card has and no other. A translation still writes its effects as one flat `effect` (and `worldsEnd` for a
// Catastrophe), Gene Pool line included. The printed text instead holds the card's `effects` and `genePool`, which are
// never in a translation file.
@Serializable
data class CardText(
    val name: String,
    val effect: String? = null,
    val flavor: String? = null,
    val worldsEnd: String? = null,
    @Transient val effects: List<Effect>? = null,
    @Transient val genePool: Int? = null,
)

// Strict like decodeCards: an unknown key fails. The JSON maps card id to its translation.
fun decodeTranslations(json: String): Map<String, CardText> = Json.decodeFromString(json)

class CardTranslations(
    private val byLanguage: Map<CardLanguage, Map<String, CardText>> = emptyMap(),
) {
    // English first, then each language the card has a translation in.
    fun languages(card: Card): List<CardLanguage> =
        listOf(CardLanguage.English) +
            CardLanguage.entries.filter { it != CardLanguage.English && byLanguage[it]?.containsKey(card.id) == true }

    fun text(
        card: NamedCard,
        language: CardLanguage,
    ): CardText = byLanguage[language]?.get(card.id) ?: card.printedText()
}

fun NamedCard.printedText() =
    CardText(
        name,
        flavor =
            when (this) {
                is Trait -> flavor
                is MeaningOfLife -> flavor
                is Trinket -> flavor
                else -> null
            },
        effects = effects,
        genePool =
            when (this) {
                is Trait -> genePool
                is Catastrophe -> genePool
                else -> null
            },
    )
