package com.coderon.phone.utils

import android.content.Context
import android.content.Intent
import android.telecom.TelecomManager

object DialerUtils {

    /**
     * Checks if this app is the default phone (dialer) app.
     */
    fun isDefaultDialer(context: Context): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        return telecomManager.defaultDialerPackage == context.packageName
    }

    /**
     * Prompts the user to set this app as the default phone (dialer) app.
     */
    fun setAsDefaultDialer(context: Context) {
        if (!isDefaultDialer(context)) { // Only ask if not already default
            val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).apply {
                putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, context.packageName)
            }
            context.startActivity(intent)
        }
    }
}

