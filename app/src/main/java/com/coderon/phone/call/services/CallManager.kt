@file:Suppress("DEPRECATION")

package com.coderon.phone.call.services

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log
import com.coderon.phone.call.domain.CallReducer
import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.call.domain.toDomainState
import com.coderon.phone.call.ui.CallUiState
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@SuppressLint("StaticFieldLeak")
object CallManager : KoinComponent {

    private const val TAG = "CallManager"

    private val sessionManager = CallSessionManager()
    private var inCallService: InCallService? = null
    
    private val _uiState = MutableStateFlow(CallUiState())
    val uiState: StateFlow<CallUiState> = _uiState.asStateFlow()

    private val contactRepository: ContactRepository by inject()
    private val callLogRepository: CallLogRepository by inject()

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private var cameraManager: CallCameraManager? = null
    private var proximityManager: CallProximityManager? = null
    private val audioManager = CallAudioManager { inCallService }
    private val timerManager = CallTimerManager(scope)
    private val callLogHandler = CallLogHandler(callLogRepository, scope)
    private val contactResolver = CallContactResolver(contactRepository)
    private val actionHandler = CallActionHandler(sessionManager, audioManager, scope, _uiState)

    /* ------------------------------------------------
       SERVICE
    ------------------------------------------------ */

    fun setService(service: InCallService?) {
        inCallService = service
        if (service != null) {
            cameraManager = CallCameraManager(service)
            proximityManager = CallProximityManager(service)
            
            _uiState.value = _uiState.value.copy(
                isFrontCamera = cameraManager?.isFrontCamera() ?: true
            )
        } else {
            sessionManager.clear()
            timerManager.stopTimer { _uiState.value = _uiState.value.copy(callDurationSeconds = it) }
            proximityManager?.release()
            proximityManager = null
            cameraManager = null
            recompute()
        }
    }

    fun rebindCamera() {
        val call = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        cameraManager?.rebindCamera(call.videoCall)
    }

    /* ------------------------------------------------
       CALL LIFECYCLE
    ------------------------------------------------ */

    fun onCallAdded(call: Call) {
        val id = System.identityHashCode(call)
        val phoneNumber = call.details.handle?.schemeSpecificPart ?: "Unknown"
        val isIncoming = call.state == Call.STATE_RINGING

        Log.d(TAG, "onCallAdded: $phoneNumber")

        if (VideoProfile.isVideo(call.details.videoState)) {
            _uiState.value = _uiState.value.copy(userWantsVideo = true)
        }

        val session = CallSession(
            id = id.toString(),
            call = call,
            state = call.state.toDomainState(),
            phoneNumber = phoneNumber,
            displayName = null,
            profilePictureUrl = null,
            isIncoming = isIncoming,
            videoCall = call.videoCall
        )
        sessionManager.addSession(id, session)

        scope.launch {
            val contact = contactResolver.resolveContact(phoneNumber)
            if (contact != null) {
                val updatedSession = sessionManager.getSession(id)?.copy(
                    displayName = contact.displayName, profilePictureUrl = contact.profilePictureUrl
                )
                if (updatedSession != null) {
                    sessionManager.updateSession(id, updatedSession)
                    recompute()
                }
            }
        }

        call.registerCallback(object : Call.Callback() {
            override fun onVideoCallChanged(call: Call?, videoCall: InCallService.VideoCall?) {
                Log.d(TAG, "onVideoCallChanged: ${videoCall != null}")
                val existing = sessionManager.getSession(id) ?: return
                sessionManager.updateSession(id, existing.copy(videoCall = videoCall))

                videoCall?.let { vc ->
                    vc.registerCallback(videoCallCallback)
                    vc.requestCameraCapabilities()
                    cameraManager?.rebindCamera(vc)
                }
                recompute()
            }

            override fun onDetailsChanged(call: Call?, details: Call.Details?) {
                val existing = sessionManager.getSession(id) ?: return

                val wasVideo = VideoProfile.isVideo(existing.call.details.videoState)
                val isNowVideo = VideoProfile.isVideo(details?.videoState ?: 0)

                val newState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    details?.state ?: existing.call.state
                } else {
                    existing.call.state
                }

                sessionManager.updateSession(id, existing.copy(
                    state = newState.toDomainState(), videoCall = call?.videoCall
                ))

                if (isNowVideo && !wasVideo) {
                    cameraManager?.rebindCamera(call?.videoCall)
                }

                recompute()
            }
        })

        call.videoCall?.let { vc ->
            vc.registerCallback(videoCallCallback)
            cameraManager?.rebindCamera(vc)
        }

