package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.call.CallManager
import com.coderon.phone.data.model.CallLog
import kotlinx.coroutines.launch

class CallViewModel(private val callManager: CallManager) : ViewModel() {

    // Function to initiate an audio call
    fun initiateCall(phoneNumber: String) {
        viewModelScope.launch {
            callManager.makeAudioCall(phoneNumber)
        }
    }

    // Function to initiate a video call
    fun initiateVideoCall(phoneNumber: String) {
        viewModelScope.launch {
            callManager.makeVideoCall(phoneNumber)
        }
    }

    // Function to initiate a VoIP call
    fun initiateVoipCall(sipAddress: String) {
        viewModelScope.launch {
            callManager.makeVoipCall(sipAddress)
        }
    }

    // Function to answer an incoming call
    fun answerCall() {
        viewModelScope.launch {
            callManager.acceptCall()
        }
    }

    // Function to decline an incoming call
    fun declineCall() {
        viewModelScope.launch {
            callManager.endCall()
        }
    }

    // Function to mute the call
    fun muteCall() {
        viewModelScope.launch {
            callManager.muteCall()
        }
    }

    // Function to unmute the call
    fun unmuteCall() {
        viewModelScope.launch {
            callManager.unmuteCall()
        }
    }

    // Function to hold a call
    fun holdCall() {
        viewModelScope.launch {
            callManager.holdCall()
        }
    }

    // Function to unhold a call
    fun unholdCall() {
        viewModelScope.launch {
            callManager.unholdCall()
        }
    }

    // Function to toggle speakerphone
    fun toggleSpeakerphone() {
        viewModelScope.launch {
            callManager.toggleSpeakerphone()
        }
    }

    // Function to start call recording
    fun startCallRecording() {
        viewModelScope.launch {
            callManager.recordCall()
        }
    }

    // Function to stop call recording
    fun stopCallRecording() {
        viewModelScope.launch {
            callManager.stopRecordingCall()
        }
    }

    // Function to fetch call logs
    suspend fun getCallLogs(): List<CallLog> {
        return callManager.getCallLogs()
    }

    /*// Function to delete a specific call log by ID
    fun deleteCallLog(id: Long) {
        viewModelScope.launch {
            callManager.deleteCallLog(id)
        }
    }*/
}
