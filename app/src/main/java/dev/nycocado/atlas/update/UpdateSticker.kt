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

// The newer version and its button: the compendium's banner passes onDismiss, About shows the release notes instead.
@Composable
fun UpdateSticker(
    update: AppUpdate,
    onInstall: () -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    releaseNotes: Boolean = false,
) {
    val theme = AtlasTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .sticker(Radius.md, theme.surfaceSticker, theme.outline, shadow = Shadow.sticker)
            .padding(start = Space.s4, end = Space.s2, top = Space.s2, bottom = Space.s4),
        verticalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicText(
                stringResource(R.string.update_available, update.version),
                Modifier.weight(1f).heightIn(min = Sizes.touchTarget).wrapContentHeight(),
                style = AtlasTypography.titleMd.copy(color = theme.onSurface),
            )
            if (onDismiss != null) RoundButton(R.drawable.ic_clear, R.string.update_dismiss, onDismiss)
        }
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
            if (releaseNotes) {
                val uriHandler = LocalUriHandler.current
                BasicText(
                    stringResource(R.string.release_notes),
                    Modifier
                        .clickable { uriHandler.openUri(update.releaseUrl) }
                        .heightIn(min = Sizes.touchTarget)
                        .wrapContentHeight()
                        .padding(horizontal = Space.s2),
                    style =
                        AtlasTypography.bodyMd.copy(
                            color = theme.onSurfaceVariant,
                            textDecoration = TextDecoration.Underline,
                        ),
                )
            }
        }
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
            UpdateSticker(SampleUpdate, {}, releaseNotes = true)
        }
    }
