package com.clicks.omnihub.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.model.PersonUnifiedThread
import com.clicks.omnihub.ui.theme.AirframeSpace
import com.clicks.omnihub.ui.theme.AirframeTheme
import com.clicks.omnihub.ui.theme.AirframeType
import java.util.Locale

/**
 * Airframe `ListRow` (`ListRow/preview.html`).
 * - Rows sit directly on `ground`, never in cards; separated by a 1px bottom hairline (`line`).
 * - Unread rows have `af-avatar--active` (`sage` background, `forest` initials), `rowTitleUnread` (`600` weight, `ink`),
 *   and a 7dp `forest` dot (`af-dot`).
 * - One-line light preview (`body-light`) and uppercase `meta` line ("OUTLOOK · 1 ATTACHMENT").
 * - Trailing outline `IconButton` check (`af-iconbtn--outline`).
 */
@Composable
fun AirframeMessageListRow(
    message: HubMessage,
    onOpenConversation: () -> Unit,
    onMarkDone: () -> Unit
) {
    val c = AirframeTheme.colors
    val metaParts = mutableListOf(message.platform.displayName)
    if (message.attachments.isNotEmpty()) {
        val count = message.attachments.size
        metaParts.add(if (count == 1) "1 attachment" else "$count attachments")
    } else if (!message.subject.isNullOrBlank()) {
        metaParts.add(message.subject)
    }
    val metaLine = metaParts.joinToString(" · ").uppercase(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenConversation() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // `af-avatar` / `af-avatar--active`
            Box(
                modifier = Modifier
                    .size(AirframeSpace.sizeAvatar)
                    .clip(CircleShape)
                    .background(if (message.isUnread) c.sage else c.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message.senderInitials,
                    style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = if (message.isUnread) c.forest else c.text
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // `af-row-body`
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.senderName,
                        style = if (message.isUnread) AirframeType.rowTitleUnread else AirframeType.rowTitle,
                        color = if (message.isUnread) c.ink else c.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (message.isUnread) {
                        Spacer(modifier = Modifier.width(AirframeSpace.space2))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(c.forest)
                        )
                    }
                    Spacer(modifier = Modifier.width(AirframeSpace.space2))
                    Text(
                        text = message.relativeTimeLabel,
                        style = AirframeType.caption,
                        color = c.textMuted
                    )
                }

                Text(
                    text = message.body,
                    style = AirframeType.bodyLight,
                    color = c.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = metaLine,
                    style = AirframeType.meta,
                    color = c.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // `af-iconbtn af-iconbtn--outline`
            AirframeIconButton(
                icon = Icons.Default.Check,
                contentDescription = "Mark ${message.senderName} done",
                style = AirframeIconButtonStyle.OUTLINE,
                onClick = onMarkDone
            )
        }

        // 1px bottom hairline
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(c.line)
        )
    }
}

/**
 * Airframe Person-Centric Unified Row (`People` tab).
 * Sits directly on `ground` with hairline separator, merging all active platforms for a contact.
 */
@Composable
fun AirframePersonUnifiedRow(
    thread: PersonUnifiedThread,
    onOpenMessageConversation: (String) -> Unit,
    onArchiveAllFromPerson: () -> Unit
) {
    val c = AirframeTheme.colors
    val latest = thread.messages.first()
    val channelsMeta = thread.activePlatforms.joinToString(" · ") { it.displayName }.uppercase(Locale.US)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenMessageConversation(latest.id) }
            .padding(top = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(AirframeSpace.sizeAvatar)
                    .clip(CircleShape)
                    .background(c.sage),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = thread.senderInitials,
                    style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = c.forest
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = thread.senderName,
                        style = AirframeType.rowTitleUnread,
                        color = c.ink
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Presence dot (`signal`)
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(c.signal)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$channelsMeta · ${thread.messages.size} MESSAGES",
                    style = AirframeType.meta,
                    color = c.textMuted
                )
            }

            AirframeIconButton(
                icon = Icons.Default.Check,
                contentDescription = "Mark ${thread.senderName} done",
                style = AirframeIconButtonStyle.OUTLINE,
                onClick = onArchiveAllFromPerson
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Individual messages from this person across channels
        thread.messages.forEach { msg ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 58.dp, bottom = 8.dp)
                    .clickable { onOpenMessageConversation(msg.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = msg.body,
                        style = AirframeType.bodyLight,
                        color = c.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${msg.platform.displayName} · ${msg.relativeTimeLabel}".uppercase(Locale.US),
                        style = AirframeType.meta,
                        color = c.textMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(c.line)
        )
    }
}

