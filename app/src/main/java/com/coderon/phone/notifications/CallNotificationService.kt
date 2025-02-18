package com.coderon.phone.notifications

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.telecom.Call
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.coderon.phone.MainActivity
import com.coderon.phone.R
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.receiver.CallReceiver
import com.coderon.phone.ui.utils.extentions.getCallState
import com.coderon.phone.ui.utils.extentions.notificationManager
import com.coderon.phone.utils.Constants.ACCEPT_CALL
import com.coderon.phone.utils.Constants.DECLINE_CALL

class CallNotificationManager(private val context: Context) {
    private val CALL_NOTIFICATION_ID = 42
    private val ACCEPT_CALL_CODE = 0
    private val DECLINE_CALL_CODE = 1
    private val notificationManager = context.notificationManager

    @SuppressLint("NewApi")
    fun setupNotification(forceLowPriority: Boolean = false) {
        val callState = CallManager.getPrimaryCall().getCallState()
        val isHighPriority = callState == Call.STATE_RINGING && !forceLowPriority
        val channelId = if (isHighPriority) "call_high_priority" else "call_default"
        val importance =
            if (isHighPriority) NotificationManager.IMPORTANCE_HIGH else NotificationManager.IMPORTANCE_DEFAULT
        val channelName = if (isHighPriority) "Incoming Calls" else "Ongoing Calls"

        val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
            setSound(null, null)
        }
        notificationManager.createNotificationChannel(notificationChannel)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val acceptCallIntent =
            Intent(context, CallReceiver::class.java).apply { action = ACCEPT_CALL }
        val acceptPendingIntent = PendingIntent.getBroadcast(
            context,
            ACCEPT_CALL_CODE,
            acceptCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val declineCallIntent =
            Intent(context, CallReceiver::class.java).apply { action = DECLINE_CALL }
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            DECLINE_CALL_CODE,
            declineCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val contentTextId = when (callState) {
            Call.STATE_RINGING -> R.string.is_calling
            Call.STATE_DIALING -> R.string.dialing
            Call.STATE_DISCONNECTED -> R.string.call_ended
            Call.STATE_DISCONNECTING -> R.string.call_ending
            else -> R.string.ongoing_call
        }

        val collapsedView = RemoteViews(context.packageName, R.layout.call_notification).apply {
            setTextViewText(R.id.notification_call_status, context.getString(contentTextId))
            setViewVisibility(
                R.id.notification_accept_call,
                if (callState == Call.STATE_RINGING) View.VISIBLE else View.GONE
            )
            setOnClickPendingIntent(R.id.notification_decline_call, declinePendingIntent)
            setOnClickPendingIntent(R.id.notification_accept_call, acceptPendingIntent)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.call)
            .setContentIntent(openAppPendingIntent)
            .setPriority(if (isHighPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(Notification.CATEGORY_CALL)
            .setCustomContentView(collapsedView)
            .setOngoing(true)
            .setSound(null)
            .setChannelId(channelId)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        if (callState == Call.STATE_ACTIVE) {
            builder.setUsesChronometer(true)
                .setWhen(System.currentTimeMillis())
        }

        if (isHighPriority) {
            builder.setFullScreenIntent(openAppPendingIntent, true)
        }

        val notification = builder.build()

        // Prevent outdated notification if call state changes mid-setup
        if (CallManager.getPrimaryCall().getCallState() == callState) {
            notificationManager.notify(CALL_NOTIFICATION_ID, notification)
        }
    }

    fun cancelNotification() {
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }
}
