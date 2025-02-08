package com.coderon.phone.data.model

data class CallLog(
    val id: Long,
    val contact: Contact?,
    val phoneNumber: String,
    val callType: CallType,
    val callDuration: String,
    val callTime: Long
)

enum class CallType {
    INCOMING, OUTGOING, MISSED, REJECTED, UNKNOWN
}
