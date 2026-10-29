package com.clicks.omnihub.model

import android.app.PendingIntent
import android.app.RemoteInput
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Supported messaging & communication channels in the Universal Message Hub.
 */
enum class MessagingPlatform(
    val id: String,
    val displayName: String,
    val shortCode: String,
    val accentColor: Color,
    val containerColor: Color,
    val packageNames: List<String>
) {
    ALL(
        id = "all",
        displayName = "All",
        shortCode = "ALL",
        accentColor = Color(0xFF8AB4F8),
        containerColor = Color(0xFF1E293B),
        packageNames = emptyList()
    ),
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        shortCode = "WA",
        accentColor = Color(0xFF34D399),
        containerColor = Color(0xFF064E3B),
        packageNames = listOf("com.whatsapp", "com.whatsapp.w4b")
    ),
    TEAMS(
        id = "teams",
        displayName = "Teams",
        shortCode = "TM",
        accentColor = Color(0xFFA5B4FC),
        containerColor = Color(0xFF312E81),
        packageNames = listOf("com.microsoft.teams")
    ),
    OUTLOOK(
        id = "outlook",
        displayName = "Outlook",
        shortCode = "OL",
        accentColor = Color(0xFF38BDF8),
        containerColor = Color(0xFF0C4A6E),
        packageNames = listOf("com.microsoft.office.outlook")
    ),
    GMAIL(
        id = "gmail",
        displayName = "Email",
        shortCode = "EM",
        accentColor = Color(0xFFFDA4AF),
        containerColor = Color(0xFF881337),
        packageNames = listOf("com.google.android.gm", "ch.protonmail.android")
    ),
    SMS(
        id = "sms",
        displayName = "Messages",
        shortCode = "SMS",
        accentColor = Color(0xFFFBBF24),
        containerColor = Color(0xFF78350F),
        packageNames = listOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms"
        )
    ),
    MESSENGER(
        id = "messenger",
        displayName = "Messenger",
        shortCode = "FB",
        accentColor = Color(0xFF60A5FA),
        containerColor = Color(0xFF1E3A8A),
        packageNames = listOf("com.facebook.orca")
    );

    companion object {
        fun fromPackageName(packageName: String): MessagingPlatform? {
            return entries.firstOrNull { platform ->
                platform.packageNames.any { pkg -> packageName.startsWith(pkg, ignoreCase = true) }
            }
        }
    }
}

enum class AttachmentType {
    IMAGE,
    DOCUMENT,
    SPREADSHEET,
    PRESENTATION,
    AUDIO,
    LINK
}

data class HubAttachment(
    val id: String,
    val fileName: String,
    val fileSizeLabel: String,
    val mimeType: String,
    val type: AttachmentType,
    val uri: Uri? = null,
    val previewBitmap: Bitmap? = null
)

data class ConversationTurn(
    val id: String,
    val senderName: String,
    val isFromMe: Boolean,
    val text: String,
    val timestampLabel: String,
    val platform: MessagingPlatform,
    val attachments: List<HubAttachment> = emptyList()
)

data class DirectReplyAction(
    val actionIntent: PendingIntent,
    val remoteInput: RemoteInput,
    val extras: Bundle = Bundle()
)

data class HubMessage(
    val id: String,
    val notificationKey: String? = null,
    val senderName: String,
    val senderRoleOrHandle: String,
    val senderInitials: String,
    val avatarColor: Color,
    val platform: MessagingPlatform,
    val subject: String? = null,
    val body: String,
    val timestampMillis: Long,
    val relativeTimeLabel: String,
    val isVip: Boolean = false,
    val isUnread: Boolean = true,
    val isDemo: Boolean = false,
    val quickReplies: List<String> = listOf("Sounds good!", "On it right now", "Give me 5 mins"),
    val directReplyAction: DirectReplyAction? = null,
    val contentIntent: PendingIntent? = null,
    val attachments: List<HubAttachment> = emptyList(),
    val conversationHistory: List<ConversationTurn> = emptyList()
)

data class PersonUnifiedThread(
    val senderName: String,
    val senderInitials: String,
    val avatarColor: Color,
    val isVip: Boolean,
    val messages: List<HubMessage>,
    val activePlatforms: List<MessagingPlatform>,
    val latestTimestampMillis: Long
)

/**
 * Represents an installed Android application with its real icon for the App Drawer and Favorites Dock.
 */
data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val categoryTag: String = "App",
    val iconBitmap: ImageBitmap? = null,
    val isFavorite: Boolean = false
)

/**
 * Primary navigation destinations of the Material 3 OmniHub Launcher:
 * - STREAM: Communication-first Unified Inbox
 * - PEOPLE: Person-Centric Threads across apps
 * - WIDGETS: Dedicated Bottom Panel / Page for Built-in & Native Android System Widgets
 * - APPS: Full Smartphone App Drawer with Real Icons, Categories & Pinned Favorites
 */
enum class HubViewMode(val label: String) {
    STREAM("Inbox"),
    PEOPLE("People"),
    WIDGETS("Widgets"),
    APPS("Apps")
}
