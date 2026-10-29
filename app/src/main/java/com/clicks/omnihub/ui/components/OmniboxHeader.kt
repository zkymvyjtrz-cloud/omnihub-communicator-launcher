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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.HubAttachment
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.theme.AirframeSpace
import com.clicks.omnihub.ui.theme.AirframeTheme
import com.clicks.omnihub.ui.theme.AirframeType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Airframe 1px Hairline Divider (`af-rule`).
 * Sections sit directly on `ground`, separated by 1px `line` hairlines with `space-section` (26dp) above and below.
 */
@Composable
fun AirframeRule() {
    val c = AirframeTheme.colors
    Spacer(modifier = Modifier.height(AirframeSpace.section))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(c.line)
    )
    Spacer(modifier = Modifier.height(AirframeSpace.section))
}

/**
 * Airframe `Eyebrow` (`af-eyebrow` / `af-eyebrow-row`).
 */
@Composable
fun AirframeEyebrow(
    label: String,
    aside: String? = null,
    onAsideClick: (() -> Unit)? = null
) {
    val c = AirframeTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(Locale.US),
            style = AirframeType.eyebrow,
            color = c.textMuted
        )
        if (aside != null) {
            Text(
                text = aside,
                style = AirframeType.body,
                color = if (onAsideClick != null) c.forest else c.text,
                modifier = if (onAsideClick != null) Modifier.clickable { onAsideClick() } else Modifier
            )
        }
    }
}

/**
 * Airframe `IconButton` (`af-iconbtn`, `af-iconbtn--filled`, `af-iconbtn--done`, `af-iconbtn--outline`).
 */
enum class AirframeIconButtonStyle {
    SURFACE,
    FILLED,
    DONE,
    OUTLINE
}

