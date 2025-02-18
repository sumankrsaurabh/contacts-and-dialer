package com.coderon.phone.ui.utils.extentions

import android.os.Build
import android.telecom.Call
import android.util.Log

fun Call?.getCallState(): Int {
    val state = when {
        this == null -> Call.STATE_DISCONNECTED
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> details.state
        else -> state
    }
    Log.d("CallExtensions", "Call state retrieved: $state")
    return state
}

fun Call.hasCapability(capability: Int): Boolean {
    val result = (this.details?.callCapabilities?.and(capability)) != 0
    Log.d("CallExtensions", "Call has capability $capability: $result")
    return result
}

fun Call.isConference(): Boolean {
    val result = this.details?.hasProperty(Call.Details.PROPERTY_CONFERENCE) == true
    Log.d("CallExtensions", "Is conference call: $result")
    return result
}

fun Call.isOutgoing(): Boolean {
    val direction = details.callDirection
    val result = direction == Call.Details.DIRECTION_OUTGOING
    Log.d("CallExtensions", "Is outgoing call: $result (Direction: $direction)")
    return result
}

fun Call.getCallerNumber(): String? {
    val number = details.handle?.schemeSpecificPart
    Log.d("CallExtensions", "Caller number: $number")
    return number
}

fun Call.getCallerName(): String? {
    val name = details.callerDisplayName ?: getCallerNumber()
    Log.d("CallExtensions", "Caller name: $name")
    return name
}

enum class State() {
    IDLE, RINGING, CONNECTING, ACTIVE, ENDED, DIALING, DISCONNECTING
}