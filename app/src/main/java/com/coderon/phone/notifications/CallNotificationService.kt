package com.coderon.phone.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.coderon.phone.MainActivity
import com.coderon.phone.R
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.call.ui.CallUiState
import com.coderon.phone.receiver.CallReceiver
import com.coderon.phone.ui.utils.extentions.notificationManager
import com.coderon.phone.utils.Constants.ACCEPT_CALL
import com.coderon.phone.utils.Constants.DECLINE_CALL

class CallNotificationManager(
    private val context: Context
) {

    private companion object {
        const val CALL_NOTIFICATION_ID = 42
        const val CHANNEL_INCOMING = "call_incoming"
        const val CHANNEL_ONGOING = "call_ongoing"
    }

    private val notificationManager: NotificationManager =
        context.notificationManager

    /* ---------------------------------------------------
       PUBLIC API
    --------------------------------------------------- */

    @SuppressLint("NewApi")
    fun setupNotification(showOngoing: Boolean = false) {
        val uiState = CallManager.uiState.value

        if (uiState.hasNoCalls) {
            cancelNotification()
            return
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
    }

    fun cancelNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    /* ---------------------------------------------------
       INTERNALS
    --------------------------------------------------- */

    private fun buildNotification(
        uiState: CallUiState,
        channelId: String,
        isIncoming: Boolean,
        showOngoing: Boolean
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
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        val collapsedView =
            RemoteViews(context.packageName, R.layout.call_notification).apply {

                setTextViewText(
                    R.id.notification_caller_name,
                    callerName
                )

                setTextViewText(
                    R.id.notification_call_status,
                    context.getString(statusTextRes)
                )

                setViewVisibility(
                    R.id.notification_accept_call,
                    if (isIncoming) View.VISIBLE else View.GONE
                )

                setOnClickPendingIntent(
                    R.id.notification_accept_call,
                    actionPendingIntent(ACCEPT_CALL)
                )

                setOnClickPendingIntent(
                    R.id.notification_decline_call,
                    actionPendingIntent(DECLINE_CALL)
                )
            }

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.call)
            .setCategory(Notification.CATEGORY_CALL)
            .setPriority(
                if (isIncoming)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setOngoing(showOngoing || !isIncoming)
            .setSound(null)
            .setContentIntent(contentIntent)
            .setCustomContentView(collapsedView)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .apply {
                if (!isIncoming && uiState.callDurationSeconds > 0) {
                    setUsesChronometer(true)
                    setWhen(
                        System.currentTimeMillis() -
                                uiState.callDurationSeconds * 1000
                    )
                }
                if (isIncoming) {
                    setFullScreenIntent(contentIntent, true)
                }
            }
            .build()
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            Intent(context, CallReceiver::class.java).apply {
                this.action = action
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("NewApi")
    private fun createChannelIfNeeded(
        channelId: String,
        isIncoming: Boolean
    ) {
        val importance =
            if (isIncoming)
                NotificationManager.IMPORTANCE_HIGH
            else
                NotificationManager.IMPORTANCE_DEFAULT

        val channelName =
            if (isIncoming) "Incoming Calls"
            else "Ongoing Calls"

        val channel = NotificationChannel(
            channelId,
            channelName,
            importance
        ).apply {
            setSound(null, null)
            enableVibration(false)
        }

        notificationManager.createNotificationChannel(channel)
    }
}
