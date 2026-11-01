package dev.nycocado.atlas.update

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.component.RoundButton
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker

// The compendium's banner can be hidden; About's stays while the update is there.
@Composable
fun UpdateSticker(
    update: AppUpdate,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
) {
    val theme = AtlasTheme.colors
    val uriHandler = LocalUriHandler.current
    Row(
        modifier
            .fillMaxWidth()
            .sticker(Radius.md, theme.surfaceSticker, theme.outline, shadow = Shadow.sticker)
            .padding(start = Space.s4, end = Space.s2, top = Space.s3, bottom = Space.s3),
    ) {
        // One line where the text and the buttons fit side by side, as on a wide window; the buttons go below the text
        // where they don't.
        FlowRow(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(Space.s2),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            BasicText(
                stringResource(R.string.update_available, update.version),
                Modifier.padding(end = Space.s4).heightIn(min = Sizes.touchTarget).wrapContentHeight(),
                style = AtlasTypography.titleMd.copy(color = theme.onSurface),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Space.s4),
                verticalArrangement = Arrangement.spacedBy(Space.s2),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    stringResource(R.string.update_install),
                    Modifier
                        .sticker(CircleShape, theme.primary, theme.outline, shadow = Shadow.sticker)
                        .clickable(role = Role.Button, onClick = onInstall)
                        .padding(horizontal = Space.s5, vertical = 14.dp),
                    style = AtlasTypography.labelLg.copy(color = theme.onPrimary),
                )
                BasicText(
                    stringResource(R.string.release_notes),
                    Modifier
                        .clickable { uriHandler.openUri(update.releaseUrl) }
                        .heightIn(min = Sizes.touchTarget)
                        .wrapContentHeight()
                        .padding(end = Space.s2),
                    style =
                        AtlasTypography.bodyMd.copy(
                            color = theme.onSurfaceVariant,
                            textDecoration = TextDecoration.Underline,
                        ),
                )
            }
        }
        if (onDismiss != null) RoundButton(R.drawable.ic_clear, R.string.update_dismiss, onDismiss)
    }
}

internal val SampleUpdate =
    AppUpdate(
        "1.2.0",
        "https://github.com/nycocado/doomlings-atlas/releases/download/v1.2.0/doomlings-atlas-v1.2.0.apk",
        "ab".repeat(32),
        "https://github.com/nycocado/doomlings-atlas/releases/tag/v1.2.0",
    )

@PreviewLightDark
@Composable
internal fun UpdateStickerPreview() =
    AtlasTheme {
        Column(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            UpdateSticker(SampleUpdate, {}, onDismiss = {})
            UpdateSticker(SampleUpdate, {})
        }
    }
