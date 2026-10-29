package com.clicks.omnihub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.AttachmentType
import com.clicks.omnihub.model.ConversationTurn
import com.clicks.omnihub.model.HubAttachment
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.PersonUnifiedThread
import com.clicks.omnihub.ui.theme.M3SurfaceBackground
import com.clicks.omnihub.ui.theme.M3SurfaceContainer
import com.clicks.omnihub.ui.theme.M3SurfaceContainerHigh

/**
 * Clean Material 3 Conversation Stream Card.
 * Clicking anywhere on the card opens the full-screen [ConversationContextSheet] with the
 * complete message history, attachments, and native system keyboard composer.
 */
@Composable
fun HubMessageCard(
    message: HubMessage,
    onOpenConversation: () -> Unit,
    onQuickReply: (String) -> Unit,
    onArchive: () -> Unit,
    onSnooze: () -> Unit,
    onToggleVip: () -> Unit
) {
    ElevatedCard(
        onClick = onOpenConversation,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(M3SurfaceContainer)
                .padding(16.dp)
        ) {
            // Top Row: Avatar, Sender, Platform Pill, Time & Star
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(message.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderInitials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.senderName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (message.isUnread) FontWeight.Bold else FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = message.platform.containerColor,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = message.platform.displayName,
                                    color = message.platform.accentColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = message.subject ?: message.senderRoleOrHandle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (message.subject != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (message.subject != null) FontWeight.Medium else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = message.relativeTimeLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = onToggleVip,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "VIP Priority",
                            tint = if (message.isVip) Color(0xFFFBBF24) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Message Body Preview
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
                lineHeight = 20.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Attachment Pills (if any files/images are attached to this conversation)
            if (message.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    message.attachments.forEach { att ->
                        Surface(
                            color = M3SurfaceContainerHigh,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = iconForAttachmentType(att.type),
                                    contentDescription = att.fileName,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = att.fileName,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Row: Thread Context Indicator + Quick Reply Chips + Triage Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (message.conversationHistory.size > 1) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${message.conversationHistory.size} messages · Tap to open",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    message.quickReplies.take(2).forEach { chip ->
                        SuggestionChip(
                            onClick = { onQuickReply(chip) },
                            label = {
                                Text(
                                    text = chip,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            shape = CircleShape
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onSnooze,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Snooze",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onArchive,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-Screen Material 3 Conversation Context View.
 * Shows:
 * - Full multi-turn message history (both incoming and sent messages)
 * - Rich Attachment Cards (PDFs, PowerPoint decks, Excel sheets, Images)
 * - "Open in [App]" button to jump directly into the native app when needed
 * - Native Device Keyboard Composer with `📎 Attach` button
 */
@Composable
fun ConversationContextSheet(
    message: HubMessage,
    pendingAttachment: HubAttachment?,
    onBack: () -> Unit,
    onSendReply: (String) -> Unit,
    onPickAttachment: () -> Unit,
    onClearPendingAttachment: () -> Unit,
    onOpenInNativeApp: () -> Unit,
    onArchive: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val replyFocusRequester = remember { FocusRequester() }
    val threadListState = rememberLazyListState()
    var replyInput by remember(message.id) { mutableStateOf("") }

    LaunchedEffect(message.conversationHistory.size) {
        if (message.conversationHistory.isNotEmpty()) {
            threadListState.animateScrollToItem(message.conversationHistory.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(M3SurfaceBackground)
            .imePadding()
    ) {
        // 1. Material 3 Conversation Top App Bar
        Surface(
            color = M3SurfaceContainer,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = {
                        keyboardController?.hide()
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Inbox"
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(message.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = message.senderInitials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = message.senderName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = message.platform.containerColor,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = message.platform.displayName,
                                    color = message.platform.accentColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = message.subject ?: message.senderRoleOrHandle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(
                        onClick = onOpenInNativeApp,
                        label = {
                            Text(
                                text = "Open App",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open in Native App",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    )
                    IconButton(onClick = onArchive) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Archive Conversation",
                            tint = Color(0xFF34D399)
                        )
                    }
                }
            }
        }

        // 2. Full Multi-Turn Conversation Context Timeline
        LazyColumn(
            state = threadListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (message.subject != null) {
                item(key = "subject_banner") {
                    Surface(
                        color = M3SurfaceContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SUBJECT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = message.subject,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            items(
                items = message.conversationHistory,
                key = { it.id }
            ) { turn ->
                ConversationBubbleItem(
                    turn = turn,
                    onAttachmentClick = onOpenInNativeApp
                )
            }
        }

        // 3. Smart Reply Suggestion Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            message.quickReplies.forEach { quickReply ->
                SuggestionChip(
                    onClick = { onSendReply(quickReply) },
                    label = { Text(quickReply) },
                    shape = CircleShape
                )
            }
        }

        // 4. Native Device Keyboard Composer + Attachment Picker Bar
        Surface(
            color = M3SurfaceContainer,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (pendingAttachment != null) {
                    AttachmentChipPreview(
                        attachment = pendingAttachment,
                        onRemove = onClearPendingAttachment
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPickAttachment) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attach File or Photo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = replyInput,
                        onValueChange = { replyInput = it },
                        placeholder = {
                            Text(
                                text = "Reply on ${message.platform.displayName}...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = M3SurfaceContainerHigh,
                            unfocusedContainerColor = M3SurfaceContainerHigh,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (replyInput.isNotBlank() || pendingAttachment != null) {
                                    onSendReply(replyInput)
                                    replyInput = ""
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(replyFocusRequester)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledIconButton(
                        onClick = {
                            if (replyInput.isNotBlank() || pendingAttachment != null) {
                                onSendReply(replyInput)
                                replyInput = ""
                            } else {
                                try {
                                    replyFocusRequester.requestFocus()
                                    keyboardController?.show()
                                } catch (_: Exception) {
                                }
                            }
                        },
                        modifier = Modifier.size(46.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Reply"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationBubbleItem(
    turn: ConversationTurn,
    onAttachmentClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (turn.isFromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (turn.isFromMe) MaterialTheme.colorScheme.primaryContainer else M3SurfaceContainerHigh,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (turn.isFromMe) 18.dp else 4.dp,
                bottomEnd = if (turn.isFromMe) 4.dp else 18.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = turn.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (turn.isFromMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )

                // Render any attachments inside this conversation turn
                if (turn.attachments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        turn.attachments.forEach { att ->
                            Surface(
                                color = M3SurfaceContainer,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onAttachmentClick() }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    if (att.previewBitmap != null) {
                                        Image(
                                            bitmap = att.previewBitmap.asImageBitmap(),
                                            contentDescription = att.fileName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = iconForAttachmentType(att.type),
                                            contentDescription = att.fileName,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = att.fileName,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = att.fileSizeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${turn.senderName} · ${turn.timestampLabel}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

/**
 * Person-Centric Unified Thread Card (Material 3).
 * Merges conversations from the same contact across WhatsApp, Teams, Outlook, SMS, etc.
 */
@Composable
fun PersonUnifiedThreadCard(
    thread: PersonUnifiedThread,
    onOpenMessageConversation: (String) -> Unit,
    onArchiveAllFromPerson: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(M3SurfaceContainer)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(thread.avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = thread.senderInitials,
                            color = Color.White,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = thread.senderName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${thread.messages.size} conversations across ${thread.activePlatforms.joinToString(", ") { it.displayName }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onArchiveAllFromPerson) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Mark All Done",
                        tint = Color(0xFF34D399)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                thread.messages.forEach { msg ->
                    Surface(
                        color = M3SurfaceContainerHigh,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenMessageConversation(msg.id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = msg.platform.containerColor,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = msg.platform.displayName,
                                        color = msg.platform.accentColor,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = msg.relativeTimeLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!msg.subject.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.subject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun iconForAttachmentType(type: AttachmentType) = when (type) {
    AttachmentType.IMAGE -> Icons.Default.Image
    AttachmentType.SPREADSHEET -> Icons.Default.TableChart
    else -> Icons.Default.Description
}
