package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

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

    @Test
    fun aSmallSpanMayHoldInksAndTokens() {
        assertEquals(emptyList(), inkMarkProblems("Play 1. [small]Ignore [red]red[/red] actions ({action}).[/small]"))
    }

    @Test
    fun anUnclosedSmallSpanIsAProblem() {
        assertEquals(listOf("unclosed [small]"), inkMarkProblems("Play 1. [small](No dominants.)"))
    }

    @Test
    fun aStraySmallCloseIsAProblem() {
        assertEquals(listOf("stray [/small]"), inkMarkProblems("Play 1. (No dominants.)[/small]"))
    }

    @Test
    fun aSmallSpanInsideASmallSpanIsAProblem() {
        assertEquals(
            listOf("[small] inside [small]", "stray [/small]"),
            inkMarkProblems("[small]a [small]b[/small][/small]"),
        )
    }

    @Test
    fun aSmallMarkInsideAnInkSpanIsAProblem() {
        assertEquals(listOf("[small] inside [red]"), inkMarkProblems("[red]a [small]b[/red] c[/small]"))
        assertEquals(listOf("[/small] inside [red]"), inkMarkProblems("[small]a [red]b[/small] c[/red]"))
    }

    @Test
    fun smallIsNotAnInk() {
        assertEquals(listOf("red"), inks("[small]a [red]b[/red][/small]"))
    }

    @Test
    fun smallSpansAreTheirPlacesAndMarksInOrder() {
        assertEquals(
            listOf(SmallSpan(0, false, false, listOf("[red]", "{action}")), SmallSpan(1, true, true, emptyList())),
            smallSpans("[blue]x[/blue] [small]a [red]b[/red] {action}[/small] c\n[small]d[/small]"),
        )
    }

    @Test
    fun aSmallSpanMovedToOtherWordsChangesItsSmallSpans() {
        assertNotEquals(
            smallSpans("Jogue 1 e descarte este. [small]Ignore ações ({action}).[/small]"),
            smallSpans("[small]Jogue 1 e descarte este.[/small] Ignore ações ({action})."),
        )
    }
}
