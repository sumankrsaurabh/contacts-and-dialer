package com.coderon.phone.utils

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.telecom.*
import androidx.annotation.RequiresPermission
import com.coderon.phone.service.MyConnectionService

object CallManager {
    fun registerPhoneAccount(context: Context) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val componentName = ComponentName(context, MyConnectionService::class.java)
        val phoneAccountHandle = PhoneAccountHandle(componentName, "MyDialer")

        val phoneAccount = PhoneAccount.builder(phoneAccountHandle, "MyDialer")
            .setCapabilities(PhoneAccount.CAPABILITY_CALL_PROVIDER)
            .build()

        telecomManager.registerPhoneAccount(phoneAccount)
    }

    @RequiresPermission(Manifest.permission.CALL_PHONE)
    fun makeCall(context: Context, phoneNumber: String) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val uri = Uri.fromParts("tel", phoneNumber, null)
        val bundle = Bundle()
        bundle.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, getPhoneAccountHandle(context))
        telecomManager.placeCall(uri, bundle)
    }

    fun answerCall(call: Call) {
        call.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun endCall(call: Call) {
        call.disconnect()
    }

    fun holdCall(call: Call) {
        call.hold()
    }

    fun resumeCall(call: Call) {
        call.unhold()
    }

    fun muteCall(audioManager: AudioManager, mute: Boolean) {
        audioManager.isMicrophoneMute = mute
    }

    private fun getPhoneAccountHandle(context: Context): PhoneAccountHandle {
        val componentName = ComponentName(context, MyConnectionService::class.java)
        return PhoneAccountHandle(componentName, "MyDialer")
    }
}
