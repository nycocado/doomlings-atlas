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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.fromHtml
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import dev.nycocado.atlas.R
import dev.nycocado.atlas.card.CardColor
import dev.nycocado.atlas.card.CardFace
import dev.nycocado.atlas.card.CardFact
import dev.nycocado.atlas.card.CardKind
import dev.nycocado.atlas.card.CardLanguage
import dev.nycocado.atlas.card.TagQualifier
import dev.nycocado.atlas.card.TagSubject
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

                is CardFact.Tagged -> {
                    val ink =
                        when (fact.qualifier) {
                            TagQualifier.Curious -> theme.tagCurious
                            TagQualifier.Exotic -> theme.tagExotic
                        }
                    Pill(qualifiedLabel(cardString(fact.label, language), ink, language), theme.tag, theme.onTag) {
                        Symbol(fact.subject.glyph, Modifier.height(18.dp))
                    }
                }

                is CardFact.Icon if fact.icon == TraitIcon.Dragon -> {
                    Pill(tagged(cardString(fact.icon.label, language), language), theme.tag, theme.onTag) {
                        Symbol(R.drawable.ic_symbol_glyph_dragon, Modifier.height(18.dp))
                    }
                }

                is CardFact.Icon -> {
                    Pill(
                        cardString(fact.icon.label, language),
                        theme.surfaceSticker,
                        theme.onSurface,
                        language = language,
                        glyph =
                            fact.icon.symbol()?.let {
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
) = Pill(language?.let { tagged(text, it) } ?: AnnotatedString(text), fill, content, glyph)

@Composable
private fun Pill(
    text: AnnotatedString,
    fill: Color,
    content: Color,
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
            text,
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

// Curious and Exotic never show alone: CardFact.Tagged carries them, and the card data test keeps them paired.
@DrawableRes
private fun TraitIcon.symbol(): Int? =
    when (this) {
        TraitIcon.Action -> R.drawable.ic_symbol_action
        TraitIcon.Attachment -> R.drawable.ic_symbol_attachment
        TraitIcon.Coral -> R.drawable.ic_symbol_coral
        TraitIcon.Curious -> null
        TraitIcon.Dragon -> R.drawable.ic_symbol_glyph_dragon
        TraitIcon.DropOfLife -> R.drawable.ic_symbol_drop_of_life
        TraitIcon.Exotic -> null
        TraitIcon.Fish -> R.drawable.ic_symbol_fish
        TraitIcon.PlayWhen -> R.drawable.ic_symbol_play_when
        TraitIcon.Shell -> R.drawable.ic_symbol_shell
        TraitIcon.Spell -> R.drawable.ic_symbol_spell
        TraitIcon.Suppress -> R.drawable.ic_symbol_suppress
        TraitIcon.WorldsEnd -> R.drawable.ic_symbol_worlds_end
    }

private val TagSubject.glyph: Int
    @DrawableRes get() =
        when (this) {
            TagSubject.Coral -> R.drawable.ic_symbol_glyph_coral
            TagSubject.Fish -> R.drawable.ic_symbol_glyph_fish
            TagSubject.Shell -> R.drawable.ic_symbol_glyph_shell
        }

private val CardFact.Tagged.label: Int
    @StringRes get() =
        when (qualifier) {
            TagQualifier.Curious -> {
                when (subject) {
                    TagSubject.Coral -> R.string.tag_curious_coral
                    TagSubject.Fish -> R.string.tag_curious_fish
                    TagSubject.Shell -> R.string.tag_curious_shell
                }
            }

            TagQualifier.Exotic -> {
                when (subject) {
                    TagSubject.Coral -> R.string.tag_exotic_coral
                    TagSubject.Fish -> R.string.tag_exotic_fish
                    TagSubject.Shell -> R.string.tag_exotic_shell
                }
            }
        }

// The label's <b> marks the qualifier, wherever the language puts it, and it shows in the given color. HTML rather than
// string annotations, which the preview renderer's resources drop.
private fun qualifiedLabel(
    html: String,
    color: Color,
    language: CardLanguage,
): AnnotatedString {
    val marked = AnnotatedString.fromHtml(html)
    return buildAnnotatedString {
        append(tagged(marked.text, language))
        marked.spanStyles.forEach { addStyle(SpanStyle(color = color), it.start, it.end) }
    }
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

// Every icon that shows on its own, then the six tags.
@PreviewLightDark
@Composable
internal fun TraitIconPillsPreview() =
    AtlasTheme {
        Column(
            Modifier.background(AtlasTheme.colors.surface).padding(Space.s4),
            verticalArrangement = Arrangement.spacedBy(Space.s3),
        ) {
            val icons = (TraitIcon.entries - TraitIcon.Curious - TraitIcon.Exotic).map(CardFact::Icon)
            val tags = TagQualifier.entries.flatMap { q -> TagSubject.entries.map { CardFact.Tagged(q, it) } }
            Pills(CardFace("Pills", icons + tags, null, emptyList(), emptyList()), CardLanguage.English)
        }
    }
