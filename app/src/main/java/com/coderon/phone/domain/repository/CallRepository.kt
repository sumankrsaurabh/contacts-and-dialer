package com.coderon.phone.domain.repository

interface CallRepository {
    fun makeCall(phoneNumber: String,subscriptionID: Int?)
    fun endCall()
    fun answerCall()
}
