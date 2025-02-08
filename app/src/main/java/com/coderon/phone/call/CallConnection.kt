package com.coderon.phone.call

import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log

class CallConnection : Connection() {

    override fun onAnswer() {
        Log.d("CallConnection", "Call answered")
        setActive()
    }

    override fun onDisconnect() {
        Log.d("CallConnection", "Call disconnected")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }

    override fun onHold() {
        Log.d("CallConnection", "Call on hold")
        setOnHold()
    }

    override fun onUnhold() {
        Log.d("CallConnection", "Call resumed")
        setActive()
    }
}
