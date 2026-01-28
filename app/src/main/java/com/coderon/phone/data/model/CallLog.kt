package com.coderon.phone.data.model

import java.lang.System.currentTimeMillis

data class CallLog(
    val id: Long = 0L,

    // If the number is saved, contact will exist
    val contact: Contact? = null,

    val phoneNumber: String,

    val callType: CallType,

    // duration in seconds
    val callDurationSeconds: Int = 0,

    // timestamp
    val callTime: Long = currentTimeMillis(),

    // SIM support
    val simSlot: Int = 1,

    val isRead: Boolean = true,

    // Voicemail support
    val isVoicemail: Boolean = false
)

enum class CallType {
    INCOMING, OUTGOING, MISSED, REJECTED, BLOCKED, VOICEMAIL, UNKNOWN
}
