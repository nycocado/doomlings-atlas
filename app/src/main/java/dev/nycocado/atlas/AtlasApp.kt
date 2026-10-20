package dev.nycocado.atlas

import android.content.Context
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.nycocado.atlas.about.AboutScreen
import dev.nycocado.atlas.about.NoticeDialog
import dev.nycocado.atlas.carddetail.CardDetailScreen
import dev.nycocado.atlas.carddetail.DetailPlaceholder
import dev.nycocado.atlas.compendium.CompendiumScreen
import dev.nycocado.atlas.compendium.CompendiumViewModel
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
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        entryProvider =
            entryProvider {
                entry<CompendiumKey>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { DetailPlaceholder() }),
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
