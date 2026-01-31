package com.coderon.phone.call.ui

import android.telecom.VideoProfile
import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.call.domain.CallState

/**
 * Single source of truth for Call UI.
 */
data class CallUiState(

    /* ---------------- CALLS ---------------- */

    val primaryCall: CallSession? = null,
    val secondaryCall: CallSession? = null,

    /* ---------------- FLAGS ---------------- */

    val isConference: Boolean = false,
    val isMuted: Boolean = false,
    val isVideo: Boolean = false,
    val isRecording: Boolean = false,

    /* ---------------- AUDIO ---------------- */

    val audioRoute: AudioRoute = AudioRoute.EARPIECE,

    /* ---------------- TIMER ---------------- */

    val callDurationSeconds: Long = 0L,

    /* ---------------- VIDEO ---------------- */
    val peerWidth: Int = 0,
    val peerHeight: Int = 0,
    val videoQuality: Int = 0,
    val dataUsage: Long = 0L,
    val maxZoom: Float = 1.0f,
    val incomingVideoUpgradeRequest: VideoProfile? = null,
    val isFrontCamera: Boolean = true,
    val cameraUpdateTick: Int = 0,
    val userWantsVideo: Boolean = false,

    /* ---------------- SCREEN ---------------- */

    val screen: CallScreenType = CallScreenType.NONE,

    /* ---------------- SUMMARY ---------------- */
    val lastCallSummary: CallSummary? = null
) {

    data class CallSummary(
        val phoneNumber: String,
        val durationSeconds: Long,
        val isIncoming: Boolean,
        val timestamp: Long
    )

    val hasNoCalls: Boolean
        get() = primaryCall == null && secondaryCall == null

    val isIncoming: Boolean
        get() = primaryCall?.state == CallState.RINGING || secondaryCall?.state == CallState.RINGING

    val isOngoing: Boolean
        get() = primaryCall?.state == CallState.ACTIVE || primaryCall?.state == CallState.DIALING || primaryCall?.state == CallState.CONNECTING

    val isCallWaiting: Boolean
        get() = primaryCall != null && secondaryCall != null && !isConference

    val shouldLaunchUi: Boolean
        get() = primaryCall != null
}

enum class CallScreenType {
    INCOMING,
    ONGOING,
    CALL_WAITING,
    CONFERENCE,
    TWO_CALLS,
    VIDEO,
    NONE
}

enum class AudioRoute {
    EARPIECE,
    SPEAKER,
    BLUETOOTH,
    WIRED
}
