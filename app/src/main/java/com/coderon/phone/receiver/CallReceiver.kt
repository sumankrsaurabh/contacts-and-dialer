package com.coderon.phone.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coderon.phone.MainActivity
import com.coderon.phone.utils.Constants.ACCEPT_CALL

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACCEPT_CALL -> {
                val openAppIntent = Intent(context, MainActivity::class.java)
                openAppIntent.flags =
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                context.startActivity(openAppIntent)
//                CallManager.accept()
            }

//            DECLINE_CALL -> CallManager.reject()
        }
    }
}