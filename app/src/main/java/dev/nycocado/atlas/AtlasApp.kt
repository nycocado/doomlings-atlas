package dev.nycocado.atlas

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import dev.nycocado.atlas.about.AboutScreen
import dev.nycocado.atlas.about.NoticeDialog
import dev.nycocado.atlas.carddetail.CardDetailScreen
import dev.nycocado.atlas.compendium.CompendiumScreen
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.theme.AtlasTheme
import kotlinx.serialization.Serializable

@Serializable
data object CompendiumKey : NavKey

@Serializable
data object AboutKey : NavKey

@Serializable
data class CardKey(
    val id: String,
) : NavKey

private const val NOTICE_SEEN = "notice_seen"

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AtlasApp() {
    val prefs = LocalContext.current.getSharedPreferences("atlas", Context.MODE_PRIVATE)
    var noticeSeen by remember { mutableStateOf(prefs.getBoolean(NOTICE_SEEN, false)) }
    val backStack = rememberNavBackStack(CompendiumKey)
    val viewModel: CompendiumViewModel = hiltViewModel()
    if (!noticeSeen) {
        NoticeDialog {
            prefs.edit { putBoolean(NOTICE_SEEN, true) }
            noticeSeen = true
        }
    }
    val listDetail = rememberListDetailSceneStrategy<NavKey>()
    // Without an open card the list fills the screen instead of sharing it with an empty detail pane.
    val sceneStrategy =
        SceneStrategy<NavKey> { entries ->
            if (backStack.none { it is CardKey }) null else with(listDetail) { calculateScene(entries) }
        }
    NavDisplay(
        backStack = backStack,
        // Shows between the panes.
        modifier = Modifier.background(AtlasTheme.colors.surface),
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(sceneStrategy),
        entryProvider =
            entryProvider {
                entry<CompendiumKey>(
                    metadata =
                        ListDetailSceneStrategy.listPane() + ListDetailSceneStrategy.preferredPaneSize(width = 0.45f),
                ) {
                    CompendiumScreen(
                        viewModel,
                        onCardClick = { id ->
                            // Beside the list, a new card replaces the open one instead of stacking.
                            backStack.removeAll { it is CardKey }
                            backStack.add(CardKey(id))
                        },
                        onAboutClick = { backStack.add(AboutKey) },
                    )
                }
                entry<CardKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                    CardDetailScreen(
                        viewModel,
                        key.id,
                        onBack = { backStack.removeLastOrNull() },
                        onAboutClick = { backStack.add(AboutKey) },
                    )
                }
                entry<AboutKey> { AboutScreen { backStack.removeLastOrNull() } }
            },
    )
}
