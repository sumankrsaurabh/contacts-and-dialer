package com.coderon.phone.call.domain

import android.telecom.Call
import android.telecom.VideoProfile
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.call.ui.CallUiState

object CallReducer {

    fun reduce(
        calls: List<CallSession>,
        previous: CallUiState
    ): CallUiState {

        if (calls.isEmpty()) return CallUiState()

        val active = calls.firstOrNull { it.state == CallState.ACTIVE }
        val ringing = calls.firstOrNull { it.state == CallState.RINGING }
        val holding = calls.firstOrNull { it.state == CallState.HOLDING }
        val dialing =
            calls.firstOrNull { it.state == CallState.DIALING || it.state == CallState.CONNECTING }

        val isVideo = calls.any { it.call.details.isVideoCall }

        return when {
            // Case 1: Video Call (High Priority)
            isVideo && active != null -> {
                previous.copy(
                    primaryCall = active,
                    screen = CallScreenType.VIDEO,
                    isVideo = true
                )
            }

            // Case 2: Incoming Call while on another call (Call Waiting)
            ringing != null && (active != null || holding != null || dialing != null) -> {
                previous.copy(
                    primaryCall = active ?: holding ?: dialing,
                    secondaryCall = ringing,
                    screen = CallScreenType.CALL_WAITING
                )
            }

            // Case 3: Simple Incoming Call
            ringing != null -> {
                previous.copy(
                    primaryCall = ringing,
                    screen = CallScreenType.INCOMING
                )
            }

            // Case 4: Multiple Calls (Conference or Two Calls)
            active != null && holding != null -> {
                val isConference =
                    active.call.details.hasProperty(Call.Details.PROPERTY_CONFERENCE) ||
                            holding.call.details.hasProperty(Call.Details.PROPERTY_CONFERENCE)

                previous.copy(
                    primaryCall = active,
                    secondaryCall = holding,
                    isConference = isConference,
                    screen = if (isConference) CallScreenType.CONFERENCE else CallScreenType.TWO_CALLS
                )
            }

            // Case 5: Single Active/Dialing Call
            active != null -> {
                previous.copy(
                    primaryCall = active,
                    screen = CallScreenType.ONGOING
                )
            }

            dialing != null -> {
                previous.copy(
                    primaryCall = dialing,
                    screen = CallScreenType.ONGOING
                )
            }

            holding != null -> {
                previous.copy(
                    primaryCall = holding,
                    screen = CallScreenType.ONGOING
                )
            }

            else -> {
                val anyCall = calls.firstOrNull()
                if (anyCall != null) {
                    previous.copy(
                        primaryCall = anyCall,
                        screen = CallScreenType.ONGOING
                    )
                } else {
                    CallUiState()
                }
            }
        }
    }

    private val Call.Details.isVideoCall: Boolean
        get() = VideoProfile.isVideo(videoState)


    private fun Call.Details.can(capability: Int): Boolean {
        return (callCapabilities and capability) == capability
    }
}
