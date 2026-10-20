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

// A bottom pill, or a rail along the side on wide windows (vertical). The rail shows icons only; labels go to
// semantics.
@Composable
fun Dock(
    onCardsClick: () -> Unit,
    modifier: Modifier = Modifier,
    vertical: Boolean = false,
) {
    val colors = AtlasTheme.colors
    val items: @Composable () -> Unit = {
        Destination.entries.forEach { destination ->
            val label = stringResource(destination.label)
            if (destination == Destination.Cards) {
                Row(
                    Modifier
                        .height(46.dp)
                        .background(colors.highlight, CircleShape)
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
                        Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(colors.onHighlight),
                    )
                    if (!vertical) {
                        BasicText(label, style = AtlasTypography.labelLg.copy(color = colors.onHighlight))
                    }
                }
            } else {
                val comingSoon = stringResource(R.string.coming_soon, label)
                Box(
                    Modifier.size(46.dp).clearAndSetSemantics {
                        contentDescription = comingSoon
                        disabled()
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painterResource(destination.icon),
                        null,
                        Modifier.size(24.dp),
                        colorFilter = ColorFilter.tint(colors.onInverseSurface.copy(alpha = 0.38f)),
                    )
                }
            }
        }
    }
    if (vertical) {
        Column(
            modifier
                .width(64.dp)
                .background(colors.inverseSurface, CircleShape)
                .padding(vertical = Space.s2),
            verticalArrangement = Arrangement.spacedBy(Space.s3),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) { items() }
    } else {
        Row(
            modifier
                .widthIn(max = MaxWidth.compact)
                .fillMaxWidth()
                .height(64.dp)
                .background(colors.inverseSurface, CircleShape)
                .padding(horizontal = Space.s2),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) { items() }
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
