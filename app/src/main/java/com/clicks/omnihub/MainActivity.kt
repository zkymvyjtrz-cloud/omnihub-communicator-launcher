package com.clicks.omnihub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.clicks.omnihub.ui.HubViewModel
import com.clicks.omnihub.ui.OmniHubLauncherScreen
import com.clicks.omnihub.ui.theme.OmniHubTheme

/**
 * Main Home Screen Activity for the Airframe-styled OmniHub Communicator Launcher.
 * Exclusively uses the device's default software/hardware keyboard and the Airframe Design System (Ivory & Night).
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isNightTheme by viewModel.isNightTheme.collectAsState()
            OmniHubTheme(isNightTheme = isNightTheme) {
                OmniHubLauncherScreen(viewModel = viewModel)
            }
        }
    }
}
