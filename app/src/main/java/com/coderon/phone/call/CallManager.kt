package com.coderon.phone.call

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Icon
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import com.coderon.phone.R
import com.coderon.phone.data.model.Voicemail
import com.coderon.phone.data.repository.BlockedNumberRepository
import com.coderon.phone.data.repository.VoicemailRepository
import com.coderon.phone.services.MyConnectionService
import com.coderon.phone.utils.VoicemailRecorder
import com.coderon.phone.utils.sendVoicemailNotification
import kotlinx.coroutines.delay

class CallManager(
    private val blockedNumberRepository: BlockedNumberRepository,
    private val voicemailRepository: VoicemailRepository,
    private val voicemailRecorder: VoicemailRecorder,
    private val context: Context
) {
    @OptIn(ExperimentalMaterial3Api::class)
    fun registerPhoneAccount(context: Context) {
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val handle = PhoneAccountHandle(
            ComponentName(context, MyConnectionService::class.java),
            "MyPhoneApp"
        )

        val phoneAccount = PhoneAccount.builder(handle, "My Phone App")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .setIcon(Icon.createWithResource(context, R.drawable.call))
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    suspend fun handleIncomingCall(phoneNumber: String): Boolean {
        val isBlocked = blockedNumberRepository.isBlocked(phoneNumber)

        return if (isBlocked) {
            rejectCall(phoneNumber)
            false // Call should not be answered
        } else {
            true // Allow the call
        }
    }

    private fun rejectCall(phoneNumber: String) {
        Log.d("CallManager", "Blocked incoming call from: $phoneNumber")
        // Use TelecomManager to reject the call (for Android 9+)
    }

    suspend fun handleMissedCall(phoneNumber: String) {
        val voicemailPath = voicemailRecorder.startRecording(phoneNumber)
        delay(30000) // Record for 30 seconds
        voicemailRecorder.stopRecording()

        val voicemail = Voicemail(
            callerNumber = phoneNumber,
            filePath = voicemailPath,
            timestamp = System.currentTimeMillis()
        )
        voicemailRepository.saveVoicemail(voicemail)

        sendVoicemailNotification(
            phoneNumber = phoneNumber, context = context
        )
    }

}
