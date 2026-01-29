package com.coderon.phone.ui.utils.extentions

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.Call
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import java.util.Locale

enum class State {
    CONNECTING, ACTIVE, DIALING, DISCONNECTING, HOLD
}

/** Formats duration in HH:mm:ss */
fun Long.formatCallDuration(): String {
    val minutes = (this % 3600) / 60
    val sec = this % 60
    return String.format(Locale.US, "%02d:%02d", minutes, sec)
}

@SuppressLint("MissingPermission")
fun getCallType(context: Context): String {
    val telephonyManager = context.getSystemService(TelephonyManager::class.java) ?: return ""
    @Suppress("DEPRECATION")
    val networkType = telephonyManager.voiceNetworkType
    return when (networkType) {
        TelephonyManager.NETWORK_TYPE_LTE -> "VoLTE"
        TelephonyManager.NETWORK_TYPE_NR -> "5G"
        else -> ""
    }
}

@SuppressLint("MissingPermission")
fun Call.getSimInfoForCall(context: Context): String {
    val handle = this.details.accountHandle ?: return "Unknown SIM"
    return handle.getSimName(context)
}

@SuppressLint("MissingPermission")
fun PhoneAccountHandle.getSimName(context: Context): String {
    val telecomManager = context.getSystemService(TelecomManager::class.java) ?: return "Unknown SIM"
    val subscriptionManager = context.getSystemService(SubscriptionManager::class.java) ?: return "Unknown SIM"

    val phoneAccount = telecomManager.getPhoneAccount(this)
    
    // Attempt to get subscription ID from the account handle ID
    val subscriptionId = this.id?.toIntOrNull()

    val subscriptionInfo = if (subscriptionId != null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            subscriptionManager.getActiveSubscriptionInfo(subscriptionId)
        } else {
            @Suppress("DEPRECATION")
            subscriptionManager.activeSubscriptionInfoList?.firstOrNull { it.subscriptionId == subscriptionId }
        }
    } else null

    return subscriptionInfo?.carrierName?.toString() ?: phoneAccount?.label?.toString() ?: "Unknown Carrier"
}
