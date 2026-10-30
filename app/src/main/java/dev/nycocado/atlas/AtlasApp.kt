package dev.nycocado.atlas

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.snap
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.nycocado.atlas.about.AboutScreen
import dev.nycocado.atlas.about.NoticeDialog
import dev.nycocado.atlas.cardpane.CardKey
import dev.nycocado.atlas.cardpane.rememberCardPane
import dev.nycocado.atlas.cardpane.rememberListDetailLayout
import dev.nycocado.atlas.compendium.CompendiumScreen
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.theme.AtlasTheme
import kotlinx.serialization.Serializable

@Serializable
data object CompendiumKey : NavKey

@Serializable
data object AboutKey : NavKey

// The one preferences file: the notice flag and the card language. The name is kept so installs keep their flag.
internal const val APP_PREFS = "atlas"
internal const val CARD_LANGUAGE = "card_language"
internal const val NOTICE_SEEN = "notice_seen"

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AtlasApp() {
    val prefs = LocalContext.current.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    var noticeSeen by remember { mutableStateOf(prefs.getBoolean(NOTICE_SEEN, false)) }
    val backStack = rememberNavBackStack(CompendiumKey)
    val viewModel: CompendiumViewModel = hiltViewModel()
    if (!noticeSeen) {
        NoticeDialog {
            prefs.edit { putBoolean(NOTICE_SEEN, true) }
            noticeSeen = true
        }
    }
    val cardPane =
        rememberCardPane(
            backStack,
            viewModel.library.collectAsStateWithLifecycle(),
            viewModel.translations.collectAsStateWithLifecycle(),
            viewModel.cardLanguage.collectAsStateWithLifecycle(),
            viewModel::chooseCardLanguage,
        )
    val layout = rememberListDetailLayout(cardPane)
    val listDetail =
        rememberListDetailSceneStrategy<NavKey>(
            shouldHandleSinglePaneLayout = layout.sideBySide,
            directive = layout.directive,
        )
    // The list and the card share one scene beside each other, which NavDisplay sees as having nothing to go back to:
    // without this, back leaves the app instead of closing the card.
    BackHandler(enabled = layout.besideCard && backStack.lastOrNull() is CardKey, onBack = cardPane::close)
    NavDisplay(
        backStack = backStack,
        // Shows wherever the panes leave room, like around the fold.
        modifier = Modifier.background(AtlasTheme.colors.surface),
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        sceneStrategies = listOf(listDetail),
        entryProvider =
            entryProvider {
                entry<CompendiumKey>(
                    metadata =
                        ListDetailSceneStrategy.listPane(
                            detailPlaceholder = { cardPane.DetailPlaceholder(layout) },
                        ) +
                            ListDetailSceneStrategy.preferredPaneSize(width = 0.5f) +
                            ListDetailSceneStrategy.paneAnimation(boundsAnimationSpec = snap()),
                ) {
                    CompendiumScreen(
                        viewModel,
                        onCardClick = cardPane::open,
                        onAboutClick = { backStack.add(AboutKey) },
                        layout = layout,
                    )
                }
                // One content key for every card at a depth, so picking another card beside the list only changes what
                // the pane shows instead of replacing the pane (which also flickered the status bar icons). A cited
                // card sits on top of its citer, so it needs a key of its own.
                entry<CardKey>(
                    clazzContentKey = { "card ${it.depth}" },
                    metadata =
                        ListDetailSceneStrategy.detailPane() +
                            ListDetailSceneStrategy.paneAnimation(
                                enterTransition = fadeIn(),
                                exitTransition = fadeOut(),
                                boundsAnimationSpec = snap(),
                            ),
                ) { key ->
                    cardPane.Detail(key, layout) { backStack.add(AboutKey) }
                }
                entry<AboutKey> { AboutScreen { if (backStack.lastOrNull() == AboutKey) backStack.removeLastOrNull() } }
            },
    )
}
