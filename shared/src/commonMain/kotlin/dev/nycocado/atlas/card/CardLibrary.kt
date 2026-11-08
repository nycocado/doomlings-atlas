package dev.nycocado.atlas.card

class CardLibrary private constructor(
    // null while the cards load.
    val cards: List<Card>?,
    private val sets: List<CardSet> = emptyList(),
) {
    // The sets of the set filter, in release order. A set whose name isn't confirmed yet has no row to show.
    val setsToChoose: List<CardSet> by lazy { sets.inReleaseOrder() }

    // null when the set's name isn't confirmed.
    fun setName(card: Card): String? = sets.find { it.id == card.set }?.name

    fun lookup(id: String): CardLookup =
        when (cards) {
            null -> CardLookup.Loading
            else -> cards.find { it.id == id }?.let(CardLookup::Found) ?: CardLookup.Missing
        }

    // Copies of a card share its name, and so do some cards of different sets: the variants of a name, in data order.
    private val variantsByName: Map<String, List<NamedCard>> by lazy {
        cards?.filterIsInstance<NamedCard>()?.groupBy { it.name }.orEmpty()
    }

    internal fun variants(name: String): List<NamedCard> = variantsByName[name].orEmpty()

    // One alternation, longest names first so a name wins over a shorter one inside it. A name is a whole word, with its
    // exact capitals, alone or with a plural "s".
    private val citedNames: Regex? by lazy {
        variantsByName.keys
            .sortedByDescending { it.length }
            .takeIf { it.isNotEmpty() }
            ?.joinToString("|", "(?<!\\p{L})(?:", ")s?(?!\\p{L})") { Regex.escape(it) }
            ?.toRegex()
    }

    // The card names cited in a text of the card, in any language, empty while the cards load. A name its variants share
    // cites the first of them. A card does not cite itself, nor a copy of itself.
    fun citations(
        card: Card,
        text: String,
    ): List<Citation> =
        citedNames
            ?.findAll(text)
            ?.mapNotNull { match ->
                val variants = variantsByName[match.value] ?: variantsByName.getValue(match.value.dropLast(1))
                variants.first().takeIf { it.name != (card as? NamedCard)?.name }?.let { Citation(match.range, it) }
            }?.toList()
            .orEmpty()

    companion object {
        val loading = CardLibrary(null)

        fun decode(
            json: String,
            setsJson: String = "[]",
        ) = CardLibrary(decodeCards(json), decodeSets(setsJson))

        internal fun of(cards: List<Card>) = CardLibrary(cards)
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
    val card: NamedCard,
)
