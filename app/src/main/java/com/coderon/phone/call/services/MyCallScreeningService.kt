package com.coderon.phone.call.services

import android.telecom.Call
import android.telecom.CallScreeningService
import com.coderon.phone.data.repository.BlockedNumberRepository
import com.coderon.phone.data.repository.SettingsRepository
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Handles incoming call screening.
 * 
 * Responsibilities:
 * - Check if the caller is in the blocked list.
 * - Decide whether to allow, reject, or silence the call based on user settings.
 */
class CallScreeningService : CallScreeningService(), KoinComponent {

    private val blockedNumberRepository: BlockedNumberRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val contactRepository: ContactRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: ""

        serviceScope.launch {
            val isBlocked = if (phoneNumber.isNotBlank()) blockedNumberRepository.isBlocked(phoneNumber) else false
            
            if (isBlocked) {
                rejectCall(callDetails)
                return@launch
            }

            // Check "Block Unknown Numbers" setting
            val blockUnknown = settingsRepository.blockUnknownNumbers.first()
            if (blockUnknown && phoneNumber.isNotBlank()) {
                val isContact = contactRepository.getContactByNumber(phoneNumber) != null
                if (!isContact) {
                    rejectCall(callDetails)
                    return@launch
                }
            }

            // Check "Spam Protection" (Mock logic for now)
            val spamProtection = settingsRepository.spamProtectionEnabled.first()
            if (spamProtection && isSpam(phoneNumber)) {
                rejectCall(callDetails)
                return@launch
            }

            allowCall(callDetails)
        }
    }

    private fun isSpam(number: String): Boolean {
        // Simple mock: numbers starting with certain prefixes could be flagged
        return number.startsWith("+1800") || number.startsWith("000")
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
