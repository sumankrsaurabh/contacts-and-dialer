package com.coderon.phone.call.domain

import android.telecom.Call

data class CallSession(
    val id: String,

    // Telecom reference (never exposed to UI)
    val call: Call,

    // Domain state
    val state: CallState,

    // Normalized caller info (UI-safe)
    val phoneNumber: String,
    val displayName: String? = null,

    // Flags
    val isIncoming: Boolean,
    val isConference: Boolean = false
)
