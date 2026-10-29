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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Top hardware-inspired status bar with dynamic Clicks Notification LED ring,
 * clock, unread badge counter, and View Mode switcher.
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
    val timeText = remember {
        SimpleDateFormat("HH:mm · EEE MMM d", Locale.US).format(Date())
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // Top hardware status row + Dynamic Notification LED
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Clicks Dynamic Notification LED indicator
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .clip(CircleShape)
                        .background(activeLedColor)
                        .border(2.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "OMNIHUB COMMUNICATOR",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ClicksOrange.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "$unreadCount UNREAD",
                        color = ClicksOrange,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isLiveBridgeConnected) Color(0x2610B981) else Color(0x26F59E0B)
                        )
                        .clickable { onOpenLiveBridgeSettings() }
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isLiveBridgeConnected) "LIVE BRIDGE ON" else "ENABLE LIVE ACCESS",
                        color = if (isLiveBridgeConnected) Color(0xFF34D399) else Color(0xFFFBBF24),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = timeText,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4 Mode Tabs: 01 Hub Stream | 02 People Hub | 03 VIP Shield | 04 Minimal Apps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HubViewMode.entries.forEach { mode ->
                val selected = mode == currentViewMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) ClicksOrange.copy(alpha = 0.18f) else ObsidianCard)
                        .border(
                            width = 1.dp,
                            color = if (selected) ClicksOrange else ObsidianBorder,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onSelectViewMode(mode) }
                        .padding(vertical = 7.dp, horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mode.label,
                            color = if (selected) TextPrimary else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "[${mode.shortcutKey}]",
                            color = if (selected) ClicksOrange else TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

/**
 * The Next-Gen Universal Omnibox Command Bar & Multi-Platform Quick Composer.
 * Lets the user type anywhere to filter threads, execute `/msg @Contact`, or launch installed apps.
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
            .background(ObsidianSurface)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Omnibox Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianCard)
                .border(
                    width = 1.dp,
                    color = if (query.isNotEmpty() || isComposerExpanded) ClicksOrange else ObsidianBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = "Omnibox Command",
                tint = ClicksOrange,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "Omnibox: Type @person, /msg, or search all apps...",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    cursorBrush = SolidColor(ClicksOrange),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
                        text = "/msg +",
                        color = ClicksOrange,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Expandable Universal Multi-Platform Composer Drawer
        AnimatedVisibility(visible = isComposerExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = ObsidianCard,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ClicksOrange.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "UNIVERSAL OMNIBOX COMPOSER",
                            color = ClicksOrange,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cross-Platform Dispatch",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Recipient Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurface)
                            .padding(horizontal = 10.dp, vertical = 7.dp)
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

                    Spacer(modifier = Modifier.height(8.dp))

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
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) platform.accentColor else ObsidianSurface)
                                    .border(
                                        width = 1.dp,
                                        color = platform.accentColor.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { composePlatform = platform }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = platform.displayName,
                                    color = if (isSelected) Color.White else platform.accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

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
                                .padding(horizontal = 10.dp, vertical = 9.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(ClicksOrange)
                                .clickable {
                                    onSendOmniboxMessage(recipientInput, composePlatform, messageBodyInput)
                                }
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Send",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (isSelected) platform.accentColor else ObsidianCard
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) platform.accentColor else platform.accentColor.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(9.dp)
                        )
                        .clickable { onSelectPlatform(platform) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${platform.displayName} · $count",
                        color = if (isSelected) Color.White else platform.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
