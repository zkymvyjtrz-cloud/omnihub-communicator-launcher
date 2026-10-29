package com.clicks.omnihub.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clicks.omnihub.data.HubRepository
import com.clicks.omnihub.model.AttachmentType
import com.clicks.omnihub.model.HubAttachment
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Material 3 OmniHub Communicator Launcher.
 * Coordinates:
 * - Unified Communication Stream &VIP Filter
 * - Full-Screen Conversation Detail & Context Sheet
 * - Dedicated Widgets Panel (Built-in Material 3 Widgets + Native Android AppWidgetHost IDs)
 * - Full Smartphone App Drawer (with real app icons, Pinned Favorites Dock, and App Info deep links)
 */
class HubViewModel : ViewModel() {

    val allMessages: StateFlow<List<HubMessage>> = HubRepository.messages
    val isLiveBridgeConnected: StateFlow<Boolean> = HubRepository.isNotificationListenerConnected

    private val _selectedPlatform = MutableStateFlow(MessagingPlatform.ALL)
    val selectedPlatform: StateFlow<MessagingPlatform> = _selectedPlatform.asStateFlow()

    private val _viewMode = MutableStateFlow(HubViewMode.STREAM)
    val viewMode: StateFlow<HubViewMode> = _viewMode.asStateFlow()

    private val _vipOnlyFilter = MutableStateFlow(false)
    val vipOnlyFilter: StateFlow<Boolean> = _vipOnlyFilter.asStateFlow()

    private val _omniboxQuery = MutableStateFlow("")
    val omniboxQuery: StateFlow<String> = _omniboxQuery.asStateFlow()

    private val _isOmniboxComposerOpen = MutableStateFlow(false)
    val isOmniboxComposerOpen: StateFlow<Boolean> = _isOmniboxComposerOpen.asStateFlow()

    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    /**
     * Airframe Theme mode: `false` = Ivory (`#F7F6F2` default), `true` = Night (`#111412`).
     */
    private val _isNightTheme = MutableStateFlow(false)
    val isNightTheme: StateFlow<Boolean> = _isNightTheme.asStateFlow()

    private val _openedConversationId = MutableStateFlow<String?>(null)
    val openedConversationId: StateFlow<String?> = _openedConversationId.asStateFlow()

    private val _pendingAttachment = MutableStateFlow<HubAttachment?>(null)
    val pendingAttachment: StateFlow<HubAttachment?> = _pendingAttachment.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    private val _selectedAppCategory = MutableStateFlow("All")
    val selectedAppCategory: StateFlow<String> = _selectedAppCategory.asStateFlow()

    /**
     * Hosted native Android AppWidget IDs bound by the user on the Widgets page.
     */
    private val _hostedAppWidgetIds = MutableStateFlow<List<Int>>(emptyList())
    val hostedAppWidgetIds: StateFlow<List<Int>> = _hostedAppWidgetIds.asStateFlow()

    /**
     * Quick scratchpad note persisted in the Widgets panel.
     */
    private val _quickNoteText = MutableStateFlow("• Review Q4 Board Deck (Slide 14)\n• Call Marcus after SFO arrival\n• Dinner at 7:30 PM in SoMa")
    val quickNoteText: StateFlow<String> = _quickNoteText.asStateFlow()

