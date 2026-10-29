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
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.ui.theme.ClicksOrange
import com.clicks.omnihub.ui.theme.ObsidianBorder
import com.clicks.omnihub.ui.theme.ObsidianCard
import com.clicks.omnihub.ui.theme.ObsidianSurface
import com.clicks.omnihub.ui.theme.TextPrimary
import com.clicks.omnihub.ui.theme.TextSecondary

/**
 * Sleek, compact Bottom Action Bar & Optional Clicks Tactile QWERTY Keypad.
 * Collapses by default on full-touch phones like the Samsung S23 Ultra so 80%+ of the screen
 * is dedicated to your messages and apps, with non-wrapping action buttons.
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
            .wrapContentHeight()
            .background(ObsidianSurface)
            .border(1.dp, ObsidianBorder)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        // Compact Status Ticker + Keypad Toggle + Reset
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
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = statusBannerText,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide Keys" else "Keypad",
                    color = ClicksOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.clickable { onToggleExpanded() }
                )
                Text(
                    text = "Reset",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.clickable { onResetDemo() }
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Non-wrapping Speed-Action Dock (Guaranteed single line on S23 Ultra)
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
                actionLabel = "Later",
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
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0E1015))
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HardwareKeyButton(
                        label = "/ CMD",
                        isHighlighted = true,
                        modifier = Modifier.weight(1.8f),
                        onClick = { onShortcutKey('/') }
                    )
                    Box(
                        modifier = Modifier
                            .weight(4.5f)
                            .clip(RoundedCornerShape(7.dp))
                            .background(ObsidianCard)
                            .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(7.dp))
                            .clickable { onSpaceScroll() }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SPACE · SCROLL",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
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
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = keyLabel,
            color = accentColor,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = actionLabel,
            color = TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
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
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHighlighted) Color(0xFF222634) else Color(0xFF181B24))
            .border(
                width = 1.dp,
                color = if (isHighlighted) ClicksOrange.copy(alpha = 0.55f) else ObsidianBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isHighlighted) ClicksOrange else TextPrimary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false
        )
    }
}
