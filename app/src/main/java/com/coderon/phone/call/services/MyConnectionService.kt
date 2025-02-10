package com.coderon.phone.call.services

import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log

class MyConnectionService : ConnectionService() {

    override fun onCreateIncomingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d("MyConnectionService", "Incoming call received")
        val connection = MyConnection()
        connection.setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD or Connection.CAPABILITY_MUTE)
        connection.setAudioModeIsVoip(true)
        connection.setActive()
        return connection
    }

    override fun onCreateOutgoingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        Log.d("MyConnectionService", "Outgoing call created")
        val connection = MyConnection()
        connection.setConnectionCapabilities(Connection.CAPABILITY_SUPPORT_HOLD or Connection.CAPABILITY_MUTE)
        connection.setAudioModeIsVoip(true)
        connection.setDialing()
        return connection
    }

    override fun onCreateIncomingConnectionFailed(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.e("MyConnectionService", "Incoming call failed")
    }

    override fun onCreateOutgoingConnectionFailed(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ) {
        Log.e("MyConnectionService", "Outgoing call failed")
    }
}
