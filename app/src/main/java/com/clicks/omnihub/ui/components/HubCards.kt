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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.PersonUnifiedThread
import com.clicks.omnihub.ui.theme.ClicksOrange
import com.clicks.omnihub.ui.theme.ObsidianBorder
import com.clicks.omnihub.ui.theme.ObsidianCard
import com.clicks.omnihub.ui.theme.ObsidianCardSelected
import com.clicks.omnihub.ui.theme.ObsidianSurface
import com.clicks.omnihub.ui.theme.TextMuted
import com.clicks.omnihub.ui.theme.TextPrimary
import com.clicks.omnihub.ui.theme.TextSecondary

/**
 * Single unified message card in the BlackBerry Hub × Clicks Communicator stream.
 * Supports one-tap smart reply chips, full inline RemoteInput direct reply,
 * VIP priority star toggle, Snooze (`S`), and Archive/Done (`A`).
 */
@Composable
fun HubMessageCard(
    message: HubMessage,
    isFocused: Boolean,
    isInlineReplying: Boolean,
    onSelect: () -> Unit,
    onToggleInlineReply: () -> Unit,
    onSendReply: (String) -> Unit,
    onArchive: () -> Unit,
    onSnooze: () -> Unit,
    onToggleVip: () -> Unit
) {
    var inlineText by remember(message.id) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isFocused) ObsidianCardSelected else ObsidianCard)
            .border(
                width = if (isFocused) 1.5.dp else 1.dp,
                color = if (isFocused) ClicksOrange.copy(alpha = 0.85f) else ObsidianBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(13.dp)
    ) {
        // Header Row: Avatar, Sender Name, Platform Badge, VIP badge, Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(message.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderInitials,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    if (message.isVip) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(CircleShape)
                                .background(ClicksOrange)
                                .border(1.5.dp, ObsidianCard, CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = message.senderName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Platform Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(message.platform.badgeBgColor)
                                .border(
                                    width = 1.dp,
                                    color = message.platform.accentColor.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = message.platform.displayName,
                                color = message.platform.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = message.subject ?: message.senderRoleOrHandle,
                        color = if (message.subject != null) TextPrimary.copy(alpha = 0.9f) else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (message.subject != null) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = message.relativeTimeLabel,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onToggleVip() }
                        .padding(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "VIP Priority",
                        tint = if (message.isVip) ClicksOrange else TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Message Preview Body
        Text(
            text = message.body,
            color = TextPrimary.copy(alpha = 0.92f),
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        // Sent Inline Replies History (if user already replied inline)
        if (message.repliedHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ClicksOrange.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.repliedHistory.forEach { sentReply ->
                    Text(
                        text = "↩ You replied via ${message.platform.displayName}: \"$sentReply\"",
                        color = ClicksOrange,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Bar: One-Tap Smart Replies + Inline Reply [R], Snooze [S], Done [A]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                message.quickReplies.take(2).forEach { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                            .clickable { onSendReply(chipText) }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "↩ $chipText",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Inline Reply Button [R]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ClicksOrange.copy(alpha = 0.18f))
                        .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .clickable { onToggleInlineReply() }
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Reply [R]",
                        color = ClicksOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Snooze Button [S]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                        .clickable { onSnooze() }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Snooze 1h",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                }

                // Archive / Mark Done Button [A]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianSurface)
                        .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                        .clickable { onArchive() }
                        .padding(horizontal = 7.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Archive Done",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Expandable Inline Direct-Reply (RemoteInput) Composer
        AnimatedVisibility(visible = isInlineReplying) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inlineText,
                        onValueChange = { inlineText = it },
                        singleLine = true,
                        textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                        cursorBrush = SolidColor(ClicksOrange),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianSurface)
                            .border(1.dp, ClicksOrange, RoundedCornerShape(10.dp))
                            .padding(horizontal = 11.dp, vertical = 9.dp),
                        decorationBox = { innerTextField ->
                            if (inlineText.isEmpty()) {
                                Text(
                                    text = "Direct reply via ${message.platform.displayName} RemoteInput...",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClicksOrange)
                            .clickable {
                                val textToSend = inlineText.ifBlank { message.quickReplies.firstOrNull() ?: "Sounds good!" }
                                onSendReply(textToSend)
                                inlineText = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 9.dp)
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
                                contentDescription = "Send Inline Reply",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Person-Centric Unified Thread Card (Mode 02: People Hub).
 * Combines all messages from a single contact across WhatsApp, Teams, Outlook, SMS, and Messenger
 * into one unified timeline.
 */
@Composable
fun PersonUnifiedThreadCard(
    thread: PersonUnifiedThread,
    onQuickReplyToMessage: (messageId: String, replyText: String) -> Unit,
    onArchiveAllFromPerson: () -> Unit
) {
    var replyText by remember(thread.senderName) { mutableStateOf("") }
    var selectedTargetMessage by remember(thread.messages) { mutableStateOf(thread.messages.first()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ObsidianCard)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
            .padding(13.dp)
    ) {
        // Person Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(thread.avatarColor)
                        .border(
                            width = if (thread.isVip) 2.dp else 0.dp,
                            color = if (thread.isVip) ClicksOrange else Color.Transparent,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = thread.senderInitials,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = thread.senderName,
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (thread.isVip) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(5.dp))
                                    .background(ClicksOrange.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "VIP",
                                    color = ClicksOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text(
                        text = "${thread.messages.size} messages across ${thread.activePlatforms.joinToString(" + ") { it.displayName }}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                    .clickable { onArchiveAllFromPerson() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Done All ✓",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Unified Cross-Platform Sub-Messages
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            thread.messages.forEach { msg ->
                val isTarget = selectedTargetMessage.id == msg.id
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(11.dp))
                        .background(ObsidianSurface)
                        .border(
                            width = 1.dp,
                            color = if (isTarget) msg.platform.accentColor.copy(alpha = 0.7f) else ObsidianBorder,
                            shape = RoundedCornerShape(11.dp)
                        )
                        .clickable { selectedTargetMessage = msg }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(msg.platform.badgeBgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = msg.platform.displayName,
                                color = msg.platform.accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = msg.relativeTimeLabel,
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    if (!msg.subject.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = msg.subject,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = msg.body,
                        color = TextPrimary.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Unified Person Reply Composer (automatically targets the selected channel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = replyText,
                onValueChange = { replyText = it },
                singleLine = true,
                textStyle = TextStyle(color = TextPrimary, fontSize = 12.sp),
                cursorBrush = SolidColor(ClicksOrange),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(9.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                decorationBox = { inner ->
                    if (replyText.isEmpty()) {
                        Text(
                            text = "Reply to ${thread.senderName} on ${selectedTargetMessage.platform.displayName}...",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                    inner()
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(selectedTargetMessage.platform.accentColor)
                    .clickable {
                        val text = replyText.ifBlank { "Got both of your messages — on it!" }
                        onQuickReplyToMessage(selectedTargetMessage.id, text)
                        replyText = ""
                    }
                    .padding(horizontal = 11.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Reply ${selectedTargetMessage.platform.shortCode}",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
