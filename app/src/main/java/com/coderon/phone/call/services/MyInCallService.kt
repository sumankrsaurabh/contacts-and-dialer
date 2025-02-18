package com.coderon.phone.call.services

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.coderon.phone.MainActivity
import com.coderon.phone.notifications.CallNotificationManager
import com.coderon.phone.ui.utils.extentions.isOutgoing

class CallService : InCallService() {
    private val callNotificationManager by lazy { CallNotificationManager(this) }

    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state in listOf(Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING)) {
                callNotificationManager.cancelNotification()
            } else {
                callNotificationManager.setupNotification()
            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.addCall(call, this)
        call.registerCallback(callListener)

        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        val isScreenLocked = keyguardManager.isDeviceLocked

        // Open MainActivity only when necessary
        if (call.isOutgoing() || isScreenLocked) {
            try {
                callNotificationManager.setupNotification(true)
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                startActivity(intent)
            } catch (e: Exception) {
                callNotificationManager.setupNotification()
            }
        } else {
            callNotificationManager.setupNotification()
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callListener)
        CallManager.removeCall(call)

        if (CallManager.hasNoCalls()) {
            callNotificationManager.cancelNotification()
        } else {
            callNotificationManager.setupNotification()
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        audioState?.let { CallManager.updateAudioState(it) }
    }

    override fun onDestroy() {
        super.onDestroy()
        callNotificationManager.cancelNotification()
    }
}
