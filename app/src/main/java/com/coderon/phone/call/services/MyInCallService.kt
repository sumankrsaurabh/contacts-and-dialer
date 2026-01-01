package com.coderon.phone.call.services

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import com.coderon.phone.MainActivity
import com.coderon.phone.notifications.CallNotificationManager

/**
 * Thin InCallService layer.
 *
 * Responsibilities:
 * - Forward Telecom callbacks to CallManager
 * - Maintain notification lifecycle
 * - Launch UI ONLY when instructed by domain state
 *
 * ❌ No business rules
 * ❌ No call logic
 * ❌ No UI decisions
 */
class CallService : InCallService() {

    private val notificationManager by lazy {
        CallNotificationManager(this)
    }

    private val callback = object : Call.Callback() {

        override fun onStateChanged(call: Call, state: Int) {
            CallManager.onCallStateChanged(call, state)
            syncNotification()
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            CallManager.onCallStateChanged(call, call.state)
            syncNotification()
        }

        override fun onConferenceableCallsChanged(
            call: Call,
            conferenceableCalls: MutableList<Call>
        ) {
            CallManager.onCallStateChanged(call, call.state)
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
        syncNotification(forceOngoing = true)
    }

    /* ---------------------------------------------------
       CALL REMOVED
    --------------------------------------------------- */

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)

        call.unregisterCallback(callback)
        CallManager.onCallRemoved(call)

        syncNotification()
    }

    /* ---------------------------------------------------
       AUDIO STATE
    --------------------------------------------------- */

    override fun onDestroy() {
        super.onDestroy()
        notificationManager.cancelNotification()
    }

    /* ---------------------------------------------------
       HELPERS
    --------------------------------------------------- */

    private fun syncNotification(forceOngoing: Boolean = false) {
        val uiState = CallManager.uiState.value

        when {
            uiState.hasNoCalls ->
                notificationManager.cancelNotification()

            else ->
                notificationManager.setupNotification(
                    showOngoing = forceOngoing || uiState.isOngoing
                )
        }
    }

    private fun maybeLaunchUi() {
        if (!CallManager.uiState.value.shouldLaunchUi) return

        try {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                }
            )
        } catch (_: Exception) {
            // Notification is fallback
        }
    }
}
