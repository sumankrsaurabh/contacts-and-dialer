package com.coderon.phone.domain

import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.data.modal.CallType

interface CallLogRepository {
    suspend fun getCallLogs(): List<CallLog>
    suspend fun saveCallLog(phoneNumber: String, callType: CallType, duration: Long)
}
