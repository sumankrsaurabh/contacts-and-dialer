package com.coderon.phone.call.domain

import android.telecom.Call
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.call.ui.CallUiState

object CallReducer {

    fun reduce(
        calls: List<CallSession>,
        previous: CallUiState
    ): CallUiState {

        val active = calls.firstOrNull { it.state == CallState.ACTIVE }
        val ringing = calls.firstOrNull { it.state == CallState.RINGING }
        val holding = calls.firstOrNull { it.state == CallState.HOLDING }

        return when {
            ringing != null && active != null -> {
                previous.copy(
                    primaryCall = active,
                    secondaryCall = ringing,
                    screen = CallScreenType.CALL_WAITING
                )
            }

            ringing != null -> {
                previous.copy(
                    primaryCall = ringing,
                    screen = CallScreenType.INCOMING
                )
            }

            active != null && holding != null -> {
                previous.copy(
                    primaryCall = active,
                    secondaryCall = holding,
                    isConference = active.call.details.hasProperty(
                        Call.Details.PROPERTY_CONFERENCE
                    ),
                    screen = CallScreenType.CONFERENCE
                )
            }

            active != null -> {
                previous.copy(
                    primaryCall = active,
                    screen = CallScreenType.ONGOING
                )
            }

            else -> CallUiState()
        }
    }
}