@Composable
fun AirframeIconButton(
    icon: ImageVector,
    contentDescription: String,
    style: AirframeIconButtonStyle = AirframeIconButtonStyle.SURFACE,
    onClick: () -> Unit
) {
    val c = AirframeTheme.colors
    val bgColor = when (style) {
        AirframeIconButtonStyle.SURFACE -> c.surface
        AirframeIconButtonStyle.FILLED -> c.forest
        AirframeIconButtonStyle.DONE -> c.sage
        AirframeIconButtonStyle.OUTLINE -> c.ground
    }
    val tintColor = when (style) {
        AirframeIconButtonStyle.SURFACE -> c.ink
        AirframeIconButtonStyle.FILLED -> c.onForest
        AirframeIconButtonStyle.DONE -> c.forest
        AirframeIconButtonStyle.OUTLINE -> c.text
    }

    Box(
        modifier = Modifier
            .size(AirframeSpace.sizeTouch)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (style == AirframeIconButtonStyle.OUTLINE) {
                    Modifier.border(1.dp, c.lineStrong, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Airframe `AppHeader` (`af-header` from `AppHeader/preview.html`).
 * Top of home screen: uppercase `OMNIHUB` wordmark in `forest`, full date written out without commas
 * ("Wednesday 28 October 2026"), and round `IconButton` actions.
 */
@Composable
fun AirframeAppHeader(
    isNightTheme: Boolean,
    isSearchOpen: Boolean,
    isComposerOpen: Boolean,
    searchQuery: String,
    pendingAttachment: HubAttachment?,
    onToggleTheme: () -> Unit,
    onToggleSearch: () -> Unit,
    onToggleComposer: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onPickAttachment: () -> Unit,
    onClearAttachment: () -> Unit,
    onSendOmniboxMessage: (recipient: String, platform: MessagingPlatform, body: String) -> Unit
) {
    val c = AirframeTheme.colors
    val keyboardController = LocalSoftwareKeyboardController.current
    val searchFocusRequester = remember { FocusRequester() }
    val composeFocusRequester = remember { FocusRequester() }

    val fullDateText = remember {
        SimpleDateFormat("EEEE d MMMM yyyy", Locale.US).format(Date())
    }

    var recipientInput by remember { mutableStateOf("Sarah Jenkins") }
    var composePlatform by remember { mutableStateOf(MessagingPlatform.TEAMS) }
    var messageBodyInput by remember { mutableStateOf("") }

    LaunchedEffect(isSearchOpen) {
        if (isSearchOpen) {
            try {
                searchFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(isComposerOpen) {
        if (isComposerOpen) {
            try {
                composeFocusRequester.requestFocus()
                keyboardController?.show()
            } catch (_: Exception) {
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "OMNIHUB",
                    style = AirframeType.wordmark,
                    color = c.forest
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space2))
                Text(
                    text = fullDateText,
                    style = AirframeType.body,
                    color = c.text
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(AirframeSpace.space2)) {
                AirframeIconButton(
                    icon = if (isNightTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Switch Ivory or Night theme",
                    style = AirframeIconButtonStyle.SURFACE,
                    onClick = onToggleTheme
                )
                AirframeIconButton(
                    icon = if (isSearchOpen) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = "Search",
                    style = AirframeIconButtonStyle.SURFACE,
                    onClick = onToggleSearch
                )
                AirframeIconButton(
                    icon = if (isComposerOpen) Icons.Default.Close else Icons.Default.Edit,
                    contentDescription = "New message",
                    style = AirframeIconButtonStyle.FILLED,
                    onClick = onToggleComposer
                )
            }
        }

        // Expandable Airframe Search Field (`af-field`)
        AnimatedVisibility(visible = isSearchOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AirframeSpace.space4)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AirframeSpace.sizeTouch)
                        .clip(CircleShape)
                        .background(c.surface)
                        .padding(horizontal = AirframeSpace.space4),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = c.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(AirframeSpace.space2))
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search people, messages or apps…",
                                style = AirframeType.body,
                                color = c.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = AirframeType.body.copy(color = c.ink),
                            cursorBrush = SolidColor(c.forest),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(searchFocusRequester)
                        )
                    }
                    if (searchQuery.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { onClearSearch() }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = c.textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expandable Airframe New Message Composer (`af-callout` / `af-composer`)
        AnimatedVisibility(visible = isComposerOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AirframeSpace.space4)
                    .clip(RoundedCornerShape(AirframeSpace.radiusXl))
                    .background(c.sageWash)
                    .padding(AirframeSpace.space4)
            ) {
                Text(
                    text = "NEW MESSAGE",
                    style = AirframeType.eyebrow.copy(fontSize = 11.sp),
                    color = c.forest
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space3))

                // Channel selector chips (`af-chip`)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(AirframeSpace.space2)
                ) {
                    MessagingPlatform.entries.filter { it != MessagingPlatform.ALL }.forEach { platform ->
                        val active = composePlatform == platform
                        AirframeChip(
                            label = platform.displayName,
                            selected = active,
                            onClick = { composePlatform = platform }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AirframeSpace.space3))

                // Recipient `af-field`
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AirframeSpace.sizeTouch)
                        .clip(CircleShape)
                        .background(c.surface)
                        .padding(horizontal = AirframeSpace.space4),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = recipientInput,
                        onValueChange = { recipientInput = it },
                        singleLine = true,
                        textStyle = AirframeType.body.copy(color = c.ink, fontWeight = FontWeight.SemiBold),
                        cursorBrush = SolidColor(c.forest),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (pendingAttachment != null) {
                    Spacer(modifier = Modifier.height(AirframeSpace.space2))
                    AirframeAttachmentTile(
                        attachment = pendingAttachment,
                        onActionClick = onClearAttachment,
                        actionIcon = Icons.Default.Close
                    )
                }

                Spacer(modifier = Modifier.height(AirframeSpace.space2))

                // Message body + Attach + Send (`af-composer`)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AirframeSpace.space2)
                ) {
                    AirframeIconButton(
                        icon = Icons.Default.AttachFile,
                        contentDescription = "Attach file",
                        style = AirframeIconButtonStyle.SURFACE,
                        onClick = onPickAttachment
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(AirframeSpace.sizeTouch)
                            .clip(CircleShape)
                            .background(c.surface)
                            .padding(horizontal = AirframeSpace.space4),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageBodyInput.isEmpty()) {
                            Text(
                                text = "Message $recipientInput on ${composePlatform.displayName}…",
                                style = AirframeType.body,
                                color = c.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = messageBodyInput,
                            onValueChange = { messageBodyInput = it },
                            singleLine = true,
                            textStyle = AirframeType.body.copy(color = c.ink),
                            cursorBrush = SolidColor(c.forest),
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(composeFocusRequester)
                        )
                    }
                    AirframeIconButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Send message",
                        style = AirframeIconButtonStyle.FILLED,
                        onClick = {
                            onSendOmniboxMessage(recipientInput, composePlatform, messageBodyInput)
                            messageBodyInput = ""
                            keyboardController?.hide()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Airframe `Metric` + `ProgressBar` (`Metric/preview.html` & `ProgressBar/preview.html`).
 * One serif `numeral-xl` figure per screen, its `numeral-unit`, a `sage-soft` status pill,
 * a plain sentence `lede`, and a `forest` progress fill on `track`.
 */
@Composable
fun AirframeHeroMetricSection(
    unreadCount: Int,
    vipNeedCount: Int,
    clearedPercent: Int,
    topSenderName: String?,
    isLiveConnected: Boolean,
    onPillClick: () -> Unit
) {
    val c = AirframeTheme.colors
    val ledeText = when {
        unreadCount == 0 -> "Every channel is clear for now."
        topSenderName != null -> "$topSenderName needs an answer before 10:30."
        else -> "You have unread messages across your channels."
    }
    val pillText = when {
        unreadCount == 0 -> "All clear"
        vipNeedCount > 0 -> "$vipNeedCount need you"
        else -> "Live sync"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        AirframeEyebrow(
            label = "Today's inbox",
            aside = if (isLiveConnected) "Live bridge on" else "Connect live →",
            onAsideClick = onPillClick
        )

        Spacer(modifier = Modifier.height(AirframeSpace.space3))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = unreadCount.toString(),
                    style = AirframeType.numeralXl,
                    color = c.ink
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "new",
                    style = AirframeType.numeralUnit,
                    color = c.text,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // `af-pill`
            Box(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .height(36.dp)
                    .clip(CircleShape)
                    .background(c.sageSoft)
                    .clickable { onPillClick() }
                    .padding(horizontal = AirframeSpace.space4),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pillText,
                    style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold),
                    color = c.forest
                )
            }
        }

        Spacer(modifier = Modifier.height(AirframeSpace.space4))

        Text(
            text = ledeText,
            style = AirframeType.lede,
            color = c.text
        )

        Spacer(modifier = Modifier.height(AirframeSpace.space4))

        // `af-progress`
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Cleared today",
                style = AirframeType.body,
                color = c.text
            )
            Text(
                text = "$clearedPercent%",
                style = AirframeType.body,
                color = c.text
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(c.track)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (clearedPercent / 100f).coerceIn(0.08f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(c.forest)
            )
        }
    }
}

/**
 * Airframe Signature `CapsuleStrip` (`CapsuleStrip/preview.html`).
 * 5–6 thin vertical capsules (13dp wide; 46dp tall in `forest` when selected, 40dp tall in `capsule-done`
 * when messages are present, `track` when 0) with stacked channel name and count underneath.
 */
@Composable
fun AirframeChannelCapsuleStrip(
    selectedPlatform: MessagingPlatform,
    platformCounts: Map<MessagingPlatform, Int>,
    onSelectPlatform: (MessagingPlatform) -> Unit
) {
    val c = AirframeTheme.colors
    val stripPlatforms = listOf(
        MessagingPlatform.ALL,
        MessagingPlatform.WHATSAPP,
        MessagingPlatform.TEAMS,
        MessagingPlatform.OUTLOOK,
        MessagingPlatform.GMAIL,
        MessagingPlatform.SMS
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        AirframeEyebrow(label = "By channel")
        Spacer(modifier = Modifier.height(AirframeSpace.space4))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            stripPlatforms.forEach { platform ->
                val count = platformCounts[platform] ?: 0
                val isSelected = platform == selectedPlatform
                val hasItems = count > 0

                val barColor = when {
                    isSelected -> c.forest
                    hasItems -> c.capsuleDone
                    else -> c.track
                }
                val barHeight = if (isSelected) AirframeSpace.sizeCapsuleCurrentHeight else AirframeSpace.sizeCapsuleHeight
                val labelColor = when {
                    isSelected -> c.ink
                    hasItems -> c.text
                    else -> c.textMuted
                }
                val labelWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .widthIn(min = AirframeSpace.sizeTouch)
                        .clip(RoundedCornerShape(AirframeSpace.radiusSm))
                        .clickable { onSelectPlatform(platform) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.height(AirframeSpace.sizeCapsuleCurrentHeight),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .width(AirframeSpace.sizeCapsuleWidth)
                                .height(barHeight)
                                .clip(CircleShape)
                                .background(barColor)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = platform.displayName,
                        style = AirframeType.caption.copy(fontWeight = labelWeight),
                        color = labelColor,
                        maxLines = 1
                    )
                    Text(
                        text = count.toString(),
                        style = AirframeType.caption.copy(fontWeight = labelWeight),
                        color = labelColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Airframe `Feature` + `StatPair` (`Feature/preview.html` & `StatPair/preview.html`).
 * Holds today's most urgent message and the screen's single `forest` primary button ("Review & reply →").
 */
@Composable
fun AirframeFeatureSection(
    featuredMessage: HubMessage,
    onOpenConversation: () -> Unit,
    onMarkDone: () -> Unit
) {
    val c = AirframeTheme.colors
    val headline = featuredMessage.subject ?: featuredMessage.senderName
    val subtitle = "${featuredMessage.senderName} · ${featuredMessage.platform.displayName} · ${featuredMessage.relativeTimeLabel}"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "NEEDS YOU",
                    style = AirframeType.eyebrow,
                    color = c.textMuted
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space3))
                Text(
                    text = headline,
                    style = AirframeType.titleLg,
                    color = c.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space2))
                Text(
                    text = subtitle,
                    style = AirframeType.lede,
                    color = c.text
                )
            }

            Spacer(modifier = Modifier.width(AirframeSpace.space3))

            AirframeIconButton(
                icon = Icons.Default.Check,
                contentDescription = "Mark done",
                style = AirframeIconButtonStyle.DONE,
                onClick = onMarkDone
            )
        }

        Spacer(modifier = Modifier.height(AirframeSpace.space3))

        Text(
            text = featuredMessage.body,
            style = AirframeType.body,
            color = c.text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(AirframeSpace.space5))

        // The screen's single `forest` primary button (`af-btn af-btn--block`)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AirframeSpace.sizeButton)
                .clip(RoundedCornerShape(AirframeSpace.radiusLg))
                .background(c.forest)
                .clickable { onOpenConversation() }
                .padding(horizontal = AirframeSpace.gutter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Review & reply",
                style = AirframeType.button,
                color = c.onForest
            )
            Spacer(modifier = Modifier.width(AirframeSpace.space3))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = c.onForest,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // `af-stats` StatPair
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = AirframeSpace.space3),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = null,
                    tint = c.text,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Attached",
                        style = AirframeType.bodyLight,
                        color = c.textMuted
                    )
                    Text(
                        text = if (featuredMessage.attachments.isEmpty()) "None" else "${featuredMessage.attachments.size} files",
                        style = AirframeType.statValue,
                        color = c.ink
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(42.dp)
                    .background(c.line)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = AirframeSpace.space5),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = c.text,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Due",
                        style = AirframeType.bodyLight,
                        color = c.textMuted
                    )
                    Text(
                        text = "10:30 AM",
                        style = AirframeType.statValue,
                        color = c.ink
                    )
                }
            }
        }
    }
}

