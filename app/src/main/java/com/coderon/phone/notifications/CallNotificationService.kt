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
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
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
import com.coderon.phone.utils.Constants.DECLINE_CALL
import com.coderon.phone.utils.Constants.TOGGLE_MUTE
import com.coderon.phone.utils.Constants.TOGGLE_SPEAKER

class CallNotificationManager(
    private val context: Context
) {

    companion object {
        const val CALL_NOTIFICATION_ID = 42
        const val MISSED_CALL_NOTIFICATION_ID = 43
        const val CHANNEL_INCOMING = "call_incoming"
        const val CHANNEL_ONGOING = "call_ongoing"
        const val CHANNEL_MISSED = "call_missed"
    }

    private val notificationManager: NotificationManager =
        context.notificationManager

    /* ---------------------------------------------------
       PUBLIC API
    --------------------------------------------------- */

    @SuppressLint("NewApi")
    fun setupNotification(showOngoing: Boolean = false): Notification? {
        val uiState = CallManager.uiState.value

        if (uiState.hasNoCalls) {
            cancelNotification()
            return null
        }

        val isIncoming = uiState.screen == CallScreenType.INCOMING
        val channelId = if (isIncoming) CHANNEL_INCOMING else CHANNEL_ONGOING

        createChannelIfNeeded(channelId, isIncoming)

        val notification = buildNotification(
            uiState = uiState,
            channelId = channelId,
            isIncoming = isIncoming,
            showOngoing = showOngoing
        )

        notificationManager.notify(CALL_NOTIFICATION_ID, notification)

        // Asynchronously update avatar if needed
        uiState.primaryCall?.profilePictureUrl?.let { url ->
            loadAvatarAndNotify(url, uiState, channelId, isIncoming, showOngoing)
        }
        
        return notification
    }

    fun showMissedCallNotification(name: String?, number: String) {
        createMissedCallChannelIfNeeded()

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val title = name ?: number
        val text = if (name != null) "Missed call from $number" else "Missed call"

        val builder = NotificationCompat.Builder(context, CHANNEL_MISSED)
            .setSmallIcon(R.drawable.missed_call)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .setColor(0xFFFF3B30.toInt()) // iOS Red

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setCategory(Notification.CATEGORY_MISSED_CALL)
        }

        notificationManager.notify(MISSED_CALL_NOTIFICATION_ID, builder.build())
    }

    fun cancelNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    /* ---------------------------------------------------
       INTERNALS
    --------------------------------------------------- */

    @SuppressLint("RemoteViewLayout")
    fun buildNotification(
        uiState: CallUiState,
        channelId: String,
        isIncoming: Boolean,
        showOngoing: Boolean,
        avatarBitmap: Bitmap? = null
    ): Notification {

        val callerName =
            uiState.primaryCall?.displayName
                ?: uiState.primaryCall?.phoneNumber
                ?: context.getString(R.string.unknown_caller)

        val statusTextRes = when (uiState.screen) {
            CallScreenType.INCOMING -> R.string.is_calling
            CallScreenType.CALL_WAITING -> R.string.call_waiting
            CallScreenType.CONFERENCE -> R.string.conference_call
            CallScreenType.ONGOING -> R.string.ongoing_call
            else -> R.string.ongoing_call
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val isCallOngoing = !isIncoming && !uiState.hasNoCalls

        val collapsedView = RemoteViews(context.packageName, R.layout.call_notification).apply {
            setTextViewText(R.id.notification_caller_name, callerName)
            setTextViewText(R.id.notification_call_status, context.getString(statusTextRes))

            if (avatarBitmap != null) {
                setImageViewBitmap(R.id.notification_avatar, avatarBitmap)
            } else {
                setImageViewResource(R.id.notification_avatar, R.drawable.profile_picture_call)
            }

            // Handle Chronometer for ongoing calls
            if (isCallOngoing && uiState.callDurationSeconds > 0) {
                setViewVisibility(R.id.notification_call_status_divider, View.VISIBLE)
                setViewVisibility(R.id.notification_chronometer, View.VISIBLE)
                setChronometer(
                    R.id.notification_chronometer,
                    SystemClock.elapsedRealtime() - (uiState.callDurationSeconds * 1000),
                    null,
                    true
                )
            } else {
                setViewVisibility(R.id.notification_call_status_divider, View.GONE)
                setViewVisibility(R.id.notification_chronometer, View.GONE)
            }

            // Toggle Remote actions visibility (Mute/Speaker)
            setViewVisibility(R.id.notification_remote_actions, if (isCallOngoing) View.VISIBLE else View.GONE)
            
            // Set button icons based on state
            setImageViewResource(R.id.notification_toggle_mute, if (uiState.isMuted) R.drawable.mic_mute_fill else R.drawable.mic)
            
            // Actions
            setViewVisibility(R.id.notification_accept_call, if (isIncoming) View.VISIBLE else View.GONE)
            
            setOnClickPendingIntent(R.id.notification_accept_call, actionPendingIntent(ACCEPT_CALL))
            setOnClickPendingIntent(R.id.notification_decline_call, actionPendingIntent(DECLINE_CALL))
            setOnClickPendingIntent(R.id.notification_toggle_mute, actionPendingIntent(TOGGLE_MUTE))
            setOnClickPendingIntent(R.id.notification_toggle_speaker, actionPendingIntent(TOGGLE_SPEAKER))
        }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.call)
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(if (isIncoming) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)
            .setOngoing(showOngoing || isCallOngoing)
            .setSound(null)
            .setContentIntent(contentIntent)
            .setCustomContentView(collapsedView)
            .setCustomBigContentView(collapsedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .apply {
                if (isIncoming) {
                    setFullScreenIntent(contentIntent, true)
                }
            }
            .build()
    }

    private fun loadAvatarAndNotify(
        url: String,
        uiState: CallUiState,
        channelId: String,
        isIncoming: Boolean,
        showOngoing: Boolean
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
                        showOngoing = showOngoing,
                        avatarBitmap = bitmap
                    )
                    notificationManager.notify(CALL_NOTIFICATION_ID, notification)
                }
            }
            .build()
        ImageLoader(context).enqueue(request)
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            Intent(context, CallReceiver::class.java).apply {
                this.action = action
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("NewApi")
    private fun createChannelIfNeeded(
        channelId: String,
        isIncoming: Boolean
    ) {
        val importance = if (isIncoming) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_LOW
        val channelName = if (isIncoming) "Incoming Calls" else "Ongoing Calls"

        val channel = NotificationChannel(channelId, channelName, importance).apply {
            setSound(null, null)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("NewApi")
    private fun createMissedCallChannelIfNeeded() {
        val channel = NotificationChannel(
            CHANNEL_MISSED,
            "Missed Calls",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}
