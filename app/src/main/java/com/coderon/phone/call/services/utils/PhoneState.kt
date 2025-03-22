package com.coderon.phone.call.services.utils

import android.telecom.Call
import com.coderon.phone.ui.utils.extentions.State

/** Phone states **/
sealed class PhoneState
object NoCall : PhoneState()
data class SingleCall(val call: Call) : PhoneState()
data class TwoCalls(val active: Call, val onHold: Call) : PhoneState()


fun updateCallState(state: Int): State {
    return when (state) {
        Call.STATE_RINGING -> State.RINGING
        Call.STATE_CONNECTING -> State.CONNECTING
        Call.STATE_ACTIVE -> State.ACTIVE
        Call.STATE_DIALING -> State.DIALING
        Call.STATE_DISCONNECTING -> State.DISCONNECTING
        Call.STATE_DISCONNECTED -> State.ENDED
        Call.STATE_HOLDING -> State.HOLD
        else -> State.IDLE
    }
}
