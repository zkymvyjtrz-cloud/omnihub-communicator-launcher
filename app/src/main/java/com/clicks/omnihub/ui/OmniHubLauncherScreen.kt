package com.clicks.omnihub.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.components.ConversationContextSheet
import com.clicks.omnihub.ui.components.HubMessageCard
import com.clicks.omnihub.ui.components.MaterialOmniboxTopBar
import com.clicks.omnihub.ui.components.OmniHubBottomNavigationBar
import com.clicks.omnihub.ui.components.PersonUnifiedThreadCard
import com.clicks.omnihub.ui.theme.M3SurfaceBackground
import com.clicks.omnihub.ui.theme.M3SurfaceContainer
import com.clicks.omnihub.ui.theme.M3SurfaceContainerHigh

/**
 * Root Home Screen Composable for the clean Material 3 OmniHub Communicator Launcher.
 * - Uses Material 3 Scaffold, FilterChips, ElevatedCards, and NavigationBar.
 * - Exclusively uses the device's default system keyboard (Samsung Keyboard / Gboard).
 * - Tapping any conversation opens [ConversationContextSheet] with full multi-turn thread history
 *   and attachment support.
 */
@Composable
fun OmniHubLauncherScreen(viewModel: HubViewModel) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val allMessages by viewModel.allMessages.collectAsState()
    val visibleMessages by viewModel.visibleMessages.collectAsState()
    val personThreads by viewModel.personThreads.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val omniboxQuery by viewModel.omniboxQuery.collectAsState()
    val isOmniboxComposerOpen by viewModel.isOmniboxComposerOpen.collectAsState()
    val openedConversation by viewModel.openedConversation.collectAsState()
    val pendingAttachment by viewModel.pendingAttachment.collectAsState()
    val isLiveBridgeConnected by viewModel.isLiveBridgeConnected.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()

    // Native Android System File / Photo Picker for Attachments
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onAttachmentPicked(context, uri)
    }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    // Handle system Back press cleanly inside Conversation Detail view or Omnibox
    BackHandler(enabled = openedConversation != null || isOmniboxComposerOpen || omniboxQuery.isNotEmpty()) {
        when {
            openedConversation != null -> viewModel.closeConversation()
            isOmniboxComposerOpen || omniboxQuery.isNotEmpty() -> viewModel.clearOmnibox()
        }
    }

    val platformCounts = remember(allMessages) {
        val counts = mutableMapOf<MessagingPlatform, Int>()
        counts[MessagingPlatform.ALL] = allMessages.size
        MessagingPlatform.entries.filter { it != MessagingPlatform.ALL }.forEach { platform ->
            counts[platform] = allMessages.count { it.platform == platform }
        }
        counts
    }

    val defaultEssentialApps = remember {
        listOf(
            InstalledAppInfo("Phone", "com.samsung.android.dialer", "Phone"),
            InstalledAppInfo("Messages", "com.google.android.apps.messaging", "Communication"),
            InstalledAppInfo("WhatsApp", "com.whatsapp", "Communication"),
            InstalledAppInfo("Microsoft Teams", "com.microsoft.teams", "Communication"),
            InstalledAppInfo("Microsoft Outlook", "com.microsoft.office.outlook", "Communication"),
            InstalledAppInfo("Gmail", "com.google.android.gm", "Communication"),
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

    Crossfade(
        targetState = openedConversation,
        label = "ConversationDetailTransition"
    ) { activeConversation ->
        if (activeConversation != null) {
            // Full-Screen Material 3 Conversation Context View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(M3SurfaceBackground)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                ConversationContextSheet(
                    message = activeConversation,
                    pendingAttachment = pendingAttachment,
                    onBack = { viewModel.closeConversation() },
                    onSendReply = { replyText ->
                        viewModel.sendReply(context, activeConversation.id, replyText)
                    },
                    onPickAttachment = {
                        attachmentPickerLauncher.launch("*/*")
                    },
                    onClearPendingAttachment = {
                        viewModel.clearPendingAttachment()
                    },
                    onOpenInNativeApp = {
                        viewModel.openOriginalApp(context, activeConversation)
                    },
                    onArchive = {
                        viewModel.archiveMessage(activeConversation.id)
                    }
                )
            }
        } else {
            // Main Material 3 Home Launcher Scaffold
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                containerColor = M3SurfaceBackground,
                topBar = {
                    MaterialOmniboxTopBar(
                        unreadCount = allMessages.count { it.isUnread },
                        isLiveBridgeConnected = isLiveBridgeConnected || viewModel.isNotificationAccessEnabled(context),
                        query = omniboxQuery,
                        isComposerExpanded = isOmniboxComposerOpen,
                        selectedPlatform = selectedPlatform,
                        platformCounts = platformCounts,
                        pendingAttachment = pendingAttachment,
                        onQueryChange = { viewModel.updateOmniboxQuery(it) },
                        onClearQuery = { viewModel.clearOmnibox() },
                        onToggleComposer = { viewModel.toggleOmniboxComposer() },
                        onSelectPlatform = { viewModel.selectPlatform(it) },
                        onPickAttachment = { attachmentPickerLauncher.launch("*/*") },
                        onClearPendingAttachment = { viewModel.clearPendingAttachment() },
                        onSendOmniboxMessage = { recipient, platform, body ->
                            viewModel.sendOmniboxOutbound(context, recipient, platform, body)
                        },
                        onOpenLiveBridgeSettings = { viewModel.openNotificationAccessSettings(context) },
                        onResetDemo = { viewModel.resetDemoStream() }
                    )
                },
                bottomBar = {
                    OmniHubBottomNavigationBar(
                        currentMode = viewMode,
                        onSelectMode = { viewModel.setViewMode(it) }
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (viewMode == HubViewMode.VIP_FOCUS) {
                        item(key = "vip_focus_banner") {
                            val heldCount = allMessages.count { !it.isVip }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "VIP Priority Focus",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Showing starred VIP contacts · $heldCount other threads muted",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                        )
                                    }
                                    TextButton(onClick = { viewModel.setViewMode(HubViewMode.STREAM) }) {
                                        Text(
                                            text = "Show All",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                                    onOpenMessageConversation = { msgId ->
                                        viewModel.openConversation(msgId)
                                    },
                                    onArchiveAllFromPerson = {
                                        thread.messages.forEach { msg ->
                                            viewModel.archiveMessage(msg.id)
                                        }
                                    }
                                )
                            }
                        }

                        HubViewMode.APPS -> {
                            item(key = "set_default_launcher_banner") {
                                Surface(
                                    color = M3SurfaceContainerHigh,
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.openDefaultLauncherSettings(context) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Default Home Launcher",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "${filteredApps.size} installed apps · Tap to set OmniHub as Default Home",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = { viewModel.openDefaultLauncherSettings(context) }) {
                                            Text("Set Default")
                                        }
                                    }
                                }
                            }

                            itemsIndexed(
                                items = filteredApps,
                                key = { _, app -> "app_${app.packageName}" }
                            ) { index, app ->
                                Surface(
                                    color = M3SurfaceContainer,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.launchApp(context, app.packageName, app.label) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = String.format("%02d", index + 1),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(14.dp))
                                            Text(
                                                text = app.label,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Surface(
                                            color = M3SurfaceContainerHigh,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = app.categoryTag,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HubViewMode.STREAM, HubViewMode.VIP_FOCUS -> {
                            if (visibleMessages.isEmpty()) {
                                item(key = "inbox_zero_state") {
                                    ElevatedCard(
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(M3SurfaceContainer)
                                                .padding(28.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "You're all caught up",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Every conversation across your connected apps has been triaged.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(14.dp))
                                            Button(onClick = { viewModel.resetDemoStream() }) {
                                                Text("Restore Sample Conversations")
                                            }
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
                                        onOpenConversation = { viewModel.openConversation(message.id) },
                                        onQuickReply = { replyText ->
                                            viewModel.sendReply(context, message.id, replyText)
                                        },
                                        onArchive = { viewModel.archiveMessage(message.id) },
                                        onSnooze = { viewModel.snoozeMessage(message.id) },
                                        onToggleVip = { viewModel.toggleVipForMessage(message) }
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