    val openedConversation: StateFlow<HubMessage?> = combine(
        allMessages,
        _openedConversationId
    ) { messages, openId ->
        if (openId == null) null else messages.firstOrNull { it.id == openId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val visibleMessages: StateFlow<List<HubMessage>> = combine(
        allMessages,
        _selectedPlatform,
        _omniboxQuery,
        _vipOnlyFilter
    ) { messages, platform, query, vipOnly ->
        val cleanQuery = query.trim()
        val isSlashCommand = cleanQuery.startsWith("/")

        messages.filter { msg ->
            val matchesPlatform = platform == MessagingPlatform.ALL || msg.platform == platform
            val matchesVipFocus = !vipOnly || msg.isVip
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
        if (_viewMode.value == HubViewMode.APPS || _viewMode.value == HubViewMode.WIDGETS) {
            _viewMode.value = HubViewMode.STREAM
        }
    }

    fun setViewMode(mode: HubViewMode) {
        _viewMode.value = mode
    }

    fun toggleVipOnlyFilter() {
        _vipOnlyFilter.value = !_vipOnlyFilter.value
    }

    fun selectAppCategory(category: String) {
        _selectedAppCategory.value = category
    }

    fun updateQuickNote(text: String) {
        _quickNoteText.value = text
    }

    fun addHostedWidgetId(appWidgetId: Int) {
        if (appWidgetId != -1) {
            _hostedAppWidgetIds.update { current ->
                if (current.contains(appWidgetId)) current else current + appWidgetId
            }
        }
    }

    fun removeHostedWidgetId(appWidgetId: Int) {
        _hostedAppWidgetIds.update { current -> current.filterNot { it == appWidgetId } }
    }

    fun updateOmniboxQuery(query: String) {
        _omniboxQuery.value = query
        if (query.startsWith("/msg", ignoreCase = true)) {
            _isOmniboxComposerOpen.value = true
        } else if (query.equals("/reset", ignoreCase = true)) {
            _omniboxQuery.value = ""
            HubRepository.resetDemoStream()
        }
    }

    fun toggleOmniboxComposer(forceOpen: Boolean? = null) {
        val next = forceOpen ?: !_isOmniboxComposerOpen.value
        _isOmniboxComposerOpen.value = next
    }

    fun toggleSearch(forceOpen: Boolean? = null) {
        val next = forceOpen ?: !_isSearchOpen.value
        _isSearchOpen.value = next
        if (!next) {
            _omniboxQuery.value = ""
        }
    }

    fun toggleNightTheme() {
        _isNightTheme.value = !_isNightTheme.value
    }

    fun clearOmnibox() {
        _omniboxQuery.value = ""
        _isOmniboxComposerOpen.value = false
        _isSearchOpen.value = false
    }

    fun openConversation(messageId: String) {
        HubRepository.markMessageRead(messageId)
        _openedConversationId.value = messageId
    }

    fun closeConversation() {
        _openedConversationId.value = null
        _pendingAttachment.value = null
    }

    fun onAttachmentPicked(context: Context, uri: Uri?) {
        if (uri == null) return
        val contentResolver = context.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        var displayName = uri.lastPathSegment ?: "Attached_File"
        var sizeBytes = 0L

        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                    if (sizeIdx >= 0) sizeBytes = cursor.getLong(sizeIdx)
                }
            }
        } catch (_: Exception) {
        }

        val sizeLabel = if (sizeBytes > 1024 * 1024) {
            String.format("%.1f MB", sizeBytes / (1024f * 1024f))
        } else if (sizeBytes > 1024) {
            "${sizeBytes / 1024} KB"
        } else {
            "File Attachment"
        }

        val type = when {
            mimeType.startsWith("image/") -> AttachmentType.IMAGE
            mimeType.contains("pdf") || mimeType.contains("word") -> AttachmentType.DOCUMENT
            mimeType.contains("sheet") || mimeType.contains("excel") || mimeType.contains("csv") -> AttachmentType.SPREADSHEET
            mimeType.contains("presentation") || mimeType.contains("powerpoint") -> AttachmentType.PRESENTATION
            mimeType.startsWith("audio/") -> AttachmentType.AUDIO
            else -> AttachmentType.DOCUMENT
        }

        _pendingAttachment.value = HubAttachment(
            id = "picked_${System.currentTimeMillis()}",
            fileName = displayName,
            fileSizeLabel = "$sizeLabel · ${type.name.lowercase().replaceFirstChar { it.uppercase() }}",
            mimeType = mimeType,
            type = type,
            uri = uri
        )
    }

    fun clearPendingAttachment() {
        _pendingAttachment.value = null
    }

    fun sendReply(context: Context, messageId: String, replyText: String) {
        val attachment = _pendingAttachment.value
        if (HubRepository.sendReply(context, messageId, replyText, attachment)) {
            _pendingAttachment.value = null
        }
    }

    fun openOriginalApp(context: Context, message: HubMessage) {
        HubRepository.openOriginalAppConversation(context, message)
    }

    fun archiveMessage(messageId: String) {
        HubRepository.archiveMessage(messageId) { notifKey ->
            HubNotificationListenerService.instance?.dismissSystemNotification(notifKey)
        }
        if (_openedConversationId.value == messageId) {
            _openedConversationId.value = null
        }
    }

