package com.coderon.phone.call.services

import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

class MyInCallService : InCallService() {

    override fun onCallAdded(call: Call?) {
        super.onCallAdded(call)
        if (call != null) {
            Log.d("MyInCallService", "New call added: ${call.details?.handle}")
//            activeCall = call
            call.registerCallback(callCallback)
        }
    }

    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
        Log.d("MyInCallService", "Call removed: ${call?.details?.handle}")
        /*if (activeCall == call) {
            activeCall = null
        }*/
    }

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)
            Log.d("MyInCallService", "Call state changed: $state")
        }

        override fun onDetailsChanged(call: Call, details: Call.Details) {
            super.onDetailsChanged(call, details)
            Log.d("MyInCallService", "Call details changed: $details")
        }
    }
}
