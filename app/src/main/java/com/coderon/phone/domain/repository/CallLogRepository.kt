package com.coderon.phone.domain.repository

import com.coderon.phone.data.model.CallLog

interface CallLogRepository {
    suspend fun getCallLogs(): List<CallLog>
    suspend fun addCallLog(callLog: CallLog)
}
