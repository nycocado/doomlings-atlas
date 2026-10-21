package dev.nycocado.atlas.about

import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.component.UnofficialSticker
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker

@Composable
fun NoticeDialog(onAccept: () -> Unit) {
    Dialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        val window = (LocalView.current.parent as DialogWindowProvider).window
        val radius = with(LocalDensity.current) { 24.dp.roundToPx() }
        // The system's window blur (Android 12+). It turns itself off when the user disables blurs or battery saver
        // is on; then only the default dim remains.
        LaunchedEffect(window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && window.windowManager.isCrossWindowBlurEnabled) {
                window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                window.attributes = window.attributes.apply { blurBehindRadius = radius }
                window.setDimAmount(0.3f)
            }
        }
        NoticeContent(onAccept)
    }
}

@Composable
private fun NoticeContent(onAccept: () -> Unit) {
    val theme = AtlasTheme.colors
    Column(
        Modifier.widthIn(max = MaxWidth.compact).padding(Space.s6),
        verticalArrangement = Arrangement.spacedBy(Space.s5),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UnofficialSticker()
        BasicText(
            stringResource(R.string.got_it),
            Modifier
                .sticker(CircleShape, theme.sunsetYellow, theme.outline, shadow = Shadow.sticker)
                .clickable(role = Role.Button, onClick = onAccept)
                .padding(horizontal = Space.s6, vertical = 14.dp),
            style = AtlasTypography.labelLg.copy(color = theme.onSunset),
        )
    }
}

@PreviewLightDark
@Composable
internal fun NoticePreview() = AtlasTheme { NoticeContent {} }
