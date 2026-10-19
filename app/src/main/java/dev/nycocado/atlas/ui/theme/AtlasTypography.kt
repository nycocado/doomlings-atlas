package dev.nycocado.atlas.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import dev.nycocado.atlas.R

// Type scale from design/tokens.json. Both fonts are variable: one file, the weight set per style.
private fun variable(
    res: Int,
    vararg weights: FontWeight,
) = FontFamily(
    weights.map {
        Font(res, it, variationSettings = FontVariation.Settings(FontVariation.weight(it.weight)))
    },
)

private val Display = variable(R.font.fredoka, FontWeight.SemiBold, FontWeight.Bold)
private val Sans = variable(R.font.atkinson_hyperlegible_next, FontWeight.Normal, FontWeight.SemiBold, FontWeight.Bold)

// Signed values line up in columns.
private const val TABULAR = "tnum"

object AtlasTypography {
    val display =
        TextStyle(
            fontFamily = Display,
            fontSize = 36.sp,
            lineHeight = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.01).em,
        )
    val headline = TextStyle(fontFamily = Display, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
    val titleLg =
        TextStyle(fontFamily = Display, fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold)
    val titleMd =
        TextStyle(fontFamily = Display, fontSize = 16.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold)
    val numeralXl =
        TextStyle(
            fontFamily = Display,
            fontSize = 64.sp,
            lineHeight = 56.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.02).em,
            fontFeatureSettings = TABULAR,
        )
    val numeral =
        TextStyle(
            fontFamily = Display,
            fontSize = 28.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = TABULAR,
        )
    val bodyLg = TextStyle(fontFamily = Sans, fontSize = 18.sp, lineHeight = 27.sp, fontWeight = FontWeight.SemiBold)
    val bodyMd = TextStyle(fontFamily = Sans, fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal)
    val bodySm = TextStyle(fontFamily = Sans, fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    val labelLg = TextStyle(fontFamily = Sans, fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Bold)
    val labelSm = TextStyle(fontFamily = Sans, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold)
}
