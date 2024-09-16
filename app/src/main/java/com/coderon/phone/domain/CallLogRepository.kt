package com.coderon.phone.domain

import com.coderon.phone.data.modal.CallLog

interface CallLogRepository {
    suspend fun getCallLogs(): List<CallLog>
}
