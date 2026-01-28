package com.coderon.phone.call.services

import android.telecom.CallAudioState
import android.telecom.InCallService
import com.coderon.phone.call.ui.AudioRoute

class CallAudioManager(private val inCallService: () -> InCallService?) {

    fun getAudioRoute(audioState: CallAudioState): AudioRoute {
        return when (audioState.route) {
            CallAudioState.ROUTE_SPEAKER -> AudioRoute.SPEAKER
            CallAudioState.ROUTE_BLUETOOTH -> AudioRoute.BLUETOOTH
            CallAudioState.ROUTE_WIRED_HEADSET -> AudioRoute.WIRED
            else -> AudioRoute.EARPIECE
        }
    }

    fun toggleMute(isMuted: Boolean) {
        inCallService()?.setMuted(!isMuted)
    }

    fun toggleSpeaker(currentRoute: AudioRoute) {
        val newRoute = if (currentRoute == AudioRoute.SPEAKER) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        inCallService()?.setAudioRoute(newRoute)
    }

    fun toggleBluetooth(currentRoute: AudioRoute) {
        val newRoute = if (currentRoute == AudioRoute.BLUETOOTH) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_BLUETOOTH
        }
        inCallService()?.setAudioRoute(newRoute)
    }
}
