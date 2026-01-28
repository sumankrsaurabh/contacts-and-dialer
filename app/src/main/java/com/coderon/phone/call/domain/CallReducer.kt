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

        // Determine if this is a video call according to the system
        val isSystemVideo = calls.any { it.call.details.isVideoCall }
        
        // Determine if we should stay in video UI. 
        // We stay in video UI if the system says so, OR if the user recently intended to be in video
        // and we are in a state where video is possible (ACTIVE or DIALING).
        val isVideo = isSystemVideo || (previous.userWantsVideo && (active != null || dialing != null))

        // Determine if we are in a conference
        val isConference = calls.any { it.call.details.hasProperty(Call.Details.PROPERTY_CONFERENCE) }

        return when {
            // Case 1: Incoming Call (with or without other calls)
            ringing != null -> {
                if (active != null || holding != null || dialing != null) {
                    previous.copy(
                        primaryCall = active ?: holding ?: dialing,
                        secondaryCall = ringing,
                        screen = CallScreenType.CALL_WAITING
                    )
                } else {
                    previous.copy(
                        primaryCall = ringing,
                        screen = CallScreenType.INCOMING
                    )
                }
            }

            // Case 2: Conference Call
            isConference -> {
                previous.copy(
                    primaryCall = active ?: holding ?: dialing ?: calls.first(),
                    secondaryCall = calls.getOrNull(1),
                    isConference = true,
                    screen = CallScreenType.CONFERENCE,
                    userWantsVideo = false
                )
            }

            // Case 3: Video Call (Single or Primary)
            isVideo && (active != null || dialing != null) -> {
                previous.copy(
                    primaryCall = active ?: dialing,
                    screen = CallScreenType.VIDEO,
                    isVideo = true,
                    userWantsVideo = true
                )
            }

            // Case 4: Two Calls (Any combination of Active, Holding, Dialing)
            (active != null && holding != null) || 
            (dialing != null && holding != null) || 
            (active != null && dialing != null) -> {
                val primary = dialing ?: active ?: holding!!
                val secondary = if (primary == dialing) (active ?: holding!!) else (holding ?: dialing!!)
                
                previous.copy(
                    primaryCall = primary,
                    secondaryCall = secondary,
                    screen = CallScreenType.TWO_CALLS,
                    userWantsVideo = false
                )
            }

            // Case 5: Single Call (Audio or Video fallback)
            dialing != null -> {
                previous.copy(
                    primaryCall = dialing,
                    screen = CallScreenType.ONGOING,
                    userWantsVideo = false
                )
            }
            active != null -> {
                previous.copy(
                    primaryCall = active,
                    screen = CallScreenType.ONGOING,
                    userWantsVideo = isSystemVideo // Keep it true if system says it's video, even if not in VIDEO screen
                )
            }
            holding != null -> {
                previous.copy(
                    primaryCall = holding,
                    screen = CallScreenType.ONGOING,
                    userWantsVideo = false
                )
            }

            else -> {
                val anyCall = calls.firstOrNull()
                if (anyCall != null) {
                    previous.copy(
                        primaryCall = anyCall,
                        screen = CallScreenType.ONGOING,
                        userWantsVideo = false
                    )
                } else {
                    CallUiState()
                }
            }
        }
    }

    private val Call.Details.isVideoCall: Boolean
        get() = VideoProfile.isVideo(videoState)
}
