package com.coderon.phone.call

import android.content.Intent
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import com.coderon.phone.ui.CallActivity

class CallConnectionService : ConnectionService() {
    override fun onCreateIncomingConnection(
        connectionManagerPhoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection? {
        val connection = CallConnection()
        connection.setConnectionCapabilities(Connection.CAPABILITY_HOLD or Connection.CAPABILITY_MUTE)

        val intent = Intent(applicationContext, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("CALLER_NAME", request?.extras?.getString("CALLER_NAME", "Unknown"))
            putExtra("PHONE_NUMBER", request?.extras?.getString("PHONE_NUMBER", ""))
            putExtra("IS_INCOMING", true)
        }
        applicationContext.startActivity(intent)

        return connection
    }


    override fun onCreateOutgoingConnection(
        phoneAccount: PhoneAccountHandle?,
        request: ConnectionRequest?
    ): Connection {
        val connection = CallConnection()
        connection.setConnectionCapabilities(Connection.CAPABILITY_HOLD or Connection.CAPABILITY_MUTE)

        val intent = Intent(applicationContext, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("PHONE_NUMBER", request?.address?.schemeSpecificPart ?: "")
            putExtra("IS_INCOMING", false)
        }
        applicationContext.startActivity(intent)

        return connection
    }

}
