package com.clicks.omnihub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.clicks.omnihub.ui.HubViewModel
import com.clicks.omnihub.ui.OmniHubLauncherScreen
import com.clicks.omnihub.ui.theme.OmniHubTheme

/**
 * Main Home Screen Activity for the Material 3 OmniHub Communicator Launcher.
 * Exclusively uses the device's default software/hardware keyboard and standard Material 3 navigation.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: HubViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OmniHubTheme {
                OmniHubLauncherScreen(viewModel = viewModel)
            }
        }
    }
}
