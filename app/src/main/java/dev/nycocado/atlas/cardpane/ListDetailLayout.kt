package dev.nycocado.atlas.cardpane

import androidx.compose.animation.core.snap
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberListDetailLayout(cardPane: CardPane): ListDetailLayout =
    ListDetailLayout(calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2()), cardPane.isOpen)

// Decided once from the panes the window allows and whether a card is open; the screens follow it instead of deciding
// again on their own. The compendium still decides its rail from its own width.
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class ListDetailLayout(
    private val windowDirective: PaneScaffoldDirective,
    private val isCardOpen: Boolean,
) {
    // Tablets, unfolded foldables and phones on their side: room for the list and a card side by side.
    val sideBySide = windowDirective.maxHorizontalPartitions > 1

    val besideCard = sideBySide && isCardOpen

    // The card beside the list has its own way to About.
    val showAbout get() = !besideCard
    val centersOnRail get() = !besideCard

    // Beside the list, the list's header owns the status bar's icons.
    val cardSetsStatusBarIcons get() = !sideBySide

    // A single column until a card opens, so opening and closing a card animates the panes instead of swapping layouts.
    val directive =
        windowDirective.copy(
            maxHorizontalPartitions = if (isCardOpen) windowDirective.maxHorizontalPartitions else 1,
            horizontalPartitionSpacerSize = 0.dp,
            // A half-open foldable keeps the list on one side of the fold even when alone; it fills the screen until
            // there is a card for the other side.
            excludedBounds = if (isCardOpen) windowDirective.excludedBounds else emptyList(),
        )

    // Jumps as a card opens, so the grid changes its columns once. Grows as it closes: the scaffold slides the closing
    // card out under the list.
    fun listPaneAnimation() =
        ListDetailSceneStrategy.paneAnimation(boundsAnimationSpec = if (isCardOpen) snap() else null)

    companion object {
        internal val Single = ListDetailLayout(PaneScaffoldDirective.Default, isCardOpen = false)
        internal val BesideCard =
            ListDetailLayout(PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2), isCardOpen = true)
    }
}
