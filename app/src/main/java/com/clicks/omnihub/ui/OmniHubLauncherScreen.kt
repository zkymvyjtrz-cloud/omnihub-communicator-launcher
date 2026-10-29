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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.components.ClicksTactileShortcutDeck
import com.clicks.omnihub.ui.components.CommunicatorHeaderBar
import com.clicks.omnihub.ui.components.HubMessageCard
import com.clicks.omnihub.ui.components.MinimalSpeedDialAppList
import com.clicks.omnihub.ui.components.OmniboxCommandBar
import com.clicks.omnihub.ui.components.PersonUnifiedThreadCard
import com.clicks.omnihub.ui.theme.ClicksOrange
import com.clicks.omnihub.ui.theme.ObsidianBackground
import com.clicks.omnihub.ui.theme.ObsidianBorder
import com.clicks.omnihub.ui.theme.ObsidianCard
import com.clicks.omnihub.ui.theme.TextPrimary
import com.clicks.omnihub.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/**
 * Root Home Screen Composable for the OmniHub Communicator Launcher.
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBackground)
    ) {
        // 1. Top Hardware-Inspired Header + Dynamic Notification LED + Mode Switcher
        CommunicatorHeaderBar(
            unreadCount = allMessages.count { it.isUnread },
            activeLedColor = activeLedColor,
            isLiveBridgeConnected = isLiveBridgeConnected || viewModel.isNotificationAccessEnabled(context),
            currentViewMode = viewMode,
            onSelectViewMode = { viewModel.setViewMode(it) },
            onOpenLiveBridgeSettings = { viewModel.openNotificationAccessSettings(context) }
        )

        // 2. Universal Omnibox Command Bar & Channel Filter Pills
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

        // 3. Central Triage Viewport (Stream, People Hub, VIP Shield, or Minimal Apps)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // VIP Focus Mode Banner
            if (viewMode == HubViewMode.VIP_FOCUS) {
                item(key = "vip_focus_banner") {
                    val heldCount = allMessages.count { !it.isVip }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ClicksOrange.copy(alpha = 0.14f))
                            .border(1.dp, ClicksOrange.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VIP PRIORITY SHIELD ACTIVE",
                                color = ClicksOrange,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Showing urgent VIP threads only · $heldCount non-VIP notifications held for later",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
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
                                fontSize = 11.sp
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
                    item(key = "minimal_apps_drawer") {
                        MinimalSpeedDialAppList(
                            installedApps = installedApps,
                            searchQuery = omniboxQuery,
                            onLaunchApp = { app ->
                                viewModel.launchApp(context, app.packageName, app.label)
                            },
                            onOpenDefaultLauncherSettings = {
                                viewModel.openDefaultLauncherSettings(context)
                            }
                        )
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
                                    .padding(28.dp),
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
                                Spacer(modifier = Modifier.height(14.dp))
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

        // 4. Bottom Clicks Tactile Shortcut Dock & Optional On-Screen QWERTY Deck
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
