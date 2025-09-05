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

fun defaultCallLog(callType: CallType = CallType.OUTGOING): CallLog {
    return CallLog(
        id = 0L,
        contact = null,
        phoneNumber = "7808140285",
        callType = callType,
        callDuration = "000",
        callTime = currentTimeMillis()
    )
}


enum class CallType {
    INCOMING, OUTGOING, MISSED, REJECTED, UNKNOWN
}
