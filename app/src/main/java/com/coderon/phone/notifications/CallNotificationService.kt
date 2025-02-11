package com.coderon.phone.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.coderon.phone.R
import com.coderon.phone.receiver.CallReceiver

object CallNotificationService {
    private const val CHANNEL_ID = "CALL_NOTIFICATIONS"
    private const val INCOMING_CALL_NOTIFICATION_ID = 2001
    private const val ONGOING_CALL_NOTIFICATION_ID = 2002

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showIncomingCallNotification(context: Context, phoneNumber: String) {
        createNotificationChannel(context)

        val acceptIntent = Intent(context, CallReceiver::class.java).apply {
            action = "ACCEPT_CALL"
            putExtra("PHONE_NUMBER", phoneNumber)
        }

        val rejectIntent = Intent(context, CallReceiver::class.java).apply {
            action = "REJECT_CALL"
            putExtra("PHONE_NUMBER", phoneNumber)
        }

        val acceptPendingIntent = PendingIntent.getBroadcast(
            context, 0, acceptIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val rejectPendingIntent = PendingIntent.getBroadcast(
            context, 1, rejectIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Incoming Call")
            .setContentText("Call from $phoneNumber")
            .setSmallIcon(R.drawable.call) // Ensure this exists
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
//            .setFullScreenIntent(getFullScreenIntent(context, phoneNumber), true)
            .addAction(R.drawable.call, "Answer", acceptPendingIntent) // Ensure correct icon
            .addAction(R.drawable.end_call, "Reject", rejectPendingIntent) // Ensure correct icon
            .setAutoCancel(true)
            .setOngoing(true)
            .build()

        NotificationManagerCompat.from(context).notify(INCOMING_CALL_NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming and ongoing calls"
                setSound(null, null) // Disable default ringtone
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showOngoingCallNotification(context: Context, phoneNumber: String) {
        createNotificationChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.call)
            .setContentTitle("Ongoing Call")
            .setContentText("Calling $phoneNumber")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        NotificationManagerCompat.from(context).notify(ONGOING_CALL_NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(INCOMING_CALL_NOTIFICATION_ID)
    }

    /*private fun getFullScreenIntent(context: Context, phoneNumber: String): PendingIntent {
        *//*val intent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("CALLER_NUMBER", phoneNumber)
        }*//*
        return PendingIntent.getActivity(
            context, 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }*/
}
