package dev.nycocado.atlas
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config

// Card names come from assets/doomlings/cards.json, the Portuguese from testPortuguese. Classic's green traits run
// Adorable, Altruistic, Antlers, Apex Predator, Appealing.
@HiltAndroidTest
class CardLanguageTest : AtlasBehaviorTest() {
    private val portuguese get() = compose.onNode(isSelectable() and hasText("Português"))

    @Test
    fun theChoiceOfOneCardOpensTheNextTranslatedCardInIt() {
        launchPastNotice()
        card("Adorable").performClick()
        detailOf("Adorable").assertIsDisplayed()
        compose.onNodeWithText("Esfregue minha barriga.").assertDoesNotExist()

        portuguese.performClick()

        portuguese.assertIsSelected()
        detailOf("Adorável").assertIsDisplayed()
        detailOf("Esfregue minha barriga.").assertIsDisplayed()
        button(R.string.back).performClick()
        card("Altruistic").performClick()

        detailOf("Altruistic").assertIsDisplayed()
        detailOf("Altruísta").assertIsDisplayed()
        detailOf("Zumbido do tamanho da Pool.").assertIsDisplayed()
        portuguese.assertIsSelected()
    }

    @Test
    fun aCardWithoutThePortugueseShowsItsPrintedTextAndTheChoiceStays() {
        launchPastNotice()
        card("Adorable").performClick()
        portuguese.performClick()
        button(R.string.back).performClick()

        scrollListTo(cardNamed("Appealing"))
        card("Appealing").performClick()

        detailOf("Orange you glad?").assertIsDisplayed()
        portuguese.assertDoesNotExist()
        button(R.string.back).performClick()
        card("Antlers").performClick()
        detailOf("Chifres").assertIsDisplayed()
        portuguese.assertIsSelected()
    }

    @Test
    fun aCardWithOnlyItsPrintedTextHasNoSelector() {
        launchPastNotice()

        scrollListTo(cardNamed("Appealing"))
        card("Appealing").performClick()

        detailOf("Appealing").assertIsDisplayed()
        button(R.string.card_language).assertDoesNotExist()
    }

    @Test
    fun englishBringsTheCardBackToItsPrintedText() {
        launchPastNotice()
        card("Adorable").performClick()
        portuguese.performClick()

        compose.onNode(isSelectable() and hasText("English")).performClick()

        detailOf("Adorable").assertIsDisplayed()
        compose.onNodeWithText("Adorável").assertDoesNotExist()
        compose.onNodeWithText("Esfregue minha barriga.").assertDoesNotExist()
    }

    @Test
    fun aCatastropheShowsItsTranslatedEffectAndWorldsEnd() {
        launchPastNotice()
        compose.onNode(isSelectable() and hasText(string(R.string.kind_catastrophes))).performClick()
        scrollListTo(hasContentDescription("Ice Age"))
        compose.onNode(hasContentDescription("Ice Age")).performClick()

        portuguese.performClick()

        detailOf("Era Glacial").assertIsDisplayed()
        detailOf("Descarte cartas da mão.").assertIsDisplayed()
        detailOf("Menos pontos vermelhos.").assertIsDisplayed()
    }

    @Test
    fun theLabelsBesideTheIconsFollowTheCardLanguage() {
        launchPastNotice()
        scrollListTo(cardNamed("Eloquence"))
        card("Eloquence").performClick()
        detailOf(string(R.string.worlds_end)).assertIsDisplayed()

        portuguese.performClick()

        detailOf("Fim do Mundo").assertIsDisplayed()
        detailOf(string(R.string.worlds_end)).assertDoesNotExist()
        assertEquals(listOf("pt"), languagesOf("Fim do Mundo"))
        button(R.string.back).performClick()
        scrollListTo(cardNamed("Altruistic"))
        card("Altruistic").performClick()

        detailOf("Gota de Vida").assertIsDisplayed()
        detailOf(string(R.string.icon_drop_of_life)).assertDoesNotExist()
        assertEquals(listOf("pt"), languagesOf("Gota de Vida"))
        button(R.string.back).performClick()
        compose.onNode(isSelectable() and hasText(string(R.string.kind_catastrophes))).performClick()
        scrollListTo(hasContentDescription("Ice Age"))
        compose.onNode(hasContentDescription("Ice Age")).performClick()

        detailOf("FIM DO MUNDO").assertIsDisplayed()
        detailOf(string(R.string.worlds_end).uppercase()).assertDoesNotExist()
    }

    @Test
    fun theDominantPillFollowsTheCardLanguage() {
        launchPastNotice()
        card("Apex Predator").performClick()
        detailOf(string(R.string.dominant)).assertIsDisplayed()
        assertEquals(listOf("en"), languagesOf("Dominant"))

        portuguese.performClick()

        detailOf("Dominante").assertIsDisplayed()
        detailOf("Dominant").assertDoesNotExist()
        assertEquals(listOf("pt"), languagesOf("Dominante"))
    }

    @Test
    fun englishBringsTheLabelsBesideTheIconsBackToTheirPrintedWords() {
        launchPastNotice()
        scrollListTo(cardNamed("Eloquence"))
        card("Eloquence").performClick()
        portuguese.performClick()
        detailOf("Fim do Mundo").assertIsDisplayed()

        compose.onNode(isSelectable() and hasText("English")).performClick()

        detailOf("World's End").assertIsDisplayed()
        detailOf("Fim do Mundo").assertDoesNotExist()
        assertEquals(listOf("en"), languagesOf("World's End"))
    }

