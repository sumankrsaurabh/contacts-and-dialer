package com.coderon.phone.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager
import com.coderon.phone.call.CallConnectionService

object PhoneAppManager {
    fun requestDefaultPhoneApp(activity: Activity) {
        val telecomManager = activity.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(activity, CallConnectionService::class.java)

        if (telecomManager.defaultDialerPackage != activity.packageName) {
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, activity.packageName)
            }
            activity.startActivityForResult(intent, 1001)
        }
    }
}
