package com.coderon.phone.call.ui

import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.call.domain.CallState

/**
 * Single source of truth for Call UI.
 *
 * This state is:
 * - Produced ONLY by CallReducer
 * - Observed by UI & notification layer
 * - Immutable & predictable
 */
data class CallUiState(

    /* ---------------- CALLS ---------------- */

    val primaryCall: CallSession? = null,
    val secondaryCall: CallSession? = null,

    /* ---------------- FLAGS ---------------- */

    val isConference: Boolean = false,
    val isMuted: Boolean = false,

    /* ---------------- AUDIO ---------------- */

    val audioRoute: AudioRoute = AudioRoute.EARPIECE,

    /* ---------------- TIMER ---------------- */

    val callDurationSeconds: Long = 0L,

    /* ---------------- SCREEN ---------------- */

    val screen: CallScreenType = CallScreenType.NONE
) {

    /* ---------------------------------------------------
       DERIVED UI HELPERS (NO REDUCER LOGIC)
    --------------------------------------------------- */

    val hasNoCalls: Boolean
        get() = primaryCall == null && secondaryCall == null

    val CallUiState.hasNoCalls: Boolean
        get() = primaryCall == null && secondaryCall == null


    val isIncoming: Boolean
        get() = primaryCall?.state == CallState.RINGING

    val isOngoing: Boolean
        get() = primaryCall?.state == CallState.ACTIVE

    val isCallWaiting: Boolean
        get() = primaryCall != null && secondaryCall != null && !isConference

    /**
     * Used by CallService to decide if UI should launch
     */
    val shouldLaunchUi: Boolean
        get() = isIncoming || isOngoing || isCallWaiting || isConference
}

/* ---------------------------------------------------
   SCREEN TYPES
--------------------------------------------------- */

enum class CallScreenType {
    INCOMING,
    ONGOING,
    CALL_WAITING,
    CONFERENCE,
    NONE
}

/* ---------------------------------------------------
   AUDIO ROUTES (UI-LEVEL, PLATFORM AGNOSTIC)
--------------------------------------------------- */

enum class AudioRoute {
    EARPIECE,
    SPEAKER,
    BLUETOOTH,
    WIRED
}
