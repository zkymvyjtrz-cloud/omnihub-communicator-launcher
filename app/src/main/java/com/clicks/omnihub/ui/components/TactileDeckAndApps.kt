package com.clicks.omnihub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.ui.theme.ClicksOrange
import com.clicks.omnihub.ui.theme.ObsidianBorder
import com.clicks.omnihub.ui.theme.ObsidianCard
import com.clicks.omnihub.ui.theme.ObsidianSurface
import com.clicks.omnihub.ui.theme.TextMuted
import com.clicks.omnihub.ui.theme.TextPrimary
import com.clicks.omnihub.ui.theme.TextSecondary

/**
 * Clicks Communicator Tactile Shortcut Dock & Optional On-Screen Physical QWERTY Deck.
 * Gives touchscreen phones the exact single-key speed of a physical Clicks / BlackBerry keypad,
 * while also reacting to real hardware keyboard presses.
 */
@Composable
fun ClicksTactileShortcutDeck(
    statusBannerText: String,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onShortcutKey: (Char) -> Unit,
    onSpaceScroll: () -> Unit,
    onResetDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Live Status Ticker + Toggle QWERTY Deck + Reset Demo
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusBannerText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = if (isExpanded) "Hide Keypad" else "Clicks Keypad",
                    color = ClicksOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onToggleExpanded() }
                )
                Text(
                    text = "Reset",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.clickable { onResetDemo() }
                )
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        // Always-Accessible Speed-Action Pill Bar (R, A, S, P, F, /, SPACE)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TactileActionPill(
                keyLabel = "R",
                actionLabel = "Reply",
                accentColor = ClicksOrange,
                modifier = Modifier.weight(1f),
                onClick = { onShortcutKey('R') }
            )
            TactileActionPill(
                keyLabel = "A",
                actionLabel = "Done",
                accentColor = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { onShortcutKey('A') }
            )
            TactileActionPill(
                keyLabel = "S",
                actionLabel = "Snooze",
                accentColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                onClick = { onShortcutKey('S') }
            )
            TactileActionPill(
                keyLabel = "P",
                actionLabel = "People",
                accentColor = Color(0xFFA78BFA),
                modifier = Modifier.weight(1f),
                onClick = { onShortcutKey('P') }
            )
            TactileActionPill(
                keyLabel = "/",
                actionLabel = "Omni",
                accentColor = ClicksOrange,
                modifier = Modifier.weight(1f),
                onClick = { onShortcutKey('/') }
            )
        }

        // Expandable Clicks Tactile Hardware Key Deck
        AnimatedVisibility(visible = isExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF0E1015))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf('Q', 'W', 'E', 'R', 'T', 'Y', 'U', 'I', 'O', 'P').forEach { ch ->
                        val isHighlighted = ch == 'R' || ch == 'P'
                        HardwareKeyButton(
                            label = ch.toString(),
                            isHighlighted = isHighlighted,
                            modifier = Modifier.weight(1f),
                            onClick = { onShortcutKey(ch) }
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf('A', 'S', 'D', 'F', 'G', 'H', 'J', 'K', 'L').forEach { ch ->
                        val isHighlighted = ch == 'A' || ch == 'S' || ch == 'F' || ch == 'J' || ch == 'K'
                        HardwareKeyButton(
                            label = ch.toString(),
                            isHighlighted = isHighlighted,
                            modifier = Modifier.weight(1f),
                            onClick = { onShortcutKey(ch) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HardwareKeyButton(
                        label = "/ CMD",
                        isHighlighted = true,
                        modifier = Modifier.weight(1.8f),
                        onClick = { onShortcutKey('/') }
                    )
                    // Capacitive Spacebar Scroll + Fingerprint Sensor Pill
                    Box(
                        modifier = Modifier
                            .weight(4.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianCard)
                            .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                            .clickable { onSpaceScroll() }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, ClicksOrange, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SPACE · PAGE SCROLL",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HardwareKeyButton(
                        label = "NEXT [J]",
                        isHighlighted = false,
                        modifier = Modifier.weight(1.8f),
                        onClick = { onShortcutKey('J') }
                    )
                }
            }
        }
    }
}

@Composable
private fun TactileActionPill(
    keyLabel: String,
    actionLabel: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(9.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(accentColor.copy(alpha = 0.2f))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = keyLabel,
                color = accentColor,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = actionLabel,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HardwareKeyButton(
    label: String,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(7.dp))
            .background(if (isHighlighted) Color(0xFF222634) else Color(0xFF181B24))
            .border(
                width = 1.dp,
                color = if (isHighlighted) ClicksOrange.copy(alpha = 0.55f) else ObsidianBorder,
                shape = RoundedCornerShape(7.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isHighlighted) ClicksOrange else TextPrimary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Monochrome Anti-Doomscrolling Minimal App Drawer (Mode 04: Minimal Apps).
 * Replaces addictive colorful icon grids with clean, high-contrast speed-dial typography.
 */
@Composable
fun MinimalSpeedDialAppList(
    installedApps: List<InstalledAppInfo>,
    searchQuery: String,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit
) {
    val essentialDefaults = listOf(
        InstalledAppInfo("Phone Dialer", "com.google.android.dialer", "Essential"),
        InstalledAppInfo("Calendar & Agenda", "com.google.android.calendar", "Schedule"),
        InstalledAppInfo("WhatsApp", "com.whatsapp", "Comms"),
        InstalledAppInfo("Microsoft Teams", "com.microsoft.teams", "Comms"),
        InstalledAppInfo("Microsoft Outlook", "com.microsoft.office.outlook", "Comms"),
        InstalledAppInfo("Gmail", "com.google.android.gm", "Comms"),
        InstalledAppInfo("Messages (SMS)", "com.google.android.apps.messaging", "Comms"),
        InstalledAppInfo("Maps & Transit", "com.google.android.apps.maps", "Navigation"),
        InstalledAppInfo("Camera", "com.android.camera", "Hardware")
    )

    val mergedApps = (installedApps.ifEmpty { essentialDefaults }).filter {
        searchQuery.isBlank() || it.label.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Set as Default Home Launcher Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ClicksOrange.copy(alpha = 0.14f))
                .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                .clickable { onOpenDefaultLauncherSettings() }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ANTI-DOOMSCROLLING MINIMAL LAUNCHER",
                    color = ClicksOrange,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Monochrome text-only app launcher · Tap to set OmniHub as Default Home",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ClicksOrange)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Set Default",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Monochrome App Rows
        mergedApps.forEachIndexed { index, app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ObsidianCard)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .clickable { onLaunchApp(app) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = String.format("%02d", index + 1),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = app.label,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianSurface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = app.categoryTag,
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
