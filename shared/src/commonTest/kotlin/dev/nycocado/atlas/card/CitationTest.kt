package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

class CitationTest {
    private fun trait(
        name: String,
        effect: String?,
    ) = """{"id": "classic/traits/${name.lowercase()}", "type": "trait", "set": "classic", "name": "$name",
        "flavor": null, "effects": [${effect?.let {
        "{\"text\": \"$it\"}"
    }.orEmpty()}], "genePool": null, "points": 1, "colors": [], "dominant": false,
        "subspecies": [], "icons": [], "image": "x.webp"}"""

    private val library =
        CardLibrary.decode(
            "[" +
                listOf(
                    trait("Kidney", "Draw a card."),
                    trait("Late", null),
                    trait("Vampirism", null),
                    trait("Blood", null),
                    trait("Blood Drive", null),
                    trait("Eater", null),
                    trait("Hunter", "Discard a [red]Kidney[/red]."),
                ).joinToString() +
                "]",
        )

    private fun card(name: String) = (library.lookup("classic/traits/${name.lowercase()}") as CardLookup.Found).card

    private fun citations(
        name: String,
        text: String,
    ) = library.citations(card(name), text).map { text.substring(it.range) to it.card.id }

    @Test
    fun citesAnotherCardAtItsPlaceInTheText() {
        val citation = library.citations(card("Eater"), "Discard Kidney now.").single()

        assertEquals(8..13, citation.range)
        assertEquals("classic/traits/kidney", citation.card.id)
    }

    @Test
    fun selfCitationGivesNone() {
        assertEquals(emptyList(), citations("Kidney", "Kidney is nice."))
    }

    @Test
    fun selfCitationInThePluralGivesNone() {
        assertEquals(emptyList(), citations("Kidney", "Kidneys are nice."))
    }

    @Test
    fun pluralOfAnotherCardIsCitedWithItsS() {
        assertEquals(listOf("Kidneys" to "classic/traits/kidney"), citations("Eater", "Take Kidneys."))
    }

    @Test
    fun lowercaseCommonWordGivesNone() {
        assertEquals(emptyList(), citations("Eater", "Do not be too late."))
    }

    @Test
    fun nameInsideALongerWordGivesNone() {
        assertEquals(emptyList(), citations("Eater", "Vampirismo e Kidneyx."))
    }

    @Test
    fun cardOfASetThatIsNotPackagedGivesNone() {
        assertEquals(emptyList(), citations("Eater", "Take Dragonfly."))
    }

    @Test
    fun longestNameWinsWhereNamesOverlap() {
        assertEquals(
            listOf("Blood Drive" to "classic/traits/blood drive", "Blood" to "classic/traits/blood"),
            citations("Eater", "Blood Drive and Blood."),
        )
    }

    @Test
    fun citationsComeInTextOrder() {
        assertEquals(
            listOf("Vampirism", "Kidney").map { it to "classic/traits/${it.lowercase()}" },
            citations("Eater", "Vampirism then Kidney."),
        )
    }

    @Test
    fun citesInAnyText() {
        assertEquals(listOf("Kidney" to "classic/traits/kidney"), citations("Eater", "Descarte Kidney."))
    }

    // The card detail cites in the words of the card face, which have no ink marks.
    @Test
    fun citesANameInAnInkedSpan() {
        val hunter = card("Hunter") as NamedCard
        val words =
            hunter
                .face()
                ?.blocks
                ?.mapNotNull { it.content as? BlockContent.Effect }
                ?.flatMap { it.text }
                ?.filterIsInstance<TextPart.Words>()
                .orEmpty()

        assertEquals(
            listOf("Kidney" to "classic/traits/kidney"),
            words.flatMap { part ->
                library.citations(hunter, part.text).map {
                    part.text.substring(it.range) to
                        it.card.id
                }
            },
        )
    }

    @Test
    fun noCitationsWhileLoading() {
        assertEquals(emptyList(), CardLibrary.loading.citations(card("Eater"), "Kidney"))
    }
}
