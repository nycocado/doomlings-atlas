package dev.nycocado.atlas.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.pressSink
import dev.nycocado.atlas.ui.theme.sinkingSticker

@Composable
fun BackButton(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasTheme.colors
    val label = stringResource(R.string.back)
    val interactions = remember { MutableInteractionSource() }
    val sink = pressSink(interactions, Shadow.sticker)
    Box(
        modifier
            .size(48.dp)
            .clickable(interactions, LocalIndication.current, role = Role.Button, onClick = onBack)
            .semantics { contentDescription = label },
    ) {
        Box(
            Modifier
                .matchParentSize()
                .sinkingSticker(sink, CircleShape, colors.surfaceSticker, colors.outline, Shadow.sticker),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(R.drawable.ic_back),
                null,
                Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(colors.onSurface),
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun BackButtonPreview() =
    AtlasTheme {
        Box(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) { BackButton({}) }
    }