/**
 * Airframe `Chip` (`af-chip`).
 */
@Composable
fun AirframeChip(
    label: String,
    selected: Boolean = false,
    onClick: () -> Unit
) {
    val c = AirframeTheme.colors
    Box(
        modifier = Modifier
            .height(AirframeSpace.sizeTouch)
            .clip(CircleShape)
            .background(if (selected) c.forest else c.ground)
            .border(
                width = 1.dp,
                color = if (selected) c.forest else c.lineStrong,
                shape = CircleShape
            )
            .clickable { onClick() }
            .padding(horizontal = AirframeSpace.space4),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = AirframeType.body.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            color = if (selected) c.onForest else c.ink,
            maxLines = 1
        )
    }
}

/**
 * Airframe `Toast` (`af-toast`).
 */
@Composable
fun AirframeToastBanner(
    message: String,
    actionLabel: String = "Reset",
    onActionClick: () -> Unit
) {
    val c = AirframeTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AirframeSpace.radiusLg))
            .background(c.forest)
            .padding(start = 18.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = message,
            style = AirframeType.body.copy(fontWeight = FontWeight.Medium),
            color = c.onForest,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .height(AirframeSpace.sizeTouch)
                .clip(RoundedCornerShape(AirframeSpace.radiusMd))
                .background(c.onForestVeil)
                .clickable { onActionClick() }
                .padding(horizontal = AirframeSpace.space4),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold),
                color = c.onForest
            )
        }
    }
}
