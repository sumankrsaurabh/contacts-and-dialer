package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import android.telecom.Call
import android.content.Context

class CallViewModel(private val context: Context) : ViewModel() {

    private var currentCall: Call? = null

    fun setCall(call: Call) {
        currentCall = call
    }

    fun answerCall() {
//        currentCall?.answer(Call.AUDIO_STATE_SPEAKER)
    }

    fun rejectCall() {
        currentCall?.reject(Call.REJECT_REASON_DECLINED)
    }
}
