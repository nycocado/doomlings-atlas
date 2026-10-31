package dev.nycocado.atlas

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import dev.nycocado.atlas.compendium.CompendiumViewModel
import dev.nycocado.atlas.ui.theme.AtlasTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: CompendiumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Headers are dark in both themes, so status bar icons are light unless a screen says otherwise.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT))
        viewModel.refreshDeviceLanguage()
        setContent {
            AtlasTheme { AtlasApp() }
        }
    }
}
