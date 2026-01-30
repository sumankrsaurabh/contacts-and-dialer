package com.coderon.phone.call.services

import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import com.coderon.phone.MainActivity
import com.coderon.phone.notifications.CallNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Thin InCallService layer.
 *
 * Responsibilities:
 * - Forward Telecom callbacks to CallManager
 * - Maintain notification lifecycle
 * - Launch UI ONLY when instructed by domain state
 */
class CallService : InCallService() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val notificationManager by lazy {
        CallNotificationManager(this)
    }

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            CallManager.onCallStateChanged(call, state)
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            CallManager.onCallStateChanged(call, call.state)
        }

        override fun onConferenceableCallsChanged(
            call: Call,
            conferenceableCalls: MutableList<Call>
        ) {
            CallManager.onCallStateChanged(call, call.state)
        }
    }

    override fun onCreate() {
        super.onCreate()
        
        // Observe UiState changes to sync notification automatically (covers timer, mute, etc.)
        serviceScope.launch {
            CallManager.uiState.collectLatest { state ->
                syncNotification()
            }
        }
    }

    /* ---------------------------------------------------
       CALL ADDED
    --------------------------------------------------- */

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)

        CallManager.setService(this)
        CallManager.onCallAdded(call)

        call.registerCallback(callback)

        maybeLaunchUi()
    }

    /* ---------------------------------------------------
       CALL REMOVED
    --------------------------------------------------- */

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)

        call.unregisterCallback(callback)
        CallManager.onCallRemoved(call)
        
        if (CallManager.uiState.value.hasNoCalls) {
            CallManager.setService(null)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    /* ---------------------------------------------------
       AUDIO STATE
    --------------------------------------------------- */

    override fun onCallAudioStateChanged(audioState: CallAudioState) {
        super.onCallAudioStateChanged(audioState)
        CallManager.onAudioStateChanged(audioState)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationManager.cancelNotification()
        CallManager.setService(null)
    }

    /* ---------------------------------------------------
       HELPERS
    --------------------------------------------------- */

    private fun syncNotification() {
        val uiState = CallManager.uiState.value

        if (uiState.hasNoCalls) {
            notificationManager.cancelNotification()
            return
        }

        val notification = notificationManager.setupNotification()
        if (notification != null) {
            // Android 14 requires specifying foreground service type
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    CallNotificationManager.CALL_NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL or 
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or 
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
                )
            } else {
                startForeground(CallNotificationManager.CALL_NOTIFICATION_ID, notification)
            }
        }
    }

    private fun maybeLaunchUi() {
        val uiState = CallManager.uiState.value
        if (!uiState.shouldLaunchUi) return

        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                action = CallNotificationManager.ACTION_SHOW_CALL
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                )
                putExtra(CallNotificationManager.EXTRA_SHOW_CALL, true)
            }
            startActivity(intent)
        } catch (_: Exception) {
            // Notification is fallback
        }
    }
}
