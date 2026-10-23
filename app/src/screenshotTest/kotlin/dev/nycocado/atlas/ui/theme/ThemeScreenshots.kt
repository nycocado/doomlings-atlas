package dev.nycocado.atlas.ui.theme

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import dev.nycocado.atlas.about.AboutPreview
import dev.nycocado.atlas.about.NoticePreview
import dev.nycocado.atlas.carddetail.CardDetailPreview
import dev.nycocado.atlas.carddetail.CatastropheDetailPreview
import dev.nycocado.atlas.cardpane.CardBesideListPreview
import dev.nycocado.atlas.compendium.CompendiumBesideCardPreview
import dev.nycocado.atlas.compendium.CompendiumHalfCollapsedPreview
import dev.nycocado.atlas.compendium.CompendiumPreview
import dev.nycocado.atlas.ui.component.CardThumbPreview
import dev.nycocado.atlas.ui.component.DockPreview
import dev.nycocado.atlas.ui.component.RoundButtonPreview
import dev.nycocado.atlas.ui.component.SunsetBandPreview
import dev.nycocado.atlas.ui.component.TraitGlyphPreview
import dev.nycocado.atlas.ui.component.UnofficialNoticePreview

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
fun CardDetailScreenshot() = CardDetailPreview()

@PreviewTest
@PreviewLightDark
@Composable
fun CatastropheDetailScreenshot() = CatastropheDetailPreview()

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
@Preview(device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun CompendiumTabletScreenshot() = CompendiumPreview()

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
