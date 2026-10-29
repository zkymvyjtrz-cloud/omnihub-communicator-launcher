package com.clicks.omnihub.ui

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.components.AirframeAppHeader
import com.clicks.omnihub.ui.components.AirframeChannelCapsuleStrip
import com.clicks.omnihub.ui.components.AirframeEyebrow
import com.clicks.omnihub.ui.components.AirframeFeatureSection
import com.clicks.omnihub.ui.components.AirframeHeroMetricSection
import com.clicks.omnihub.ui.components.AirframeMessageListRow
import com.clicks.omnihub.ui.components.AirframePersonUnifiedRow
import com.clicks.omnihub.ui.components.AirframeRule
import com.clicks.omnihub.ui.components.AirframeTabBar
import com.clicks.omnihub.ui.components.AirframeToastBanner
import com.clicks.omnihub.ui.components.ConversationContextSheet
import com.clicks.omnihub.ui.components.DedicatedWidgetsDashboardPage
import com.clicks.omnihub.ui.components.SmartphoneAppDrawerGridPage
import com.clicks.omnihub.ui.theme.AirframeSpace
import com.clicks.omnihub.ui.theme.AirframeTheme
import com.clicks.omnihub.ui.theme.AirframeType

private const val APPWIDGET_HOST_ID = 2048

/**
 * Root Home Screen Composable for the Airframe-styled OmniHub Communicator Launcher.
 * Strictly adheres to the Airframe Design System (`/Users/joserivera/Downloads/Airframe`):
 * - Warm paper `ground` (`#F7F6F2` Ivory / `#111412` Night) with 1px `line` hairlines instead of heavy cards.
 * - Single-column editorial rhythm (`26dp` `space-gutter`, `26dp` `space-section`).
 * - One hero `numeral-xl` figure per screen (`7 new`, `5 people`, `72 °F`).
 * - Signature `CapsuleStrip`, `Feature` + `StatPair`, `ListRow`, `MessageBubble`, `Composer`, and 4-item `TabBar`.
 * - Exclusively uses the device's native default keyboard.
 */
