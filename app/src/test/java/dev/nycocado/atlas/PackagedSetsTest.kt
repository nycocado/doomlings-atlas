package dev.nycocado.atlas

import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// The real repository and the real packaged files, unlike the UI tests' made-up sets.
@HiltAndroidTest
class PackagedSetsTest : AtlasBehaviorTest() {
    @Test
    fun theEnabledSetsDecodeWithAName() {
        val library = runBlocking { CardRepository(context).library() }

        assertEquals(listOf("Classic"), library.setsToChoose.map { it.name })
        assertTrue(library.cards!!.all { library.setName(it) == "Classic" })
    }
}
