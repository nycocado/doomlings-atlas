package dev.nycocado.atlas

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.nycocado.atlas.carddetail.CardDetailScreen
import dev.nycocado.atlas.carddetail.DetailPlaceholder
import dev.nycocado.atlas.compendium.CompendiumScreen
import dev.nycocado.atlas.compendium.CompendiumViewModel
import kotlinx.serialization.Serializable

@Serializable
data object CompendiumKey : NavKey

@Serializable
data class CardKey(
    val id: String,
) : NavKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AtlasApp() {
    val backStack = rememberNavBackStack(CompendiumKey)
    val viewModel: CompendiumViewModel = hiltViewModel()
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        entryProvider =
            entryProvider {
                entry<CompendiumKey>(
                    metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { DetailPlaceholder() }),
                ) {
                    CompendiumScreen(viewModel) { id ->
                        // Beside the list, a new card replaces the open one instead of stacking.
                        backStack.removeAll { it is CardKey }
                        backStack.add(CardKey(id))
                    }
                }
                entry<CardKey>(metadata = ListDetailSceneStrategy.detailPane()) { key ->
                    CardDetailScreen(viewModel, key.id) { backStack.removeLastOrNull() }
                }
            },
    )
}
