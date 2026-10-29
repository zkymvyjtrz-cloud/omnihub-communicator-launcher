package com.clicks.omnihub.data

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.MessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Central state repository for the Universal Message Hub.
 * Bridges real-time notifications from [com.clicks.omnihub.service.HubNotificationListenerService]
 * with interactive inline replies, snooze/archive triage, and realistic multi-platform demo threads.
 */
object HubRepository {

    private val _messages = MutableStateFlow<List<HubMessage>>(createInitialSeedMessages())
    val messages: StateFlow<List<HubMessage>> = _messages.asStateFlow()

    private val _isNotificationListenerConnected = MutableStateFlow(false)
    val isNotificationListenerConnected: StateFlow<Boolean> = _isNotificationListenerConnected.asStateFlow()

    private val _statusBanner = MutableStateFlow("OmniHub Ready · Press R to Reply, A to Archive, / for Omnibox")
    val statusBanner: StateFlow<String> = _statusBanner.asStateFlow()

    fun setListenerConnected(connected: Boolean) {
        _isNotificationListenerConnected.value = connected
        if (connected) {
            postStatus("Live Notification Bridge Active · Listening for WhatsApp, Teams, Outlook, SMS")
        }
    }

    fun postStatus(statusText: String) {
        _statusBanner.value = statusText
    }

    /**
     * Upserts a live notification from Android's NotificationListenerService into the Hub stream.
     */
    fun upsertLiveNotification(message: HubMessage) {
        _messages.update { current ->
            val filtered = current.filterNot {
                (it.notificationKey != null && it.notificationKey == message.notificationKey) || it.id == message.id
            }
            (listOf(message) + filtered).sortedByDescending { it.timestampMillis }
        }
        postStatus("New [${message.platform.displayName}] from ${message.senderName}")
    }

    /**
     * Removes a notification when dismissed in the system shade or archived in the Hub.
     */
    fun removeByNotificationKey(notificationKey: String) {
        _messages.update { current ->
            current.filterNot { it.notificationKey == notificationKey }
        }
    }

    /**
     * Archives / marks a message done inside the Hub stream.
     */
    fun archiveMessage(messageId: String, onDismissLiveNotification: ((String) -> Unit)? = null) {
        val target = _messages.value.firstOrNull { it.id == messageId } ?: return
        target.notificationKey?.let { key ->
            onDismissLiveNotification?.invoke(key)
        }
        _messages.update { current -> current.filterNot { it.id == messageId } }
        postStatus("Archived [${target.platform.displayName}] thread from ${target.senderName}")
    }

    /**
     * Snoozes a message (removes from active immediate stream and logs snooze state).
     */
    fun snoozeMessage(messageId: String) {
        val target = _messages.value.firstOrNull { it.id == messageId } ?: return
        _messages.update { current -> current.filterNot { it.id == messageId } }
        postStatus("Snoozed ${target.senderName} (${target.platform.displayName}) for 1 hour")
    }

    /**
     * Toggles VIP Priority status for a contact across all their messages.
     */
    fun toggleVipForSender(senderName: String) {
        var newVipState = false
        _messages.update { current ->
            current.map { msg ->
                if (msg.senderName.equals(senderName, ignoreCase = true)) {
                    newVipState = !msg.isVip
                    msg.copy(isVip = newVipState)
                } else {
                    msg
                }
            }
        }
        val status = if (newVipState) "Added $senderName to VIP Priority Shield" else "Removed $senderName from VIP"
        postStatus(status)
    }

