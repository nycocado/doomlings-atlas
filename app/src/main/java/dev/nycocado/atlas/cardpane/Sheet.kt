package dev.nycocado.atlas.cardpane

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.offset
import dev.nycocado.atlas.carddetail.CardDetailSample
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker

private val SheetShape = Radius.lg.copy(topStart = ZeroCornerSize, topEnd = ZeroCornerSize)

// Beside the list, the card detail is a sticker hanging from the top of the screen; its top edge sits just above
// the screen. The status bar icons stay light there, so a soft shade behind the bar keeps them readable over light
// panels.
@Composable
internal fun Modifier.sheet(): Modifier {
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

// On the scene's background, which shows around the sheet.
@PreviewLightDark
@Composable
internal fun CardBesideListPreview() =
    AtlasTheme {
        Box(Modifier.background(AtlasTheme.colors.surface).sheet()) { CardDetailSample(ListDetailLayout.BesideCard) }
    }