    @Test
    fun theChoiceSurvivesRecreatingTheActivity() {
        val scenario = launchPastNotice()
        card("Adorable").performClick()
        portuguese.performClick()

        scenario.recreate()

        portuguese.assertIsSelected()
        detailOf("Adorável").assertIsDisplayed()
    }

    @Test
    fun theChoiceSurvivesARelaunch() {
        val first = launchPastNotice()
        card("Adorable").performClick()
        portuguese.performClick()
        first.close()

        launch()
        card("Altruistic").performClick()

        detailOf("Altruísta").assertIsDisplayed()
        portuguese.assertIsSelected()
    }

    @Test
    fun withPortugueseChosenTheSearchFindsACardByATranslatedWord() {
        launchPastNotice()
        search("Zumbido")
        card("Altruistic").assertDoesNotExist()
        clearSearch()
        card("Adorable").performClick()
        portuguese.performClick()
        button(R.string.back).performClick()

        search("Zumbido")

        card("Altruistic").assertIsDisplayed()
        card("Adorable").assertDoesNotExist()
    }

    @Test
    fun theTextIsTaggedWithItsLanguage() {
        launchPastNotice()
        card("Adorable").performClick()
        assertEquals(listOf("en"), languagesOf("Adorable"))

        portuguese.performClick()

        assertEquals(listOf("pt"), languagesOf("Esfregue minha barriga."))
        assertEquals(listOf("pt"), languagesOf("Adorável"))
        assertEquals(listOf("en"), languagesOf("Adorable"))
    }

    @Test
    fun eachOptionReadsItsEndonymAloneInItsLanguage() {
        launchPastNotice()
        card("Adorable").performClick()

        // The codes EN / PT show on screen (see the screenshots); TalkBack must not read them beside the endonym.
        assertEquals(listOf("pt"), languagesOf("Português"))
        assertEquals(
            listOf("Português"),
            portuguese.fetchSemanticsNode().config[SemanticsProperties.Text].map { it.text },
        )
    }

    @Test
    fun theSelectorHangsOnTheTopEdgeOfTheFirstBubble() {
        launchPastNotice()
        card("Adorable").performClick()
        portuguese.performClick()

        val group = button(R.string.card_language).getUnclippedBoundsInRoot()
        val bubbleTop = detailOf("Esfregue minha barriga.").getUnclippedBoundsInRoot().top - 18.dp

        assertTrue(group.top < bubbleTop && group.bottom > bubbleTop)
    }

    @Test
    fun aCatastropheHasOneSelectorOnItsEffectBubble() {
        launchPastNotice()
        compose.onNode(isSelectable() and hasText(string(R.string.kind_catastrophes))).performClick()
        scrollListTo(hasContentDescription("Ice Age"))
        compose.onNode(hasContentDescription("Ice Age")).performClick()
        portuguese.performClick()

        compose.onAllNodesWithContentDescription(string(R.string.card_language)).assertCountEquals(1)
        val group = button(R.string.card_language).getUnclippedBoundsInRoot()
        val worldsEnd = detailOf("Menos pontos vermelhos.").getUnclippedBoundsInRoot()
        assertTrue(group.bottom < worldsEnd.top)
    }

    @Test
    fun theSelectorPassesTheAccessibilitySweep() {
        launchPastNotice()
        card("Adorable").performClick()
        portuguese.assertIsDisplayed()

        assertAccessible()
    }

    private fun search(query: String) {
        compose.onNode(hasSetTextAction()).performTextInput(query)
        compose.waitForIdle()
    }

    private fun clearSearch() {
        compose.onNode(hasSetTextAction()).performTextReplacement("")
        compose.waitForIdle()
    }

    private fun languagesOf(text: String): List<String> =
        compose
            .onNodeWithText(text)
            .fetchSemanticsNode()
            .config
            .getOrNull(SemanticsProperties.Text)
            .orEmpty()
            .flatMap { it.spanStyles }
            .mapNotNull {
                it.item.localeList
                    ?.firstOrNull()
                    ?.language
            }.distinct()
}

// The labels beside the icons follow the card language, not the interface's: a Portuguese device opens in Português,
// and English brings the printed words back whatever the interface says.
@HiltAndroidTest
@Config(qualifiers = "pt-w411dp-h1200dp-xhdpi")
class CardLanguagePortugueseInterfaceTest : AtlasBehaviorTest() {
    @Test
    fun theLabelsBesideTheIconsStayInTheCardLanguageNotTheInterfaceOnes() {
        launchPastNotice()
        scrollListTo(cardNamed("Eloquence"))
        card("Eloquence").performClick()

        detailOf("Fim do Mundo").assertIsDisplayed()
        detailOf("World's End").assertDoesNotExist()

        compose.onNode(isSelectable() and hasText("English")).performClick()

        detailOf("World's End").assertIsDisplayed()
        detailOf("Fim do Mundo").assertDoesNotExist()

        compose.onNode(isSelectable() and hasText("Português")).performClick()

        detailOf("Fim do Mundo").assertIsDisplayed()
        detailOf("World's End").assertDoesNotExist()
    }
}

@HiltAndroidTest
@Config(qualifiers = "w1280dp-h800dp-xhdpi")
class CardLanguageWideTest : AtlasBehaviorTest() {
    @Test
    fun theSelectorBesideTheListPassesTheAccessibilitySweep() {
        launchPastNotice()
        card("Adorable").performClick()

        compose.onNode(isSelectable() and hasText("Português")).assertIsDisplayed()
        assertAccessible()
    }
}
