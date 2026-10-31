package dev.nycocado.atlas.ui.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// For screens whose top isn't a dark header (see MainActivity). Every screen sets its own icons when it enters and none
// resets them when it leaves: on a pop the returning screen enters before the leaving one is disposed, so a reset would
// override it.
@Composable
fun StatusBarIcons(dark: Boolean) {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    DisposableEffect(window, dark) {
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = dark
        onDispose {}
    }
}
