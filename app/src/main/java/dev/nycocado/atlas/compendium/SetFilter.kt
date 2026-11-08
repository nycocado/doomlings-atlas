package dev.nycocado.atlas.compendium

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardSet
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Shadow
import dev.nycocado.atlas.ui.theme.Sizes
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker

@Composable
internal fun SetsChip(
    chosen: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val theme = AtlasTheme.colors
    val active = chosen > 0
    BasicText(
        if (active) stringResource(R.string.sets_filter_chosen, chosen) else stringResource(R.string.sets_filter),
        modifier
            .sticker(
                CircleShape,
                if (active) theme.highlight else theme.surfaceSticker,
                theme.outline,
                shadow = Shadow.sticker,
            ).clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Space.s4, vertical = Space.s2),
        style = AtlasTypography.labelLg.copy(color = if (active) theme.onHighlight else theme.onSurface),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SetsSheet(
    sets: List<CardSet>,
    chosen: Set<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val theme = AtlasTheme.colors
    // AtlasTheme provides no MaterialTheme (ADR 0006), so everything the sheet would take from it is passed here.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = Radius.lg.copy(bottomStart = ZeroCornerSize, bottomEnd = ZeroCornerSize),
        containerColor = theme.surface,
        contentColor = theme.onSurface,
        scrimColor = theme.inverseSurface.copy(alpha = 0.5f),
        dragHandle = { SheetHandle() },
    ) {
        SetsList(sets, chosen, onToggle)
    }
}

@Composable
private fun SheetHandle() {
    val label = stringResource(R.string.close_sets)
    // The sheet makes its handle slot a clickable that dismisses, and takes its label from what the slot holds.
    Box(
        Modifier.fillMaxWidth().padding(vertical = Space.s3).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 36.dp, height = 4.dp).background(AtlasTheme.colors.outline, CircleShape))
    }
}

@Composable
private fun SetsList(
    sets: List<CardSet>,
    chosen: Set<String>,
    onToggle: (String) -> Unit,
) {
    val theme = AtlasTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = Space.s5, end = Space.s5, bottom = Space.s5),
    ) {
        BasicText(
            stringResource(R.string.sets_filter),
            Modifier.padding(bottom = Space.s2).semantics { heading() },
            style = AtlasTypography.titleLg.copy(color = theme.onSurface),
        )
        sets.forEach { set ->
            // Only sets with a confirmed name reach this list.
            SetRow(set.name.orEmpty(), set.id in chosen) { onToggle(set.id) }
        }
    }
}

@Composable
private fun SetRow(
    name: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val theme = AtlasTheme.colors
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.touchTarget)
            .toggleable(checked, role = Role.Checkbox, onValueChange = { onToggle() }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s3),
    ) {
        Box(
            Modifier
                .size(24.dp)
                .sticker(
                    RoundedCornerShape(6.dp),
                    if (checked) theme.primary else theme.surfaceSticker,
                    theme.outline,
                    Stroke.thin,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Image(
                    painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(theme.onPrimary),
                )
            }
        }
        BasicText(name, style = AtlasTypography.titleMd.copy(color = theme.onSurface))
    }
}

// Printed names of three sets, read off their cards (assets/doomlings/<set>/set.json).
internal val SampleSets =
    listOf(
        CardSet("imaginary-ends", "Imaginary Ends", 2024),
        CardSet("castle-glass", "Castle Glass", 2025),
        CardSet("mokoko-village", "Mokoko Village", 2025),
    )

@PreviewLightDark
@Composable
internal fun SetsListPreview() =
    AtlasTheme {
        Column(Modifier.background(AtlasTheme.colors.surface)) {
            SheetHandle()
            SetsList(SampleSets, setOf("castle-glass")) {}
        }
    }

@PreviewLightDark
@Composable
internal fun SetsChipPreview() =
    AtlasTheme {
        Row(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            horizontalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            SetsChip(0) {}
            SetsChip(2) {}
        }
    }
