package com.clicks.omnihub.model

import android.app.PendingIntent
import android.app.RemoteInput
import android.os.Bundle
import androidx.compose.ui.graphics.Color

/**
 * Supported messaging & communication channels in the Universal Message Hub.
 */
enum class MessagingPlatform(
    val id: String,
    val displayName: String,
    val shortCode: String,
    val accentColor: Color,
    val badgeBgColor: Color,
    val packageNames: List<String>
) {
    ALL(
        id = "all",
        displayName = "All Hub",
        shortCode = "ALL",
        accentColor = Color(0xFFF97316), // Clicks Orange
        badgeBgColor = Color(0x33F97316),
        packageNames = emptyList()
    ),
    WHATSAPP(
        id = "whatsapp",
        displayName = "WhatsApp",
        shortCode = "WA",
        accentColor = Color(0xFF10B981), // Emerald
        badgeBgColor = Color(0x2610B981),
        packageNames = listOf("com.whatsapp", "com.whatsapp.w4b")
    ),
    TEAMS(
        id = "teams",
        displayName = "MS Teams",
        shortCode = "TM",
        accentColor = Color(0xFF818CF8), // Indigo
        badgeBgColor = Color(0x26818CF8),
        packageNames = listOf("com.microsoft.teams")
    ),
    OUTLOOK(
        id = "outlook",
        displayName = "Outlook",
        shortCode = "OL",
        accentColor = Color(0xFF38BDF8), // Sky Blue
        badgeBgColor = Color(0x2638BDF8),
        packageNames = listOf("com.microsoft.office.outlook")
    ),
    GMAIL(
        id = "gmail",
        displayName = "Email",
        shortCode = "EM",
        accentColor = Color(0xFFF43F5E), // Rose
        badgeBgColor = Color(0x26F43F5E),
        packageNames = listOf("com.google.android.gm", "ch.protonmail.android")
    ),
    SMS(
        id = "sms",
        displayName = "Messages",
        shortCode = "SMS",
        accentColor = Color(0xFFF59E0B), // Amber
        badgeBgColor = Color(0x26F59E0B),
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
        accentColor = Color(0xFF3B82F6), // Blue
        badgeBgColor = Color(0x263B82F6),
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

/**
 * Encapsulates an Android Notification's direct-reply RemoteInput action so the user
 * can respond inline directly from the Home Launcher stream without opening the target app.
 */
data class DirectReplyAction(
    val actionIntent: PendingIntent,
    val remoteInput: RemoteInput,
    val extras: Bundle = Bundle()
)

/**
 * Represents a single communication item inside the Universal Message Hub stream.
 */
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
    val repliedHistory: List<String> = emptyList()
)

/**
 * Groups multiple HubMessages from the same person across different messaging platforms
 * (e.g. Sarah Jenkins on Teams + WhatsApp) into a unified Person-Centric Hub card.
 */
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
 * Represents an installed Android application for the Omnibox search & minimal speed-dial drawer.
 */
data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val categoryTag: String = "App"
)

/**
 * Primary view modes of the OmniHub Communicator Launcher.
 */
enum class HubViewMode(val label: String, val shortcutKey: String) {
    STREAM("Hub Stream", "H"),
    PEOPLE("People Threads", "P"),
    VIP_FOCUS("VIP Shield", "F"),
    APPS("Minimal Apps", "L")
}
