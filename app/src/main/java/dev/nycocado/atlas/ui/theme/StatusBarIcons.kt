package dev.nycocado.atlas.ui.theme

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

// For screens whose top isn't a dark header (see MainActivity). Applied on resume: during a back gesture the screen below
// is composed already but only started, so a cancelled gesture still leaves the screen on top with its own icons. No
// screen resets them on leaving: on a pop the returning one resumes before the leaving one is disposed.
@Composable
fun StatusBarIcons(dark: Boolean) {
    val window = LocalActivity.current?.window ?: return
    val view = LocalView.current
    LifecycleResumeEffect(window, dark) {
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = dark
        onPauseOrDispose {}
    }
}
