package dev.nycocado.atlas.ui.theme

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

// Tablets, unfolded foldables and phones on their side: the list and the card side by side.
@Composable
fun wideWindow(): Boolean =
    currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
    )
