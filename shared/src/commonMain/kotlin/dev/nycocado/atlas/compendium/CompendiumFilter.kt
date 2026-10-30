package dev.nycocado.atlas.compendium

import ca.solostudios.stringsimilarity.DamerauLevenshtein
import dev.nycocado.atlas.card.Age
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.Catastrophe
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.card.printedText
import kotlinx.serialization.Serializable
import kotlin.math.abs

// The compendium's segments. Other card types (Gene Pool, Birth of Life) are not listed.
@Serializable
enum class CardKind { Trait, Age, Catastrophe }

@Serializable
data class CompendiumFilter(
    val kind: CardKind = CardKind.Trait,
    val query: String = "",
    // Traits only. null stands for colorless; empty shows every color.
    val colors: Set<CardColor?> = emptySet(),
)

val Card.kind: CardKind?
    get() =
        when (this) {
            is Trait -> CardKind.Trait
            is Age -> CardKind.Age
            is Catastrophe -> CardKind.Catastrophe
            else -> null
        }

// A query word matches a word of the card's name or text when it is part of it or within a few typos of it (none up to
// 3 letters, 1 up to 7, 2 from 8; at most 1 outside the name). Every query word must match in the same text, the printed
// one or the translation in the card language. The list is ordered by relevance (the query as a whole in a text, then
// words that start with the query's, then typos), then by printed name.
fun List<Card>.filterCompendium(
    filter: CompendiumFilter,
    translations: CardTranslations = CardTranslations(),
    language: CardLanguage = CardLanguage.English,
): List<NamedCard> {
    val query = normalize(filter.query.trim())
    return filterIsInstance<NamedCard>()
        .filter { card ->
            card.kind == filter.kind &&
                (
                    filter.colors.isEmpty() || card !is Trait ||
                        card.colors.ifEmpty { listOf(null) }.any { it in filter.colors }
                )
        }.mapNotNull { card ->
            val tier =
                if (query.isEmpty()) {
                    0
                } else {
                    listOf(card.printedText(), translations.text(card, language))
                        .mapNotNull { text -> card.face(text)?.searchable?.let { matchTier(query, it) } }
                        .minOrNull()
                }
            tier?.let { it to card }
        }.sortedWith(compareBy({ it.first }, { normalize(it.second.name) }))
        .map { it.second }
}

private val wordRegex = Regex("[\\p{L}\\p{N}]+")

private val damerau = DamerauLevenshtein()

// 0 the query is part of a text, 1 its words are the start of words of a text, 2 within typos, null no match.
// searchable[0] is the name.
private fun matchTier(
    query: String,
    searchable: List<String>,
): Int? {
    val queryWords = wordRegex.findAll(query).map { it.value }.toList()
    // A query with symbols ("+1") only matches as written.
    val wordsOnly = queryWords.joinToString(" ") == query
    return searchable
        .mapIndexedNotNull { index, text ->
            val normalized = normalize(text)
            val words = wordRegex.findAll(normalized).map { it.value }.toList()
            when {
                query in normalized -> 0
                !wordsOnly -> null
                queryWords.all { q -> words.any { it.startsWith(q) } } -> 1
                queryWords.all { q -> words.any { it.contains(q) || withinTypos(q, it, index == 0) } } -> 2
                else -> null
            }
        }.minOrNull()
}

private fun withinTypos(
    queryWord: String,
    word: String,
    inName: Boolean,
): Boolean {
    val allowed = typosAllowed(queryWord.length).let { if (inName) it else minOf(it, 1) }
    return allowed > 0 && abs(queryWord.length - word.length) <= allowed &&
        damerau.distance(queryWord, word) <= allowed
}

private fun typosAllowed(length: Int) =
    when {
        length <= 3 -> 0
        length <= 7 -> 1
        else -> 2
    }

internal fun normalize(text: String): String = stripAccents(text.lowercase())

internal expect fun stripAccents(text: String): String
