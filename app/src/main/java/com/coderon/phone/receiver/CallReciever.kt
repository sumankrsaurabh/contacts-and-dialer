package com.coderon.phone.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coderon.phone.services.MyConnection

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phoneNumber = intent.getStringExtra("PHONE_NUMBER") ?: return

        when (intent.action) {
            "ACCEPT_CALL" -> {
                val call = MyConnection()
                call.setActive()
            }
            "REJECT_CALL" -> {
                val call = MyConnection()
                call.onReject()
            }
        }
    }
}
