package dev.nycocado.atlas.ui.theme

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Stroke as StrokeStyle

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
        // The fill and any clipped content (card images) are antialiased at the same edge as the border, which lets a
        // light seam show between the sticker and its shadow. A thin stroke centered on that edge covers it.
        .drawWithContent {
            drawContent()
            drawOutline(shape.createOutline(size, layoutDirection, this), outline, style = StrokeStyle(1.dp.toPx()))
        }

// How far a pressed sticker sinks into its shadow: right on press, so the feedback never delays the action.
@Composable
fun pressSink(
    interactions: InteractionSource,
    shadow: Dp,
): Dp {
    val pressed by interactions.collectIsPressedAsState()
    return animateDpAsState(if (pressed) shadow else 0.dp, label = "sink").value
}

fun Modifier.sinkingSticker(
    sink: Dp,
    shape: Shape,
    fill: Color,
    outline: Color,
    shadow: Dp,
): Modifier =
    offset { IntOffset(sink.roundToPx(), sink.roundToPx()) }
        .sticker(shape, fill, outline, shadow = shadow - sink)

@PreviewLightDark
@Composable
internal fun StickerPreview() =
    AtlasTheme {
        val theme = AtlasTheme.colors
        Column(
            Modifier.background(theme.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            BasicText(
                "Automimicry",
                Modifier
                    .sticker(
                        Radius.md,
                        theme.surfaceSticker,
                        theme.outline,
                        shadow = Shadow.stickerLg,
                    ).padding(Space.s4),
                style = AtlasTypography.display.copy(color = theme.onSurface),
            )
            BasicText(
                "Build piles",
                Modifier
                    .sticker(CircleShape, theme.primary, theme.outline, shadow = Shadow.sticker)
                    .padding(horizontal = Space.s4, vertical = Space.s3),
                style = AtlasTypography.labelLg.copy(color = theme.onPrimary),
            )
            BasicText(
                "Unofficial, not affiliated with or endorsed by Doomlings LLC",
                style = AtlasTypography.bodySm.copy(color = theme.onSurfaceVariant),
            )
        }
    }
