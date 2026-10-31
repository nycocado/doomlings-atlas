package dev.nycocado.atlas.carddetail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import dev.nycocado.atlas.R

// A card symbol. Decorative: the text beside it, or the inline text it stands in for, names it.
@Composable
internal fun Symbol(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    actionColor: Color = Color.Unspecified,
) {
    val vector = ImageVector.vectorResource(id)
    // The action symbol is printed in the card's color; its drawable names those paths card_color.
    val painter =
        if (id == R.drawable.ic_symbol_action) {
            rememberVectorPainter(remember(vector, actionColor) { vector.withCardColor(actionColor) })
        } else {
            rememberVectorPainter(vector)
        }
    Image(painter, null, modifier)
}

// ImageVector has no way to change a path's fill, so the paths are copied with the card_color ones refilled. Converted
// symbols are flat lists of paths, without groups.
private fun ImageVector.withCardColor(color: Color): ImageVector =
    ImageVector
        .Builder(name, defaultWidth, defaultHeight, viewportWidth, viewportHeight)
        .apply {
            root.filterIsInstance<VectorPath>().forEach {
                addPath(
                    it.pathData,
                    it.pathFillType,
                    it.name,
                    if (it.name == "card_color") SolidColor(color) else it.fill,
                    it.fillAlpha,
                    it.stroke,
                    it.strokeAlpha,
                    it.strokeLineWidth,
                    it.strokeLineCap,
                    it.strokeLineJoin,
                    it.strokeLineMiter,
                )
            }
        }.build()
