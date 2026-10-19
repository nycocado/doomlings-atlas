package dev.nycocado.atlas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.theme.AtlasTheme
import dev.nycocado.atlas.ui.theme.AtlasTypography

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AtlasTheme {
                // Placeholder until the compendium screen: shows that the cards load.
                val results = hiltViewModel<CompendiumViewModel>().results.collectAsStateWithLifecycle().value
                Box(Modifier.fillMaxSize().background(AtlasTheme.colors.surface), Alignment.Center) {
                    BasicText(
                        "${getString(R.string.app_name)}: ${results?.size ?: "…"} traits",
                        style = AtlasTypography.headline.copy(color = AtlasTheme.colors.onSurface),
                    )
                }
            }
        }
    }
}
