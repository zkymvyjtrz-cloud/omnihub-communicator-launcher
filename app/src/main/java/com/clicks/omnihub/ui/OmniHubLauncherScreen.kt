package com.clicks.omnihub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.components.ClicksTactileShortcutDeck
import com.clicks.omnihub.ui.components.CommunicatorHeaderBar
import com.clicks.omnihub.ui.components.HubMessageCard
import com.clicks.omnihub.ui.components.OmniboxCommandBar
import com.clicks.omnihub.ui.components.PersonUnifiedThreadCard
import com.clicks.omnihub.ui.theme.ClicksOrange
import com.clicks.omnihub.ui.theme.ObsidianBackground
import com.clicks.omnihub.ui.theme.ObsidianBorder
import com.clicks.omnihub.ui.theme.ObsidianCard
import com.clicks.omnihub.ui.theme.ObsidianSurface
import com.clicks.omnihub.ui.theme.TextMuted
import com.clicks.omnihub.ui.theme.TextPrimary
import com.clicks.omnihub.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Root Home Screen Composable for the OmniHub Communicator Launcher.
 * Optimized for full-screen utilization on tall flagship phones (e.g., Galaxy S23 Ultra):
 * - Compact fixed top header + Omnibox
 * - Full-height `weight(1f)` LazyColumn that always starts immediately below the header
 * - Compact single-line bottom shortcut bar
 */