@Composable
fun OmniHubLauncherScreen(viewModel: HubViewModel) {
    val c = AirframeTheme.colors
    val context = LocalContext.current
    val listState = rememberLazyListState()

    val allMessages by viewModel.allMessages.collectAsState()
    val visibleMessages by viewModel.visibleMessages.collectAsState()
    val personThreads by viewModel.personThreads.collectAsState()
    val selectedPlatform by viewModel.selectedPlatform.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val omniboxQuery by viewModel.omniboxQuery.collectAsState()
    val isOmniboxComposerOpen by viewModel.isOmniboxComposerOpen.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val isNightTheme by viewModel.isNightTheme.collectAsState()
    val openedConversation by viewModel.openedConversation.collectAsState()
    val pendingAttachment by viewModel.pendingAttachment.collectAsState()
    val isLiveBridgeConnected by viewModel.isLiveBridgeConnected.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val selectedAppCategory by viewModel.selectedAppCategory.collectAsState()
    val hostedWidgetIds by viewModel.hostedAppWidgetIds.collectAsState()
    val quickNoteText by viewModel.quickNoteText.collectAsState()

    // Native Android AppWidgetHost & AppWidgetManager for hosting real system widgets
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetHost = remember { AppWidgetHost(context, APPWIDGET_HOST_ID) }

    DisposableEffect(appWidgetHost) {
        try {
            appWidgetHost.startListening()
        } catch (_: Exception) {
        }
        onDispose {
            try {
                appWidgetHost.stopListening()
            } catch (_: Exception) {
            }
        }
    }

    // Launcher for picking a native Android System Widget
    val widgetPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val appWidgetId = data?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1) ?: -1
        if (result.resultCode == Activity.RESULT_OK && appWidgetId != -1) {
            viewModel.addHostedWidgetId(appWidgetId)
        } else if (appWidgetId != -1) {
            try {
                appWidgetHost.deleteAppWidgetId(appWidgetId)
            } catch (_: Exception) {
            }
        }
    }

    // Native Android System File / Photo Picker for Attachments
    val attachmentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        viewModel.onAttachmentPicked(context, uri)
    }

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    // Handle system Back press cleanly
    BackHandler(
        enabled = openedConversation != null ||
            isOmniboxComposerOpen ||
            isSearchOpen ||
            omniboxQuery.isNotEmpty() ||
            viewMode != HubViewMode.STREAM
    ) {
        when {
            openedConversation != null -> viewModel.closeConversation()
            isOmniboxComposerOpen || isSearchOpen || omniboxQuery.isNotEmpty() -> viewModel.clearOmnibox()
            viewMode != HubViewMode.STREAM -> viewModel.setViewMode(HubViewMode.STREAM)
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

    val unreadCount = remember(allMessages) { allMessages.count { it.isUnread } }
    val urgentCount = remember(allMessages) { allMessages.count { it.isUnread && it.isVip } }
    val totalAttachments = remember(allMessages) { allMessages.sumOf { it.attachments.size } }
    val clearedPercent = remember(allMessages, unreadCount) {
        val baseline = (allMessages.size + 12).coerceAtLeast(1)
        val done = (baseline - unreadCount).coerceAtLeast(1)
        ((done * 100) / baseline).coerceIn(18, 100)
    }

    val featuredUrgentMessage = remember(visibleMessages, allMessages) {
        visibleMessages.firstOrNull { it.isUnread && it.isVip }
            ?: visibleMessages.firstOrNull { it.isUnread }
            ?: allMessages.firstOrNull()
    }

    val defaultEssentialApps = remember {
        listOf(
            InstalledAppInfo("Phone", "com.samsung.android.dialer", "Communication", isFavorite = true),
            InstalledAppInfo("Messages", "com.google.android.apps.messaging", "Communication", isFavorite = true),
            InstalledAppInfo("WhatsApp", "com.whatsapp", "Communication", isFavorite = true),
            InstalledAppInfo("Microsoft Teams", "com.microsoft.teams", "Communication", isFavorite = true),
            InstalledAppInfo("Microsoft Outlook", "com.microsoft.office.outlook", "Communication", isFavorite = true),
            InstalledAppInfo("Gmail", "com.google.android.gm", "Communication", isFavorite = true),
            InstalledAppInfo("Calendar", "com.samsung.android.calendar", "Productivity", isFavorite = true),
            InstalledAppInfo("Google Maps", "com.google.android.apps.maps", "Tools", isFavorite = true),
            InstalledAppInfo("Camera", "com.sec.android.app.camera", "Media", isFavorite = true)
        )
    }

    val activeAppsList = if (installedApps.isNotEmpty()) installedApps else defaultEssentialApps

    Crossfade(
        targetState = openedConversation,
        label = "AirframeConversationTransition"
    ) { activeConversation ->
        if (activeConversation != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.ground)
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
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(c.ground)
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                containerColor = c.ground,
                topBar = {
                    AirframeAppHeader(
                        isNightTheme = isNightTheme,
                        isLiveBridgeConnected = isLiveBridgeConnected || viewModel.isNotificationAccessEnabled(context),
                        query = omniboxQuery,
                        isSearchOpen = isSearchOpen,
                        isComposerOpen = isOmniboxComposerOpen,
                        pendingAttachment = pendingAttachment,
                        onToggleTheme = { viewModel.toggleNightTheme() },
                        onToggleSearch = { viewModel.toggleSearch() },
                        onQueryChange = { viewModel.updateOmniboxQuery(it) },
                        onClearQuery = { viewModel.clearOmnibox() },
                        onToggleComposer = { viewModel.toggleOmniboxComposer() },
                        onPickAttachment = { attachmentPickerLauncher.launch("*/*") },
                        onClearPendingAttachment = { viewModel.clearPendingAttachment() },
                        onSendOmniboxMessage = { recipient, platform, body ->
                            viewModel.sendOmniboxOutbound(context, recipient, platform, body)
                        },
                        onOpenLiveBridgeSettings = { viewModel.openNotificationAccessSettings(context) }
                    )
                },
                bottomBar = {
                    AirframeTabBar(
                        currentMode = viewMode,
                        onSelectMode = { viewModel.setViewMode(it) }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(c.ground)
                        .padding(innerPadding)
                ) {
                    when (viewMode) {
                        HubViewMode.WIDGETS -> {
                            DedicatedWidgetsDashboardPage(
                                appWidgetHost = appWidgetHost,
                                appWidgetManager = appWidgetManager,
                                hostedWidgetIds = hostedWidgetIds,
                                quickNoteText = quickNoteText,
                                onUpdateQuickNote = { viewModel.updateQuickNote(it) },
                                onAddSystemWidgetClick = {
                                    try {
                                        val appWidgetId = appWidgetHost.allocateAppWidgetId()
                                        val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK).apply {
                                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                                        }
                                        widgetPickerLauncher.launch(pickIntent)
                                    } catch (_: Exception) {
                                    }
                                },
                                onRemoveSystemWidget = { id ->
                                    try {
                                        appWidgetHost.deleteAppWidgetId(id)
                                    } catch (_: Exception) {
                                    }
                                    viewModel.removeHostedWidgetId(id)
                                },
                                onOpenSystemAction = { action ->
                                    viewModel.launchSystemAction(context, action)
                                }
                            )
                        }

                        HubViewMode.APPS -> {
                            SmartphoneAppDrawerGridPage(
                                installedApps = activeAppsList,
                                selectedCategory = selectedAppCategory,
                                searchQuery = omniboxQuery,
                                onSelectCategory = { viewModel.selectAppCategory(it) },
                                onLaunchApp = { app ->
                                    viewModel.launchApp(context, app.packageName, app.label)
                                },
                                onLongPressApp = { app ->
                                    viewModel.openAppInfoSettings(context, app.packageName)
                                },
                                onOpenDefaultLauncherSettings = {
                                    viewModel.openDefaultLauncherSettings(context)
                                }
                            )
                        }

                        HubViewMode.PEOPLE -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(c.ground),
                                contentPadding = PaddingValues(horizontal = AirframeSpace.gutter, vertical = 14.dp)
                            ) {
                                // Airframe Hero Metric for People screen
                                item(key = "people_hero_metric") {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        AirframeEyebrow(
                                            label = "People & relationships",
                                            aside = "${allMessages.size} messages"
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            Row(verticalAlignment = Alignment.Bottom) {
                                                Text(
                                                    text = personThreads.size.toString(),
                                                    style = AirframeType.numeralXl,
                                                    color = c.ink
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = "people",
                                                    style = AirframeType.numeralUnit,
                                                    color = c.text,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .padding(bottom = 6.dp)
                                                    .height(36.dp)
                                                    .clip(CircleShape)
                                                    .background(c.sageSoft)
                                                    .padding(horizontal = AirframeSpace.space4),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${personThreads.count { it.isVip }} VIP · Multi-channel",
                                                    style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold),
                                                    color = c.forest
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(AirframeSpace.space4))
                                        Text(
                                            text = "Every conversation grouped by person across WhatsApp, Teams, Outlook, Gmail, and SMS.",
                                            style = AirframeType.lede,
                                            color = c.text
                                        )
                                    }
                                    AirframeRule()
                                    AirframeEyebrow(
                                        label = "Active contacts",
                                        aside = "Tap to open thread"
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                items(
                                    items = personThreads,
                                    key = { "person_${it.senderName}" }
                                ) { thread ->
                                    AirframePersonUnifiedRow(
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
                        }

                        HubViewMode.STREAM -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(c.ground),
                                contentPadding = PaddingValues(horizontal = AirframeSpace.gutter, vertical = 14.dp)
                            ) {
                                // 1. Airframe Hero `Metric` + `ProgressBar` Section
                                item(key = "inbox_airframe_hero_metric") {
                                    AirframeHeroMetricSection(
                                        eyebrowLabel = "Today's Inbox",
                                        eyebrowAside = "${allMessages.count { it.platform != MessagingPlatform.ALL }.coerceAtLeast(1)} threads · 5 apps",
                                        numeralValue = unreadCount.toString(),
                                        pillText = "$urgentCount urgent · $totalAttachments files",
                                        ledeText = "One calm feed across every app you talk in.",
                                        clearedPercent = clearedPercent,
                                        onPillClick = { viewModel.toggleVipOnlyFilter() }
                                    )
                                }

                                // 2. Airframe Signature `CapsuleStrip` ("By channel")
                                item(key = "inbox_airframe_capsule_strip") {
                                    AirframeRule()
                                    AirframeChannelCapsuleStrip(
                                        selectedPlatform = selectedPlatform,
                                        platformCounts = platformCounts,
                                        onSelectPlatform = { viewModel.selectPlatform(it) }
                                    )
                                }

                                // 3. Airframe `Feature` + `StatPair` ("Needs you" + single `forest` primary button)
                                if (featuredUrgentMessage != null) {
                                    item(key = "inbox_airframe_feature_section") {
                                        AirframeRule()
                                        AirframeFeatureSection(
                                            featuredMessage = featuredUrgentMessage,
                                            onOpenConversation = {
                                                viewModel.openConversation(featuredUrgentMessage.id)
                                            },
                                            onMarkDone = {
                                                viewModel.archiveMessage(featuredUrgentMessage.id)
                                            }
                                        )
                                    }
                                }

                                // 4. Airframe `ListRow` Section ("Earlier")
                                item(key = "inbox_earlier_eyebrow") {
                                    AirframeRule()
                                    AirframeEyebrow(
                                        label = if (selectedPlatform == MessagingPlatform.ALL) "Earlier" else selectedPlatform.displayName,
                                        aside = "Reset demo",
                                        onAsideClick = { viewModel.resetDemoStream() }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                if (visibleMessages.isEmpty()) {
                                    item(key = "inbox_zero_toast") {
                                        Spacer(modifier = Modifier.height(AirframeSpace.space3))
                                        AirframeToastBanner(
                                            message = "All conversations triaged",
                                            actionLabel = "Restore",
                                            onActionClick = { viewModel.resetDemoStream() }
                                        )
                                    }
                                } else {
                                    items(
                                        items = visibleMessages,
                                        key = { it.id }
                                    ) { message ->
                                        AirframeMessageListRow(
                                            message = message,
                                            onOpenConversation = { viewModel.openConversation(message.id) },
                                            onMarkDone = { viewModel.archiveMessage(message.id) }
                                        )
                                    }
                                }

                                item(key = "inbox_bottom_breathing_room") {
                                    Spacer(modifier = Modifier.height(AirframeSpace.space6))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
