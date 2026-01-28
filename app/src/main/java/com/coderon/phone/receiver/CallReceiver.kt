package com.coderon.phone.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.TelephonyManager
import com.coderon.phone.MainActivity
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.notifications.CallNotificationManager
import com.coderon.phone.ui.utils.extentions.checkPermissions
import com.coderon.phone.utils.Constants.ACCEPT_CALL
import com.coderon.phone.utils.Constants.ACTION_UPDATE_CALL_NOTIFICATION
import com.coderon.phone.utils.Constants.CALLBACK_MISSED_CALL
import com.coderon.phone.utils.Constants.DECLINE_CALL
import com.coderon.phone.utils.Constants.SEND_MESSAGE
import com.coderon.phone.utils.Constants.TOGGLE_HOLD
import com.coderon.phone.utils.Constants.TOGGLE_MUTE
import com.coderon.phone.utils.Constants.TOGGLE_SPEAKER

class CallReceiver : BroadcastReceiver() {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        if (!context.checkPermissions(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.CALL_PHONE
            )
        ) {
            return
        }

        val phoneNumber = intent.getStringExtra("PHONE_NUMBER")

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
            TOGGLE_HOLD -> handleToggleHold(context)
            CALLBACK_MISSED_CALL -> handleCallback(context, phoneNumber)
            SEND_MESSAGE -> handleSendMessage(context, phoneNumber)
            ACTION_UPDATE_CALL_NOTIFICATION -> updateCallNotification(context)
        }
    }

    private fun handleAcceptCall(context: Context) {
        CallManager.accept()
        launchCallScreen(context) // Answer and immediately show UI
        updateCallNotification(context)
    }

    private fun handleRejectCall(context: Context) {
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

    private fun handleToggleHold(context: Context) {
        val uiState = CallManager.uiState.value
        if (uiState.primaryCall?.state == com.coderon.phone.call.domain.CallState.HOLDING) {
            CallManager.unhold()
        } else {
            CallManager.hold()
        }
        updateCallNotification(context)
    }

    private fun handleCallback(context: Context, number: String?) {
        if (number == null) return
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$number")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        CallNotificationManager(context).cancelMissedCallNotification()
    }

    private fun handleSendMessage(context: Context, number: String?) {
        if (number == null) return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$number")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        CallNotificationManager(context).cancelMissedCallNotification()
    }

    private fun updateCallNotification(context: Context) {
        CallNotificationManager(context).setupNotification()
    }

    private fun launchCallScreen(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "com.coderon.phone.ACTION_SHOW_CALL"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_SHOW_CALL", true)
        }
        context.startActivity(intent)
    }
}
