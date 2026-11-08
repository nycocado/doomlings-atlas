package dev.nycocado.atlas.ui.theme

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import dev.nycocado.atlas.about.AboutPreview
import dev.nycocado.atlas.about.AboutUpdatePreview
import dev.nycocado.atlas.about.NoticePreview
import dev.nycocado.atlas.carddetail.ActionNoteTokenDetailPreview
import dev.nycocado.atlas.carddetail.CardDetailLongNamePreview
import dev.nycocado.atlas.carddetail.CardDetailPreview
import dev.nycocado.atlas.carddetail.CatastropheDetailPreview
import dev.nycocado.atlas.carddetail.CitedCardDetailPreview
import dev.nycocado.atlas.carddetail.DecorInkBlocksPreview
import dev.nycocado.atlas.carddetail.DualColorDetailPreview
import dev.nycocado.atlas.carddetail.InkBlocksPreview
import dev.nycocado.atlas.carddetail.InkBlocksSample
import dev.nycocado.atlas.carddetail.InlineTokensPreview
import dev.nycocado.atlas.carddetail.LanguageSelectorPreview
import dev.nycocado.atlas.carddetail.PillsPreview
import dev.nycocado.atlas.carddetail.SubspeciesTokenDetailPreview
import dev.nycocado.atlas.carddetail.TraitIconPillsPreview
import dev.nycocado.atlas.carddetail.TraitWorldsEndDetailPreview
import dev.nycocado.atlas.carddetail.TranslatedCatastropheDetailPreview
import dev.nycocado.atlas.carddetail.TranslatedTraitDetailPreview
import dev.nycocado.atlas.carddetail.TranslatedTraitSample
import dev.nycocado.atlas.carddetail.TrinketBlocksPreview
import dev.nycocado.atlas.cardpane.CardBesideListPreview
import dev.nycocado.atlas.compendium.CardGroupCollapsedPreview
import dev.nycocado.atlas.compendium.CardGroupOpenPreview
import dev.nycocado.atlas.compendium.CardGroupOpenWidePreview
import dev.nycocado.atlas.compendium.ColorDiscPreview
import dev.nycocado.atlas.compendium.CompendiumBesideCardPreview
import dev.nycocado.atlas.compendium.CompendiumHalfCollapsedPreview
import dev.nycocado.atlas.compendium.CompendiumLongNamePreview
import dev.nycocado.atlas.compendium.CompendiumPreview
import dev.nycocado.atlas.compendium.CompendiumUpdatePreview
import dev.nycocado.atlas.compendium.FiltersAgesPreview
import dev.nycocado.atlas.compendium.FiltersGreenPreview
import dev.nycocado.atlas.compendium.FiltersPreview
import dev.nycocado.atlas.compendium.HeaderHalfCollapsedPreview
import dev.nycocado.atlas.compendium.HeaderPreview
import dev.nycocado.atlas.compendium.HeaderRailPreview
import dev.nycocado.atlas.compendium.KindSegmentsPreview
import dev.nycocado.atlas.compendium.SearchFieldPreview
import dev.nycocado.atlas.compendium.SearchFieldTypedPreview
import dev.nycocado.atlas.ui.component.CardStackPreview
import dev.nycocado.atlas.ui.component.CardThumbPreview
import dev.nycocado.atlas.ui.component.DockPreview
import dev.nycocado.atlas.ui.component.RoundButtonPreview
import dev.nycocado.atlas.ui.component.SunsetBandPreview
import dev.nycocado.atlas.ui.component.TraitGlyphPreview
import dev.nycocado.atlas.ui.component.UnofficialNoticePreview
import dev.nycocado.atlas.update.UpdateStickerPreview

@PreviewTest
@PreviewLightDark
@Composable
fun StickerScreenshot() = StickerPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun TraitGlyphScreenshot() = TraitGlyphPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CardThumbScreenshot() = CardThumbPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CardStackScreenshot() = CardStackPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun DockScreenshot() = DockPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CompendiumScreenshot() = CompendiumPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CompendiumHalfCollapsedScreenshot() = CompendiumHalfCollapsedPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CardGroupCollapsedScreenshot() = CardGroupCollapsedPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CardGroupOpenScreenshot() = CardGroupOpenPreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun CardGroupCollapsedFontScale200Screenshot() = CardGroupCollapsedPreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun CardGroupOpenFontScale200Screenshot() = CardGroupOpenPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CardDetailScreenshot() = CardDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CatastropheDetailScreenshot() = CatastropheDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun ActionNoteTokenDetailScreenshot() = ActionNoteTokenDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun TrinketBlocksScreenshot() = TrinketBlocksPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun InkBlocksScreenshot() = InkBlocksPreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun InkBlocksFontScale200Screenshot() = AtlasTheme { InkBlocksSample() }

