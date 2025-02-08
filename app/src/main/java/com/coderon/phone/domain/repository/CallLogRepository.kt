package com.coderon.phone.domain.repository

import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType

interface CallLogRepository {
    suspend fun getCallLogs(): List<CallLog>
    suspend fun saveCallLog(phoneNumber: String, callType: CallType, duration: Long)
}
