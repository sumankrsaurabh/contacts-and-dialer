package com.coderon.phone.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.coderon.phone.R

fun sendVoicemailNotification(context: Context, phoneNumber: String) {
    val channelId = "voicemail_channel"
    val manager = context.getSystemService(NotificationManager::class.java)

    val channel = NotificationChannel(
        channelId,
        "Voicemail Notifications",
        NotificationManager.IMPORTANCE_HIGH
    )
    manager.createNotificationChannel(channel)

    val notification = NotificationCompat.Builder(context, channelId)
        .setContentTitle("New Voicemail")
        .setContentText("Voicemail from $phoneNumber")
        .setSmallIcon(R.drawable.call)
        .build()

    manager.notify(1001, notification)
}