/**
 * Airframe File Tile (`af-file` + `af-file-kind` from `MessageBubble/preview.html`).
 * 36x36dp `sage` tile with 3-letter code in `forest` (`PPT`, `XLS`, `PDF`, `IMG`), filename in `ink`,
 * size in `textMuted`, and trailing action icon.
 */
@Composable
fun AirframeAttachmentTile(
    attachment: HubAttachment,
    onActionClick: () -> Unit,
    actionIcon: ImageVector = Icons.Default.Download
) {
    val c = AirframeTheme.colors
    val kindCode = when (attachment.type) {
        AttachmentType.PRESENTATION -> "PPT"
        AttachmentType.SPREADSHEET -> "XLS"
        AttachmentType.IMAGE -> "IMG"
        AttachmentType.AUDIO -> "WAV"
        AttachmentType.LINK -> "URL"
        AttachmentType.DOCUMENT -> if (attachment.fileName.endsWith(".pdf", true)) "PDF" else "DOC"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(c.ground)
            .clickable { onActionClick() }
            .padding(start = 8.dp, top = 6.dp, bottom = 6.dp, end = 4.dp)
    ) {
        if (attachment.previewBitmap != null) {
            Image(
                bitmap = attachment.previewBitmap.asImageBitmap(),
                contentDescription = attachment.fileName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(end = 4.dp, bottom = 6.dp)
                    .clip(RoundedCornerShape(AirframeSpace.radiusSm))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(AirframeSpace.radiusSm))
                    .background(c.sage),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = kindCode,
                    style = AirframeType.meta.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = c.forest
                )
            }

            Spacer(modifier = Modifier.width(AirframeSpace.space3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.fileName,
                    style = AirframeType.caption.copy(fontWeight = FontWeight.Medium),
                    color = c.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = attachment.fileSizeLabel,
                    style = AirframeType.caption.copy(fontSize = 12.sp),
                    color = c.textMuted,
                    maxLines = 1
                )
            }

            Box(
                modifier = Modifier
                    .size(AirframeSpace.sizeTouch)
                    .clip(CircleShape)
                    .clickable { onActionClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = attachment.fileName,
                    tint = c.ink,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Airframe Full Conversation Detail Screen (`MessageBubble`, `Callout`, and `Composer` specs).
 * - Detail Header: Back `IconButton` on left, Open App & Done `IconButton`s on right (no wordmark).
 * - Subject / Contact Header: `title-xl` serif name ("Sarah Jenkins"), presence dot, and `lede` subtitle.
 * - Suggested Reply `af-callout` ("SUGGESTED REPLY" in `sage-wash`) at top of composer area.
 * - Thread (`af-thread`): Incoming bubbles on `surface` with bottom-left `radius-xs` tail;
 *   Outgoing bubbles in `forest` with `on-forest` text and bottom-right `radius-xs` tail;
 *   each followed by uppercase `af-meta` ("TEAMS · 9:41 AM").
 * - Docked Composer Tray (`af-tray` on `surface-raised` with `line` top border):
 *   Quick-reply `af-chip`s, `af-select-pill` channel switcher, rounded `af-field` (using native keyboard),
 *   `forest` send button (`af-iconbtn--filled`), and status caption ("Sarah is active on Teams, so your reply goes there.").
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
    val c = AirframeTheme.colors
    val keyboardController = LocalSoftwareKeyboardController.current
    val replyFocusRequester = remember { FocusRequester() }
    val threadListState = rememberLazyListState()

    var replyInput by remember(message.id) { mutableStateOf("") }
    var activeReplyPlatform by remember(message.platform) { mutableStateOf(message.platform) }
    val firstName = remember(message.senderName) { message.senderName.substringBefore(" ") }

    LaunchedEffect(message.conversationHistory.size) {
        if (message.conversationHistory.isNotEmpty()) {
            threadListState.animateScrollToItem(message.conversationHistory.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ground)
            .imePadding()
    ) {
        // 1. Airframe Detail Screen Header (Back button on left, round actions on right, no wordmark)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = AirframeSpace.gutter,
                    end = AirframeSpace.gutter,
                    top = AirframeSpace.space4,
                    bottom = AirframeSpace.space3
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AirframeIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to inbox",
                style = AirframeIconButtonStyle.SURFACE,
                onClick = {
                    keyboardController?.hide()
                    onBack()
                }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(AirframeSpace.space2)) {
                AirframeIconButton(
                    icon = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open in ${message.platform.displayName}",
                    style = AirframeIconButtonStyle.SURFACE,
                    onClick = onOpenInNativeApp
                )
                AirframeIconButton(
                    icon = Icons.Default.Check,
                    contentDescription = "Mark done",
                    style = AirframeIconButtonStyle.DONE,
                    onClick = onArchive
                )
            }
        }

        // 2. Scrollable Editorial Header + Thread Bubbles + Suggested Reply Callout
        LazyColumn(
            state = threadListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = AirframeSpace.gutter, vertical = AirframeSpace.space3),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Editorial `title-xl` Subject Header
            item(key = "detail_editorial_header") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(c.signal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVE ON ${activeReplyPlatform.displayName.uppercase(Locale.US)}",
                            style = AirframeType.eyebrow,
                            color = c.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(AirframeSpace.space2))
                    Text(
                        text = message.senderName,
                        style = AirframeType.titleXl,
                        color = c.ink
                    )
                    if (!message.subject.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(AirframeSpace.space2))
                        Text(
                            text = message.subject,
                            style = AirframeType.lede,
                            color = c.text
                        )
                    }
                    Spacer(modifier = Modifier.height(AirframeSpace.space5))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(c.line)
                    )
                }
            }

            // Conversation Turns (`af-thread` / `af-msg` / `af-bubble`)
            items(
                items = message.conversationHistory,
                key = { it.id }
            ) { turn ->
                AirframeMessageBubbleItem(
                    turn = turn,
                    onAttachmentClick = onOpenInNativeApp
                )
            }

            // Airframe `af-callout` ("Suggested reply")
            if (message.quickReplies.isNotEmpty()) {
                item(key = "suggested_callout") {
                    val suggested = message.quickReplies.first()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AirframeSpace.radiusXl))
                            .background(c.sageWash)
                            .clickable { onSendReply(suggested) }
                            .padding(horizontal = AirframeSpace.space4, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "SUGGESTED REPLY · TAP TO SEND",
                            style = AirframeType.meta.copy(color = c.forest, fontWeight = FontWeight.Bold),
                            color = c.forest
                        )
                        Text(
                            text = suggested,
                            style = AirframeType.rowTitle,
                            color = c.ink
                        )
                    }
                }
            }
        }

        // 3. Docked Airframe Composer Tray (`af-tray` on `surface-raised` with `line` top border)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceRaised)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(c.line)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AirframeSpace.space4, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick reply chips (`af-chip`)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    message.quickReplies.forEach { qr ->
                        AirframeChip(
                            label = qr,
                            onClick = { onSendReply(qr) }
                        )
                    }
                }

                if (pendingAttachment != null) {
                    AirframeAttachmentTile(
                        attachment = pendingAttachment,
                        onActionClick = onClearPendingAttachment,
                        actionIcon = Icons.Default.Close
                    )
                }

                // `af-composer`: Channel `af-select-pill` + Attach + `af-field` + `af-iconbtn--filled`
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AirframeSpace.space2)
                ) {
                    // `af-select-pill` (Tap to cycle platform)
                    Row(
                        modifier = Modifier
                            .height(AirframeSpace.sizeTouch)
                            .clip(CircleShape)
                            .background(c.sageSoft)
                            .clickable {
                                val platforms = MessagingPlatform.entries.filter { it != MessagingPlatform.ALL }
                                val nextIdx = (platforms.indexOf(activeReplyPlatform) + 1) % platforms.size
                                activeReplyPlatform = platforms[nextIdx]
                            }
                            .padding(start = 14.dp, end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeReplyPlatform.displayName,
                            style = AirframeType.body.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                            color = c.forest
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = "Switch channel",
                            tint = c.forest,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Attach file button
                    AirframeIconButton(
                        icon = Icons.Default.AttachFile,
                        contentDescription = "Attach file or photo",
                        style = AirframeIconButtonStyle.SURFACE,
                        onClick = onPickAttachment
                    )

                    // `af-field` (Exclusively uses device default keyboard)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(AirframeSpace.sizeTouch)
                            .clip(CircleShape)
                            .background(c.surface)
                            .padding(horizontal = AirframeSpace.space4),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (replyInput.isEmpty()) {
                            Text(
                                text = "Message $firstName…",
                                style = AirframeType.body,
                                color = c.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = replyInput,
                            onValueChange = { replyInput = it },
                            singleLine = true,
                            textStyle = AirframeType.body.copy(color = c.ink),
                            cursorBrush = SolidColor(c.forest),
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
                                .fillMaxWidth()
                                .focusRequester(replyFocusRequester)
                        )
                    }

                    // `af-iconbtn af-iconbtn--filled` Send button (Upward arrow per `Composer/preview.html`)
                    AirframeIconButton(
                        icon = Icons.Default.ArrowUpward,
                        contentDescription = "Send",
                        style = AirframeIconButtonStyle.FILLED,
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
                        }
                    )
                }

                // Bottom caption line (`af-row-time`)
                Text(
                    text = "$firstName is active on ${activeReplyPlatform.displayName}, so your reply goes there.",
                    style = AirframeType.caption,
                    color = c.textMuted
                )
            }
        }
    }
}

