package com.coderon.phone.domain.repository

import com.coderon.phone.data.model.CallLog
import kotlinx.coroutines.flow.Flow

interface CallLogRepository {
    fun observeCallLogs(): Flow<List<CallLog>>
    suspend fun getCallLogs(): List<CallLog>
    suspend fun addCallLog(callLog: CallLog)
    suspend fun deleteCallLog(callLog: CallLog)
}
