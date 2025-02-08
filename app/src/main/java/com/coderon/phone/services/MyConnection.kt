package com.coderon.phone.services

import android.telecom.Connection
import android.telecom.DisconnectCause

class MyConnection : Connection() {

    override fun onAnswer() {
        setActive()
    }

    override fun onDisconnect() {
        setDisconnected(DisconnectCause(DisconnectCause.LOCAL))
        destroy()
    }

    override fun onReject() {
        setDisconnected(DisconnectCause(DisconnectCause.REJECTED))
        destroy()
    }
}
