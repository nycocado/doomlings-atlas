package dev.nycocado.atlas.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.FocusRing
import dev.nycocado.atlas.ui.theme.Radius

// A card or a band header as one button: the focus ring of a card, and a description that replaces what is inside.
@Composable
fun Modifier.describedButton(
    description: String,
    interactions: MutableInteractionSource,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier =
    clickable(
        interactions,
        FocusRing(AtlasTheme.colors.focusRing, Radius.cardSm),
        onClickLabel = onClickLabel,
        onClick = onClick,
    ).clearAndSetSemantics {
        contentDescription = description
        role = Role.Button
        onClick(onClickLabel) { onClick().let { true } }
    }
