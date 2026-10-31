package dev.nycocado.atlas.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space

private enum class Destination(
    @param:StringRes val label: Int,
    @param:DrawableRes val icon: Int,
) {
    Cards(R.string.dock_cards, R.drawable.ic_dock_cards),
    Score(R.string.dock_score, R.drawable.ic_dock_score),
    Scanner(R.string.dock_scanner, R.drawable.ic_dock_scanner),
    Tutorial(R.string.dock_tutorial, R.drawable.ic_dock_tutorial),
}

// The rail (vertical) shows icons only; labels go to semantics.
@Composable
fun Dock(
    onCardsClick: () -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
) {
    val theme = AtlasTheme.colors
    Box(
        modifier
            .then(
                if (vertical) {
                    Modifier.width(Sizes.dock)
                } else {
                    Modifier.widthIn(max = MaxWidth.compact).fillMaxWidth().height(Sizes.dock)
                },
            ).background(theme.inverseSurface, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (vertical) {
            Column(
                Modifier.padding(vertical = Space.s2),
                verticalArrangement = Arrangement.spacedBy(Space.s3),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) { Destination.entries.forEach { DockItem(it, vertical, onCardsClick) } }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = Space.s2),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) { Destination.entries.forEach { DockItem(it, vertical, onCardsClick) } }
        }
    }
}

@Composable
private fun DockItem(
    destination: Destination,
    vertical: Boolean,
    onCardsClick: () -> Unit,
) {
    val theme = AtlasTheme.colors
    val label = stringResource(destination.label)
    if (destination == Destination.Cards) {
        Row(
            Modifier
                .height(Sizes.touchTarget)
                .background(theme.highlight, CircleShape)
                .clickable(onClick = onCardsClick)
                .padding(horizontal = if (vertical) 12.dp else Space.s4)
                .clearAndSetSemantics {
                    contentDescription = label
                    selected = true
                    onClick { onCardsClick().let { true } }
                },
            horizontalArrangement = Arrangement.spacedBy(Space.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(destination.icon),
                null,
                Modifier.size(Sizes.icon),
                colorFilter = ColorFilter.tint(theme.onHighlight),
            )
            if (!vertical) {
                BasicText(label, style = AtlasTypography.labelLg.copy(color = theme.onHighlight), maxLines = 1)
            }
        }
    } else {
        val comingSoon = stringResource(R.string.coming_soon, label)
        Box(
            Modifier.size(Sizes.touchTarget).clearAndSetSemantics {
                contentDescription = comingSoon
                disabled()
            },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painterResource(destination.icon),
                null,
                Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(theme.onInverseSurface.copy(alpha = 0.38f)),
            )
        }
    }
}

@PreviewLightDark
@Composable
internal fun DockPreview() =
    AtlasTheme {
        Row(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) {
            Dock({}, Modifier.weight(1f))
            Dock({}, Modifier.padding(start = Space.s4), vertical = true)
        }
    }
