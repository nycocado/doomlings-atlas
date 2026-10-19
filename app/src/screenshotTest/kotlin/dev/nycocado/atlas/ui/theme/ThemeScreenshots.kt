package dev.nycocado.atlas.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.tools.screenshot.PreviewTest
import dev.nycocado.atlas.compendium.CompendiumPreview
import dev.nycocado.atlas.ui.component.CardThumbPreview
import dev.nycocado.atlas.ui.component.DockPreview
import dev.nycocado.atlas.ui.component.TraitGlyphPreview

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
