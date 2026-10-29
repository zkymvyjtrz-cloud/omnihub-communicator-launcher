package com.clicks.omnihub.ui

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clicks.omnihub.data.HubRepository
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.model.MessagingPlatform
import com.clicks.omnihub.model.PersonUnifiedThread
import com.clicks.omnihub.service.HubNotificationListenerService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel powering the Clicks Communicator / BlackBerry Hub-inspired Home Launcher.
 * Coordinates:
 * 1. Unified Hub Stream & Channel Filters (WhatsApp, Teams, Outlook, Gmail, SMS, Messenger)
 * 2. Next-Gen Omnibox Command Bar (`/msg @Contact`, instant search, app launcher)
 * 3. Person-Centric Unified Threads (merging messages across apps by person)
 * 4. Physical QWERTY / Clicks Tactile Shortcuts (`R` Reply, `A` Archive, `S` Snooze, `F` Focus, `P` People)
 * 5. VIP Focus Shield & Monochrome Minimal App Launcher
 */
class HubViewModel : ViewModel() {

    val allMessages: StateFlow<List<HubMessage>> = HubRepository.messages
    val isLiveBridgeConnected: StateFlow<Boolean> = HubRepository.isNotificationListenerConnected
    val statusBanner: StateFlow<String> = HubRepository.statusBanner

    private val _selectedPlatform = MutableStateFlow(MessagingPlatform.ALL)
    val selectedPlatform: StateFlow<MessagingPlatform> = _selectedPlatform.asStateFlow()

    private val _viewMode = MutableStateFlow(HubViewMode.STREAM)
    val viewMode: StateFlow<HubViewMode> = _viewMode.asStateFlow()

    private val _omniboxQuery = MutableStateFlow("")
    val omniboxQuery: StateFlow<String> = _omniboxQuery.asStateFlow()

    private val _isOmniboxComposerOpen = MutableStateFlow(false)
    val isOmniboxComposerOpen: StateFlow<Boolean> = _isOmniboxComposerOpen.asStateFlow()

    private val _focusedMessageId = MutableStateFlow<String?>("demo_teams_sarah")
    val focusedMessageId: StateFlow<String?> = _focusedMessageId.asStateFlow()

    private val _inlineReplyingMessageId = MutableStateFlow<String?>(null)
    val inlineReplyingMessageId: StateFlow<String?> = _inlineReplyingMessageId.asStateFlow()

