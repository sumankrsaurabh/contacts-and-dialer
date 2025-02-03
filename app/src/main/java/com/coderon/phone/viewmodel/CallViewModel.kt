package com.coderon.phone.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.coderon.phone.domain.repository.CallRepository

class CallViewModel(private val callRepository: CallRepository) : ViewModel() {

    fun makeCall(phoneNumber: String, subscriptionID: Int) {
        Log.d("CallViewModel", "Making call with subscription ID: $subscriptionID")
        callRepository.makeCall(phoneNumber, subscriptionID)
    }

    fun endCall() {
        callRepository.endCall()
    }

    fun answerCall() {
        callRepository.answerCall()
    }
}
