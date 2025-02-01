package com.coderon.phone.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.coderon.phone.data.repository.BlockedNumberDatabase
import java.lang.reflect.Method

class CallBlockerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (state == TelephonyManager.EXTRA_STATE_RINGING && phoneNumber != null) {
                val blockedDB = BlockedNumberDatabase(context)
                if (blockedDB.isBlocked(phoneNumber)) {
                    endCall(context)
                }
            }
        }
    }

    private fun endCall(context: Context) {
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val clazz = Class.forName(telephonyManager.javaClass.name)
            val method: Method = clazz.getDeclaredMethod("getITelephony")
            method.isAccessible = true
            val telephonyInterface = method.invoke(telephonyManager)
            val endCallMethod: Method = telephonyInterface.javaClass.getDeclaredMethod("endCall")
            endCallMethod.invoke(telephonyInterface)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
