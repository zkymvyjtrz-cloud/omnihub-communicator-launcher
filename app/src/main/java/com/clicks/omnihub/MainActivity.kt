package com.clicks.omnihub

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.clicks.omnihub.ui.HubViewModel
import com.clicks.omnihub.ui.OmniHubLauncherScreen
import com.clicks.omnihub.ui.theme.OmniHubTheme

/**
 * Main Home Screen Activity for the OmniHub Communicator Launcher.
 *
 * - Registered for `android.intent.category.HOME` so it can serve as your primary Android Home Launcher.
 * - Intercepts physical QWERTY keyboard shortcuts (Clicks Communicator, Unihertz, or Bluetooth keyboards):
 *   - `R` -> Inline Direct Reply via Android RemoteInput
 *   - `A` -> Archive / Mark message Done
 *   - `S` -> Snooze message for 1 hour
 *   - `P` -> Toggle Person-Centric Unified Threads
 *   - `F` -> Toggle VIP Priority Shield
 *   - `L` -> Toggle Monochrome Minimal App Launcher
 *   - `/` -> Open Next-Gen Omnibox Command Bar
 *   - `J` / `K` -> Move message focus Down / Up
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

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && !event.isPrintingKey && keyCode == KeyEvent.KEYCODE_SLASH) {
            if (viewModel.handleHardwareShortcutKey('/')) return true
        }
        val unicodeChar = event?.unicodeChar ?: 0
        if (unicodeChar > 0) {
            val ch = unicodeChar.toChar()
            // Trigger single-key hardware triage shortcuts when Omnibox composer isn't capturing free text
            if (!viewModel.isOmniboxComposerOpen.value && viewModel.inlineReplyingMessageId.value == null) {
                if (viewModel.handleHardwareShortcutKey(ch)) {
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // As a Home Screen Launcher, pressing Back clears Omnibox or returns to the primary Hub Stream
        // rather than exiting the home screen.
        if (viewModel.isOmniboxComposerOpen.value || viewModel.omniboxQuery.value.isNotEmpty()) {
            viewModel.clearOmnibox()
        }
    }
}
