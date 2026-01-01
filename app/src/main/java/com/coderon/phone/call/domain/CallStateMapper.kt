package com.coderon.phone.call.domain

import android.telecom.Call

fun Int.toDomainState(): CallState = when (this) {
    Call.STATE_RINGING -> CallState.RINGING
    Call.STATE_DIALING -> CallState.DIALING
    Call.STATE_CONNECTING -> CallState.CONNECTING
    Call.STATE_ACTIVE -> CallState.ACTIVE
    Call.STATE_HOLDING -> CallState.HOLDING
    Call.STATE_DISCONNECTING -> CallState.DISCONNECTING
    Call.STATE_DISCONNECTED -> CallState.ENDED
    else -> CallState.IDLE
}
