package com.coderon.phone.data.model

import java.lang.System.currentTimeMillis

data class CallLog(
    val id: Long,
    val contact: Contact?,
    val phoneNumber: String,
    val callType: CallType,
    val callDuration: String,
    val callTime: Long
)

fun defaultCallLog(): CallLog {
    return CallLog(
        id = 0L,
        contact = null,
        phoneNumber = "7808140285",
        callType = CallType.OUTGOING,
        callDuration = "000",
        callTime = currentTimeMillis()
    )
}


enum class CallType {
    INCOMING, OUTGOING, MISSED, REJECTED, UNKNOWN
}
