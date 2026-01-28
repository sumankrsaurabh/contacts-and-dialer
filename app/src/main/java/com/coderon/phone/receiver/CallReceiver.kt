package com.coderon.phone.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.coderon.phone.MainActivity
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.notifications.CallNotificationManager
import com.coderon.phone.ui.utils.extentions.checkPermissions
import com.coderon.phone.utils.Constants.ACCEPT_CALL
import com.coderon.phone.utils.Constants.ACTION_UPDATE_CALL_NOTIFICATION
import com.coderon.phone.utils.Constants.DECLINE_CALL
import com.coderon.phone.utils.Constants.TOGGLE_MUTE
import com.coderon.phone.utils.Constants.TOGGLE_SPEAKER
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(Dispatchers.Main)

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (!context.checkPermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE
            )
        ) {
            return
        }

        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                    launchCallScreen(context)
                }
            }

            ACCEPT_CALL -> handleAcceptCall(context)
            DECLINE_CALL -> handleRejectCall(context)
            TOGGLE_MUTE -> handleToggleMute(context)
            TOGGLE_SPEAKER -> handleToggleSpeaker(context)
            ACTION_UPDATE_CALL_NOTIFICATION -> updateCallNotification(context)
        }
    }

    private fun handleAcceptCall(context: Context) {
        CallManager.accept()
        updateCallNotification(context)
    }

    private fun handleRejectCall(context: Context) {
        // Reject specifically for incoming, disconnect for ongoing
        if (CallManager.uiState.value.isIncoming) {
            CallManager.reject()
        } else {
            CallManager.disconnectPrimary()
        }
        updateCallNotification(context)
    }

    private fun handleToggleMute(context: Context) {
        CallManager.toggleMute()
        updateCallNotification(context)
    }

    private fun handleToggleSpeaker(context: Context) {
        CallManager.toggleSpeaker()
        updateCallNotification(context)
    }

    private fun updateCallNotification(context: Context) {
        CallNotificationManager(context).setupNotification()
    }

    private fun launchCallScreen(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("CALL_TYPE", "incoming")
        }
        context.startActivity(intent)
    }
}
