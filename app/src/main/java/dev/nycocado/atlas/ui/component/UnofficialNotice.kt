package dev.nycocado.atlas.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker

@Composable
fun UnofficialFooter(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    BasicText(
        stringResource(R.string.unofficial_notice),
        modifier
            .clickable(onClickLabel = stringResource(R.string.about), onClick = onClick)
            .padding(horizontal = Space.s4, vertical = Space.s2),
        style =
            AtlasTypography.bodySm.copy(
                color = theme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
            ),
    )
}

@Composable
fun UnofficialSticker(modifier: Modifier = Modifier) {
    val theme = AtlasTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .sticker(Radius.md, theme.surfaceSticker, theme.outline, shadow = Shadow.stickerLg)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        BasicText(
            stringResource(R.string.unofficial_label).uppercase(),
            Modifier
                .sticker(CircleShape, theme.highlight, theme.outline)
                .padding(horizontal = Space.s3, vertical = Space.s1),
            style = AtlasTypography.labelSm.copy(color = theme.onHighlight),
        )
        BasicText(
            stringResource(R.string.unofficial_notice),
            style = AtlasTypography.bodyLg.copy(color = theme.onSurface),
        )
        BasicText(
            stringResource(R.string.unofficial_explanation),
            style = AtlasTypography.bodyMd.copy(color = theme.onSurfaceVariant),
        )
    }
}

@PreviewLightDark
@Composable
internal fun UnofficialNoticePreview() =
    AtlasTheme {
        Column(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            UnofficialSticker()
            UnofficialFooter({})
        }
    }
