package com.coderon.phone.call.services

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class MyCallScreeningService : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: "Unknown"

        Log.d("CallScreeningService", "Incoming call from: $phoneNumber")

        if (isSpam(phoneNumber)) {
            Log.d("CallScreeningService", "Blocking spam call: $phoneNumber")
            val response = CallResponse.Builder()
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipCallLog(true)
                .setSkipNotification(true)
                .build()

            respondToCall(callDetails, response)
        } else {
            Log.d("CallScreeningService", "Allowing call: $phoneNumber")
            val response = CallResponse.Builder().build()
            respondToCall(callDetails, response)
        }
    }

    private fun isSpam(phoneNumber: String): Boolean {
        // TODO: Implement spam detection logic (e.g., check against spam database)
        return phoneNumber.startsWith("800") // Example: Blocking numbers starting with "800"
    }
}
