package dev.nycocado.atlas.carddetail

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Space

// Each option names its language in itself, so it needs no string resource.
private val CardLanguage.endonym: String
    get() =
        when (this) {
            CardLanguage.English -> "English"
            CardLanguage.Portuguese -> "Português"
            CardLanguage.Spanish -> "Español"
        }

@Composable
internal fun LanguageSelector(
    languages: List<CardLanguage>,
    selected: CardLanguage,
    onSelect: (CardLanguage) -> Unit,
) {
    val theme = AtlasTheme.colors
    val label = stringResource(R.string.card_language)
    // The highlight is drawn behind the options and slides between them. The index starts at the selected option, so
    // nothing slides when the card opens.
    val position by animateFloatAsState(
        languages.indexOf(selected).toFloat(),
        tween(LANGUAGE_CHANGE_MILLIS, easing = FastOutSlowInEasing),
        label = "language highlight",
    )
    val gap = 4.dp
    // Compose widens the touch target of a 30dp option to the 48dp minimum without changing its layout.
    Row(
        Modifier
            .background(theme.inverseSurface, CircleShape)
            .padding(3.dp)
            .width(IntrinsicSize.Max)
            .drawBehind {
                val option = (size.width - gap.toPx() * (languages.size - 1)) / languages.size
                val start = position * (option + gap.toPx())
                val x = if (layoutDirection == LayoutDirection.Ltr) start else size.width - option - start
                drawRoundRect(theme.highlight, Offset(x, 0f), Size(option, size.height), CornerRadius(size.height / 2))
            }.selectableGroup()
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.spacedBy(gap),
    ) {
        languages.forEach { language ->
            val active = language == selected
            Box(
                Modifier
                    .weight(1f)
                    .height(30.dp)
                    .widthIn(min = 44.dp)
                    .clip(CircleShape)
                    .selectable(active, role = Role.RadioButton) { onSelect(language) }
                    .clearAndSetSemantics {
                        text = tagged(language.endonym, language)
                        this.selected = active
                        role = Role.RadioButton
                        onClick {
                            onSelect(language)
                            true
                        }
                    }.padding(horizontal = Space.s3),
                contentAlignment = Alignment.Center,
            ) {
                // TalkBack reads the endonym above, tagged with its language; the code is only for the eye.
                BasicText(
                    language.code.uppercase(),
                    style =
                        AtlasTypography.labelSm.copy(
                            color = if (active) theme.onHighlight else theme.onInverseSurface,
                        ),
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun LanguageSelectorPreview() =
    AtlasTheme {
        Box(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) {
            LanguageSelector(
                listOf(CardLanguage.English, CardLanguage.Portuguese, CardLanguage.Spanish),
                CardLanguage.Portuguese,
                {},
            )
        }
    }
