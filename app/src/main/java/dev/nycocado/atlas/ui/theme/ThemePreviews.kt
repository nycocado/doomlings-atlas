package dev.nycocado.atlas.ui.theme

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

// Required on every component and screen: renders it in both themes.
@Preview(name = "Day")
@Preview(name = "Game night", uiMode = UI_MODE_NIGHT_YES)
annotation class ThemePreviews
