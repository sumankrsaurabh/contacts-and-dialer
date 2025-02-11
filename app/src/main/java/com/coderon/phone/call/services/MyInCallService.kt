package com.coderon.phone.call.services

import android.app.KeyguardManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log

class CallService : InCallService() {
//    private val callNotificationManager by lazy { CallNotificationService() }

    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
//                CallNotificationService.cancelNotification()
                Log.d("CallService","Notification created")
            } else {
//                callNotificationManager.setupNotification()
                Log.d("CallService","Notification canceled")

            }
        }
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
//        CallManager.onCallAdded(call)
//        CallManager.inCallService = this
        call.registerCallback(callListener)

        val isScreenLocked = (getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked
        /*if (!powerManager.isInteractive || call.isOutgoing() || isScreenLocked || config.alwaysShowFullscreen) {
            try {
                callNotificationManager.setupNotification(true)
                startActivity(CallActivity.getStartIntent(this))
            } catch (e: Exception) {
                // seems like startActivity can throw AndroidRuntimeException and ActivityNotFoundException, not yet sure when and why, lets show a notification
                callNotificationManager.setupNotification()
            }
        } else {
            callNotificationManager.setupNotification()
        }*/
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        call.unregisterCallback(callListener)
       /* val wasPrimaryCall = call == CallManager.getPrimaryCall()
        CallManager.onCallRemoved(call)
        if (CallManager.getPhoneState() == NoCall) {
            CallManager.inCallService = null
            callNotificationManager.cancelNotification()
        } else {
            callNotificationManager.setupNotification()
            if (wasPrimaryCall) {
                startActivity(CallActivity.getStartIntent(this))
            }
        }*/
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        if (audioState != null) {
//            CallManager.onAudioStateChanged(audioState)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        callNotificationManager.cancelNotification()
    }
}