    private val _showTactileShortcutDeck = MutableStateFlow(false)
    val showTactileShortcutDeck: StateFlow<Boolean> = _showTactileShortcutDeck.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    /**
     * Filtered message stream based on active platform pill, Omnibox search query, and VIP Focus mode.
     */
    val visibleMessages: StateFlow<List<HubMessage>> = combine(
        allMessages,
        _selectedPlatform,
        _omniboxQuery,
        _viewMode
    ) { messages, platform, query, mode ->
        val cleanQuery = query.trim()
        val isSlashCommand = cleanQuery.startsWith("/")

        messages.filter { msg ->
            val matchesPlatform = platform == MessagingPlatform.ALL || msg.platform == platform
            val matchesVipFocus = mode != HubViewMode.VIP_FOCUS || msg.isVip
            val matchesSearch = if (cleanQuery.isEmpty() || isSlashCommand) {
                true
            } else {
                msg.senderName.contains(cleanQuery, ignoreCase = true) ||
                    msg.body.contains(cleanQuery, ignoreCase = true) ||
                    (msg.subject?.contains(cleanQuery, ignoreCase = true) == true) ||
                    msg.platform.displayName.contains(cleanQuery, ignoreCase = true)
            }
            matchesPlatform && matchesVipFocus && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Person-Centric Unified Threads: merges messages from the same contact across WhatsApp, Teams, Outlook, SMS, etc.
     */
    val personThreads: StateFlow<List<PersonUnifiedThread>> = combine(
        allMessages,
        _omniboxQuery
    ) { messages, query ->
        val cleanQuery = query.trim().removePrefix("@")
        val filtered = if (cleanQuery.isEmpty() || cleanQuery.startsWith("/")) {
            messages
        } else {
            messages.filter {
                it.senderName.contains(cleanQuery, ignoreCase = true) ||
                    it.body.contains(cleanQuery, ignoreCase = true)
            }
        }

        filtered
            .groupBy { it.senderName.lowercase().trim() }
            .map { (_, personMsgs) ->
                val sorted = personMsgs.sortedByDescending { it.timestampMillis }
                val first = sorted.first()
                PersonUnifiedThread(
                    senderName = first.senderName,
                    senderInitials = first.senderInitials,
                    avatarColor = first.avatarColor,
                    isVip = sorted.any { it.isVip },
                    messages = sorted,
                    activePlatforms = sorted.map { it.platform }.distinct(),
                    latestTimestampMillis = first.timestampMillis
                )
            }
            .sortedByDescending { it.latestTimestampMillis }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectPlatform(platform: MessagingPlatform) {
        _selectedPlatform.value = platform
        if (_viewMode.value == HubViewMode.APPS) {
            _viewMode.value = HubViewMode.STREAM
        }
        HubRepository.postStatus("Channel Filter: ${platform.displayName}")
    }

    fun setViewMode(mode: HubViewMode) {
        _viewMode.value = mode
        HubRepository.postStatus("Switched to ${mode.label} [${mode.shortcutKey}]")
    }

    fun updateOmniboxQuery(query: String) {
        _omniboxQuery.value = query
        if (query.startsWith("/msg", ignoreCase = true) || query.startsWith("@")) {
            _isOmniboxComposerOpen.value = true
        } else if (query.equals("/clear", ignoreCase = true)) {
            _omniboxQuery.value = ""
            _isOmniboxComposerOpen.value = false
        } else if (query.equals("/reset", ignoreCase = true)) {
            _omniboxQuery.value = ""
            HubRepository.resetDemoStream()
        }
    }

    fun toggleOmniboxComposer(forceOpen: Boolean? = null) {
        val next = forceOpen ?: !_isOmniboxComposerOpen.value
        _isOmniboxComposerOpen.value = next
        if (next && _omniboxQuery.value.isBlank()) {
            _omniboxQuery.value = "/msg @Sarah Jenkins "
        }
    }

    fun clearOmnibox() {
        _omniboxQuery.value = ""
        _isOmniboxComposerOpen.value = false
    }

    fun focusMessage(messageId: String) {
        _focusedMessageId.value = messageId
    }

    fun toggleInlineReply(messageId: String) {
        _focusedMessageId.value = messageId
        _inlineReplyingMessageId.value = if (_inlineReplyingMessageId.value == messageId) null else messageId
    }

    fun toggleTactileShortcutDeck() {
        _showTactileShortcutDeck.value = !_showTactileShortcutDeck.value
    }

    fun sendReply(context: Context, messageId: String, replyText: String) {
        if (HubRepository.sendInlineReply(context, messageId, replyText)) {
            _inlineReplyingMessageId.value = null
        }
    }

    fun archiveFocusedOrSpecificMessage(messageId: String? = null) {
        val targetId = messageId ?: _focusedMessageId.value ?: visibleMessages.value.firstOrNull()?.id ?: return
        val nextCandidate = visibleMessages.value.firstOrNull { it.id != targetId }?.id
        HubRepository.archiveMessage(targetId) { notifKey ->
            HubNotificationListenerService.instance?.dismissSystemNotification(notifKey)
        }
        _focusedMessageId.value = nextCandidate
        if (_inlineReplyingMessageId.value == targetId) {
            _inlineReplyingMessageId.value = null
        }
    }

    fun snoozeFocusedOrSpecificMessage(messageId: String? = null) {
        val targetId = messageId ?: _focusedMessageId.value ?: visibleMessages.value.firstOrNull()?.id ?: return
        val nextCandidate = visibleMessages.value.firstOrNull { it.id != targetId }?.id
        HubRepository.snoozeMessage(targetId)
        _focusedMessageId.value = nextCandidate
    }

    fun toggleVipForMessage(message: HubMessage) {
        HubRepository.toggleVipForSender(message.senderName)
    }

    fun sendOmniboxOutbound(recipient: String, platform: MessagingPlatform, body: String) {
        if (body.isBlank()) return
        HubRepository.dispatchOmniboxOutboundMessage(recipient, platform, body)
        _isOmniboxComposerOpen.value = false
        _omniboxQuery.value = ""
    }

    fun resetDemoStream() {
        HubRepository.resetDemoStream()
        _focusedMessageId.value = "demo_teams_sarah"
    }

    /**
     * Handles physical QWERTY / Clicks Communicator single-key shortcuts when the user is not
     * actively typing in a text field.
     */
    fun handleHardwareShortcutKey(keyChar: Char): Boolean {
        return when (keyChar.uppercaseChar()) {
            'R' -> {
                val targetId = _focusedMessageId.value ?: visibleMessages.value.firstOrNull()?.id
                if (targetId != null) {
                    toggleInlineReply(targetId)
                    HubRepository.postStatus("Shortcut [R] · Inline Reply opened")
                    true
                } else false
            }
            'A' -> {
                archiveFocusedOrSpecificMessage()
                true
            }
            'S' -> {
                snoozeFocusedOrSpecificMessage()
                true
            }
            'P' -> {
                val nextMode = if (_viewMode.value == HubViewMode.PEOPLE) HubViewMode.STREAM else HubViewMode.PEOPLE
                setViewMode(nextMode)
                true
            }
            'F' -> {
                val nextMode = if (_viewMode.value == HubViewMode.VIP_FOCUS) HubViewMode.STREAM else HubViewMode.VIP_FOCUS
                setViewMode(nextMode)
                true
            }
            'L' -> {
                val nextMode = if (_viewMode.value == HubViewMode.APPS) HubViewMode.STREAM else HubViewMode.APPS
                setViewMode(nextMode)
                true
            }
            '/' -> {
                toggleOmniboxComposer(forceOpen = true)
                true
            }
            'J' -> {
                moveFocusDelta(1)
                true
            }
            'K' -> {
                moveFocusDelta(-1)
                true
            }
            else -> false
        }
    }

    fun moveFocusDelta(delta: Int) {
        val currentList = visibleMessages.value
        if (currentList.isEmpty()) return
        val currentIdx = currentList.indexOfFirst { it.id == _focusedMessageId.value }.coerceAtLeast(0)
        val nextIdx = (currentIdx + delta).coerceIn(0, currentList.lastIndex)
        val nextMsg = currentList[nextIdx]
        _focusedMessageId.value = nextMsg.id
        HubRepository.postStatus("Focused [${nextMsg.platform.displayName}] ${nextMsg.senderName}")
    }

    /**
     * Loads installed launchable Android apps so the Omnibox and Monochrome Speed-Dial drawer
     * can launch any application on the device.
     */
    fun loadInstalledApps(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }
                val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
                val apps = resolveInfos
                    .mapNotNull { info ->
                        val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                        if (pkg == context.packageName) return@mapNotNull null
                        val label = info.loadLabel(pm)?.toString() ?: pkg
                        InstalledAppInfo(
                            label = label,
                            packageName = pkg,
                            categoryTag = categorizePackage(pkg)
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }

                _installedApps.value = apps
            } catch (_: Exception) {
            }
        }
    }

    fun launchApp(context: Context, packageName: String, label: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                HubRepository.postStatus("Launched $label")
            } else {
                HubRepository.postStatus("App $label is not installed on this device")
            }
        } catch (e: Exception) {
            HubRepository.postStatus("Could not launch $label")
        }
    }

    fun isNotificationAccessEnabled(context: Context): Boolean {
        return NotificationManagerCompat.getEnabledListenerPackages(context)
            .contains(context.packageName)
    }

    fun openNotificationAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openDefaultLauncherSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    private fun categorizePackage(pkg: String): String {
        return when {
            MessagingPlatform.fromPackageName(pkg) != null -> "Comms"
            pkg.contains("calendar", ignoreCase = true) -> "Schedule"
            pkg.contains("dialer", ignoreCase = true) || pkg.contains("telecom", ignoreCase = true) -> "Phone"
            pkg.contains("maps", ignoreCase = true) -> "Navigation"
            pkg.contains("camera", ignoreCase = true) -> "Camera"
            pkg.contains("notes", ignoreCase = true) || pkg.contains("keep", ignoreCase = true) -> "Notes"
            else -> "Utility"
        }
    }
}
