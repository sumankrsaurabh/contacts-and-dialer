@file:Suppress("DEPRECATION")

package com.coderon.phone.call.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import com.coderon.phone.call.domain.CallReducer
import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.call.domain.toDomainState
import com.coderon.phone.call.ui.AudioRoute
import com.coderon.phone.call.ui.CallUiState
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.coderon.phone.data.model.CallLog as CallLogData

@SuppressLint("StaticFieldLeak")
object CallManager : KoinComponent {

    /** Active call sessions (keyed by Telecom Call identity) */
    private val sessions = mutableMapOf<Int, CallSession>()
    
    /** Start times for calculating duration accurately */
    private val sessionStartTimes = mutableMapOf<Int, Long>()

    private var inCallService: InCallService? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null

    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val contactRepository: ContactRepository by inject()
    private val callLogRepository: CallLogRepository by inject()
    
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    /* ------------------------------------------------
       SERVICE
    ------------------------------------------------ */

    fun setService(service: InCallService?) {
        inCallService = service
        if (service != null) {
            initProximitySensor(service)
        } else {
            sessions.clear()
            sessionStartTimes.clear()
            stopTimer()
            releaseProximitySensor()
            recompute()
        }
    }

    /* ------------------------------------------------
       CALL LIFECYCLE
    ------------------------------------------------ */

    fun onCallAdded(call: Call) {
        val id = System.identityHashCode(call)

        val phoneNumber =
            call.details.handle?.schemeSpecificPart ?: "Unknown"

        val isIncoming = call.state == Call.STATE_RINGING

        val session = CallSession(
            id = id.toString(),
            call = call,
            state = call.state.toDomainState(),
            phoneNumber = phoneNumber,
            displayName = null,
            profilePictureUrl = null,
            isIncoming = isIncoming
        )
        sessions[id] = session

        scope.launch(Dispatchers.IO) {
            val contact = resolveContact(phoneNumber)
            if (contact != null) {
                sessions[id] = sessions[id]?.copy(
                    displayName = contact.displayName,
                    profilePictureUrl = contact.profilePictureUrl
                ) ?: return@launch
                recompute()
            }
        }

        recompute()
        updateTimerState()
        updateProximitySensor()
    }


    fun onCallStateChanged(call: Call, newState: Int) {
        val id = System.identityHashCode(call)
        val existing = sessions[id] ?: return

        // Mark start time when call becomes active
        if (newState == Call.STATE_ACTIVE && !sessionStartTimes.containsKey(id)) {
            sessionStartTimes[id] = System.currentTimeMillis()
        }

        sessions[id] = existing.copy(
            state = newState.toDomainState()
        )
        recompute()
        updateTimerState()
        updateProximitySensor()
    }

    fun onCallRemoved(call: Call) {
        val id = System.identityHashCode(call)
        val session = sessions[id]
        
        if (session != null) {
            saveCallLog(session)
        }

        sessions.remove(id)
        sessionStartTimes.remove(id)
        
        recompute()
        updateTimerState()
        updateProximitySensor()
    }

    private fun saveCallLog(session: CallSession) {
        val startTime = sessionStartTimes[System.identityHashCode(session.call)]
        val duration = if (startTime != null) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }

        val callType = when {
            session.isIncoming && session.state.isEnded && duration == 0 -> CallType.MISSED
            session.isIncoming -> CallType.INCOMING
            else -> CallType.OUTGOING
        }