@Composable
private fun AirframeMessageBubbleItem(
    turn: ConversationTurn,
    onAttachmentClick: () -> Unit
) {
    val c = AirframeTheme.colors
    val isOut = turn.isFromMe

    val bubbleShape = if (isOut) {
        RoundedCornerShape(
            topStart = AirframeSpace.radiusXl,
            topEnd = AirframeSpace.radiusXl,
            bottomStart = AirframeSpace.radiusXl,
            bottomEnd = AirframeSpace.radiusXs
        )
    } else {
        RoundedCornerShape(
            topStart = AirframeSpace.radiusXl,
            topEnd = AirframeSpace.radiusXl,
            bottomStart = AirframeSpace.radiusXs,
            bottomEnd = AirframeSpace.radiusXl
        )
    }

    val metaLabel = buildString {
        append(turn.platform.displayName)
        append(" · ")
        append(turn.timestampLabel)
        if (isOut) append(" · Read")
    }.uppercase(Locale.US)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOut) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        // Text bubble (`af-bubble`)
        if (turn.text.isNotBlank()) {
            Box(
                modifier = Modifier
                    .widthIn(max = 310.dp)
                    .clip(bubbleShape)
                    .background(if (isOut) c.forest else c.surface)
                    .padding(horizontal = 15.dp, vertical = 12.dp)
            ) {
                Text(
                    text = turn.text,
                    style = AirframeType.body,
                    color = if (isOut) c.onForest else c.text
                )
            }
        }

        // Attachment bubble (`af-file` inside `af-bubble` container)
        if (turn.attachments.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .clip(bubbleShape)
                    .background(c.surface)
                    .padding(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                turn.attachments.forEach { att ->
                    AirframeAttachmentTile(
                        attachment = att,
                        onActionClick = onAttachmentClick
                    )
                }
            }
        }

        // Uppercase `af-meta`
        Text(
            text = metaLabel,
            style = AirframeType.meta,
            color = c.textMuted,
            modifier = Modifier.padding(horizontal = AirframeSpace.space1)
        )
    }
}
