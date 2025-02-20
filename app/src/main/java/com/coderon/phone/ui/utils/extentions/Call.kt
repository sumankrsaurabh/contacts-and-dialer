package com.coderon.phone.ui.utils.extentions

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
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

enum class AudioRoute(val value: Int) {
    SPEAKER(CallAudioState.ROUTE_SPEAKER),
    EARPIECE(CallAudioState.ROUTE_EARPIECE),
    BLUETOOTH(CallAudioState.ROUTE_BLUETOOTH),
    WIRED_HEADSET(CallAudioState.ROUTE_WIRED_HEADSET)
}


fun isBluetoothAvailable(context: Context): Boolean {
    val devices = context.audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)

    return devices.any {
        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO || it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
    }
}

/** Formats duration in HH:mm:ss */
fun Long.formatCallDuration(): String {
    val minutes = (this % 3600) / 60
    val sec = this % 60
    return String.format("%02d:%02d", minutes, sec)
}