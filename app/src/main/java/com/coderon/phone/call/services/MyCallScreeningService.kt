package com.coderon.phone.call.services

import android.telecom.Call
import android.telecom.CallScreeningService
import com.coderon.phone.data.repository.BlockedNumberRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles incoming call screening.
 * 
 * Responsibilities:
 * - Check if the caller is in the blocked list.
 * - Decide whether to allow, reject, or silence the call.
 */
class CallScreeningService : CallScreeningService(), KoinComponent {

    private val blockedNumberRepository: BlockedNumberRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        if (phoneNumber.isBlank()) {
            allowCall(callDetails)
            return
        }

        serviceScope.launch {
            val isBlocked = blockedNumberRepository.isBlocked(phoneNumber)
            if (isBlocked) {
                rejectCall(callDetails)
            } else {
                allowCall(callDetails)
            }
        }
    }

    private fun allowCall(callDetails: Call.Details) {
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        )
    }

    private fun rejectCall(callDetails: Call.Details) {
        respondToCall(
            callDetails,
            CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .build()
        )
    }
}
