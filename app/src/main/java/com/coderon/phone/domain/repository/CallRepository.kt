package com.coderon.phone.domain.repository

import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.data.modal.CallType
import kotlinx.coroutines.flow.Flow

interface CallRepository {
    fun makeCall(phoneNumber: String)
    fun endCall()
    fun answerCall()
    fun getCallLogs(): Flow<List<CallLog>>
    suspend fun saveCallLog(phoneNumber: String, callType: CallType, duration: Long)
}
