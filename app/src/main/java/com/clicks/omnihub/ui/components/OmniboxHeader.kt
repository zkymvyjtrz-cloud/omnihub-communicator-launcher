package com.clicks.omnihub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.theme.ClicksOrange
import com.clicks.omnihub.ui.theme.ObsidianBorder
import com.clicks.omnihub.ui.theme.ObsidianCard
import com.clicks.omnihub.ui.theme.ObsidianSurface
import com.clicks.omnihub.ui.theme.TextMuted
import com.clicks.omnihub.ui.theme.TextPrimary
import com.clicks.omnihub.ui.theme.TextSecondary

/**
 * Ultra-clean, single-line Top Header + Mode Tabs designed to maximize screen real estate
 * on tall flagship displays like the Samsung Galaxy S23 Ultra.
 */
@Composable
fun CommunicatorHeaderBar(
    unreadCount: Int,
    activeLedColor: Color,
    isLiveBridgeConnected: Boolean,
    currentViewMode: HubViewMode,
    onSelectViewMode: (HubViewMode) -> Unit,
    onOpenLiveBridgeSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(ObsidianSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Compact Single-Line Status Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Clicks Dynamic Notification LED indicator
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(activeLedColor)
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OMNIHUB",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ClicksOrange.copy(alpha = 0.18f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$unreadCount UNREAD",
                        color = ClicksOrange,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Compact Live Bridge Status Pill (never wraps to 2 lines)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isLiveBridgeConnected) Color(0x2610B981) else Color(0x26F59E0B)
                    )
                    .border(
                        width = 1.dp,
                        color = if (isLiveBridgeConnected) Color(0xFF10B981).copy(alpha = 0.4f) else Color(0xFFF59E0B).copy(alpha = 0.4f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onOpenLiveBridgeSettings() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isLiveBridgeConnected) "● LIVE BRIDGE" else "● ENABLE LIVE",
                    color = if (isLiveBridgeConnected) Color(0xFF34D399) else Color(0xFFFBBF24),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sleek Single-Row Segmented Mode Tabs (Guaranteed single line on all font scales)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val shortTabLabels = mapOf(
                HubViewMode.STREAM to "Stream",
                HubViewMode.PEOPLE to "People",
                HubViewMode.VIP_FOCUS to "VIP",
                HubViewMode.APPS to "Apps"
            )

            HubViewMode.entries.forEach { mode ->
                val selected = mode == currentViewMode
                val label = shortTabLabels[mode] ?: mode.label
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) ClicksOrange else ObsidianCard)
                        .border(
                            width = 1.dp,
                            color = if (selected) ClicksOrange else ObsidianBorder,
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelectViewMode(mode) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Compact Universal Omnibox Command Bar & Channel Filter Pills.
 */
@Composable
fun OmniboxCommandBar(
    query: String,
    isComposerExpanded: Boolean,
    selectedPlatform: MessagingPlatform,
    platformCounts: Map<MessagingPlatform, Int>,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onToggleComposer: () -> Unit,
    onSelectPlatform: (MessagingPlatform) -> Unit,
    onSendOmniboxMessage: (recipient: String, platform: MessagingPlatform, body: String) -> Unit
) {
    var recipientInput by remember { mutableStateOf("Sarah Jenkins") }
    var composePlatform by remember { mutableStateOf(MessagingPlatform.TEAMS) }
    var messageBodyInput by remember { mutableStateOf("Slide 14 is approved — ready for the 10:30 AM call!") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(ObsidianSurface)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        // Compact Single-Line Omnibox Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(ObsidianCard)
                .border(
                    width = 1.dp,
                    color = if (query.isNotEmpty() || isComposerExpanded) ClicksOrange else ObsidianBorder,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Omnibox Command",
                tint = ClicksOrange,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search messages, @person, or apps...",
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(ClicksOrange),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            if (query.isNotEmpty() || isComposerExpanded) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Omnibox",
                        tint = TextSecondary,
                        modifier = Modifier.size(15.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ClicksOrange.copy(alpha = 0.18f))
                        .clickable { onToggleComposer() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "+ Compose",
                        color = ClicksOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // Expandable Universal Multi-Platform Composer Drawer
        AnimatedVisibility(visible = isComposerExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                color = ObsidianCard,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ClicksOrange.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNIVERSAL OMNIBOX COMPOSER",
                            color = ClicksOrange,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cross-Platform Dispatch",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Recipient Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurface)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "TO:",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = recipientInput,
                            onValueChange = { recipientInput = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            cursorBrush = SolidColor(ClicksOrange),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Channel Selector Pills (WhatsApp, Teams, Outlook, Gmail, SMS, Messenger)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MessagingPlatform.entries.filter { it != MessagingPlatform.ALL }.forEach { platform ->
                            val isSelected = composePlatform == platform
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(7.dp))
                                    .background(if (isSelected) platform.accentColor else ObsidianSurface)
                                    .border(
                                        width = 1.dp,
                                        color = platform.accentColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(7.dp)
                                    )
                                    .clickable { composePlatform = platform }
                                    .padding(horizontal = 9.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = platform.displayName,
                                    color = if (isSelected) Color.White else platform.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Message Body + Send Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = messageBodyInput,
                            onValueChange = { messageBodyInput = it },
                            textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                            cursorBrush = SolidColor(ClicksOrange),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianSurface)
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ClicksOrange)
                                .clickable {
                                    onSendOmniboxMessage(recipientInput, composePlatform, messageBodyInput)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Send",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // BlackBerry Hub Horizontal Channel Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MessagingPlatform.entries.forEach { platform ->
                val isSelected = platform == selectedPlatform
                val count = platformCounts[platform] ?: 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) platform.accentColor else ObsidianCard
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) platform.accentColor else platform.accentColor.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectPlatform(platform) }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${platform.displayName} · $count",
                        color = if (isSelected) Color.White else platform.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
