package com.coderon.phone.ui.utils.extentions

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.ActivityCompat

fun Context.checkPermissions(vararg permissions: String): Boolean {
    return listOf(permissions.forEach {
        ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }).any()
}

fun isSPlus(): Boolean {
    return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
}

val Context.notificationManager: NotificationManager get() = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
val Context.audioManager: AudioManager get() = getSystemService(Context.AUDIO_SERVICE) as AudioManager
val Context.powerManager: PowerManager get() = getSystemService(Context.POWER_SERVICE) as PowerManager