    fun snoozeMessage(messageId: String) {
        HubRepository.snoozeMessage(messageId)
        if (_openedConversationId.value == messageId) {
            _openedConversationId.value = null
        }
    }

    fun toggleVipForMessage(message: HubMessage) {
        HubRepository.toggleVipForSender(message.senderName)
    }

    fun sendOmniboxOutbound(context: Context, recipient: String, platform: MessagingPlatform, body: String) {
        val attachment = _pendingAttachment.value
        if (body.isBlank() && attachment == null) return
        HubRepository.dispatchOmniboxOutboundMessage(context, recipient, platform, body, attachment)
        _pendingAttachment.value = null
        _isOmniboxComposerOpen.value = false
        _omniboxQuery.value = ""
    }

    fun resetDemoStream() {
        HubRepository.resetDemoStream()
    }

    /**
     * Loads all launchable Android apps along with their real icons (`loadIcon(pm)`) and
     * marks essential smartphone apps as favorites for the Pinned Favorites Dock.
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
                        val iconBitmap = try {
                            drawableToImageBitmap(info.loadIcon(pm))
                        } catch (_: Exception) {
                            null
                        }
                        val category = categorizePackage(pkg, label)
                        val isFav = isDefaultFavoritePackage(pkg, label)
                        InstalledAppInfo(
                            label = label,
                            packageName = pkg,
                            categoryTag = category,
                            iconBitmap = iconBitmap,
                            isFavorite = isFav
                        )
                    }
                    .distinctBy { it.packageName }
                    .sortedBy { it.label.lowercase() }

                _installedApps.value = apps
            } catch (_: Exception) {
            }
        }
    }

    fun toggleFavoriteApp(packageName: String) {
        _installedApps.update { list ->
            list.map { app ->
                if (app.packageName == packageName) app.copy(isFavorite = !app.isFavorite) else app
            }
        }
    }

    fun launchApp(context: Context, packageName: String, label: String = "") {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (_: Exception) {
        }
    }

    fun launchSystemAction(context: Context, action: String) {
        try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openAppInfoSettings(context: Context, packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
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

    private fun drawableToImageBitmap(drawable: Drawable): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap.asImageBitmap()
        }
        val width = drawable.intrinsicWidth.coerceAtLeast(96).coerceAtMost(192)
        val height = drawable.intrinsicHeight.coerceAtLeast(96).coerceAtMost(192)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }

    private fun isDefaultFavoritePackage(pkg: String, label: String): Boolean {
        val lowerPkg = pkg.lowercase()
        val lowerLabel = label.lowercase()
        return lowerPkg.contains("dialer") ||
            lowerPkg.contains("camera") ||
            lowerPkg.contains("chrome") ||
            lowerPkg.contains("sbrowser") ||
            lowerPkg.contains("whatsapp") ||
            lowerPkg.contains("teams") ||
            lowerPkg.contains("outlook") ||
            lowerPkg.contains("gm") ||
            lowerPkg.contains("messaging") ||
            lowerPkg.contains("gallery") ||
            lowerPkg.contains("photos") ||
            lowerLabel == "phone" ||
            lowerLabel == "camera" ||
            lowerLabel == "messages"
    }

    private fun categorizePackage(pkg: String, label: String): String {
        val p = pkg.lowercase()
        val l = label.lowercase()
        return when {
            MessagingPlatform.fromPackageName(pkg) != null ||
                p.contains("Slack", ignoreCase = true) ||
                p.contains("discord") ||
                p.contains("telegram") ||
                p.contains("dialer") ||
                l.contains("phone") ||
                l.contains("mail") -> "Communication"
            p.contains("calendar") || p.contains("notes") || p.contains("keep") ||
                p.contains("docs") || p.contains("office") || p.contains("notion") ||
                p.contains("obsidian") -> "Productivity"
            p.contains("youtube") || p.contains("spotify") || p.contains("netflix") ||
                p.contains("music") || p.contains("gallery") || p.contains("photos") ||
                p.contains("camera") || p.contains("video") -> "Media"
            p.contains("maps") || p.contains("chrome") || p.contains("browser") ||
                p.contains("settings") || p.contains("clock") || p.contains("calculator") ||
                p.contains("files") || p.contains("wallet") -> "Tools"
            else -> "Apps"
        }
    }
}
