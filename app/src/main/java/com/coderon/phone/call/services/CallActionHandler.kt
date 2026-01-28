package com.coderon.phone.call.services

import android.content.Context
import android.content.Intent
import android.telecom.VideoProfile
import com.coderon.phone.call.ui.CallUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CallActionHandler(
    private val sessionManager: CallSessionManager,
    private val audioManager: CallAudioManager,
    private val scope: CoroutineScope,
    private val uiState: MutableStateFlow<CallUiState>
) {

    fun accept() {
        val incomingSession = sessionManager.sessions.values.firstOrNull { it.state.isIncoming } ?: return
        val incomingVideoState = incomingSession.call.details.videoState

        if (VideoProfile.isVideo(incomingVideoState)) {
            uiState.value = uiState.value.copy(userWantsVideo = true)
            incomingSession.call.answer(VideoProfile.STATE_BIDIRECTIONAL)
        } else {
            uiState.value = uiState.value.copy(userWantsVideo = false)
            incomingSession.call.answer(VideoProfile.STATE_AUDIO_ONLY)
        }
    }

    fun reject() {
        sessionManager.sessions.values.firstOrNull { it.state.isIncoming }?.call?.disconnect()
    }

    fun disconnectPrimary() {
        sessionManager.sessions.values.firstOrNull { it.state.isActive || it.state.isOutgoing }?.call?.disconnect()
    }

    fun hold() {
        sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call?.hold()
    }

    fun unhold() {
        sessionManager.sessions.values.firstOrNull { it.state.isHolding }?.call?.unhold()
    }

    fun toggleMute(isMuted: Boolean) {
        audioManager.toggleMute(isMuted)
    }

    fun toggleSpeaker(currentRoute: com.coderon.phone.call.ui.AudioRoute) {
        audioManager.toggleSpeaker(currentRoute)
    }

    fun toggleBluetooth(currentRoute: com.coderon.phone.call.ui.AudioRoute) {
        audioManager.toggleBluetooth(currentRoute)
    }

    fun playDtmfTone(digit: Char) {
        val activeCall = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        activeCall.playDtmfTone(digit)
        scope.launch {
            delay(150)
            activeCall.stopDtmfTone()
        }
    }

    fun stopDtmfTone() {
        sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call?.stopDtmfTone()
    }

    fun swap() {
        val active = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call
        val holding = sessionManager.sessions.values.firstOrNull { it.state.isHolding }?.call
        active?.hold()
        holding?.unhold()
    }

    fun mergeConference() {
        val primary = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        primary.conferenceableCalls.firstOrNull()?.let {
            primary.conference(it)
        }
    }

    fun endAll() {
        sessionManager.sessions.values.forEach { it.call.disconnect() }
    }

    fun addCall(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun toggleVideo() {
        val call = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val videoCall = call.videoCall ?: return

        val current = call.details.videoState
        val isCurrentlyVideo = VideoProfile.isVideo(current)
        val newState = if (isCurrentlyVideo) VideoProfile.STATE_AUDIO_ONLY
        else VideoProfile.STATE_BIDIRECTIONAL

        uiState.value = uiState.value.copy(userWantsVideo = !isCurrentlyVideo)
        videoCall.sendSessionModifyRequest(VideoProfile(newState))
    }

    fun acceptVideoUpgrade() {
        val activeCall = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val profile = uiState.value.incomingVideoUpgradeRequest ?: return

        uiState.value = uiState.value.copy(userWantsVideo = true)
        activeCall.videoCall?.sendSessionModifyResponse(profile)
        uiState.value = uiState.value.copy(incomingVideoUpgradeRequest = null)
    }

    fun declineVideoUpgrade() {
        val activeCall = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        uiState.value.incomingVideoUpgradeRequest ?: return

        val responseProfile = VideoProfile(VideoProfile.STATE_AUDIO_ONLY)
        activeCall.videoCall?.sendSessionModifyResponse(responseProfile)
        uiState.value = uiState.value.copy(incomingVideoUpgradeRequest = null)
    }
}
