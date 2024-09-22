package com.coderon.phone.services

import android.telecom.Call
import android.telecom.InCallService
import com.coderon.phone.viewmodel.CallViewModel
import org.koin.android.ext.android.inject

class CallService : InCallService() {

    // Inject the CallViewModel using Koin
    private val callViewModel: CallViewModel by inject()

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        callViewModel.onCallAdded(call)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        callViewModel.onCallRemoved(call)
    }
}
