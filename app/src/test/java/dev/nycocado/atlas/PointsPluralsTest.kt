package dev.nycocado.atlas

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.nycocado.atlas.ui.component.pointsText
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

// The points of a card as the string resources write them. Expected texts are written out, not built from the code.
@RunWith(RobolectricTestRunner::class)
class PointsPluralsTest {
    private fun points(
        language: String,
        value: Int,
    ): String {
        RuntimeEnvironment.setQualifiers("+$language")
        val context = ApplicationProvider.getApplicationContext<Context>()
        return context.resources.pointsText(value)
    }

    @Test
    fun englishPoints() {
        assertEquals("1 point", points("en", 1))
        assertEquals("2 points", points("en", 2))
        assertEquals("−1 point", points("en", -1))
        assertEquals("0 points", points("en", 0))
    }

    @Test
    fun portuguesePoints() {
        assertEquals("1 ponto", points("pt", 1))
        assertEquals("2 pontos", points("pt", 2))
        assertEquals("−1 ponto", points("pt", -1))
        assertEquals("0 pontos", points("pt", 0))
    }
}
