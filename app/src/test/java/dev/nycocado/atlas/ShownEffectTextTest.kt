package dev.nycocado.atlas

import dev.nycocado.atlas.card.CardSection
import dev.nycocado.atlas.card.EffectKind
import dev.nycocado.atlas.carddetail.shownEffectText
import org.junit.Assert.assertEquals
import org.junit.Test

class ShownEffectTextTest {
    @Test
    fun tokensAreLeftOutOfTheShownText() {
        val effect = CardSection.Effect("Ignore actions ({action}) and Shells ({shell}).")

        assertEquals("Ignore actions and Shells.", shownEffectText(effect, null, null))
    }

    @Test
    fun aLabelKindHeadsItsText() {
        val effect = CardSection.Effect("Draw 1.", EffectKind.Rush)

        assertEquals("Rush:\nDraw 1.", shownEffectText(effect, "Rush", null))
    }

    @Test
    fun aTimingKindHasNoLabelInItsText() {
        val effect = CardSection.Effect("Draw 1.", EffectKind.Action)

        assertEquals("Draw 1.", shownEffectText(effect, null, null))
    }

    @Test
    fun theValueEndsTheText() {
        val effect = CardSection.Effect("Reveal: 2 reds in hand.", EffectKind.PocketObjective)

        assertEquals(
            "Pocket Objective:\nReveal: 2 reds in hand.\n\nValue: 2",
            shownEffectText(effect, "Pocket Objective", "Value: 2"),
        )
    }
}
