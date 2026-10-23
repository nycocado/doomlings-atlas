package dev.nycocado.atlas

import android.content.Context
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.nycocado.atlas.about.AboutScreen
import dev.nycocado.atlas.about.NoticeDialog
import dev.nycocado.atlas.cardpane.CardKey
import dev.nycocado.atlas.cardpane.rememberCardPane
import dev.nycocado.atlas.compendium.CompendiumScreen
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.theme.AtlasTheme
import kotlinx.serialization.Serializable

@Serializable
data object CompendiumKey : NavKey

@Serializable
data object AboutKey : NavKey

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
    val cardPane = rememberCardPane(backStack, viewModel.cards.collectAsStateWithLifecycle())
    val hasCard = cardPane.isOpen
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    // Tablets, unfolded foldables and phones on their side: room for the list and a card side by side. Read from the
    // panes the scaffold allows, so the screens below follow the scene instead of deciding it again on their own.
    val sideBySide = directive.maxHorizontalPartitions > 1
    // Then one list-detail scene covers both states, with a single column until a card opens, so opening and closing a
    // card animates the panes instead of swapping layouts.
    val listDetail =
        rememberListDetailSceneStrategy<NavKey>(
            shouldHandleSinglePaneLayout = sideBySide,
            directive =
                directive.copy(
                    maxHorizontalPartitions = if (hasCard) directive.maxHorizontalPartitions else 1,
                    horizontalPartitionSpacerSize = 0.dp,
                    // A half-open foldable keeps the list on one side of the fold even when alone; it fills the
                    // screen until there is a card for the other side.
                    excludedBounds = if (hasCard) directive.excludedBounds else emptyList(),
                ),
        )
    NavDisplay(
        backStack = backStack,
        // Shows wherever the panes leave room, like around the fold and while they animate.
        modifier = Modifier.background(AtlasTheme.colors.surface),
        onBack = { backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetail),
        entryProvider =
            entryProvider {
                entry<CompendiumKey>(
                    metadata =
                        ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { cardPane.DetailPlaceholder() },
                        ) +
                            ListDetailSceneStrategy.preferredPaneSize(width = 0.5f) +
                            // Jumps as a card opens, so the grid changes its columns once. Grows as it closes: the
                            // scaffold slides the closing card out under the list.
                            ListDetailSceneStrategy.paneAnimation(boundsAnimationSpec = if (hasCard) snap() else null),
                ) {
                    CompendiumScreen(
                        viewModel,
                        onCardClick = cardPane::open,
                        onAboutClick = { backStack.add(AboutKey) },
                        besideCard = sideBySide && hasCard,
                    )
                }
                // One content key for every card, so picking another card beside the list only changes what the
                // pane shows instead of replacing the pane (which also flickered the status bar icons).
                entry<CardKey>(
                    clazzContentKey = { "card" },
                    metadata = ListDetailSceneStrategy.detailPane(),
                ) { key ->
                    cardPane.Detail(key.id, sideBySide) { backStack.add(AboutKey) }
                }
                entry<AboutKey> { AboutScreen { backStack.removeLastOrNull() } }
            },
    )
}
