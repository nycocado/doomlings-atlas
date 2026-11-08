package dev.nycocado.atlas

import androidx.activity.compose.setContent
import androidx.compose.ui.test.hasText
import dagger.hilt.android.testing.HiltAndroidTest
import dev.nycocado.atlas.carddetail.CardDetailContent
import dev.nycocado.atlas.carddetail.FishCollector
import dev.nycocado.atlas.ui.theme.AtlasTheme
import org.junit.Test

// TestCardRepositoryModule keeps only Classic, which has no dual-color trait, so its card detail is drawn in place of
// the app's screen.
@HiltAndroidTest
class DualColorDetailTest : AtlasBehaviorTest() {
    @Test
    fun aDualColorTraitNamesBothColorsAndIsAccessible() {
        launchPastNotice().onActivity {
            it.setContent {
                AtlasTheme { CardDetailContent(FishCollector, tilt = -3f, onBack = {}, onAboutClick = {}) }
            }
        }

        compose.onNode(hasText(string(R.string.color_blue))).assertExists()
        compose.onNode(hasText(string(R.string.color_purple))).assertExists()
        assertAccessible()
    }
}
