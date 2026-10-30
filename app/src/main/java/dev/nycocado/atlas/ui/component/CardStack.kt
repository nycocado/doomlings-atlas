package dev.nycocado.atlas.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.Space

// A card group: the thumbnail of its first variant with two silhouettes behind the art, and the count in the name.
// Nothing goes over the art.
@Composable
fun CardStack(
    image: String,
    name: String,
    colors: List<CardColor?>,
    description: String,
    tilt: Float,
    onClick: () -> Unit,
    onClickLabel: String,
    modifier: Modifier = Modifier,
) = CardThumb(image, name, colors, description, tilt, onClick, modifier, onClickLabel) { sink ->
    // Offset and tilt stay within the margin the compendium leaves around each card.
    Silhouette(3.dp, -2.dp, 2.5f, sink)
    Silhouette(-3.dp, -1.dp, -2.5f, sink)
}

@Composable
private fun BoxScope.Silhouette(
    x: Dp,
    y: Dp,
    tilt: Float,
    sink: Dp,
) {
    Box(
        Modifier
            .matchParentSize()
            .offset(x, y)
            .rotate(tilt)
            .cardSticker(sink),
    )
}

@PreviewLightDark
@Composable
internal fun CardStackPreview() =
    AtlasTheme {
        BlankCardImages {
            Row(
                Modifier.background(AtlasTheme.colors.surface).padding(Space.s5),
                horizontalArrangement = Arrangement.spacedBy(Space.s5),
            ) {
                CardStack("", "Kidney ×6", listOf(CardColor.Red), "", -2f, {}, "", Modifier.width(150.dp))
                CardStack("", "Swarm ×5", listOf(CardColor.Green), "", 2f, {}, "", Modifier.width(150.dp))
            }
        }
    }
