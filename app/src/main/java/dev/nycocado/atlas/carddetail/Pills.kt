package dev.nycocado.atlas.carddetail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
    setName: String? = null,
) {
    val theme = AtlasTheme.colors
    val icons = face.facts.filterIsInstance<CardFact.Icon>().map { it.icon }
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
                    // The set's name is never translated.
                    setName?.let { Pill(it, theme.surfaceSticker, theme.onSurface, language = CardLanguage.English) }
                }

                CardFact.Dominant -> {
                    Pill(
                        cardString(R.string.dominant, language),
                        theme.highlight,
                        theme.onHighlight,
                        language = language,
                    ) {
                        Symbol(R.drawable.ic_symbol_dominant, Modifier.height(18.dp))
                    }
                }

                is CardFact.Icon -> {
                    Pill(
                        cardString(fact.icon.label, language),
                        theme.surfaceSticker,
                        theme.onSurface,
                        language = language,
                        glyph =
                            fact.icon.symbol(icons)?.let {
                                {
                                    Symbol(
                                        it,
                                        Modifier.height(18.dp),
                                        actionColors = theme.actionColors(face.colors),
                                    )
                                }
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
            CardKind.GenePool -> R.string.type_gene_pool
            CardKind.MeaningOfLife -> R.string.type_meaning_of_life
            CardKind.Treasure -> R.string.type_treasure
            CardKind.Trinket -> R.string.type_trinket
            CardKind.Merchant -> R.string.type_merchant
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

// Curious and Exotic are printed as one tag with the card's coral, fish or shell, which the card data test guarantees.
@DrawableRes
private fun TraitIcon.symbol(icons: List<TraitIcon>): Int? =
    when (this) {
        TraitIcon.Action -> {
            R.drawable.ic_symbol_action
        }

        TraitIcon.Attachment -> {
            R.drawable.ic_symbol_attachment
        }

        TraitIcon.Coral -> {
            R.drawable.ic_symbol_coral
        }

        TraitIcon.Curious -> {
            tagOf(
                icons,
                R.drawable.ic_symbol_tag_curious_coral,
                R.drawable.ic_symbol_tag_curious_fish,
                R.drawable.ic_symbol_tag_curious_shell,
            )
        }

        TraitIcon.Dragon -> {
            R.drawable.ic_symbol_tag_dragon
        }

        TraitIcon.DropOfLife -> {
            R.drawable.ic_symbol_drop_of_life
        }

        TraitIcon.Exotic -> {
            tagOf(
                icons,
                R.drawable.ic_symbol_tag_exotic_coral,
                R.drawable.ic_symbol_tag_exotic_fish,
                R.drawable.ic_symbol_tag_exotic_shell,
            )
        }

        TraitIcon.Fish -> {
            R.drawable.ic_symbol_fish
        }

        TraitIcon.PlayWhen -> {
            R.drawable.ic_symbol_play_when
        }

        TraitIcon.Shell -> {
            R.drawable.ic_symbol_shell
        }

        TraitIcon.Spell -> {
            R.drawable.ic_symbol_spell
        }

        TraitIcon.Suppress -> {
            R.drawable.ic_symbol_suppress
        }

        TraitIcon.WorldsEnd -> {
            R.drawable.ic_symbol_worlds_end
        }
    }

@DrawableRes
private fun tagOf(
    icons: List<TraitIcon>,
    @DrawableRes coral: Int,
    @DrawableRes fish: Int,
    @DrawableRes shell: Int,
): Int? =
    when {
        TraitIcon.Coral in icons -> coral
        TraitIcon.Fish in icons -> fish
        TraitIcon.Shell in icons -> shell
        else -> null
    }

@PreviewLightDark
@Composable
internal fun SetPillPreview() =
    AtlasTheme {
        Row(Modifier.background(AtlasTheme.colors.surface).padding(Space.s4)) {
            Pills(
                CardFace("Pills", listOf(CardFact.Kind(CardKind.Trait)), null, emptyList(), emptyList()),
                CardLanguage.English,
                setName = "Legends of Enderas",
            )
        }
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

// Every trait icon, then Curious and Exotic with each coral, fish and shell, whose tag they are printed on.
@PreviewLightDark
@Composable
internal fun TraitIconPillsPreview() =
    AtlasTheme {
        Column(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s3),
        ) {
            val rows =
                listOf(TraitIcon.entries) +
                    listOf(TraitIcon.Coral, TraitIcon.Fish, TraitIcon.Shell).map {
                        listOf(TraitIcon.Curious, TraitIcon.Exotic, it)
                    }
            rows.forEach { icons ->
                Pills(
                    CardFace("Pills", icons.map(CardFact::Icon), null, emptyList(), emptyList()),
                    CardLanguage.English,
                )
            }
        }
    }
