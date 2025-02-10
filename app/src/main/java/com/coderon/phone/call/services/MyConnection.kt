package com.coderon.phone.call.services

import android.telecom.Connection
import android.telecom.DisconnectCause
import android.util.Log

class MyConnection : Connection() {

    override fun onAnswer() {
        Log.d("MyConnection", "Call answered")
        setActive()
    }

    override fun onReject() {
        Log.d("MyConnection", "Call rejected")
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }

    override fun onDisconnect() {
        Log.d("MyConnection", "Call disconnected")
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }

    override fun onHold() {
        Log.d("MyConnection", "Call put on hold")
        setOnHold()
    }

    override fun onUnhold() {
        Log.d("MyConnection", "Call resumed from hold")
        setActive()
    }

    fun onMute() {
        Log.d("MyConnection", "Call muted")
        // Implement mute logic
    }

    fun onUnmute() {
        Log.d("MyConnection", "Call unmuted")
        // Implement unmute logic
    }
}
