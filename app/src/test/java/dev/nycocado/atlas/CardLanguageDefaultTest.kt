package dev.nycocado.atlas

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertFalse
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private const val ADORABLE_FLAVOR =
    "Hey, you look like a sucker. Rub my belly. Now, get in this lava so I can float to safety."

// Until the player picks a card language it is the language Android gives the app, when it is a card language. The
// test translations are in testPortuguese and testSpanish.
@HiltAndroidTest
@Config(qualifiers = "pt-w411dp-h891dp-xxhdpi")
class CardLanguageDefaultPortugueseTest : AtlasBehaviorTest() {
    private val option get() = compose.onNode(isSelectable() and hasText("Português"))

    @Test
    fun aTranslatedCardOpensInPortugueseWithNothingStored() {
        launchPastNotice()
        card("Adorable").performClick()

        option.assertIsSelected()
        detailOf("Adorável").assertIsDisplayed()
        detailOf("Esfregue minha barriga.").assertIsDisplayed()
    }

    @Test
    fun theSearchMatchesATranslatedWord() {
        launchPastNotice()

        compose.onNode(hasSetTextAction()).performTextInput("Zumbido")
        compose.waitForIdle()

        card("Altruistic").assertIsDisplayed()
        card("Adorable").assertDoesNotExist()
    }

    @Test
    fun aCardWithoutPortugueseShowsItsPrintedText() {
        launchPastNotice()
        scrollListTo(cardNamed("Appealing"))
        card("Appealing").performClick()

        detailOf("Orange you glad?").assertIsDisplayed()
        option.assertDoesNotExist()
    }

    @Test
    fun pickingEnglishSurvivesARelaunch() {
        val first = launchPastNotice()
        card("Adorable").performClick()
        compose.onNode(isSelectable() and hasText("English")).performClick()
        first.close()

        launch()
        card("Altruistic").performClick()

        detailOf("Altruísta").assertDoesNotExist()
        detailOf("Altruistic").assertIsDisplayed()
        compose.onNode(isSelectable() and hasText("English")).assertIsSelected()
    }

    @Test
    fun nothingIsStoredUntilThePlayerPicks() {
        launchPastNotice()
        card("Adorable").performClick()

        assertFalse(context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE).contains(CARD_LANGUAGE))
    }
}

@HiltAndroidTest
@Config(qualifiers = "es-w411dp-h891dp-xxhdpi")
class CardLanguageDefaultSpanishTest : AtlasBehaviorTest() {
    @Test
    fun aTranslatedCardOpensInSpanish() {
        launchPastNotice()
        card("Altruistic").performClick()

        compose.onNode(isSelectable() and hasText("Español")).assertIsSelected()
        detailOf("Zumbido del tamaño de la Pool.").assertIsDisplayed()
    }
}

@HiltAndroidTest
@Config(qualifiers = "fr-w411dp-h891dp-xxhdpi")
class CardLanguageDefaultFrenchTest : AtlasBehaviorTest() {
    @Test
    fun aLanguageThatIsNotACardLanguageOpensPrinted() {
        launchPastNotice()
        card("Altruistic").performClick()

        compose.onNode(isSelectable() and hasText("English")).assertIsSelected()
        detailOf("Value is equal to the size of your Gene Pool.").assertIsDisplayed()
    }
}

// The device language can change while the app is open: the activity is recreated and keeps its ViewModel.
@HiltAndroidTest
class CardLanguageDeviceChangeTest : AtlasBehaviorTest() {
    @Test
    fun theDefaultFollowsTheDeviceLanguageChangedWhileTheAppIsOpen() {
        val scenario = launchPastNotice()

        RuntimeEnvironment.setQualifiers("+pt")
        scenario.recreate()
        card("Adorable").performClick()

        compose.onNode(isSelectable() and hasText("Português")).assertIsSelected()
        detailOf("Adorável").assertIsDisplayed()
    }

    @Test
    fun aStoredPickWinsOverTheDeviceLanguageChange() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()
        compose.onNode(isSelectable() and hasText("English")).performClick()
        button(R.string.back).performClick()

        RuntimeEnvironment.setQualifiers("+pt")
        scenario.recreate()
        card("Adorable").performClick()

        compose.onNode(isSelectable() and hasText("English")).assertIsSelected()
    }
}

// The compose clock is paused, so the frames between the two texts can be looked at.
@HiltAndroidTest
class CardLanguageAnimationTest : AtlasBehaviorTest() {
    @Test
    fun theTextCrossfadesFromThePrintedToTheTranslation() {
        launchPastNotice()
        card("Adorable").performClick()
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false

        compose.onNode(isSelectable() and hasText("Português")).performClick()
        compose.mainClock.advanceTimeBy(100)

        compose.onNodeWithText(ADORABLE_FLAVOR).assertExists()
        compose.onNodeWithText("Esfregue minha barriga.").assertExists()

        compose.mainClock.advanceTimeBy(1_000)

        compose.onNodeWithText(ADORABLE_FLAVOR).assertDoesNotExist()
        compose.onNodeWithText("Esfregue minha barriga.").assertExists()
    }

    @Test
    fun aDifferentCardOpensWithoutAnimation() {
        launchPastNotice()
        card("Adorable").performClick()
        compose.onNode(isSelectable() and hasText("Português")).performClick()
        compose.waitForIdle()
        button(R.string.back).performClick()
        compose.waitForIdle()
        compose.mainClock.autoAdvance = false

        card("Altruistic").performClick()
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.advanceTimeByFrame()

        compose.onNodeWithText("Zumbido do tamanho da Pool.").assertExists()
        compose.onNodeWithText("Value is equal to the size of your Gene Pool.").assertDoesNotExist()
    }
}
