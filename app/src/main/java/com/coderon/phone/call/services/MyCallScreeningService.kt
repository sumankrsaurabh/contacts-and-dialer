package com.coderon.phone.call.services

import android.telecom.Call
import android.telecom.CallScreeningService

class SimpleCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        when {
            /*number != null && isNumberBlocked(number.normalizePhoneNumber()) -> {
                respondToCall(callDetails, isBlocked = true)
            }*/

            /*number != null && baseConfig.blockUnknownNumbers -> {
                val simpleContactsHelper = SimpleContactsHelper(this)
                val privateCursor = getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
                simpleContactsHelper.exists(number, privateCursor) { exists ->
                    respondToCall(callDetails, isBlocked = !exists)
                }
            }*/

            /*number == null && baseConfig.blockHiddenNumbers -> {
                respondToCall(callDetails, isBlocked = true)
            }*/

            else -> {
                respondToCall(callDetails, isBlocked = false)
            }
        }
    }

    private fun respondToCall(callDetails: Call.Details, isBlocked: Boolean) {
        val response = CallResponse.Builder()
            .setDisallowCall(isBlocked)
            .setRejectCall(isBlocked)
            .setSkipCallLog(isBlocked)
            .setSkipNotification(isBlocked)
            .build()

        respondToCall(callDetails, response)
    }
}
