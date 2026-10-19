package dev.nycocado.atlas.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.trait

// The five trait colors as the filter discs show them; null is colorless.
val TraitColorOptions: List<CardColor?> = CardColor.entries + null

@StringRes
fun colorName(color: CardColor?): Int =
    when (color) {
        CardColor.Blue -> R.string.color_blue
        CardColor.Green -> R.string.color_green
        CardColor.Purple -> R.string.color_purple
        CardColor.Red -> R.string.color_red
        null -> R.string.color_colorless
    }

// Drop, leaf, hexagon, flame or dashed ring (docs/design.md). Decorative: the color name goes in text or semantics.
@Composable
fun TraitGlyph(
    color: CardColor?,
    tint: Color,
    size: Dp = 24.dp,
) {
    val modifier = Modifier.size(size)
    when (color) {
        // VectorDrawable has no dashed strokes, so the ring is drawn here.
        null -> {
            Canvas(modifier) {
                val unit = this.size.minDimension / 24
                drawCircle(
                    tint,
                    radius = 7.5f * unit,
                    style =
                        Stroke(
                            2.8f * unit,
                            pathEffect =
                                PathEffect.dashPathEffect(
                                    floatArrayOf(
                                        4.2f * unit,
                                        2.6f * unit,
                                    ),
                                ),
                        ),
                )
            }
        }

        else -> {
            val glyph =
                when (color) {
                    CardColor.Blue -> R.drawable.ic_trait_blue
                    CardColor.Green -> R.drawable.ic_trait_green
                    CardColor.Purple -> R.drawable.ic_trait_purple
                    CardColor.Red -> R.drawable.ic_trait_red
                }
            Image(painterResource(glyph), null, modifier, colorFilter = ColorFilter.tint(tint))
        }
    }
}

@PreviewLightDark
@Composable
internal fun TraitGlyphPreview() =
    AtlasTheme {
        Row(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            horizontalArrangement = Arrangement.spacedBy(Space.s3),
        ) {
            TraitColorOptions.forEach { TraitGlyph(it, AtlasTheme.colors.trait(it).ink) }
        }
    }
