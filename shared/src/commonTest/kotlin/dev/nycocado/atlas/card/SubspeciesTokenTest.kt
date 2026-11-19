package dev.nycocado.atlas.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SubspeciesTokenTest {
    @Test
    fun eachSubspeciesNameGivesItsToken() {
        val tokens =
            listOf("Dinoling", "Mythling", "Techling", "Deepling", "Moonling", "Glitterling")
                .map { CardFact.Subspecies(it).token }
        assertEquals(
            listOf(Token.Dinoling, Token.Mythling, Token.Techling, Token.Deepling, Token.Moonling, Token.Glitterling),
            tokens,
        )
    }

    @Test
    fun anUnknownNameOrAnotherTokenGivesNone() {
        assertNull(CardFact.Subspecies("Sample-ling").token)
        assertNull(CardFact.Subspecies("Action").token)
    }
}
