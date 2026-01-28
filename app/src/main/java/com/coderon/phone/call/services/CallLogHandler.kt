package com.coderon.phone.call.services

import android.telecom.InCallService
import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.data.model.CallType
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.notifications.CallNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.coderon.phone.data.model.CallLog as CallLogData

class CallLogHandler(
    private val callLogRepository: CallLogRepository,
    private val scope: CoroutineScope
) {
    fun saveCallLog(inCallService: InCallService?, session: CallSession, startTime: Long?) {
        val duration = if (startTime != null) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }

        val callType = when {
            session.isIncoming && session.state.isEnded && duration == 0 -> CallType.MISSED
            session.isIncoming -> CallType.INCOMING
            else -> CallType.OUTGOING
        }

        if (callType == CallType.MISSED) {
            inCallService?.let {
                CallNotificationManager(it).showMissedCallNotification(
                    session.displayName, session.phoneNumber
                )
            }
        }

        scope.launch(Dispatchers.IO) {
            callLogRepository.addCallLog(
                CallLogData(
                    phoneNumber = session.phoneNumber,
                    callType = callType,
                    callDurationSeconds = duration,
                    callTime = System.currentTimeMillis()
                )
            )
        }
    }
}
