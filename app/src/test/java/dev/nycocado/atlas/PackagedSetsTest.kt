package dev.nycocado.atlas

import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardRepository
import dev.nycocado.atlas.card.NamedCard
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

        assertEquals(
            listOf(
                "Classic",
                "Overlush",
                "Upgrade",
                "Imaginary Ends",
                "Castle Glass",
                "Legends of Enderas",
                "Mokoko Village",
                "Nightflower Island",
                "Shadow Puppets",
                "Special Edition",
            ),
            library.setsToChoose.map { it.name },
        )
        assertTrue(library.cards!!.all { library.setName(it) != null })
    }

    @Test
    fun everyNamedCardHasItsPackagedPortugueseAndSpanish() {
        val repository = CardRepository(context)
        val library = runBlocking { repository.library() }
        val translations = runBlocking { repository.translations() }
        val named = library.cards!!.filterIsInstance<NamedCard>()

        assertEquals(896, named.size)
        for (language in listOf(CardLanguage.Portuguese, CardLanguage.Spanish)) {
            assertEquals(
                emptyList<String>(),
                named.filter { translations.translatedFace(it, language) == null }.map { it.id },
            )
        }
    }

    @Test
    fun everyCardOfEverySetIsPackaged() {
        val library = runBlocking { CardRepository(context).library() }

        assertEquals(902, library.cards!!.size)
    }
}
