package dev.nycocado.atlas

import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.CardText
import dev.nycocado.atlas.card.CardTranslations
import dev.nycocado.atlas.carddetail.ArdentGrip
import dev.nycocado.atlas.carddetail.CardDetailContent
import dev.nycocado.atlas.ui.theme.AtlasTheme
import org.junit.Test

// TestCardRepositoryModule keeps only Classic, which has no Treasure, so its card detail is drawn in place of the app's
// screen, with the Portuguese text of assets/doomlings/legends-of-enderas/pt.json.
@HiltAndroidTest
class TreasureStoryTest : AtlasBehaviorTest() {
    private val translations =
        CardTranslations(
            mapOf(
                CardLanguage.Portuguese to
                    mapOf(
                        ArdentGrip.id to
                            CardText(
                                name = "Luva Ardente",
                                effects = listOf("Defina seu Gene Pool como 5.", "Compre 1 carta."),
                                story =
                                    listOf(
                                        "Minha palma já suava antes mesmo de eu calçar a luva. Testei o seu poder, " +
                                            "conjurando joias e barras de ouro. A cada joia invocada, a luva " +
                                            "esquentava mais, mas ignorei a dor em busca da fortuna.",
                                        "\"Vou ficar rico e eles vão passar vergonha!\", pensei.",
                                        "Quando não consegui invocar mais nada, tirei a luva. Uma pilha de fuligem " +
                                            "preta caiu lá de dentro. Fiquei horrorizado ao descobrir que não havia " +
                                            "nada depois do pulso.",
                                        "Fiz uma pausa, pensei nas minhas opções e então enfiei a luva na outra mão.",
                                    ),
                            ),
                    ),
            ),
        )

    private val expandedToggle
        get() =
            compose.onNode(
                hasText(string(R.string.story_less)) and
                    SemanticsMatcher.expectValue(
                        SemanticsProperties.StateDescription,
                        string(R.string.story_expanded),
                    ),
            )

    @Test
    fun anOpenStoryStaysOpenInAnotherLanguageAndIsAccessible() {
        launchPastNotice().onActivity {
            it.setContent {
                var language by remember { mutableStateOf(CardLanguage.English) }
                AtlasTheme {
                    CardDetailContent(
                        ArdentGrip,
                        tilt = -3f,
                        onBack = {},
                        onAboutClick = {},
                        translations = translations,
                        languages = translations.languages(ArdentGrip),
                        language = language,
                        onLanguageChange = { language = it },
                    )
                }
            }
        }

        text(R.string.story_more).performClick()
        expandedToggle.assertExists()

        compose.onNode(isSelectable() and hasText("Português")).performClick()
        expandedToggle.assertExists()
        assertAccessible()
    }
}
