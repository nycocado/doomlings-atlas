package dev.nycocado.atlas.compendium

import ca.solostudios.stringsimilarity.DamerauLevenshtein
import dev.nycocado.atlas.card.Card
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.card.NamedCard
import dev.nycocado.atlas.card.Trait
import dev.nycocado.atlas.card.face
import dev.nycocado.atlas.card.kind
import kotlinx.serialization.Serializable
import kotlin.math.abs

@Serializable
data class CompendiumFilter(
    val kind: CardKind = CardKind.Trait,
    val query: String = "",
    // Traits only. null stands for colorless; empty shows every color.
    val colors: Set<CardColor?> = emptySet(),
    // Set ids, for every kind; empty shows every set.
    val sets: Set<String> = emptySet(),
)

// Every query word must match somewhere in the card (name, effect, World's End), all in the printed text or all in the
// translation in the card language. The list runs from exact to prefix to typo matches, then by printed name.
fun List<Card>.filterCompendium(
    filter: CompendiumFilter,
    translations: CardTranslations = CardTranslations(),
    language: CardLanguage = CardLanguage.English,
): List<NamedCard> {
    val query = normalize(filter.query.trim())
    val substring by lazy { substringRegex(query) }
    return filterIsInstance<NamedCard>()
        .filter { card ->
            card.kind == filter.kind &&
                (filter.sets.isEmpty() || card.set in filter.sets) &&
                (
                    filter.colors.isEmpty() || card !is Trait ||
                        card.colorsOrColorless.any { it in filter.colors }
                )
        }.mapNotNull { card ->
            val rank =
                if (query.isEmpty()) {
                    MatchRank.Exact
                } else {
                    listOf(card.face(), translations.translatedFace(card, language))
                        .mapNotNull { face -> face?.searchable?.let { matchRank(query, substring, it) } }
                        .minOrNull()
                }
            rank?.let { it to card }
        }.sortedWith(compareBy<Pair<MatchRank, NamedCard>>({ it.first }, { normalize(it.second.name) }))
        .map { it.second }
}

private enum class MatchRank { Exact, Prefix, Typo }

private val wordRegex = Regex("[\\p{L}\\p{N}]+")

private val damerau = DamerauLevenshtein()

private fun words(text: String) = wordRegex.findAll(text).map { it.value }.toList()

// searchable[0] is the name.
private fun matchRank(
    query: String,
    substring: Regex,
    searchable: List<String>,
): MatchRank? {
    val texts = searchable.map(::normalize)
    if (texts.any { substring.containsMatchIn(it) }) return MatchRank.Exact
    val queryWords = words(query).ifEmpty { return null }
    val cardWords = texts.flatMapIndexed { index, text -> words(text).map { it to (index == 0) } }
    return when {
        queryWords.all { q -> cardWords.any { (word, _) -> startsWith(q, word) } } -> MatchRank.Prefix

        queryWords.all { q ->
            cardWords.any { (word, inName) -> startsWith(q, word) || (!q.hasDigit() && withinTypos(q, word, inName)) }
        } -> MatchRank.Typo

        else -> null
    }
}

// A number at either end of the query doesn't match inside a longer number.
private fun substringRegex(query: String) =
    Regex(
        (if (query.first().isDigit()) "(?<!\\d)" else "") + Regex.escape(query) +
            (if (query.last().isDigit()) "(?!\\d)" else ""),
    )

private fun String.hasDigit() = any(Char::isDigit)

// Numbers are exact: "1" must not find "12".
private fun startsWith(
    queryWord: String,
    word: String,
) = if (queryWord.hasDigit()) queryWord == word else word.startsWith(queryWord)

private fun withinTypos(
    queryWord: String,
    word: String,
    inName: Boolean,
): Boolean {
    val allowed = typosAllowed(minOf(queryWord.length, word.length)).let { if (inName) it else minOf(it, 1) }
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