    /**
     * Sends an inline direct reply.
     * - If the message came from a live Android notification with a [RemoteInput] action,
     *   it dispatches the real PendingIntent with the user's reply text so WhatsApp/Teams/Outlook/SMS
     *   sends the reply immediately in the background.
     * - Updates the Hub state and records the reply.
     */
    fun sendInlineReply(context: Context, messageId: String, replyText: String): Boolean {
        val cleanReply = replyText.trim()
        if (cleanReply.isEmpty()) return false

        val target = _messages.value.firstOrNull { it.id == messageId } ?: return false
        var sentViaSystemRemoteInput = false

        target.directReplyAction?.let { action ->
            try {
                val fillInIntent = Intent().addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                val resultsBundle = Bundle().apply {
                    putCharSequence(action.remoteInput.resultKey, cleanReply)
                }
                RemoteInput.addResultsToIntent(arrayOf(action.remoteInput), fillInIntent, resultsBundle)
                action.actionIntent.send(context, 0, fillInIntent)
                sentViaSystemRemoteInput = true
            } catch (e: Exception) {
                sentViaSystemRemoteInput = false
            }
        }

        // Append reply history and mark as read/handled
        _messages.update { current ->
            current.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(
                        isUnread = false,
                        repliedHistory = msg.repliedHistory + cleanReply
                    )
                } else {
                    msg
                }
            }
        }

        val modeTag = if (sentViaSystemRemoteInput) "Live RemoteInput" else "Inline Hub"
        postStatus("Sent via ${target.platform.displayName} ($modeTag) to ${target.senderName}: \"$cleanReply\"")
        return true
    }

    /**
     * Composes a brand-new outbound message from the Omnibox Command Bar and logs it to the stream.
     */
    fun dispatchOmniboxOutboundMessage(
        recipientName: String,
        platform: MessagingPlatform,
        messageBody: String
    ) {
        val cleanRecipient = recipientName.trim().removePrefix("@").ifBlank { "Contact" }
        val initials = cleanRecipient.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { "CT" }

        val newEntry = HubMessage(
            id = "omni_${System.currentTimeMillis()}",
            senderName = cleanRecipient,
            senderRoleOrHandle = "Outbound via Omnibox · ${platform.displayName}",
            senderInitials = initials,
            avatarColor = platform.accentColor,
            platform = platform,
            subject = null,
            body = "You: $messageBody",
            timestampMillis = System.currentTimeMillis(),
            relativeTimeLabel = "Just now",
            isVip = true,
            isUnread = false,
            isDemo = true,
            repliedHistory = listOf(messageBody)
        )

        _messages.update { current -> listOf(newEntry) + current }
        postStatus("Omnibox dispatched message to $cleanRecipient on ${platform.displayName}")
    }

    /**
     * Restores the rich multi-platform demo threads so the user can test triage, inline replies,
     * and person-centric grouping at any time.
     */
    fun resetDemoStream() {
        _messages.update { current ->
            val liveOnly = current.filterNot { it.isDemo }
            (liveOnly + createInitialSeedMessages()).sortedByDescending { it.timestampMillis }
        }
        postStatus("Restored all multi-platform Hub demo threads")
    }

    private fun createInitialSeedMessages(): List<HubMessage> {
        val now = System.currentTimeMillis()
        return listOf(
            HubMessage(
                id = "demo_teams_sarah",
                senderName = "Sarah Jenkins",
                senderRoleOrHandle = "VP Product · Acme Corp",
                senderInitials = "SJ",
                avatarColor = Color(0xFF4F46E5),
                platform = MessagingPlatform.TEAMS,
                subject = "Q4 Board Deck Sign-off",
                body = "Can you approve slide 14 before the 10:30 AM call? Finance just updated the forecast numbers.",
                timestampMillis = now - 90_000L,
                relativeTimeLabel = "1m ago",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Approved, looks great!", "Give me 5 mins to review", "Let's hop on a quick call")
            ),
            HubMessage(
                id = "demo_wa_sarah",
                senderName = "Sarah Jenkins",
                senderRoleOrHandle = "Mobile · +1 (415) 890-2311",
                senderInitials = "SJ",
                avatarColor = Color(0xFF4F46E5),
                platform = MessagingPlatform.WHATSAPP,
                subject = null,
                body = "Sent you the deck link on Teams — ping me here on WhatsApp if you're in transit!",
                timestampMillis = now - 60_000L,
                relativeTimeLabel = "Just now",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("On it right now 👍", "In a cab, reviewing now", "Call you in 2m")
            ),
            HubMessage(
                id = "demo_outlook_marcus",
                senderName = "Marcus Vance",
                senderRoleOrHandle = "marcus.vance@architecture.io",
                senderInitials = "MV",
                avatarColor = Color(0xFF0284C7),
                platform = MessagingPlatform.OUTLOOK,
                subject = "Re: Edge Gateway Latency Cut by 42%",
                body = "We moved token validation to the edge cluster and p99 latency dropped to 18ms. Ready for rollout.",
                timestampMillis = now - 480_000L,
                relativeTimeLabel = "8m ago",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Awesome numbers, ship it!", "Sharing with engineering", "Thanks Marcus!")
            ),
            HubMessage(
                id = "demo_wa_elena",
                senderName = "Elena Rostova",
                senderRoleOrHandle = "Design Lead · Studio",
                senderInitials = "ER",
                avatarColor = Color(0xFF059669),
                platform = MessagingPlatform.WHATSAPP,
                subject = null,
                body = "Here are the updated high-contrast OLED icons for the Clicks tactile bar. Thoughts?",
                timestampMillis = now - 840_000L,
                relativeTimeLabel = "14m ago",
                isVip = false,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Love the high contrast!", "Let's test on hardware", "Looks super clean")
            ),
            HubMessage(
                id = "demo_sms_marcus",
                senderName = "Marcus Vance",
                senderRoleOrHandle = "SMS · +1 (650) 442-9810",
                senderInitials = "MV",
                avatarColor = Color(0xFF0284C7),
                platform = MessagingPlatform.SMS,
                subject = null,
                body = "Landed at SFO Gate B12. Grabbing espresso before heading to HQ.",
                timestampMillis = now - 1_320_000L,
                relativeTimeLabel = "22m ago",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("See you at HQ!", "Grab me an espresso ☕", "Safe travels!")
            ),
            HubMessage(
                id = "demo_fb_david",
                senderName = "David Kim",
                senderRoleOrHandle = "Facebook Messenger",
                senderInitials = "DK",
                avatarColor = Color(0xFF2563EB),
                platform = MessagingPlatform.MESSENGER,
                subject = null,
                body = "Are we still on for dinner at 7:30 PM in SoMa tonight?",
                timestampMillis = now - 2_400_000L,
                relativeTimeLabel = "40m ago",
                isVip = false,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Yes! See you at 7:30", "Running 15 mins late", "Can we do 8:00 PM?")
            ),
            HubMessage(
                id = "demo_gmail_alex",
                senderName = "Alex Rivera",
                senderRoleOrHandle = "alex@venturefoundry.vc",
                senderInitials = "AR",
                avatarColor = Color(0xFFE11D48),
                platform = MessagingPlatform.GMAIL,
                subject = "Term Sheet & Partner Sync",
                body = "Attached the redlined partner memo for tomorrow morning's sync. Let me know if the timeline works.",
                timestampMillis = now - 3_600_000L,
                relativeTimeLabel = "1h ago",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Timeline works for me", "Reviewing the memo now", "Confirmed for tomorrow")
            )
        )
    }
}
