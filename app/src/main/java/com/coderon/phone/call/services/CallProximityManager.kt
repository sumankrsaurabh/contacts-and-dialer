package com.coderon.phone.call.services

import android.content.Context
import android.os.PowerManager
import com.coderon.phone.call.ui.AudioRoute

class CallProximityManager(context: Context) {
    private var proximityWakeLock: PowerManager.WakeLock? = null

    init {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(
            PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Phone:ProximityWakeLock"
        )
    }

    fun updateProximitySensor(
        hasActiveOrOutgoingCall: Boolean,
        audioRoute: AudioRoute,
        isVideo: Boolean
    ) {
        val shouldBeActive = hasActiveOrOutgoingCall &&
                audioRoute != AudioRoute.SPEAKER &&
                audioRoute != AudioRoute.BLUETOOTH &&
                !isVideo

        if (shouldBeActive) {
            if (proximityWakeLock?.isHeld == false) {
                proximityWakeLock?.acquire(1 * 60 * 60 * 1000L)
            }
        } else {
            if (proximityWakeLock?.isHeld == true) {
                proximityWakeLock?.release()
            }
        }
    }

    fun release() {
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock?.release()
        }
        proximityWakeLock = null
    }
}
