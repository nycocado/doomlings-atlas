package dev.nycocado.atlas.card

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Language codes are the file names of the translations in assets/doomlings (pt.json) and of docs/glossary.
enum class CardLanguage(
    val code: String,
) {
    English("en"),
    Portuguese("pt"),
    Spanish("es"),
}

// A card's name and the text fields it has, with the field names of cards.json. Complete or absent: a translation
// has every text field its card has and no other.
@Serializable
data class CardTranslation(
    val name: String,
    val effect: String? = null,
    val flavor: String? = null,
    val worldsEnd: String? = null,
)

// Strict like decodeCards: an unknown key fails. The JSON maps card id to its translation.
fun decodeTranslations(json: String): Map<String, CardTranslation> = Json.decodeFromString(json)

// The text of a card in one language, as shown: the translation when there is one, else the printed text.
data class CardText(
    val name: String,
    val effect: String?,
    val flavor: String?,
    val worldsEnd: String?,
)

class CardTranslations(
    private val byLanguage: Map<CardLanguage, Map<String, CardTranslation>> = emptyMap(),
) {
    // English first, then each language the card has a translation in.
    fun languages(card: Card): List<CardLanguage> =
        listOf(CardLanguage.English) +
            CardLanguage.entries.filter { it != CardLanguage.English && byLanguage[it]?.containsKey(card.id) == true }

    fun text(
        card: NamedCard,
        language: CardLanguage,
    ): CardText {
        val translation = byLanguage[language]?.get(card.id) ?: return card.printedText()
        return CardText(translation.name, translation.effect, translation.flavor, translation.worldsEnd)
    }
}

fun NamedCard.printedText() =
    CardText(
        name,
        effect,
        when (this) {
            is Trait -> flavor
            is MeaningOfLife -> flavor
            is Trinket -> flavor
            else -> null
        },
        (this as? Catastrophe)?.worldsEnd,
    )