        scope.launch(Dispatchers.IO) {
            callLogRepository.addCallLog(
                CallLogData(
                    phoneNumber = session.phoneNumber,
                    callType = callType,
                    callDurationSeconds = duration,
                    callTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun onAudioStateChanged(audioState: CallAudioState) {
        val route = when (audioState.route) {
            CallAudioState.ROUTE_SPEAKER -> AudioRoute.SPEAKER
            CallAudioState.ROUTE_BLUETOOTH -> AudioRoute.BLUETOOTH
            CallAudioState.ROUTE_WIRED_HEADSET -> AudioRoute.WIRED
            else -> AudioRoute.EARPIECE
        }
        _uiState.value = _uiState.value.copy(
            audioRoute = route,
            isMuted = audioState.isMuted
        )
        updateProximitySensor()
    }

    /* ------------------------------------------------
       TIMER LOGIC
    ------------------------------------------------ */

    private fun updateTimerState() {
        val hasActiveCall = sessions.values.any { it.state.isActive }
        if (hasActiveCall && timerJob == null) {
            startTimer()
        } else if (!hasActiveCall && timerJob != null) {
            stopTimer()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val activeSession = sessions.values.firstOrNull { it.state.isActive }
                if (activeSession != null) {
                    val startTime = sessionStartTimes[System.identityHashCode(activeSession.call)]
                    if (startTime != null) {
                        val seconds = (System.currentTimeMillis() - startTime) / 1000
                        _uiState.value = _uiState.value.copy(callDurationSeconds = seconds)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _uiState.value = _uiState.value.copy(callDurationSeconds = 0L)
    }

    /* ------------------------------------------------
       PROXIMITY SENSOR
    ------------------------------------------------ */

    private fun initProximitySensor(context: Context) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (proximityWakeLock == null) {
            proximityWakeLock = powerManager.newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                "Phone:ProximityWakeLock"
            )
        }
    }

    private fun updateProximitySensor() {
        val shouldBeActive = sessions.values.any { it.state.isActive || it.state.isOutgoing } &&
                _uiState.value.audioRoute != AudioRoute.SPEAKER &&
                _uiState.value.audioRoute != AudioRoute.BLUETOOTH &&
                !_uiState.value.isVideo

        if (shouldBeActive) {
            if (proximityWakeLock?.isHeld == false) {
                proximityWakeLock?.acquire(1 * 60 * 60 * 1000L) // 1 hour timeout safety
            }
        } else {
            if (proximityWakeLock?.isHeld == true) {
                proximityWakeLock?.release()
            }
        }
    }

    private fun releaseProximitySensor() {
        if (proximityWakeLock?.isHeld == true) {
            proximityWakeLock?.release()
        }
        proximityWakeLock = null
    }

    /* ------------------------------------------------
       REDUCER
    ------------------------------------------------ */

    private fun recompute() {
        _uiState.value = CallReducer.reduce(
            sessions.values.toList(),
            _uiState.value
        )
    }

    /* ------------------------------------------------
       USER ACTIONS
    ------------------------------------------------ */

    fun accept() {
        sessions.values
            .firstOrNull { it.state.isIncoming }
            ?.call
            ?.answer(VideoProfile.STATE_AUDIO_ONLY)
    }

    fun reject() {
        sessions.values.firstOrNull { it.state.isIncoming }?.call?.disconnect()
    }

    fun disconnectPrimary() {
        sessions.values.firstOrNull { it.state.isActive || it.state.isOutgoing }?.call?.disconnect()
    }

    fun hold() {
        sessions.values
            .firstOrNull { it.state.isActive }
            ?.call
            ?.hold()
    }

    fun unhold() {
        sessions.values
            .firstOrNull { it.state.isHolding }
            ?.call
            ?.unhold()
    }

    fun toggleMute() {
        val newMuteState = !_uiState.value.isMuted
        inCallService?.setMuted(newMuteState)
    }

    fun toggleSpeaker() {
        val currentRoute = _uiState.value.audioRoute
        val newRoute = if (currentRoute == AudioRoute.SPEAKER) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_SPEAKER
        }
        inCallService?.setAudioRoute(newRoute)
    }

    fun toggleBluetooth() {
        val currentRoute = _uiState.value.audioRoute
        val newRoute = if (currentRoute == AudioRoute.BLUETOOTH) {
            CallAudioState.ROUTE_EARPIECE
        } else {
            CallAudioState.ROUTE_BLUETOOTH
        }
        inCallService?.setAudioRoute(newRoute)
    }

    fun playDtmfTone(digit: Char) {
        val activeCall = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        activeCall.playDtmfTone(digit)
        scope.launch {
            delay(150)
            activeCall.stopDtmfTone()
        }
    }
    
    fun stopDtmfTone() {
        sessions.values.firstOrNull { it.state.isActive }?.call?.stopDtmfTone()
    }

    fun swap() {
        val active = sessions.values.firstOrNull { it.state.isActive }?.call
        val holding = sessions.values.firstOrNull { it.state.isHolding }?.call
        active?.hold()
        holding?.unhold()
    }

    fun mergeConference() {
        val primary = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        primary.conferenceableCalls.firstOrNull()?.let {
            primary.conference(it)
        }
    }

    fun endAll() {
        sessions.values.forEach { it.call.disconnect() }
    }

    fun addCall(context: Context) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /* ---------------- VIDEO CALL ACTIONS ---------------- */

    fun toggleVideo() {
        val activeCall = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val currentVideoState = activeCall.details.videoState
        val newVideoState = if (VideoProfile.isVideo(currentVideoState)) {
            VideoProfile.STATE_AUDIO_ONLY
        } else {
            VideoProfile.STATE_BIDIRECTIONAL
        }
        activeCall.videoCall?.sendSessionModifyRequest(VideoProfile(newVideoState))
    }

    fun flipCamera() {
        // Implementation would require keeping track of front/back camera IDs
    }

    private suspend fun resolveContact(number: String): Contact? {
        return try {
            contactRepository.getContactByNumber(number)
        } catch (e: Exception) {
            null
        }
    }
}