        recompute()
        updateTimerState()
        updateProximitySensor()
    }

    private val videoCallCallback = object : InCallService.VideoCall.Callback() {
        override fun onSessionModifyRequestReceived(videoProfile: VideoProfile?) {
            Log.d(TAG, "onSessionModifyRequestReceived")
            if (videoProfile != null) {
                _uiState.value = _uiState.value.copy(incomingVideoUpgradeRequest = videoProfile)
            }
        }

        override fun onSessionModifyResponseReceived(
            status: Int, requestedProfile: VideoProfile?, responseProfile: VideoProfile?
        ) {
            Log.d(TAG, "onSessionModifyResponseReceived: $status")
            recompute()
        }

        override fun onCallSessionEvent(event: Int) {
            recompute()
        }

        override fun onPeerDimensionsChanged(width: Int, height: Int) {
            Log.d(TAG, "onPeerDimensionsChanged: ${width}x${height}")
            _uiState.value = _uiState.value.copy(peerWidth = width, peerHeight = height)
        }

        override fun onVideoQualityChanged(videoQuality: Int) {
            _uiState.value = _uiState.value.copy(videoQuality = videoQuality)
        }

        override fun onCallDataUsageChanged(dataUsage: Long) {
            _uiState.value = _uiState.value.copy(dataUsage = dataUsage)
        }

        override fun onCameraCapabilitiesChanged(cameraCapabilities: VideoProfile.CameraCapabilities?) {
            _uiState.value = _uiState.value.copy(maxZoom = cameraCapabilities?.maxZoom ?: 1.0f)
        }
    }


    fun onCallStateChanged(call: Call, newState: Int) {
        val id = System.identityHashCode(call)
        val existing = sessionManager.getSession(id) ?: return

        if (newState == Call.STATE_ACTIVE && sessionManager.getStartTime(id) == null) {
            sessionManager.setStartTime(id, System.currentTimeMillis())
        }

        sessionManager.updateSession(id, existing.copy(
            state = newState.toDomainState()
        ))
        recompute()
        updateTimerState()
        updateProximitySensor()
    }

    fun onCallRemoved(call: Call) {
        val id = System.identityHashCode(call)
        val session = sessionManager.getSession(id)

        if (session != null) {
            callLogHandler.saveCallLog(inCallService, session, sessionManager.getStartTime(id))
        }

        sessionManager.removeSession(id)

        recompute()
        updateTimerState()
        updateProximitySensor()
    }

    fun onAudioStateChanged(audioState: CallAudioState) {
        val route = audioManager.getAudioRoute(audioState)
        _uiState.value = _uiState.value.copy(
            audioRoute = route, isMuted = audioState.isMuted
        )
        updateProximitySensor()
    }

    /* ------------------------------------------------
       TIMER LOGIC
    ------------------------------------------------ */

    private fun updateTimerState() {
        val hasActiveCall = sessionManager.sessions.values.any { it.state.isActive }
        timerManager.updateTimerState(
            hasActiveCall = hasActiveCall,
            getStartTime = {
                val activeSession = sessionManager.sessions.values.firstOrNull { it.state.isActive }
                activeSession?.let { sessionManager.getStartTime(System.identityHashCode(it.call)) }
            },
            onTick = { seconds ->
                _uiState.value = _uiState.value.copy(callDurationSeconds = seconds)
            }
        )
    }

    /* ------------------------------------------------
       PROXIMITY SENSOR
    ------------------------------------------------ */

    private fun updateProximitySensor() {
        proximityManager?.updateProximitySensor(
            hasActiveOrOutgoingCall = sessionManager.sessions.values.any { it.state.isActive || it.state.isOutgoing },
            audioRoute = _uiState.value.audioRoute,
            isVideo = _uiState.value.isVideo
        )
    }

    /* ------------------------------------------------
       REDUCER
    ------------------------------------------------ */

    private fun recompute() {
        _uiState.value = CallReducer.reduce(
            sessionManager.sessions.values.toList(), _uiState.value
        )
    }

    /* ------------------------------------------------
       USER ACTIONS
    ------------------------------------------------ */

    fun accept() = actionHandler.accept()
    fun reject() = actionHandler.reject()
    fun disconnectPrimary() = actionHandler.disconnectPrimary()
    fun hold() = actionHandler.hold()
    fun unhold() = actionHandler.unhold()
    fun toggleMute() = actionHandler.toggleMute(_uiState.value.isMuted)
    fun toggleSpeaker() = actionHandler.toggleSpeaker(_uiState.value.audioRoute)
    fun toggleBluetooth() = actionHandler.toggleBluetooth(_uiState.value.audioRoute)
    fun playDtmfTone(digit: Char) = actionHandler.playDtmfTone(digit)
    fun stopDtmfTone() = actionHandler.stopDtmfTone()
    fun swap() = actionHandler.swap()
    fun mergeConference() = actionHandler.mergeConference()
    fun endAll() = actionHandler.endAll()
    fun addCall(context: Context) = actionHandler.addCall(context)

    /* ---------------- VIDEO CALL ACTIONS ---------------- */

    fun toggleVideo() = actionHandler.toggleVideo()
    fun acceptVideoUpgrade() = actionHandler.acceptVideoUpgrade()
    fun declineVideoUpgrade() = actionHandler.declineVideoUpgrade()

    fun flipCamera() {
        val call = sessionManager.sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val success = cameraManager?.flipCamera(call.videoCall) ?: false
        
        if (success) {
            _uiState.value = _uiState.value.copy(
                isFrontCamera = cameraManager?.isFrontCamera() ?: true,
                cameraUpdateTick = _uiState.value.cameraUpdateTick + 1
            )
            recompute()
        }
    }
}
