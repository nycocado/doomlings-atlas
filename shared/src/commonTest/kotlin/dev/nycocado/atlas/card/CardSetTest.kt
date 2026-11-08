package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class CardSetTest {
    private val trait =
        Trait("a/t", "a", "t.webp", "T", null, emptyList(), null, 1, emptyList(), false, emptyList(), emptyList())

    private fun libraryOf(vararg sets: String) = CardLibrary.decode("[]", sets.joinToString(",", "[", "]"))

    private fun set(
        id: String,
        name: String?,
        year: Int?,
    ) = """{"id": "$id", "name": ${name?.let { "\"$it\"" }}, "year": $year}"""

    @Test
    fun setsSortByYearThenNameWithSpecialLast() {
        val library =
            libraryOf(
                set("special", "Special Edition", 2023),
                set("zeta", "Zeta", 2025),
                set("alpha", "Alpha", 2025),
                set("old", "Old", 2023),
                set("first", "First", null),
            )
        assertEquals(listOf("first", "old", "alpha", "zeta", "special"), library.setsToChoose.map { it.id })
    }

    @Test
    fun aSetWithoutAConfirmedNameIsNotOffered() {
        assertEquals(listOf("b"), libraryOf(set("a", null, 2023), set("b", "B", 2023)).setsToChoose.map { it.id })
    }

    @Test
    fun aCardGivesItsSetsPrintedNameWhenItHasOne() {
        assertEquals("Named", libraryOf(set("a", "Named", 2023)).setName(trait))
        assertNull(libraryOf(set("a", null, 2023)).setName(trait))
        assertNull(libraryOf().setName(trait))
    }

    @Test
    fun aSetMustSayItsNameAndYearEvenIfNull() {
        assertFailsWith<Exception> { libraryOf("""{"id": "a", "year": 2023}""") }
    }
}
