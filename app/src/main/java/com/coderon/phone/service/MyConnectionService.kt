package com.coderon.phone.service

import android.content.Intent
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.DisconnectCause
import android.telecom.PhoneAccountHandle
import android.util.Log

class MyConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val phoneNumber = request.address?.schemeSpecificPart ?: "Unknown"
        val callerName = getCallerNameFromContacts(phoneNumber) ?: phoneNumber

        Log.d("MyConnectionService", "Incoming call from: $callerName")

        // Notify UI about incoming call
        notifyIncomingCall(phoneNumber)

        return MyConnection().apply { setActive() }
    }

    override fun onCreateOutgoingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest
    ): Connection {
        val phoneNumber = request.address?.schemeSpecificPart ?: "Unknown"
        Log.d("MyConnectionService", "Outgoing call to: $phoneNumber")

        return MyConnection().apply { setActive() }
    }

    private fun notifyIncomingCall(phoneNumber: String) {
        val intent = Intent("com.coderon.phone.INCOMING_CALL").apply {
            putExtra("phoneNumber", phoneNumber)
        }
        sendBroadcast(intent) // Send broadcast to notify UI
    }

    private fun getCallerNameFromContacts(phoneNumber: String?): String? {
        // TODO: Implement contact lookup logic here (query ContactsProvider)
        return null
    }
}

class MyConnection : Connection() {

    init {
        setAudioModeIsVoip(true) // Enables VoIP mode for proper audio handling
    }

    override fun onAnswer() {
        Log.d("MyConnection", "Call answered")
        setActive()
    }

    override fun onDisconnect() {
        Log.d("MyConnection", "Call disconnected")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }

    override fun onAbort() {
        Log.d("MyConnection", "Call aborted")
        setDisconnected(DisconnectCause(DisconnectCause.CANCELED))
        destroy()
    }

    override fun onHold() {
        Log.d("MyConnection", "Call on hold")
        setOnHold()
    }

    override fun onUnhold() {
        Log.d("MyConnection", "Call resumed")
        setActive()
    }
}
