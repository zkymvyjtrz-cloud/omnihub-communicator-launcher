package com.clicks.omnihub.ui

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.ui.components.ConversationContextSheet
import com.clicks.omnihub.ui.components.DedicatedWidgetsDashboardPage
import com.clicks.omnihub.ui.components.HubMessageCard
import com.clicks.omnihub.ui.components.MaterialOmniboxTopBar
import com.clicks.omnihub.ui.components.OmniHubBottomNavigationBar
import com.clicks.omnihub.ui.components.PersonUnifiedThreadCard
import com.clicks.omnihub.ui.components.QuickSmartphoneFavoritesBar
import com.clicks.omnihub.ui.components.SmartphoneAppDrawerGridPage
import com.clicks.omnihub.ui.theme.M3SurfaceBackground
import com.clicks.omnihub.ui.theme.M3SurfaceContainer

private const val APPWIDGET_HOST_ID = 2048

/**
 * Root Home Screen Composable for the Material 3 OmniHub Communicator Launcher.
 * Combines:
 * - Communication-First Unified Stream (`Inbox`) & Person-Centric Threads (`People`)
 * - Full-Screen Conversation Detail & Context Sheet (`ConversationContextSheet`)
 * - Dedicated Widgets Panel (`Widgets` tab) with Built-in Material 3 Widgets + Native Android `AppWidgetHost`
 * - Full Smartphone App Drawer (`Apps` tab) with Real Icons, Categories & Pinned Favorites Dock
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
            omniboxQuery.isNotEmpty() ||
            viewMode != HubViewMode.STREAM
    ) {
        when {
            openedConversation != null -> viewModel.closeConversation()
            isOmniboxComposerOpen || omniboxQuery.isNotEmpty() -> viewModel.clearOmnibox()
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
    val favoriteDockApps = remember(activeAppsList) {
        activeAppsList.filter { it.isFavorite }.take(4)
    }

    Crossfade(
        targetState = openedConversation,
        label = "ConversationDetailTransition"
    ) { activeConversation ->
        if (activeConversation != null) {
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
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Show Quick Smartphone Favorites Dock on the Inbox & People tabs so apps/widgets are 1 tap away
                        if (viewMode == HubViewMode.STREAM || viewMode == HubViewMode.PEOPLE) {
                            QuickSmartphoneFavoritesBar(
                                favoriteApps = favoriteDockApps,
                                onLaunchApp = { app ->
                                    viewModel.launchApp(context, app.packageName, app.label)
                                },
                                onOpenDialer = {
                                    viewModel.launchSystemAction(context, Intent.ACTION_DIAL)
                                },
                                onOpenCamera = {
                                    viewModel.launchSystemAction(context, MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)
                                },
                                onOpenBrowser = {
                                    viewModel.launchApp(context, "com.android.chrome", "Chrome")
                                },
                                onOpenWidgetsPage = {
                                    viewModel.setViewMode(HubViewMode.WIDGETS)
                                },
                                onOpenAppsDrawer = {
                                    viewModel.setViewMode(HubViewMode.APPS)
                                }
                            )
                        }
                        OmniHubBottomNavigationBar(
                            currentMode = viewMode,
                            onSelectMode = { viewModel.setViewMode(it) }
                        )
                    }
                }
            ) { innerPadding ->
                BoxContainer(
                    modifier = Modifier
                        .fillMaxSize()
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
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
                        }

                        HubViewMode.STREAM -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
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
}

@Composable
private fun BoxContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        content()
    }
}
