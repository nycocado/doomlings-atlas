package dev.nycocado.atlas.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class SignedTest {
    @Test
    fun negativeUsesTheTypographicMinus() {
        assertEquals("−1", signed(-1))
        assertEquals("−3", signed(-3))
    }

    @Test
    fun zeroAndPositivesAreWrittenAsTheyAre() {
        assertEquals("0", signed(0))
        assertEquals("1", signed(1))
        assertEquals("12", signed(12))
    }
}
