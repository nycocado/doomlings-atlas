package dev.nycocado.atlas.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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

// Quiet is for secondary actions, so they don't outshine the header's title.
enum class RoundButtonStyle { OnSurface, OnHeader, QuietOnHeader }

@Composable
fun RoundButton(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: RoundButtonStyle = RoundButtonStyle.OnSurface,
) {
    val colors = AtlasTheme.colors
    // On a dark header the theme's outline can be the header's own color, which hides the border and shadow.
    val (fill, outline, content) =
        when (style) {
            RoundButtonStyle.OnSurface -> {
                Triple(colors.surfaceSticker, colors.outline, colors.onSurface)
            }

            RoundButtonStyle.OnHeader -> {
                Triple(colors.surfaceSticker, colors.onInverseSurfaceVariant, colors.onSurface)
            }

            RoundButtonStyle.QuietOnHeader -> {
                Triple(colors.inverseSurface, colors.onInverseSurfaceVariant, colors.onInverseSurface)
            }
        }
    val description = stringResource(label)
    val interactions = remember { MutableInteractionSource() }
    val sink = pressSink(interactions, Shadow.sticker)
    Box(
        modifier
            .size(48.dp)
            .clickable(interactions, LocalIndication.current, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
    ) {
        Box(
            Modifier
                .matchParentSize()
                .sinkingSticker(sink, CircleShape, fill, outline, Shadow.sticker),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(icon),
                null,
                Modifier.size(22.dp),
                colorFilter = ColorFilter.tint(content),
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun RoundButtonPreview() =
    AtlasTheme {
        Row(Modifier.background(AtlasTheme.colors.surface)) {
            Box(Modifier.padding(Space.s4)) { RoundButton(R.drawable.ic_back, R.string.back, {}) }
            Row(Modifier.background(AtlasTheme.colors.inverseSurface).padding(Space.s4)) {
                RoundButton(R.drawable.ic_back, R.string.back, {}, style = RoundButtonStyle.OnHeader)
                RoundButton(
                    R.drawable.ic_info,
                    R.string.about,
                    {},
                    Modifier.padding(start = Space.s4),
                    style = RoundButtonStyle.QuietOnHeader,
                )
            }
        }
    }
