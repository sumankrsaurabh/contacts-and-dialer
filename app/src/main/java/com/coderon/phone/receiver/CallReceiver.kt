package com.coderon.phone.receiver

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telecom.TelecomManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.coderon.phone.call.services.MyConnection
import com.coderon.phone.notifications.CallNotificationService

class CallReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.P)
    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_NEW_OUTGOING_CALL -> {
                val phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
                phoneNumber?.let {
                    handleOutgoingCall(context, it)
                }
            }
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                incomingNumber?.let {
                    handleIncomingCall(context, state, it)
                }
            }
            "ACCEPT_CALL" -> {
                val phoneNumber = intent.getStringExtra("PHONE_NUMBER")
                phoneNumber?.let { answerCall(context, it) }
            }
            "REJECT_CALL" -> {
                val phoneNumber = intent.getStringExtra("PHONE_NUMBER")
                phoneNumber?.let { rejectCall(context, it) }
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private fun handleIncomingCall(context: Context, state: String?, phoneNumber: String) {
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Show incoming call UI
                CallNotificationService.showIncomingCallNotification(context, phoneNumber)
//                CallNotificationService.showIncomingCallScreen(context, phoneNumber)
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Call answered
                CallNotificationService.showOngoingCallNotification(context, phoneNumber)
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Call ended
                CallNotificationService.dismissNotification(context)
            }
        }
    }

    private fun handleOutgoingCall(context: Context, phoneNumber: String) {
      /*  // Show outgoing call UI
        val intent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("CALLER_NUMBER", phoneNumber)
        }
        context.startActivity(intent)*/
    }

    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    private fun answerCall(context: Context, phoneNumber: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val telecomManager = context.getSystemService(TelecomManager::class.java)
            telecomManager?.acceptRingingCall()
        } else {
            val call = MyConnection()
            call.setActive()
        }
    }

    @RequiresPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    private fun rejectCall(context: Context, phoneNumber: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecomManager = context.getSystemService(TelecomManager::class.java)
            telecomManager?.endCall()
        } else {
            val call = MyConnection()
            call.onReject()
        }
    }
}