@Composable
fun OmniHubLauncherScreen(viewModel: HubViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val allMessages by viewModel.allMessages.collectAsState()
    val visibleMessages by viewModel.visibleMessages.collectAsState()
    val personThreads by viewModel.personThreads.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val omniboxQuery by viewModel.omniboxQuery.collectAsState()
    val isOmniboxComposerOpen by viewModel.isOmniboxComposerOpen.collectAsState()
    val focusedMessageId by viewModel.focusedMessageId.collectAsState()
    val inlineReplyingMessageId by viewModel.inlineReplyingMessageId.collectAsState()
    val showTactileDeck by viewModel.showTactileShortcutDeck.collectAsState()
    val isLiveBridgeConnected by viewModel.isLiveBridgeConnected.collectAsState()
    val statusBanner by viewModel.statusBanner.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    val platformCounts = remember(allMessages) {
        val counts = mutableMapOf<MessagingPlatform, Int>()
        counts[MessagingPlatform.ALL] = allMessages.size
        MessagingPlatform.entries.filter { it != MessagingPlatform.ALL }.forEach { platform ->
            counts[platform] = allMessages.count { it.platform == platform }
        }
        counts
    }

    val activeLedColor = remember(focusedMessageId, visibleMessages) {
        val focused = visibleMessages.firstOrNull { it.id == focusedMessageId }
            ?: visibleMessages.firstOrNull()
        focused?.platform?.accentColor ?: ClicksOrange
    }

    val defaultEssentialApps = remember {
        listOf(
            InstalledAppInfo("Phone Dialer", "com.samsung.android.dialer", "Phone"),
            InstalledAppInfo("Messages (SMS)", "com.google.android.apps.messaging", "Comms"),
            InstalledAppInfo("WhatsApp", "com.whatsapp", "Comms"),
            InstalledAppInfo("Microsoft Teams", "com.microsoft.teams", "Comms"),
            InstalledAppInfo("Microsoft Outlook", "com.microsoft.office.outlook", "Comms"),
            InstalledAppInfo("Gmail", "com.google.android.gm", "Comms"),
            InstalledAppInfo("Calendar", "com.samsung.android.calendar", "Schedule"),
            InstalledAppInfo("Google Maps", "com.google.android.apps.maps", "Navigation"),
            InstalledAppInfo("Camera", "com.sec.android.app.camera", "Camera")
        )
    }

    val filteredApps = remember(installedApps, omniboxQuery) {
        val source = installedApps.ifEmpty { defaultEssentialApps }
        if (omniboxQuery.isBlank() || omniboxQuery.startsWith("/")) {
            source
        } else {
            source.filter { it.label.contains(omniboxQuery, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // 1. Compact Top Header + Mode Tabs
        CommunicatorHeaderBar(
            unreadCount = allMessages.count { it.isUnread },
            activeLedColor = activeLedColor,
            isLiveBridgeConnected = isLiveBridgeConnected || viewModel.isNotificationAccessEnabled(context),
            currentViewMode = viewMode,
            onSelectViewMode = { viewModel.setViewMode(it) },
            onOpenLiveBridgeSettings = { viewModel.openNotificationAccessSettings(context) }
        )

        // 2. Compact Omnibox Command Bar & Channel Filter Pills
        OmniboxCommandBar(
            query = omniboxQuery,
            isComposerExpanded = isOmniboxComposerOpen,
            selectedPlatform = selectedPlatform,
            platformCounts = platformCounts,
            onQueryChange = { viewModel.updateOmniboxQuery(it) },
            onClearQuery = { viewModel.clearOmnibox() },
            onToggleComposer = { viewModel.toggleOmniboxComposer() },
            onSelectPlatform = { viewModel.selectPlatform(it) },
            onSendOmniboxMessage = { recipient, platform, body ->
                viewModel.sendOmniboxOutbound(recipient, platform, body)
            }
        )

        // 3. Full-Screen Scrollable Content Area (Takes ~80% of the S23 Ultra screen)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // VIP Focus Mode Banner
            if (viewMode == HubViewMode.VIP_FOCUS) {
                item(key = "vip_focus_banner") {
                    val heldCount = allMessages.count { !it.isVip }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClicksOrange.copy(alpha = 0.14f))
                            .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VIP PRIORITY SHIELD ACTIVE",
                                color = ClicksOrange,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Urgent VIP threads only · $heldCount non-VIP held for later",
                                color = TextPrimary,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ObsidianCard)
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(8.dp))
                                .clickable { viewModel.setViewMode(HubViewMode.STREAM) }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Show All",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }

            when (viewMode) {
                HubViewMode.PEOPLE -> {
                    items(
                        items = personThreads,
                        key = { "person_${it.senderName}" }
                    ) { thread ->
                        PersonUnifiedThreadCard(
                            thread = thread,
                            onQuickReplyToMessage = { msgId, text ->
                                viewModel.sendReply(context, msgId, text)
                            },
                            onArchiveAllFromPerson = {
                                thread.messages.forEach { msg ->
                                    viewModel.archiveFocusedOrSpecificMessage(msg.id)
                                }
                            }
                        )
                    }
                }

                HubViewMode.APPS -> {
                    item(key = "set_default_launcher_banner") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(ClicksOrange.copy(alpha = 0.14f))
                                .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.openDefaultLauncherSettings(context) }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MINIMAL SPEED-DIAL LAUNCHER",
                                    color = ClicksOrange,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${filteredApps.size} installed apps · Tap to set Default Home",
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Render each app as a native LazyColumn item so long app lists never clip or collapse!
                    itemsIndexed(
                        items = filteredApps,
                        key = { _, app -> "app_${app.packageName}" }
                    ) { index, app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(11.dp))
                                .background(ObsidianCard)
                                .border(1.dp, ObsidianBorder, RoundedCornerShape(11.dp))
                                .clickable { viewModel.launchApp(context, app.packageName, app.label) }
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
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
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
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
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                HubViewMode.STREAM, HubViewMode.VIP_FOCUS -> {
                    if (visibleMessages.isEmpty()) {
                        item(key = "inbox_zero_state") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ObsidianCard)
                                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "INBOX ZERO ACHIEVED ✨",
                                    color = ClicksOrange,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "All multi-platform communications have been triaged.",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(ClicksOrange)
                                        .clickable { viewModel.resetDemoStream() }
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = "Restore Demo Stream",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        items(
                            items = visibleMessages,
                            key = { it.id }
                        ) { message ->
                            HubMessageCard(
                                message = message,
                                isFocused = message.id == focusedMessageId,
                                isInlineReplying = message.id == inlineReplyingMessageId,
                                onSelect = { viewModel.focusMessage(message.id) },
                                onToggleInlineReply = { viewModel.toggleInlineReply(message.id) },
                                onSendReply = { replyText ->
                                    viewModel.sendReply(context, message.id, replyText)
                                },
                                onArchive = { viewModel.archiveFocusedOrSpecificMessage(message.id) },
                                onSnooze = { viewModel.snoozeFocusedOrSpecificMessage(message.id) },
                                onToggleVip = { viewModel.toggleVipForMessage(message) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Compact Bottom Dock (Keypad collapsed by default for full-screen S23 Ultra view)
        ClicksTactileShortcutDeck(
            statusBannerText = statusBanner,
            isExpanded = showTactileDeck,
            onToggleExpanded = { viewModel.toggleTactileShortcutDeck() },
            onShortcutKey = { keyChar ->
                viewModel.handleHardwareShortcutKey(keyChar)
            },
            onSpaceScroll = {
                scope.launch {
                    val nextIndex = (listState.firstVisibleItemIndex + 2)
                        .coerceAtMost((visibleMessages.size - 1).coerceAtLeast(0))
                    listState.animateScrollToItem(nextIndex)
                }
            },
            onResetDemo = { viewModel.resetDemoStream() }
        )
    }
}
