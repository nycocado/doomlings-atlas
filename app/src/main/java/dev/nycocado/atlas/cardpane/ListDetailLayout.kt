package dev.nycocado.atlas.cardpane

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberListDetailLayout(cardPane: CardPane): ListDetailLayout {
    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfoV2())
    val isOpen = cardPane.isOpen
    // The same instance while nothing changes, so the screens that take it can skip recomposing.
    return remember(directive, isOpen) { ListDetailLayout(directive, isOpen) }
}

// Decided once from the panes the window allows and whether a card is open; the screens follow it instead of deciding
// again on their own. The compendium still decides its rail from its own width.
class ListDetailLayout(
    windowDirective: PaneScaffoldDirective,
    isCardOpen: Boolean,
) {
    val sideBySide = windowDirective.maxHorizontalPartitions > 1

    val besideCard = sideBySide && isCardOpen

    // The card beside the list has its own way to About.
    val showAbout get() = !besideCard

    val centersOnRail get() = !besideCard

    // Beside the list, the list's header owns the status bar's icons.
    val cardSetsStatusBarIcons get() = !sideBySide

    // A single column until a card opens: with two partitions the list would take half the window and leave the other
    // half to the detail placeholder.
    // The constructor, not copy(): copy() resets shouldAutoFocusCurrentDestination to true. A field the directive gains
    // in a library update has to be passed on here too, or it falls back to its default.
    val directive =
        PaneScaffoldDirective(
            maxHorizontalPartitions = if (isCardOpen) windowDirective.maxHorizontalPartitions else 1,
            horizontalPartitionSpacerSize = 0.dp,
            maxVerticalPartitions = windowDirective.maxVerticalPartitions,
            verticalPartitionSpacerSize = windowDirective.verticalPartitionSpacerSize,
            defaultPanePreferredWidth = windowDirective.defaultPanePreferredWidth,
            defaultPanePreferredHeight = windowDirective.defaultPanePreferredHeight,
            // A half-open foldable keeps the list on one side of the fold even when alone; it fills the screen until
            // there is a card for the other side.
            excludedBounds = if (isCardOpen) windowDirective.excludedBounds else emptyList(),
            // Otherwise the scaffold moves focus into the pane that becomes current, and in the list's pane that lands
            // on the search field, which opens the keyboard on its own (#83). Limiting it to the card's pane isn't
            // enough: as the card closes the scaffold still sees the old directive for a frame.
            shouldAutoFocusCurrentDestination = false,
        )

    companion object {
        internal val Single = ListDetailLayout(PaneScaffoldDirective.Default, isCardOpen = false)
        internal val BesideCard =
            ListDetailLayout(PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2), isCardOpen = true)
    }
}
