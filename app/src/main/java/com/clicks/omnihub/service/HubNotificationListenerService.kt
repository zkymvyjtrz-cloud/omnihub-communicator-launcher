package com.clicks.omnihub.service

import android.app.Notification
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.clicks.omnihub.data.HubRepository
import com.clicks.omnihub.model.AttachmentType
import com.clicks.omnihub.model.ConversationTurn
import com.clicks.omnihub.model.DirectReplyAction
import com.clicks.omnihub.model.HubAttachment
import com.clicks.omnihub.model.HubMessage
import com.clicks.omnihub.model.MessagingPlatform
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Live Android Notification Bridge that captures real incoming messages from
 * WhatsApp, Microsoft Teams, Outlook, Gmail, SMS/Messages, and Facebook Messenger,
 * extracts full multi-message conversation history (`EXTRA_MESSAGES`), image/file attachments,
 * and inline direct-reply (`RemoteInput`) actions.
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
        if (pkg == packageName) return null

        val notification = sbn.notification ?: return null
        val extras = notification.extras ?: return null

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

        // Extract any attached picture preview from Notification extras
        val extractedAttachments = mutableListOf<HubAttachment>()
        @Suppress("DEPRECATION")
        val pictureBitmap = extras.getParcelable<Bitmap>(Notification.EXTRA_PICTURE)
        if (pictureBitmap != null) {
            extractedAttachments.add(
                HubAttachment(
                    id = "pic_${sbn.postTime}",
                    fileName = "Image_Attachment.jpg",
                    fileSizeLabel = "Image Preview",
                    mimeType = "image/jpeg",
                    type = AttachmentType.IMAGE,
                    previewBitmap = pictureBitmap
                )
            )
        }

        // Extract full multi-turn conversation history from Notification.EXTRA_MESSAGES if present
        val extractedTurns = extractConversationHistory(
            extras = extras,
            fallbackSender = rawTitle,
            fallbackText = rawText,
            fallbackTime = sbn.postTime,
            platform = matchedPlatform,
            attachments = extractedAttachments
        )

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
            contentIntent = notification.contentIntent,
            attachments = extractedAttachments,
            conversationHistory = extractedTurns
        )
    }

    private fun extractConversationHistory(
        extras: Bundle,
        fallbackSender: String,
        fallbackText: String,
        fallbackTime: Long,
        platform: MessagingPlatform,
        attachments: List<HubAttachment>
    ): List<ConversationTurn> {
        val turns = mutableListOf<ConversationTurn>()
        val timeFmt = SimpleDateFormat("h:mm a", Locale.US)

        try {
            @Suppress("DEPRECATION")
            val parcelables: Array<Parcelable>? = extras.getParcelableArray(Notification.EXTRA_MESSAGES)
            if (!parcelables.isNullOrEmpty()) {
                parcelables.forEachIndexed { idx, p ->
                    val b = p as? Bundle ?: return@forEachIndexed
                    val text = b.getCharSequence("text")?.toString() ?: return@forEachIndexed
                    val sender = b.getCharSequence("sender")?.toString() ?: fallbackSender
                    val timeMillis = b.getLong("time", fallbackTime)
                    val dataMime = b.getString("type")
                    @Suppress("DEPRECATION")
                    val dataUri = b.getParcelable<android.net.Uri>("uri")

                    val turnAttachments = mutableListOf<HubAttachment>()
                    if (dataUri != null) {
                        turnAttachments.add(
                            HubAttachment(
                                id = "uri_${timeMillis}_$idx",
                                fileName = dataUri.lastPathSegment ?: "Attachment",
                                fileSizeLabel = dataMime ?: "Media Attachment",
                                mimeType = dataMime ?: "*/*",
                                type = if (dataMime?.startsWith("image") == true) AttachmentType.IMAGE else AttachmentType.DOCUMENT,
                                uri = dataUri
                            )
                        )
                    }

                    turns.add(
                        ConversationTurn(
                            id = "live_turn_${timeMillis}_$idx",
                            senderName = sender,
                            isFromMe = false,
                            text = text,
                            timestampLabel = timeFmt.format(Date(timeMillis)),
                            platform = platform,
                            attachments = turnAttachments
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }

        if (turns.isEmpty()) {
            turns.add(
                ConversationTurn(
                    id = "live_turn_$fallbackTime",
                    senderName = fallbackSender,
                    isFromMe = false,
                    text = fallbackText,
                    timestampLabel = timeFmt.format(Date(fallbackTime)),
                    platform = platform,
                    attachments = attachments
                )
            )
        }
        return turns
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
            diffMinutes < 1L -> "Now"
            diffMinutes < 60L -> "${diffMinutes}m"
            else -> "${diffMinutes / 60L}h"
        }
    }
}
