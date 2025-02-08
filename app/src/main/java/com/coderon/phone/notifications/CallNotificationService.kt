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
import com.coderon.phone.ui.IncomingCallActivity

object CallNotificationService {
    private const val CHANNEL_ID = "CALL_NOTIFICATIONS"

    fun showIncomingCallNotification(context: Context, phoneNumber: String) {
        val acceptIntent = Intent(context, CallReceiver::class.java).apply {
            action = "ACCEPT_CALL"
            putExtra("PHONE_NUMBER", phoneNumber)
        }

        val rejectIntent = Intent(context, CallReceiver::class.java).apply {
            action = "REJECT_CALL"
            putExtra("PHONE_NUMBER", phoneNumber)
        }

        val notification = NotificationCompat.Builder(context, "call_channel")
            .setContentTitle("Incoming Call")
            .setContentText("Call from $phoneNumber")
            .setSmallIcon(R.drawable.call)
            .addAction(R.drawable.call, "Answer", PendingIntent.getBroadcast(context, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(R.drawable.end_call, "Reject", PendingIntent.getBroadcast(context, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(2001, notification)
    }


    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Call Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming calls"
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

        NotificationManagerCompat.from(context).notify(2, notification)
    }

    fun showIncomingCallScreen(context: Context, phoneNumber: String) {
        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("CALLER_NUMBER", phoneNumber)
        }
        context.startActivity(intent)
    }

}
