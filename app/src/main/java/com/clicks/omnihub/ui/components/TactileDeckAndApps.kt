package com.clicks.omnihub.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.ui.theme.M3SurfaceContainer

/**
 * Clean Material 3 Bottom NavigationBar.
 * Replaces the simulated QWERTY keypad so the device's native keyboard is used exclusively.
 */
@Composable
fun OmniHubBottomNavigationBar(
    currentMode: HubViewMode,
    onSelectMode: (HubViewMode) -> Unit
) {
    NavigationBar(
        containerColor = M3SurfaceContainer
    ) {
        HubViewMode.entries.forEach { mode ->
            val selected = mode == currentMode
            val icon = when (mode) {
                HubViewMode.STREAM -> Icons.Default.Inbox
                HubViewMode.PEOPLE -> Icons.Default.Group
                HubViewMode.VIP_FOCUS -> Icons.Default.Star
                HubViewMode.APPS -> Icons.Default.Apps
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectMode(mode) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.label
                    )
                },
                label = {
                    Text(
                        text = mode.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}
