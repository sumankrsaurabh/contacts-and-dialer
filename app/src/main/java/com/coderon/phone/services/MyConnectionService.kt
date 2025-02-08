package com.coderon.phone.services

import android.telecom.Call
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import com.coderon.phone.notifications.CallNotificationService.showIncomingCallScreen

class MyConnectionService : ConnectionService() {

    companion object {
        private val activeCalls = mutableMapOf<String, Call>()

        fun getCallByHandle(callId: String?): Call? {
            return activeCalls[callId]
        }
    }

    override fun onCreateIncomingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = MyConnection()
        connection.setRinging()

        // Show the lock screen call UI
        showIncomingCallScreen(applicationContext, "Incoming Call")

        return connection
    }


    override fun onCreateOutgoingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = MyConnection()
        connection.setDialing()
        connection.setActive()
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateIncomingConnectionFailed(phoneAccount, request)
    }

    override fun onCreateOutgoingConnectionFailed(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        super.onCreateOutgoingConnectionFailed(phoneAccount, request)
    }


}
