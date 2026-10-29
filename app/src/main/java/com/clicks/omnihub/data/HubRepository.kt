package com.clicks.omnihub.data

import android.app.RemoteInput
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import com.clicks.omnihub.model.AttachmentType
import com.clicks.omnihub.model.ConversationTurn
import com.clicks.omnihub.model.HubAttachment
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.MessagingPlatform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Central state repository for the Universal Message Hub.
 * Manages live notifications, full conversation context threads, inline & full-screen replies,
 * and incoming/outgoing attachments.
 */
object HubRepository {

    private val _messages = MutableStateFlow<List<HubMessage>>(createInitialSeedMessages())
    val messages: StateFlow<List<HubMessage>> = _messages.asStateFlow()

    private val _isNotificationListenerConnected = MutableStateFlow(false)
    val isNotificationListenerConnected: StateFlow<Boolean> = _isNotificationListenerConnected.asStateFlow()

    private val _statusBanner = MutableStateFlow("Universal Hub active")
    val statusBanner: StateFlow<String> = _statusBanner.asStateFlow()

    fun setListenerConnected(connected: Boolean) {
        _isNotificationListenerConnected.value = connected
    }

    fun postStatus(statusText: String) {
        _statusBanner.value = statusText
    }

    /**
     * Upserts a live notification from Android's NotificationListenerService into the Hub stream,
     * preserving existing conversation history if the thread already exists.
     */
    fun upsertLiveNotification(message: HubMessage) {
        _messages.update { current ->
            val existing = current.firstOrNull {
                (it.notificationKey != null && it.notificationKey == message.notificationKey) || it.id == message.id
            }
            val mergedHistory = if (existing != null && message.conversationHistory.size <= 1) {
                (existing.conversationHistory + message.conversationHistory).distinctBy { "${it.senderName}_${it.text}" }
            } else {
                message.conversationHistory
            }
            val updatedMsg = message.copy(conversationHistory = mergedHistory)
            val filtered = current.filterNot {
                (it.notificationKey != null && it.notificationKey == message.notificationKey) || it.id == message.id
            }
            (listOf(updatedMsg) + filtered).sortedByDescending { it.timestampMillis }
        }
    }

    fun removeByNotificationKey(notificationKey: String) {
        _messages.update { current ->
            current.filterNot { it.notificationKey == notificationKey }
        }
    }

    fun markMessageRead(messageId: String) {
        _messages.update { current ->
            current.map { msg ->
                if (msg.id == messageId) msg.copy(isUnread = false) else msg
            }
        }
    }

    fun archiveMessage(messageId: String, onDismissLiveNotification: ((String) -> Unit)? = null) {
        val target = _messages.value.firstOrNull { it.id == messageId } ?: return
        target.notificationKey?.let { key ->
            onDismissLiveNotification?.invoke(key)
        }
        _messages.update { current -> current.filterNot { it.id == messageId } }
        postStatus("Archived conversation with ${target.senderName}")
    }

