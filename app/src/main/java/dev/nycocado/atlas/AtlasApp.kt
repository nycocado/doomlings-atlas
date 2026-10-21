package dev.nycocado.atlas

import android.content.Context
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.core.content.edit
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.nycocado.atlas.about.AboutScreen
import dev.nycocado.atlas.about.NoticeDialog
import dev.nycocado.atlas.carddetail.CardDetailScreen
import dev.nycocado.atlas.compendium.CompendiumScreen
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
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
    val hasCard = backStack.any { it is CardKey }
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
                        ListDetailSceneStrategy.listPane() +
                            ListDetailSceneStrategy.preferredPaneSize(width = 0.5f) +
                            // The list jumps to its new width, so the grid changes its columns once instead of on
                            // every frame of the resize; the cards then glide to their new places.
                            ListDetailSceneStrategy.paneAnimation(boundsAnimationSpec = snap()),
                ) {
                    CompendiumScreen(
                        viewModel,
                        onCardClick = { id ->
                            // Beside the list, a new card replaces the open one instead of stacking.
                            backStack.removeAll { it is CardKey }
                            backStack.add(CardKey(id))
                        },
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
                    val sheet = if (sideBySide) Modifier.sheet() else Modifier
                    Box(sheet) {
                        CardDetailScreen(
                            viewModel,
                            key.id,
                            besideList = sideBySide,
                            onBack = { backStack.removeLastOrNull() },
                            onAboutClick = { backStack.add(AboutKey) },
                        )
                    }
                }
                entry<AboutKey> { AboutScreen { backStack.removeLastOrNull() } }
            },
    )
}

private val SheetShape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)

// Beside the list, the card detail is a sticker hanging from the top of the screen; its top edge sits just above
// the screen. The status bar icons stay light there, so a soft shade behind the bar keeps them readable over light
// panels.
@Composable
private fun Modifier.sheet(): Modifier {
    val theme = AtlasTheme.colors
    val statusBar = WindowInsets.statusBars.getTop(LocalDensity.current).toFloat()
    return windowInsetsPadding(WindowInsets.navigationBars)
        .padding(start = Space.s3, end = Space.s4, bottom = Space.s4)
        .layout { measurable, constraints ->
            val bleed = Stroke.sticker.roundToPx()
            val placeable = measurable.measure(constraints.offset(vertical = bleed))
            layout(placeable.width, placeable.height - bleed) { placeable.place(0, -bleed) }
        }.sticker(SheetShape, theme.surface, theme.outline, shadow = Shadow.stickerLg)
        .clip(SheetShape)
        .drawWithContent {
            drawContent()
            // The sheet starts above the screen by its stroke, so the shade reaches down to the bar's edge.
            drawRect(Color.Black.copy(alpha = 0.2f), size = Size(size.width, statusBar + Stroke.sticker.toPx()))
        }
}
