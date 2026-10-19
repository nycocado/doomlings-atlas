package dev.nycocado.atlas.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// The visual signature (docs/design.md): fill, outline border and an optional hard shadow drawn outside the bounds.
fun Modifier.sticker(
    shape: Shape,
    fill: Color,
    outline: Color,
    stroke: Dp = Stroke.sticker,
    shadow: Dp = 0.dp,
): Modifier =
    drawBehind {
        if (shadow > 0.dp) {
            translate(shadow.toPx(), shadow.toPx()) {
                drawOutline(shape.createOutline(size, layoutDirection, this), outline)
            }
        }
    }.background(fill, shape)
        .border(stroke, outline, shape)

@ThemePreviews
@Composable
private fun StickerPreview() =
    AtlasTheme {
        val colors = AtlasTheme.colors
        Column(
            Modifier.background(colors.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            BasicText(
                "Automimicry",
                Modifier
                    .sticker(
                        Radius.md,
                        colors.surfaceSticker,
                        colors.outline,
                        shadow = Shadow.stickerLg,
                    ).padding(Space.s4),
                style = AtlasTypography.display.copy(color = colors.onSurface),
            )
            BasicText(
                "Build piles",
                Modifier
                    .sticker(CircleShape, colors.primary, colors.outline, shadow = Shadow.sticker)
                    .padding(horizontal = Space.s4, vertical = Space.s3),
                style = AtlasTypography.labelLg.copy(color = colors.onPrimary),
            )
            BasicText(
                "Unofficial, not affiliated with or endorsed by Doomlings LLC",
                style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant),
            )
        }
    }
