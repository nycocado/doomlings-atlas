package dev.nycocado.atlas.card

import kotlinx.serialization.Serializable
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

// The shape of a card's entry in a translation file: its name and the text fields it has in one language, complete or
// absent (every text field its card has and no other). The effects are one flat `effect` (and `worldsEnd` for a
// Catastrophe), Gene Pool line included.
@Serializable
data class CardText(
    val name: String,
    val effect: String? = null,
    val flavor: String? = null,
    val worldsEnd: String? = null,
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

    // The printed text when the card has no translation in that language.
    fun face(
        card: NamedCard,
        language: CardLanguage,
    ): CardFace? = translatedFace(card, language) ?: card.face()

    // null when the card has no translation in that language, without the printed fallback.
    fun translatedFace(
        card: NamedCard,
        language: CardLanguage,
    ): CardFace? = byLanguage[language]?.get(card.id)?.let(card::face)
}
