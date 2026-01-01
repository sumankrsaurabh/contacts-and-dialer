package com.coderon.phone.call.domain

enum class CallState {
    IDLE,
    DIALING,
    CONNECTING,
    RINGING,
    ACTIVE,
    HOLDING,
    DISCONNECTING,
    ENDED;

    /* ---------- HELPERS ---------- */

    val isIncoming get() = this == RINGING
    val isOutgoing get() = this == DIALING || this == CONNECTING
    val isActive get() = this == ACTIVE
    val isHolding get() = this == HOLDING
    val isEnded get() = this == ENDED
}
