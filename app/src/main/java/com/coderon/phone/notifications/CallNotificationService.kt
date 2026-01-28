package com.coderon.phone.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import coil.ImageLoader
import coil.request.ImageRequest
import com.coderon.phone.MainActivity
import com.coderon.phone.R
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.call.ui.CallUiState
import com.coderon.phone.receiver.CallReceiver
import com.coderon.phone.ui.utils.extentions.notificationManager
import com.coderon.phone.utils.Constants.ACCEPT_CALL
import com.coderon.phone.utils.Constants.CALLBACK_MISSED_CALL
import com.coderon.phone.utils.Constants.DECLINE_CALL
import com.coderon.phone.utils.Constants.SEND_MESSAGE
import com.coderon.phone.utils.Constants.TOGGLE_HOLD
import com.coderon.phone.utils.Constants.TOGGLE_MUTE
import com.coderon.phone.utils.Constants.TOGGLE_SPEAKER

/**
 * Modern Notification Manager using NotificationCompat.CallStyle.
 * Connects directly to [CallManager] for real-time state.
 */
class CallNotificationManager(
    private val context: Context
) {

    companion object {
        const val CALL_NOTIFICATION_ID = 42
        const val MISSED_CALL_NOTIFICATION_ID = 43
        const val CHANNEL_INCOMING = "call_incoming"
        const val CHANNEL_ONGOING = "call_ongoing"
        const val CHANNEL_MISSED = "call_missed"
        const val ACTION_SHOW_CALL = "com.coderon.phone.ACTION_SHOW_CALL"
        const val EXTRA_SHOW_CALL = "EXTRA_SHOW_CALL"
    }

    private val notificationManager: NotificationManager =
        context.notificationManager

    /* ---------------------------------------------------
       PUBLIC API
    --------------------------------------------------- */

    /**
     * Builds the call notification. 
     * NOTE: Does NOT call notify() itself here to allow the caller (Service)
     * to use startForeground() correctly, avoiding CallStyle validation crashes.
     */
    @SuppressLint("NewApi")
    fun setupNotification(): Notification? {
        val uiState = CallManager.uiState.value

        if (uiState.hasNoCalls) {
            cancelNotification()
            return null
        }

        val isIncoming =
            uiState.screen == CallScreenType.INCOMING || uiState.screen == CallScreenType.CALL_WAITING
        val channelId = if (isIncoming) CHANNEL_INCOMING else CHANNEL_ONGOING

        createChannelIfNeeded(channelId, isIncoming)

        // buildNotification will be called again once avatar is loaded
        uiState.primaryCall?.profilePictureUrl?.let { url ->
            loadAvatarAndNotify(url, uiState, channelId, isIncoming)
        }

        return buildNotification(
            uiState = uiState,
            channelId = channelId,
            isIncoming = isIncoming,
            avatarBitmap = null
        )
    }

    fun showMissedCallNotification(name: String?, number: String) {
        createMissedCallChannelIfNeeded()

        val contentIntent = createContentIntent()
        val callerName = name ?: number

        val builder = NotificationCompat.Builder(context, CHANNEL_MISSED)
            .setSmallIcon(R.drawable.call)
            .setContentTitle(callerName)
            .setContentText("Missed call")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_MISSED_CALL)
            .setColor(0xFFFF3B30.toInt())
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.call,
                    "Call back",
                    actionPendingIntent(CALLBACK_MISSED_CALL, number)
                ).build()
            )
            .addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.message,
                    "Message",
                    actionPendingIntent(SEND_MESSAGE, number)
                ).build()
            )

        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    fun cancelMissedCallNotification() {
        notificationManager.cancel(MISSED_CALL_NOTIFICATION_ID)
    }

    /* ---------------------------------------------------
       INTERNALS
    --------------------------------------------------- */

    private fun buildNotification(
        uiState: CallUiState,
        channelId: String,
        isIncoming: Boolean,
        avatarBitmap: Bitmap? = null
    ): Notification {
        val call = uiState.primaryCall ?: return buildFallbackNotification(channelId)

        val callerName = call.displayName ?: call.phoneNumber
        val callerIcon = if (avatarBitmap != null) {
            IconCompat.createWithBitmap(avatarBitmap)
        } else {
            IconCompat.createWithResource(context, R.drawable.profile_picture_call)
        }

        val person = Person.Builder()
            .setName(callerName)
            .setIcon(callerIcon)
            .setImportant(true)
            .build()

        val contentIntent = createContentIntent()

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.call)
            .setContentTitle(callerName)
            .setContentIntent(contentIntent)
            .setPriority(if (isIncoming) NotificationCompat.PRIORITY_MAX else NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setSilent(true)

        // Modern CallStyle (Android 12+)
        // Incoming calls MUST have a fullScreenIntent to be posted safely with CallStyle
        // Ongoing calls MUST be in a foreground service.
        val style = if (isIncoming) {
            NotificationCompat.CallStyle.forIncomingCall(
                person,
                actionPendingIntent(DECLINE_CALL),
                actionPendingIntent(ACCEPT_CALL)
            )
        } else {
            NotificationCompat.CallStyle.forOngoingCall(
                person,
                actionPendingIntent(DECLINE_CALL)
            )
        }

        builder.setStyle(style)

        if (isIncoming) {
            builder.setFullScreenIntent(contentIntent, true)
        } else {
            // Ongoing state actions
            val isOnHold = call.state == com.coderon.phone.call.domain.CallState.HOLDING

            builder.addAction(
                NotificationCompat.Action.Builder(
                    if (uiState.isMuted) R.drawable.mic_mute_fill else R.drawable.mic,
                    if (uiState.isMuted) "Unmute" else "Mute",
                    actionPendingIntent(TOGGLE_MUTE)
                ).build()
            )

            builder.addAction(
                NotificationCompat.Action.Builder(
                    if (isOnHold) R.drawable.pause else R.drawable.pause,
                    if (isOnHold) "Resume" else "Hold",
                    actionPendingIntent(TOGGLE_HOLD)
                ).build()
            )

            builder.addAction(
                NotificationCompat.Action.Builder(
                    R.drawable.volume_high,
                    "Speaker",
                    actionPendingIntent(TOGGLE_SPEAKER)
                ).build()
            )

            if (uiState.callDurationSeconds > 0) {
                builder.setWhen(System.currentTimeMillis() - (uiState.callDurationSeconds * 1000))
                builder.setUsesChronometer(true)
            }
        }

        return builder.build()
    }

    private fun buildFallbackNotification(channelId: String): Notification {
        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.call)
            .setContentTitle("Call in progress")
            .build()
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_SHOW_CALL
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SHOW_CALL, true)
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun loadAvatarAndNotify(
        url: String,
        uiState: CallUiState,
        channelId: String,
        isIncoming: Boolean
    ) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .target { drawable ->
                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val notification = buildNotification(
                        uiState = uiState,
                        channelId = channelId,
                        isIncoming = isIncoming,
                        avatarBitmap = bitmap
                    )
                    // Safe notify: If incoming, always safe due to fullScreenIntent.
                    // If ongoing, we assume the Service has already called startForeground.
                    try {
                        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
                    } catch (e: Exception) {
                        // Suppress potential validation errors during transitions
                    }
                }
            }
            .build()
        ImageLoader(context).enqueue(request)
    }

    private fun actionPendingIntent(action: String, phoneNumber: String? = null): PendingIntent {
        val intent = Intent(context, CallReceiver::class.java).apply {
            this.action = action
            if (phoneNumber != null) {
                putExtra("PHONE_NUMBER", phoneNumber)
            }
        }
        return PendingIntent.getBroadcast(
            context,
            action.hashCode() + (phoneNumber?.hashCode() ?: 0),
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("NewApi")
    private fun createChannelIfNeeded(channelId: String, isIncoming: Boolean) {
        if (isIncoming) NotificationManager.IMPORTANCE_MAX else NotificationManager.IMPORTANCE_HIGH
        val channelName = if (isIncoming) "Incoming Calls" else "Ongoing Calls"

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            setSound(null, null)
            enableVibration(isIncoming)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("NewApi")
    private fun createMissedCallChannelIfNeeded() {
        val channel = NotificationChannel(
            CHANNEL_MISSED, "Missed Calls", NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
