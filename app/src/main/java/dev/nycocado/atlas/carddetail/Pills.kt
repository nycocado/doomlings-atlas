package dev.nycocado.atlas.carddetail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardFace
import dev.nycocado.atlas.card.CardFact
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.TraitIcon
import dev.nycocado.atlas.ui.component.TraitGlyph
import dev.nycocado.atlas.ui.component.colorName
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.Stroke
import dev.nycocado.atlas.ui.theme.sticker
import dev.nycocado.atlas.ui.theme.trait

@Composable
internal fun Pills(
    face: CardFace,
    language: CardLanguage,
) {
    val color = face.colors.firstOrNull()
    val theme = AtlasTheme.colors
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Space.s2),
        verticalArrangement = Arrangement.spacedBy(Space.s2),
    ) {
        face.facts.forEach { fact ->
            when (fact) {
                is CardFact.Color -> {
                    val palette = theme.trait(fact.color)
                    Pill(
                        stringResource(colorName(fact.color)),
                        palette.fill,
                        palette.onFill,
                    ) { TraitGlyph(fact.color, palette.onFill, 18.dp) }
                }

                is CardFact.Kind -> {
                    Pill(stringResource(fact.kind.label), theme.surfaceSticker, theme.onSurface)
                }

                CardFact.Dominant -> {
                    Pill(
                        cardString(R.string.dominant, language),
                        theme.highlight,
                        theme.onHighlight,
                        language = language,
                    ) {
                        Symbol(R.drawable.ic_symbol_dominant)
                    }
                }

                is CardFact.Icon -> {
                    Pill(
                        cardString(fact.icon.label, language),
                        theme.surfaceSticker,
                        theme.onSurface,
                        language = language,
                        glyph =
                            fact.icon.symbol?.let {
                                { Symbol(it, actionColor = theme.trait(color).fill) }
                            },
                    )
                }

                // The coined -ling names stay English whatever the card language.
                is CardFact.Subspecies -> {
                    Pill(fact.name, theme.surfaceSticker, theme.onSurface, language = CardLanguage.English)
                }
            }
        }
    }
}

private val CardKind.label: Int
    @StringRes get() =
        when (this) {
            CardKind.Trait -> R.string.type_trait
            CardKind.Age -> R.string.type_age
            CardKind.Catastrophe -> R.string.type_catastrophe
        }

@Composable
private fun Pill(
    text: String,
    fill: Color,
    content: Color,
    language: CardLanguage? = null,
    glyph: @Composable (() -> Unit)? = null,
) {
    Row(
        Modifier
            .heightIn(min = 32.dp)
            .sticker(CircleShape, fill, AtlasTheme.colors.outline, Stroke.thin)
            .padding(horizontal = Space.s3),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        glyph?.invoke()
        BasicText(
            language?.let { tagged(text, it) } ?: AnnotatedString(text),
            style = AtlasTypography.labelLg.copy(color = content),
        )
    }
}

// Decorative: the pill's text names it.
@Composable
private fun Symbol(
    @DrawableRes id: Int,
    actionColor: Color = Color.Unspecified,
) {
    val vector = ImageVector.vectorResource(id)
    // The action symbol is printed in the card's color; its drawable names those paths card_color.
    val painter =
        if (id == R.drawable.ic_symbol_action) {
            rememberVectorPainter(remember(vector, actionColor) { vector.withCardColor(actionColor) })
        } else {
            rememberVectorPainter(vector)
        }
    Image(painter, null, Modifier.height(18.dp))
}

// ImageVector has no way to change a path's fill, so the paths are copied with the card_color ones refilled. Converted
// symbols are flat lists of paths, without groups.
private fun ImageVector.withCardColor(color: Color): ImageVector =
    ImageVector
        .Builder(name, defaultWidth, defaultHeight, viewportWidth, viewportHeight)
        .apply {
            root.filterIsInstance<VectorPath>().forEach {
                addPath(
                    it.pathData,
                    it.pathFillType,
                    it.name,
                    if (it.name == "card_color") SolidColor(color) else it.fill,
                    it.fillAlpha,
                    it.stroke,
                    it.strokeAlpha,
                    it.strokeLineWidth,
                    it.strokeLineCap,
                    it.strokeLineJoin,
                    it.strokeLineMiter,
                )
            }
        }.build()

private val TraitIcon.label: Int
    @StringRes get() =
        when (this) {
            TraitIcon.Action -> R.string.icon_action
            TraitIcon.Attachment -> R.string.icon_attachment
            TraitIcon.Coral -> R.string.icon_coral
            TraitIcon.Curious -> R.string.icon_curious
            TraitIcon.Dragon -> R.string.icon_dragon
            TraitIcon.DropOfLife -> R.string.icon_drop_of_life
            TraitIcon.Exotic -> R.string.icon_exotic
            TraitIcon.Fish -> R.string.icon_fish
            TraitIcon.PlayWhen -> R.string.icon_play_when
            TraitIcon.Shell -> R.string.icon_shell
            TraitIcon.Spell -> R.string.icon_spell
            TraitIcon.Suppress -> R.string.icon_suppress
            TraitIcon.WorldsEnd -> R.string.worlds_end
        }

// Only the symbols the enabled sets use are converted; the rest have none yet.
private val TraitIcon.symbol: Int?
    @DrawableRes get() =
        when (this) {
            TraitIcon.Action -> R.drawable.ic_symbol_action
            TraitIcon.DropOfLife -> R.drawable.ic_symbol_drop_of_life
            TraitIcon.PlayWhen -> R.drawable.ic_symbol_play_when
            TraitIcon.WorldsEnd -> R.drawable.ic_symbol_worlds_end
            else -> null
        }

@PreviewLightDark
@Composable
internal fun PillsPreview() =
    AtlasTheme {
        Row(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) {
            Pills(
                CardFace(
                    "Pills",
                    listOf(
                        CardFact.Color(CardColor.Blue),
                        CardFact.Kind(CardKind.Trait),
                        CardFact.Dominant,
                        CardFact.Icon(TraitIcon.Action),
                        CardFact.Icon(TraitIcon.WorldsEnd),
                        CardFact.Subspecies("Sample-ling"),
                    ),
                    null,
                    emptyList(),
                    emptyList(),
                ),
                CardLanguage.English,
            )
        }
    }
