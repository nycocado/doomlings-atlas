package dev.nycocado.atlas.carddetail

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.ui.theme.AtlasColors
import dev.nycocado.atlas.ui.theme.trait
import dev.nycocado.atlas.ui.theme.traitPaint

// A card symbol. Decorative: the text beside it, or the inline text it stands in for, names it.
@Composable
internal fun Symbol(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    actionColors: List<Color> = emptyList(),
) {
    val vector = ImageVector.vectorResource(id)
    // The action symbol is printed in the card's colors; its drawable names those paths card_color.
    val painter =
        if (id == R.drawable.ic_symbol_action) {
            rememberVectorPainter(remember(vector, actionColors) { vector.withCardColors(actionColors) })
        } else {
            rememberVectorPainter(vector)
        }
    Image(painter, null, modifier)
}

// ImageVector has no way to change a path's fill, so the paths are copied with the card_color ones refilled. Converted
// symbols are flat lists of paths, without groups.
private fun ImageVector.withCardColors(colors: List<Color>): ImageVector =
    ImageVector
        .Builder(name, defaultWidth, defaultHeight, viewportWidth, viewportHeight)
        .apply {
            root.filterIsInstance<VectorPath>().forEach {
                addPath(
                    it.pathData,
                    it.pathFillType,
                    it.name,
                    if (it.name == "card_color") traitPaint(colors, Offset(viewportWidth, viewportHeight)) else it.fill,
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

// A card without a color prints its action symbols in the colorless fill.
internal fun AtlasColors.actionColors(colors: List<CardColor?>): List<Color> =
    colors.ifEmpty { listOf(null) }.map { trait(it).fill }
