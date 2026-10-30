package dev.nycocado.atlas.card

class CardLibrary private constructor(
    // null while the cards load.
    val cards: List<Card>?,
) {
    fun lookup(id: String): CardLookup =
        when (cards) {
            null -> CardLookup.Loading
            else -> cards.find { it.id == id }?.let(CardLookup::Found) ?: CardLookup.Missing
        }

    // Copies of a card share its name, and so do some cards of different sets: the first one in the data is cited.
    private val idsByName: Map<String, String> by lazy {
        cards
            ?.filterIsInstance<NamedCard>()
            ?.distinctBy { it.name }
            ?.associate { it.name to it.id }
            .orEmpty()
    }

    // One alternation, longest names first so a name wins over a shorter one inside it. A name is a whole word, with its
    // exact capitals, alone or with a plural "s".
    private val citedNames: Regex? by lazy {
        idsByName.keys
            .sortedByDescending { it.length }
            .takeIf { it.isNotEmpty() }
            ?.joinToString("|", "(?<!\\p{L})(?:", ")s?(?!\\p{L})") { Regex.escape(it) }
            ?.toRegex()
    }

    // The card names cited in a text of the card, in any language, empty while the cards load. A card does not cite
    // itself, nor a copy of itself.
    fun citations(
        card: Card,
        text: String,
    ): List<Citation> =
        citedNames
            ?.findAll(text)
            ?.mapNotNull { match ->
                val name = match.value.takeIf { it in idsByName } ?: match.value.dropLast(1)
                name.takeIf { it != (card as? NamedCard)?.name }?.let { Citation(match.range, idsByName.getValue(it)) }
            }?.toList()
            .orEmpty()

    companion object {
        val loading = CardLibrary(null)

        fun decode(json: String) = CardLibrary(decodeCards(json))
    }
}

sealed interface CardLookup {
    data class Found(
        val card: Card,
    ) : CardLookup

    // The data has no card with that id, e.g. one named by a back stack restored after an update.
    data object Missing : CardLookup

    data object Loading : CardLookup
}

data class Citation(
    val range: IntRange,
    val cardId: String,
)
