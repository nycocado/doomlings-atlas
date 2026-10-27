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