    fun snoozeMessage(messageId: String) {
        val target = _messages.value.firstOrNull { it.id == messageId } ?: return
        _messages.update { current -> current.filterNot { it.id == messageId } }
        postStatus("Snoozed ${target.senderName} for 1 hour")
    }

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
    }

    /**
     * Sends a reply (with optional attachment) using either Android's background RemoteInput
     * or an attachment share intent, and appends the turn to the full conversation thread.
     */
    fun sendReply(
        context: Context,
        messageId: String,
        replyText: String,
        attachment: HubAttachment? = null
    ): Boolean {
        val cleanReply = replyText.trim()
        if (cleanReply.isEmpty() && attachment == null) return false

        val target = _messages.value.firstOrNull { it.id == messageId } ?: return false
        val nowTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())

        // 1. If the user attached a file/photo with a real URI, hand off to the target app or share sheet
        if (attachment?.uri != null) {
            shareAttachmentToPlatform(context, target.platform, cleanReply, attachment.uri, attachment.mimeType)
        } else {
            // 2. Otherwise dispatch text inline via Android Notification RemoteInput
            target.directReplyAction?.let { action ->
                try {
                    val fillInIntent = Intent().addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                    val resultsBundle = Bundle().apply {
                        putCharSequence(action.remoteInput.resultKey, cleanReply)
                    }
                    RemoteInput.addResultsToIntent(arrayOf(action.remoteInput), fillInIntent, resultsBundle)
                    action.actionIntent.send(context, 0, fillInIntent)
                } catch (_: Exception) {
                }
            }
        }

        val newTurn = ConversationTurn(
            id = "turn_${System.currentTimeMillis()}",
            senderName = "You",
            isFromMe = true,
            text = if (cleanReply.isNotEmpty()) cleanReply else "Sent attachment: ${attachment?.fileName}",
            timestampLabel = nowTime,
            platform = target.platform,
            attachments = listOfNotNull(attachment)
        )

        _messages.update { current ->
            current.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(
                        isUnread = false,
                        body = if (cleanReply.isNotEmpty()) "You: $cleanReply" else "You attached ${attachment?.fileName}",
                        relativeTimeLabel = "Just now",
                        timestampMillis = System.currentTimeMillis(),
                        attachments = if (attachment != null) msg.attachments + attachment else msg.attachments,
                        conversationHistory = msg.conversationHistory + newTurn
                    )
                } else {
                    msg
                }
            }
        }

        return true
    }

    /**
     * Opens the underlying native app (WhatsApp, Teams, Outlook, Gmail, SMS) directly to the conversation
     * via the notification's contentIntent, or launches the app package if it's a demo thread.
     */
    fun openOriginalAppConversation(context: Context, message: HubMessage) {
        try {
            if (message.contentIntent != null) {
                message.contentIntent.send()
                return
            }
            val pkg = message.platform.packageNames.firstOrNull { candidate ->
                context.packageManager.getLaunchIntentForPackage(candidate) != null
            }
            if (pkg != null) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                if (launchIntent != null) {
                    context.startActivity(launchIntent)
                    return
                }
            }
            postStatus("Opened ${message.platform.displayName} thread for ${message.senderName}")
        } catch (_: Exception) {
            postStatus("Could not launch external app for ${message.platform.displayName}")
        }
    }

    /**
     * Shares a picked file/image URI directly to the target messaging platform (WhatsApp, Teams, Outlook, etc.)
     * with `FLAG_GRANT_READ_URI_PERMISSION`.
     */
    fun shareAttachmentToPlatform(
        context: Context,
        platform: MessagingPlatform,
        captionText: String,
        uri: Uri,
        mimeType: String
    ) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType.ifBlank { "*/*" }
                putExtra(Intent.EXTRA_STREAM, uri)
                if (captionText.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, captionText)
                }
                clipData = ClipData.newRawUri("Attachment", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Target the specific messaging app if installed
            val installedPkg = platform.packageNames.firstOrNull { pkg ->
                context.packageManager.getLaunchIntentForPackage(pkg) != null
            }
            if (installedPkg != null) {
                sendIntent.setPackage(installedPkg)
                context.startActivity(sendIntent)
            } else {
                val chooser = Intent.createChooser(sendIntent, "Send attachment via ${platform.displayName}").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Composes a brand-new outbound message from the Omnibox Command Bar (with optional attachment).
     */
    fun dispatchOmniboxOutboundMessage(
        context: Context,
        recipientName: String,
        platform: MessagingPlatform,
        messageBody: String,
        attachment: HubAttachment? = null
    ) {
        val cleanRecipient = recipientName.trim().removePrefix("@").ifBlank { "Contact" }
        val initials = cleanRecipient.split(" ")
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .take(2)
            .joinToString("")
            .ifEmpty { "CT" }
        val nowTime = SimpleDateFormat("h:mm a", Locale.US).format(Date())

        if (attachment?.uri != null) {
            shareAttachmentToPlatform(context, platform, messageBody, attachment.uri, attachment.mimeType)
        }

        val initialTurn = ConversationTurn(
            id = "turn_${System.currentTimeMillis()}",
            senderName = "You",
            isFromMe = true,
            text = messageBody.ifBlank { "Sent attachment: ${attachment?.fileName}" },
            timestampLabel = nowTime,
            platform = platform,
            attachments = listOfNotNull(attachment)
        )

        val newEntry = HubMessage(
            id = "omni_${System.currentTimeMillis()}",
            senderName = cleanRecipient,
            senderRoleOrHandle = platform.displayName,
            senderInitials = initials,
            avatarColor = platform.accentColor,
            platform = platform,
            subject = null,
            body = "You: ${initialTurn.text}",
            timestampMillis = System.currentTimeMillis(),
            relativeTimeLabel = "Just now",
            isVip = true,
            isUnread = false,
            isDemo = true,
            attachments = listOfNotNull(attachment),
            conversationHistory = listOf(initialTurn)
        )

        _messages.update { current -> listOf(newEntry) + current }
    }

    fun resetDemoStream() {
        _messages.update { current ->
            val liveOnly = current.filterNot { it.isDemo }
            (liveOnly + createInitialSeedMessages()).sortedByDescending { it.timestampMillis }
        }
    }

    private fun createInitialSeedMessages(): List<HubMessage> {
        val now = System.currentTimeMillis()

        val deckAttachment = HubAttachment(
            id = "att_q4_deck",
            fileName = "Q4_Board_Forecast_v4.pptx",
            fileSizeLabel = "4.8 MB · PowerPoint",
            mimeType = "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            type = AttachmentType.PRESENTATION
        )
        val financialSheetAttachment = HubAttachment(
            id = "att_q4_xlsx",
            fileName = "FY26_Revenue_Model.xlsx",
            fileSizeLabel = "1.2 MB · Excel",
            mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            type = AttachmentType.SPREADSHEET
        )
        val latencyReportPdf = HubAttachment(
            id = "att_latency_pdf",
            fileName = "Edge_Gateway_Benchmark_Report.pdf",
            fileSizeLabel = "840 KB · PDF Document",
            mimeType = "application/pdf",
            type = AttachmentType.DOCUMENT
        )
        val uiMockupImage = HubAttachment(
            id = "att_oled_mock",
            fileName = "Material3_System_Spec_S23Ultra.png",
            fileSizeLabel = "2.4 MB · PNG Image",
            mimeType = "image/png",
            type = AttachmentType.IMAGE
        )
        val termSheetPdf = HubAttachment(
            id = "att_termsheet_pdf",
            fileName = "SeriesB_Partner_Memo_Redline.pdf",
            fileSizeLabel = "1.6 MB · PDF Document",
            mimeType = "application/pdf",
            type = AttachmentType.DOCUMENT
        )

        return listOf(
            HubMessage(
                id = "demo_teams_sarah",
                senderName = "Sarah Jenkins",
                senderRoleOrHandle = "VP Product · Microsoft Teams",
                senderInitials = "SJ",
                avatarColor = Color(0xFF6366F1),
                platform = MessagingPlatform.TEAMS,
                subject = "Q4 Board Deck Sign-off",
                body = "Can you approve slide 14 before the 10:30 AM call? Finance just updated the forecast model.",
                timestampMillis = now - 90_000L,
                relativeTimeLabel = "1m",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Approved, looks great!", "Give me 5 mins to review", "Let's hop on a quick call"),
                attachments = listOf(deckAttachment, financialSheetAttachment),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "t_s1",
                        senderName = "Sarah Jenkins",
                        isFromMe = false,
                        text = "Hey! We just wrapped the pre-board sync with Finance.",
                        timestampLabel = "9:34 AM",
                        platform = MessagingPlatform.TEAMS
                    ),
                    ConversationTurn(
                        id = "t_s2",
                        senderName = "You",
                        isFromMe = true,
                        text = "Awesome — did they finalize the Q4 ARR slide?",
                        timestampLabel = "9:36 AM",
                        platform = MessagingPlatform.TEAMS
                    ),
                    ConversationTurn(
                        id = "t_s3",
                        senderName = "Sarah Jenkins",
                        isFromMe = false,
                        text = "Yes, attached both the updated presentation and the revenue model spreadsheet below.",
                        timestampLabel = "9:39 AM",
                        platform = MessagingPlatform.TEAMS,
                        attachments = listOf(deckAttachment, financialSheetAttachment)
                    ),
                    ConversationTurn(
                        id = "t_s4",
                        senderName = "Sarah Jenkins",
                        isFromMe = false,
                        text = "Can you approve slide 14 before the 10:30 AM call? Finance just updated the forecast model.",
                        timestampLabel = "9:40 AM",
                        platform = MessagingPlatform.TEAMS
                    )
                )
            ),
            HubMessage(
                id = "demo_wa_sarah",
                senderName = "Sarah Jenkins",
                senderRoleOrHandle = "+1 (415) 890-2311 · WhatsApp",
                senderInitials = "SJ",
                avatarColor = Color(0xFF6366F1),
                platform = MessagingPlatform.WHATSAPP,
                subject = null,
                body = "Sent you the deck files on Teams — ping me here on WhatsApp if you're in transit!",
                timestampMillis = now - 60_000L,
                relativeTimeLabel = "Now",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("On it right now 👍", "Reviewing the deck now", "Call you in 2 mins"),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "w_s1",
                        senderName = "Sarah Jenkins",
                        isFromMe = false,
                        text = "Are you heading into the office or joining from mobile?",
                        timestampLabel = "9:38 AM",
                        platform = MessagingPlatform.WHATSAPP
                    ),
                    ConversationTurn(
                        id = "w_s2",
                        senderName = "Sarah Jenkins",
                        isFromMe = false,
                        text = "Sent you the deck files on Teams — ping me here on WhatsApp if you're in transit!",
                        timestampLabel = "9:41 AM",
                        platform = MessagingPlatform.WHATSAPP
                    )
                )
            ),
            HubMessage(
                id = "demo_outlook_marcus",
                senderName = "Marcus Vance",
                senderRoleOrHandle = "marcus.vance@architecture.io",
                senderInitials = "MV",
                avatarColor = Color(0xFF0284C7),
                platform = MessagingPlatform.OUTLOOK,
                subject = "Re: Edge Gateway Latency Cut by 42%",
                body = "We moved token validation to the edge cluster and p99 latency dropped to 18ms. Full PDF benchmark attached.",
                timestampMillis = now - 480_000L,
                relativeTimeLabel = "8m",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Great numbers, ship it!", "Sharing with engineering", "Thanks Marcus!"),
                attachments = listOf(latencyReportPdf),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "o_m1",
                        senderName = "You",
                        isFromMe = true,
                        text = "Marcus, how did the canary rollout for the US-East edge gateway perform overnight?",
                        timestampLabel = "8:15 AM",
                        platform = MessagingPlatform.OUTLOOK
                    ),
                    ConversationTurn(
                        id = "o_m2",
                        senderName = "Marcus Vance",
                        isFromMe = false,
                        text = "We moved token validation to the edge cluster and p99 latency dropped from 31ms to 18ms (-42%). Zero error spikes across 14M requests. Full PDF benchmark attached.",
                        timestampLabel = "9:33 AM",
                        platform = MessagingPlatform.OUTLOOK,
                        attachments = listOf(latencyReportPdf)
                    )
                )
            ),
            HubMessage(
                id = "demo_wa_elena",
                senderName = "Elena Rostova",
                senderRoleOrHandle = "Design Lead · WhatsApp",
                senderInitials = "ER",
                avatarColor = Color(0xFF10B981),
                platform = MessagingPlatform.WHATSAPP,
                subject = null,
                body = "Here are the updated Material 3 adaptive specs for the S23 Ultra display. Thoughts?",
                timestampMillis = now - 840_000L,
                relativeTimeLabel = "14m",
                isVip = false,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Love the Material 3 clean look!", "Looks super crisp", "Approved for build"),
                attachments = listOf(uiMockupImage),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "w_e1",
                        senderName = "Elena Rostova",
                        isFromMe = false,
                        text = "Cleaned up the card spacing, typography hierarchy, and native keyboard sheet.",
                        timestampLabel = "9:25 AM",
                        platform = MessagingPlatform.WHATSAPP
                    ),
                    ConversationTurn(
                        id = "w_e2",
                        senderName = "Elena Rostova",
                        isFromMe = false,
                        text = "Here are the updated Material 3 adaptive specs for the S23 Ultra display. Thoughts?",
                        timestampLabel = "9:27 AM",
                        platform = MessagingPlatform.WHATSAPP,
                        attachments = listOf(uiMockupImage)
                    )
                )
            ),
            HubMessage(
                id = "demo_sms_marcus",
                senderName = "Marcus Vance",
                senderRoleOrHandle = "+1 (650) 442-9810 · SMS",
                senderInitials = "MV",
                avatarColor = Color(0xFF0284C7),
                platform = MessagingPlatform.SMS,
                subject = null,
                body = "Landed at SFO Gate B12. Grabbing espresso before heading to HQ.",
                timestampMillis = now - 1_320_000L,
                relativeTimeLabel = "22m",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("See you at HQ!", "Grab me an espresso ☕", "Safe travels!"),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "s_m1",
                        senderName = "Marcus Vance",
                        isFromMe = false,
                        text = "Boarding my flight from Seattle now, see you in SF around 9:15.",
                        timestampLabel = "7:10 AM",
                        platform = MessagingPlatform.SMS
                    ),
                    ConversationTurn(
                        id = "s_m2",
                        senderName = "Marcus Vance",
                        isFromMe = false,
                        text = "Landed at SFO Gate B12. Grabbing espresso before heading to HQ.",
                        timestampLabel = "9:19 AM",
                        platform = MessagingPlatform.SMS
                    )
                )
            ),
            HubMessage(
                id = "demo_fb_david",
                senderName = "David Kim",
                senderRoleOrHandle = "Messenger",
                senderInitials = "DK",
                avatarColor = Color(0xFF3B82F6),
                platform = MessagingPlatform.MESSENGER,
                subject = null,
                body = "Are we still on for dinner at 7:30 PM in SoMa tonight?",
                timestampMillis = now - 2_400_000L,
                relativeTimeLabel = "40m",
                isVip = false,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Yes! See you at 7:30", "Running 15 mins late", "Can we do 8:00 PM?"),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "fb_d1",
                        senderName = "David Kim",
                        isFromMe = false,
                        text = "Hey! Booked a table for 2 at Nopa for 7:30 PM.",
                        timestampLabel = "Yesterday",
                        platform = MessagingPlatform.MESSENGER
                    ),
                    ConversationTurn(
                        id = "fb_d2",
                        senderName = "David Kim",
                        isFromMe = false,
                        text = "Are we still on for dinner at 7:30 PM in SoMa tonight?",
                        timestampLabel = "9:01 AM",
                        platform = MessagingPlatform.MESSENGER
                    )
                )
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
                relativeTimeLabel = "1h",
                isVip = true,
                isUnread = true,
                isDemo = true,
                quickReplies = listOf("Timeline works for me", "Reviewing the memo now", "Confirmed for tomorrow"),
                attachments = listOf(termSheetPdf),
                conversationHistory = listOf(
                    ConversationTurn(
                        id = "gm_a1",
                        senderName = "Alex Rivera",
                        isFromMe = false,
                        text = "Great catching up yesterday. Our legal team reviewed the governance clauses.",
                        timestampLabel = "8:40 AM",
                        platform = MessagingPlatform.GMAIL
                    ),
                    ConversationTurn(
                        id = "gm_a2",
                        senderName = "Alex Rivera",
                        isFromMe = false,
                        text = "Attached the redlined partner memo for tomorrow morning's sync. Let me know if the timeline works.",
                        timestampLabel = "8:41 AM",
                        platform = MessagingPlatform.GMAIL,
                        attachments = listOf(termSheetPdf)
                    )
                )
            )
        )
    }
}
