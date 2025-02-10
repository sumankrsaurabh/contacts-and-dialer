package com.coderon.phone.call

import android.content.ComponentName
import android.content.Context
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.coderon.phone.call.services.MyConnectionService
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.repository.BlockedNumberRepository
import com.coderon.phone.data.repository.VoicemailRepository
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.utils.VoicemailRecorder

class CallManager(
    private val context: Context,
    private val blockedNumberRepository: BlockedNumberRepository,
    private val voicemailRepository: VoicemailRepository,
    private val voicemailRecorder: VoicemailRecorder,
    private val callLogRepository: CallLogRepository
) {

    private val telecomManager: TelecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    fun registerPhoneAccount() {
        val phoneAccountHandle = PhoneAccountHandle(
            ComponentName(context, MyConnectionService::class.java),
            "MyPhoneAccount"
        )
        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "My Dialer")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }


    // Handles an incoming call
    suspend fun handleIncomingCall(phoneNumber: String): Boolean {
        if (blockedNumberRepository.isBlocked(phoneNumber)) {
            rejectCall(phoneNumber)
            return false
        }
        // Show incoming call UI (Handled by TelecomManager or ConnectionService)
        return true
    }

    // Rejects a call
    fun rejectCall(phoneNumber: String) {
        // Logic to reject call
        println("Call rejected from: $phoneNumber")
    }

    // Handles missed calls
    suspend fun handleMissedCall(phoneNumber: String) {
        callLogRepository.addCallLog(
            CallLog(
                phoneNumber = phoneNumber,
                callType = CallType.MISSED,
                callDuration = 0.toString(),
                callTime = System.currentTimeMillis(),
                contact = null,
                id = 0
            )
        )
        println("Missed call from: $phoneNumber")
    }

    // Initiates an audio call
    fun makeAudioCall(phoneNumber: String) {
        val uri = android.net.Uri.fromParts("tel", phoneNumber, null)
        val intent = android.content.Intent(android.content.Intent.ACTION_CALL, uri)
        context.startActivity(intent)
    }

    // Initiates a video call
    fun makeVideoCall(phoneNumber: String) {
        val uri = android.net.Uri.fromParts("tel", phoneNumber, null)
        val intent = android.content.Intent(android.content.Intent.ACTION_CALL, uri)
        intent.putExtra("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", 1)
        context.startActivity(intent)
    }

    // Initiates a VoIP call
    fun makeVoipCall(sipAddress: String) {
        // Implement SIP/VoIP calling logic here
        println("Making VoIP call to: $sipAddress")
    }

    // Accepts an incoming call
    fun acceptCall() {
        // Logic to accept the call
        println("Call accepted")
    }

    // Ends a call
    fun endCall() {
        // Logic to end the call
        println("Call ended")
    }

    // Holds a call
    fun holdCall() {
        // Logic to hold the call
        println("Call on hold")
    }

    // Unholds a call
    fun unholdCall() {
        // Logic to unhold the call
        println("Call resumed")
    }

    // Mutes a call
    fun muteCall() {
        // Logic to mute the call
        println("Call muted")
    }

    // Unmutes a call
    fun unmuteCall() {
        // Logic to unmute the call
        println("Call unmuted")
    }

    // Toggles speakerphone mode
    fun toggleSpeakerphone() {
        // Logic to enable/disable speakerphone
        println("Speakerphone toggled")
    }

    // Starts call recording
    fun recordCall() {
//        voicemailRecorder.startRecording()
        println("Call recording started")
    }

    // Stops call recording
    fun stopRecordingCall() {
        voicemailRecorder.stopRecording()
        println("Call recording stopped")
    }

    // Adds a call to the call log
    suspend fun addToCallLog(callLog: CallLog) {
        callLogRepository.addCallLog(callLog)
        println("Call log added: $callLog")
    }

    // Retrieves all call logs
    suspend fun getCallLogs(): List<CallLog> {
        return callLogRepository.getCallLogs()
    }
}
