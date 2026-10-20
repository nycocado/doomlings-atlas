package dev.nycocado.atlas.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.ui.theme.AtlasTheme

@Composable
fun SunsetBand(modifier: Modifier = Modifier) {
    val colors = AtlasTheme.colors
    Row(modifier.fillMaxWidth().height(14.dp)) {
        listOf(colors.sunsetRed, colors.sunsetOrange, colors.sunsetYellow, colors.sunsetSea).forEach {
            Spacer(Modifier.weight(1f).height(14.dp).background(it))
        }
    }
}

@PreviewLightDark
@Composable
internal fun SunsetBandPreview() = AtlasTheme { SunsetBand() }
