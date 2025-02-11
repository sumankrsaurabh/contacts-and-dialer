package com.coderon.phone.call.extentions

import android.os.Build
import android.telecom.Call
import android.telecom.Call.STATE_CONNECTING
import android.telecom.Call.STATE_DIALING
import android.telecom.Call.STATE_SELECT_PHONE_ACCOUNT

private val OUTGOING_CALL_STATES =
    arrayOf(STATE_CONNECTING, STATE_DIALING, STATE_SELECT_PHONE_ACCOUNT)

@Suppress("DEPRECATION")
fun Call?.getStateCompat(): Int {
    return when {
        this == null -> Call.STATE_DISCONNECTED
        isSPlus() -> details.state
        else -> state
    }
}

fun Call?.getCallDuration(): Int {
    return if (this != null) {
        val connectTimeMillis = details.connectTimeMillis
        if (connectTimeMillis == 0L) {
            return 0
        }
        ((System.currentTimeMillis() - connectTimeMillis) / 1000).toInt()
    } else {
        0
    }
}

fun Call.isOutgoing(): Boolean {
    return if (isQPlus()) {
        details.callDirection == Call.Details.DIRECTION_OUTGOING
    } else {
        OUTGOING_CALL_STATES.contains(getStateCompat())
    }
}

fun Call.hasCapability(capability: Int): Boolean = (details.callCapabilities and capability) != 0

fun Call?.isConference(): Boolean =
    this?.details?.hasProperty(Call.Details.PROPERTY_CONFERENCE) == true

fun isQPlus(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
}
fun isSPlus(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
}