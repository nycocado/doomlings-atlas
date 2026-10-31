package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals

// Broken samples for the checks CardDataIntegrityTest and TranslationDataIntegrityTest run on the real data.
class InkMarksTest {
    @Test
    fun wellFormedSpansHaveNoProblem() {
        assertEquals(
            emptyList(),
            inkMarkProblems("Play a [red]red[/red] trait ({action}), then a [rainbow]rainbow {action} one[/rainbow]."),
        )
    }

    @Test
    fun anUnknownInkIsAProblem() {
        assertEquals(listOf("unknown ink [pink]"), inkMarkProblems("A [pink]pink[/pink] trait."))
    }

    @Test
    fun anUnclosedSpanIsAProblem() {
        assertEquals(listOf("unclosed [red]"), inkMarkProblems("A [red]red trait."))
    }

    @Test
    fun aStrayBracketIsAProblem() {
        assertEquals(listOf("stray [/red]"), inkMarkProblems("A red[/red] trait."))
        assertEquals(listOf("stray ["), inkMarkProblems("A [red trait."))
        assertEquals(listOf("stray ]"), inkMarkProblems("A red] trait."))
    }

    @Test
    fun aSpanClosedByAnotherInkIsAProblem() {
        assertEquals(listOf("[red] closed by [/blue]"), inkMarkProblems("A [red]red[/blue] trait."))
    }

    @Test
    fun aNestedSpanIsAProblem() {
        assertEquals(
            listOf("[blue] inside [red]", "stray [/red]"),
            inkMarkProblems("A [red]red [blue]blue[/blue][/red] trait."),
        )
    }

    @Test
    fun aSpanCrossingATokenIsAProblem() {
        assertEquals(listOf("span crossing {act[red]ion}"), inkMarkProblems("A {act[red]ion} trait[/red]."))
        assertEquals(listOf("span crossing {action[/red]}"), inkMarkProblems("A [red]red {action[/red]}."))
    }

    @Test
    fun inksAreTheOpeningMarksInOrder() {
        assertEquals(listOf("red", "blue", "red"), inks("[red]a[/red] [blue]b[/blue] [red]c[/red] {action}"))
    }
}
