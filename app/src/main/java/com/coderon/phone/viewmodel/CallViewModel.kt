package com.coderon.phone.viewmodel

import android.telecom.Call
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {

    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState = _callState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted = _isMuted.asStateFlow()

    private val _callDuration = MutableStateFlow("00:00")
    val callDuration = _callDuration.asStateFlow()

    fun onCallAdded(call: Call) {
        call.registerCallback(object : Call.Callback() {
            override fun onStateChanged(call: Call, state: Int) {
                viewModelScope.launch {
                    when (state) {
                        Call.STATE_ACTIVE -> _callState.value = CallState.Active(call)
                        Call.STATE_RINGING -> _callState.value = CallState.Ringing(call)
                        Call.STATE_DISCONNECTED -> _callState.value = CallState.Disconnected
                        Call.STATE_HOLDING -> _callState.value = CallState.Holding(call)
                        // Handle other states
                    }
                }
            }
        })
    }

    fun toggleMute() {
        _isMuted.update { !it }
        // Implement actual mute/unmute functionality with Telecom API here
    }

    fun toggleSpeaker() {
        // Implement speaker toggle functionality
    }

    fun endCall(call: Call) {
        call.disconnect()
    }

    fun getCallerName(call: Call): String {
        // Implement logic to retrieve caller name from contacts
        return "John Doe" // Example
    }
    fun onCallRemoved(call: Call) {
        viewModelScope.launch {
            _callState.value = CallState.Idle
        }
    }
}

sealed class CallState {
    object Idle : CallState()
    data class Active(val call: Call) : CallState()
    data class Ringing(val call: Call) : CallState()
    object Disconnected : CallState()
    data class Holding(val call: Call) : CallState()
}
