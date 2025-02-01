package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.domain.repository.CallRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CallViewModel(private val callRepository: CallRepository) : ViewModel() {

    private val _callLogs = MutableStateFlow<List<CallLog>>(emptyList())
    val callLogs: StateFlow<List<CallLog>> get() = _callLogs

    fun makeCall(phoneNumber: String) {
        callRepository.makeCall(phoneNumber)
    }

    fun endCall() {
        callRepository.endCall()
    }

    fun answerCall() {
        callRepository.answerCall()
    }

    fun loadCallLogs() {
        viewModelScope.launch {
            callRepository.getCallLogs().collect { logs ->
                _callLogs.value = logs
            }
        }
    }
}
