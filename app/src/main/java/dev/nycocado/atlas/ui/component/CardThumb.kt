package dev.nycocado.atlas.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait

private const val CARD_RATIO = 541f / 756f

// colors is null for cards without color (ages, catastrophes); an empty list is a colorless trait.
@Composable
fun CardThumb(
    image: String,
    name: String,
    colors: List<CardColor>?,
    description: String,
    tilt: Float,
    modifier: Modifier = Modifier,
) {
    val theme = AtlasTheme.colors
    Column(modifier.rotate(tilt).clearAndSetSemantics { contentDescription = description }) {
        AsyncImage(
            model = "file:///android_asset/cards/$image",
            contentDescription = null,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(CARD_RATIO)
                    .sticker(Radius.cardSm, theme.surfaceSticker, theme.outline, shadow = Shadow.sticker)
                    .clip(Radius.cardSm),
        )
        Row(
            Modifier.padding(top = Space.s3),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            colors?.ifEmpty { listOf(null) }?.forEach { TraitGlyph(it, theme.trait(it).ink, 16.dp) }
            BasicText(name, style = AtlasTypography.titleMd.copy(color = theme.onSurface))
        }
    }
}

// Card images live in the generated assets, so previews show a blank card.
@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun BlankCardImages(content: @Composable () -> Unit) {
    val blank = AtlasTheme.colors.surfaceSticker.toArgb()
    CompositionLocalProvider(
        LocalAsyncImagePreviewHandler provides
            AsyncImagePreviewHandler {
                ColorImage(blank)
            },
        content = content,
    )
}

@PreviewLightDark
@Composable
internal fun CardThumbPreview() =
    AtlasTheme {
        BlankCardImages {
            Row(
                Modifier.background(AtlasTheme.colors.surface).padding(Space.s5),
                horizontalArrangement = Arrangement.spacedBy(Space.s5),
            ) {
                CardThumb("", "Blue trait", listOf(CardColor.Blue), "", -2.5f, Modifier.width(150.dp))
                CardThumb("", "Two colors", listOf(CardColor.Green, CardColor.Red), "", 2f, Modifier.width(150.dp))
                CardThumb("", "An age", null, "", 1.5f, Modifier.width(150.dp))
            }
        }
    }
