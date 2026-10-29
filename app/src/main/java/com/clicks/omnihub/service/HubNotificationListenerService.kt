package com.clicks.omnihub.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.clicks.omnihub.data.HubRepository
import com.clicks.omnihub.model.DirectReplyAction
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.MessagingPlatform

/**
 * Live Android Notification Bridge that captures real incoming messages from
 * WhatsApp, Microsoft Teams, Outlook, Gmail, SMS/Messages, and Facebook Messenger,
 * extracts any inline direct-reply [android.app.RemoteInput] actions, and feeds
 * them into the Universal Message Hub stream.
 */
class HubNotificationListenerService : NotificationListenerService() {

    companion object {
        @Volatile
        var instance: HubNotificationListenerService? = null
            private set
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        HubRepository.setListenerConnected(true)
        syncActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        instance = null
        HubRepository.setListenerConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null || sbn.isOngoing) return
        parseStatusBarNotification(sbn)?.let { hubMsg ->
            HubRepository.upsertLiveNotification(hubMsg)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.key?.let { key ->
            HubRepository.removeByNotificationKey(key)
        }
    }

    fun dismissSystemNotification(key: String) {
        try {
            cancelNotification(key)
        } catch (_: Exception) {
        }
    }

    private fun syncActiveNotifications() {
        try {
            val active = activeNotifications ?: return
            for (sbn in active) {
                if (!sbn.isOngoing) {
                    parseStatusBarNotification(sbn)?.let { msg ->
                        HubRepository.upsertLiveNotification(msg)
                    }
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun parseStatusBarNotification(sbn: StatusBarNotification): HubMessage? {
        val pkg = sbn.packageName ?: return null
        if (pkg == packageName) return null // Ignore our own notifications

        val notification = sbn.notification ?: return null
        val extras = notification.extras ?: return null

        // Skip group summary notifications so we don't duplicate individual message items
        if ((notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0) {
            return null
        }

        val rawTitle = extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: return null

        val rawText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: return null

        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()

        // Match known platform or extract remote-input capable messaging app
        val directReply = extractDirectReplyAction(notification)
        val matchedPlatform = MessagingPlatform.fromPackageName(pkg)
            ?: if (directReply != null) MessagingPlatform.SMS else return null

        val initials = rawTitle
            .trim()
            .split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { matchedPlatform.shortCode }

        val smartReplies = notification.contextualActions
            ?.mapNotNull { it.title?.toString() }
            ?.take(3)
            ?.takeIf { it.isNotEmpty() }
            ?: listOf("Sounds good!", "On it 👍", "Give me 5 mins")

        return HubMessage(
            id = "live_${sbn.key.hashCode()}",
            notificationKey = sbn.key,
            senderName = rawTitle,
            senderRoleOrHandle = subText ?: matchedPlatform.displayName,
            senderInitials = initials,
            avatarColor = matchedPlatform.accentColor,
            platform = matchedPlatform,
            subject = if (matchedPlatform == MessagingPlatform.OUTLOOK || matchedPlatform == MessagingPlatform.GMAIL) subText else null,
            body = rawText,
            timestampMillis = sbn.postTime,
            relativeTimeLabel = formatRelativeTime(sbn.postTime),
            isVip = true,
            isUnread = true,
            isDemo = false,
            quickReplies = smartReplies,
            directReplyAction = directReply,
            contentIntent = notification.contentIntent
        )
    }

    private fun extractDirectReplyAction(notification: Notification): DirectReplyAction? {
        val actions = notification.actions ?: return null
        for (action in actions) {
            val remoteInputs = action.remoteInputs ?: continue
            val pendingIntent = action.actionIntent ?: continue
            for (remoteInput in remoteInputs) {
                if (remoteInput.allowFreeFormInput) {
                    return DirectReplyAction(
                        actionIntent = pendingIntent,
                        remoteInput = remoteInput,
                        extras = action.extras
                    )
                }
            }
        }
        return null
    }

    private fun formatRelativeTime(postTimeMillis: Long): String {
        val diffMinutes = ((System.currentTimeMillis() - postTimeMillis) / 60_000L).coerceAtLeast(0L)
        return when {
            diffMinutes < 1L -> "Just now"
            diffMinutes < 60L -> "${diffMinutes}m ago"
            else -> "${diffMinutes / 60L}h ago"
        }
    }
}