@PreviewTest
@PreviewLightDark
@Composable
fun DecorInkBlocksScreenshot() = DecorInkBlocksPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun TraitWorldsEndDetailScreenshot() = TraitWorldsEndDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun SubspeciesTokenDetailScreenshot() = SubspeciesTokenDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun TraitIconPillsScreenshot() = TraitIconPillsPreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun TraitIconPillsFontScale200Screenshot() = TraitIconPillsPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun DualColorDetailScreenshot() = DualColorDetailPreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun DualColorDetailFontScale200Screenshot() = DualColorDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun InlineTokensScreenshot() = InlineTokensPreview()

@PreviewTest
@Preview(name = "Light", locale = "pt")
@Preview(name = "Dark", locale = "pt", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
fun TranslatedTraitDetailScreenshot() = TranslatedTraitDetailPreview()

@PreviewTest
@Preview(name = "Light", locale = "pt")
@Preview(name = "Dark", locale = "pt", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
fun TranslatedCatastropheDetailScreenshot() = TranslatedCatastropheDetailPreview()

@PreviewTest
@Preview(fontScale = 2f, locale = "pt")
@Composable
fun TranslatedTraitDetailFontScale200Screenshot() = AtlasTheme { TranslatedTraitSample() }

@PreviewTest
@PreviewLightDark
@Composable
fun UnofficialNoticeScreenshot() = UnofficialNoticePreview()

@PreviewTest
@PreviewLightDark
@Composable
fun NoticeScreenshot() = NoticePreview()

@PreviewTest
@PreviewLightDark
@Composable
fun AboutScreenshot() = AboutPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun AboutUpdateScreenshot() = AboutUpdatePreview()

@PreviewTest
@PreviewLightDark
@Composable
fun UpdateStickerScreenshot() = UpdateStickerPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CompendiumUpdateScreenshot() = CompendiumUpdatePreview()

@PreviewTest
@PreviewLightDark
@Composable
fun RoundButtonScreenshot() = RoundButtonPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun SunsetBandScreenshot() = SunsetBandPreview()

@PreviewTest
@Preview(locale = "pt")
@Composable
fun CompendiumPortugueseScreenshot() = CompendiumPreview()

@PreviewTest
@Preview(locale = "pt")
@Composable
fun CardDetailPortugueseScreenshot() = CardDetailPreview()

@PreviewTest
@Preview(locale = "pt")
@Composable
fun CatastropheDetailPortugueseScreenshot() = CatastropheDetailPreview()

@PreviewTest
@Preview(locale = "pt")
@Composable
fun AboutPortugueseScreenshot() = AboutPreview()

@PreviewTest
@Preview(locale = "pt")
@Composable
fun NoticePortugueseScreenshot() = NoticePreview()

@PreviewTest
@Preview(locale = "pt")
@Composable
fun UnofficialNoticePortugueseScreenshot() = UnofficialNoticePreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun CompendiumFontScale200Screenshot() = CompendiumLongNamePreview()

@PreviewTest
@Preview(fontScale = 2f, locale = "pt")
@Composable
fun CompendiumFontScale200PortugueseScreenshot() = CompendiumLongNamePreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun CardDetailFontScale200Screenshot() = CardDetailLongNamePreview()

@PreviewTest
@Preview(name = "Light", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(
    name = "Dark",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Composable
fun CardGroupOpenTabletScreenshot() = CardGroupOpenWidePreview()

@PreviewTest
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun CompendiumTabletScreenshot() = CompendiumPreview()

@PreviewTest
@Preview(name = "Light", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(
    name = "Dark",
    device = "spec:width=1280dp,height=800dp,dpi=240",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
)
@Composable
fun CompendiumUpdateTabletScreenshot() = CompendiumUpdatePreview()

@PreviewTest
@Preview(fontScale = 2f, locale = "pt")
@Composable
fun UpdateStickerFontScale200PortugueseScreenshot() = UpdateStickerPreview()

// One pane of the tablet above: beside a card, the list-detail scene gives the list and the card half the screen each.
private const val PANE = "spec:width=640dp,height=800dp,dpi=240"

@Preview(name = "Light", device = PANE)
@Preview(name = "Dark", device = PANE, uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
annotation class PanePreviews

@PreviewTest
@PanePreviews
@Composable
fun CompendiumBesideCardScreenshot() = CompendiumBesideCardPreview()

@PreviewTest
@PanePreviews
@Composable
fun CardBesideListScreenshot() = CardBesideListPreview()

// The narrowest rail layout, where the sticker's text and buttons no longer fit on one line.
@PreviewTest
@Preview(device = PANE, fontScale = 2f, locale = "pt")
@Composable
fun UpdateStickerPaneFontScale200PortugueseScreenshot() = UpdateStickerPreview()

@PreviewTest
@Preview(device = PANE, locale = "pt")
@Composable
fun CompendiumBesideCardPortugueseScreenshot() = CompendiumBesideCardPreview()

@PreviewTest
@Preview(device = PANE, locale = "pt")
@Composable
fun CardBesideListPortugueseScreenshot() = CardBesideListPreview()

@PreviewTest
@Preview(locale = "es")
@Composable
fun CompendiumSpanishScreenshot() = CompendiumPreview()

@PreviewTest
@Preview(locale = "es")
@Composable
fun CardDetailSpanishScreenshot() = CardDetailPreview()

@PreviewTest
@Preview(locale = "es")
@Composable
fun CatastropheDetailSpanishScreenshot() = CatastropheDetailPreview()

@PreviewTest
@Preview(locale = "es")
@Composable
fun AboutSpanishScreenshot() = AboutPreview()

@PreviewTest
@Preview(locale = "es")
@Composable
fun NoticeSpanishScreenshot() = NoticePreview()

@PreviewTest
@Preview(locale = "es")
@Composable
fun UnofficialNoticeSpanishScreenshot() = UnofficialNoticePreview()

@PreviewTest
@Preview(fontScale = 2f, locale = "es")
@Composable
fun CompendiumFontScale200SpanishScreenshot() = CompendiumLongNamePreview()

@PreviewTest
@Preview(device = PANE, locale = "es")
@Composable
fun CompendiumBesideCardSpanishScreenshot() = CompendiumBesideCardPreview()

@PreviewTest
@Preview(device = PANE, locale = "es")
@Composable
fun CardBesideListSpanishScreenshot() = CardBesideListPreview()

@PreviewTest
@Preview(name = "Light", locale = "es")
@Preview(name = "Dark", locale = "es", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
fun TranslatedTraitDetailSpanishScreenshot() = TranslatedTraitDetailPreview()

@PreviewTest
@Preview(name = "Light", locale = "es")
@Preview(name = "Dark", locale = "es", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
fun TranslatedCatastropheDetailSpanishScreenshot() = TranslatedCatastropheDetailPreview()

@PreviewTest
@Preview(fontScale = 2f, locale = "es")
@Composable
fun TranslatedTraitDetailFontScale200SpanishScreenshot() = AtlasTheme { TranslatedTraitSample() }

@PreviewTest
@PreviewLightDark
@Composable
fun CitedCardDetailScreenshot() = CitedCardDetailPreview()

@PreviewTest
@Preview(fontScale = 2f)
@Composable
fun CitedCardDetailFontScale200Screenshot() = CitedCardDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun PillsScreenshot() = PillsPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun LanguageSelectorScreenshot() = LanguageSelectorPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun HeaderScreenshot() = HeaderPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun HeaderHalfCollapsedScreenshot() = HeaderHalfCollapsedPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun HeaderRailScreenshot() = HeaderRailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun SearchFieldScreenshot() = SearchFieldPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun SearchFieldTypedScreenshot() = SearchFieldTypedPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun FiltersScreenshot() = FiltersPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun FiltersGreenScreenshot() = FiltersGreenPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun FiltersAgesScreenshot() = FiltersAgesPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun ColorDiscScreenshot() = ColorDiscPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun KindSegmentsScreenshot() = KindSegmentsPreview()
