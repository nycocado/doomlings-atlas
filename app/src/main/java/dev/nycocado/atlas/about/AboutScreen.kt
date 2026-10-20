package dev.nycocado.atlas.about

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withJson
import dev.nycocado.atlas.R
import dev.nycocado.atlas.ui.component.BackButton
import dev.nycocado.atlas.ui.component.SunsetBand
import dev.nycocado.atlas.ui.component.UnofficialSticker
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography
import dev.nycocado.atlas.ui.theme.MaxWidth
import dev.nycocado.atlas.ui.theme.Radius
import dev.nycocado.atlas.ui.theme.Space
import dev.nycocado.atlas.ui.theme.sticker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LicenseEntry(
    val id: String,
    val name: String,
    val license: String,
    val text: String?,
)

data class Licenses(
    val app: LicenseEntry,
    val fonts: List<LicenseEntry>,
    val libraries: List<LicenseEntry>,
)

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val licenses by produceState<Licenses?>(null) {
        value = withContext(Dispatchers.IO) { loadLicenses(context) }
    }
    val version =
        context.packageManager
            .getPackageInfo(context.packageName, 0)
            .versionName
            .orEmpty()
    AboutContent(version, licenses, onBack)
}

private fun loadLicenses(context: Context): Licenses {
    fun read(path: String) =
        context.assets
            .open("licenses/$path")
            .bufferedReader()
            .use { it.readText() }

    // OFL texts start with "Copyright ... The <font> Project Authors".
    val fonts =
        context.assets.list("licenses").orEmpty().filter { it.endsWith("-OFL.txt") }.map { file ->
            val text = read(file)
            val name =
                Regex("The (.+?) Project Authors").find(text)?.groupValues?.get(1) ?: file.removeSuffix("-OFL.txt")
            LicenseEntry(file, name, "OFL-1.1", text)
        }
    // Generated at build time by the AboutLibraries plugin from the app's dependencies.
    val libraries =
        Libs
            .Builder()
            .withJson(context, R.raw.aboutlibraries)
            .build()
            .libraries
            .map { library ->
                LicenseEntry(
                    library.uniqueId,
                    library.name,
                    library.licenses.joinToString { it.spdxId ?: it.name },
                    library.licenses
                        .mapNotNull { it.licenseContent }
                        .joinToString("\n\n")
                        .ifEmpty { null },
                )
            }.sortedBy { it.name.lowercase() }
    return Licenses(
        LicenseEntry("app", context.getString(R.string.app_name), "GPL-3.0", read("LICENSE")),
        fonts,
        libraries,
    )
}

@Composable
fun AboutContent(
    version: String,
    licenses: Licenses?,
    onBack: () -> Unit,
) {
    val colors = AtlasTheme.colors
    var open by rememberSaveable { mutableStateOf<String?>(null) }
    val toggle = { id: String -> open = if (open == id) null else id }
    LazyColumn(
        Modifier.fillMaxSize().background(colors.surface),
        contentPadding = PaddingValues(bottom = Space.s6),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Header(onBack) }
        item {
            Column(
                Modifier.widthIn(max = MaxWidth.content).padding(horizontal = Space.s5, vertical = Space.s6),
                verticalArrangement = Arrangement.spacedBy(Space.s3),
            ) {
                UnofficialSticker()
                BasicText(
                    stringResource(R.string.version, version),
                    Modifier.padding(top = Space.s2),
                    style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant),
                )
            }
        }
        if (licenses != null) {
            section(R.string.licenses_app, listOf(licenses.app), open, toggle)
            section(R.string.licenses_fonts, licenses.fonts, open, toggle)
            section(R.string.licenses_libraries, licenses.libraries, open, toggle)
        }
        item { Spacer(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) }
    }
}

private fun LazyListScope.section(
    @StringRes title: Int,
    entries: List<LicenseEntry>,
    open: String?,
    onToggle: (String) -> Unit,
) {
    item(key = title) {
        BasicText(
            stringResource(title).uppercase(),
            Modifier
                .widthIn(max = MaxWidth.content)
                .fillMaxWidth()
                .padding(start = Space.s5, end = Space.s5, top = Space.s4, bottom = Space.s2)
                .semantics { heading() },
            style = AtlasTypography.labelSm.copy(color = AtlasTheme.colors.onSurfaceVariant),
        )
    }
    items(entries, key = { it.id }) { entry ->
        LicenseRow(
            entry,
            open == entry.id,
            { onToggle(entry.id) },
            Modifier.widthIn(max = MaxWidth.content).padding(horizontal = Space.s5, vertical = 6.dp),
        )
    }
}

@Composable
private fun LicenseRow(
    entry: LicenseEntry,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AtlasTheme.colors
    Column(
        modifier
            .fillMaxWidth()
            .sticker(Radius.cardLg, colors.surfaceSticker, colors.outline)
            .clickable(enabled = entry.text != null, onClick = onClick)
            .padding(horizontal = Space.s4, vertical = Space.s3),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Space.s3)) {
            BasicText(
                entry.name,
                Modifier.weight(1f),
                style = AtlasTypography.labelLg.copy(color = colors.onSurface),
            )
            BasicText(entry.license, style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant))
        }
        AnimatedVisibility(expanded) {
            BasicText(
                entry.text.orEmpty(),
                Modifier.padding(top = Space.s3),
                style = AtlasTypography.bodySm.copy(color = colors.onSurfaceVariant),
            )
        }
    }
}

@Composable
private fun Header(onBack: () -> Unit) {
    val colors = AtlasTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(colors.inverseSurface)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            Modifier.padding(start = Space.s4, end = Space.s5, top = Space.s3, bottom = Space.s4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Space.s4),
        ) {
            BackButton(onBack)
            BasicText(
                stringResource(R.string.about),
                Modifier.semantics { heading() },
                style = AtlasTypography.headline.copy(color = colors.onInverseSurface),
            )
        }
        SunsetBand()
    }
}

@PreviewLightDark
@Composable
internal fun AboutPreview() =
    AtlasTheme {
        AboutContent(
            "1.0.0",
            Licenses(
                LicenseEntry("app", "Doomlings Atlas", "GPL-3.0", "License text."),
                listOf(LicenseEntry("font", "A font", "OFL-1.1", "License text.")),
                listOf(LicenseEntry("lib", "A library", "Apache-2.0", null)),
            ),
            onBack = {},
        )
    }
