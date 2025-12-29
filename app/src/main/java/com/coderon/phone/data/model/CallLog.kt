package com.coderon.phone.data.model

import java.lang.System.currentTimeMillis

data class CallLog(
    val id: Long = 0L,

    // If the number is saved, contact will exist
    val contact: Contact? = null,

    val phoneNumber: String,

    val callType: CallType,

    // duration in seconds (better than string)
    val callDurationSeconds: Int = 0,

    // timestamp
    val callTime: Long = currentTimeMillis(),

    // SIM support (dual SIM phones)
    val simSlot: Int = 1,

    val isRead: Boolean = true,

    // Voicemail support
    val isVoicemail: Boolean = false
)

fun defaultCallLog(
    phoneNumber: String = "7808140285",
    callType: CallType = CallType.OUTGOING
): CallLog {
    return CallLog(
        phoneNumber = phoneNumber,
        callType = callType
    )
}

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    VOICEMAIL,
    UNKNOWN
}
