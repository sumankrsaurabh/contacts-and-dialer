@file:Suppress("DEPRECATION")

package com.coderon.phone.call.services

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.util.Log
import androidx.core.content.ContextCompat
import com.coderon.phone.call.domain.CallReducer
import com.coderon.phone.call.domain.CallSession
import com.coderon.phone.call.domain.toDomainState
import com.coderon.phone.call.ui.AudioRoute
import com.coderon.phone.call.ui.CallUiState
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.CallLogRepository
import com.coderon.phone.domain.repository.ContactRepository
import com.coderon.phone.notifications.CallNotificationManager
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

    private const val TAG = "CallManager"

    private var currentFacing: Int = CameraCharacteristics.LENS_FACING_FRONT

    private val sessions = mutableMapOf<Int, CallSession>()
    private val sessionStartTimes = mutableMapOf<Int, Long>()

    private var inCallService: InCallService? = null
    private var proximityWakeLock: PowerManager.WakeLock? = null

    private var currentCameraId: String? = null

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
            initCameraId(service)
        } else {
            sessions.clear()
            sessionStartTimes.clear()
            stopTimer()
            releaseProximitySensor()
            recompute()
        }
    }

    private fun initCameraId(context: Context) {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val front = getCameraForFacing(
            manager, CameraCharacteristics.LENS_FACING_FRONT
        )

        currentCameraId = front
        currentFacing = CameraCharacteristics.LENS_FACING_FRONT
    }

    private fun getCameraForFacing(
        manager: CameraManager,
        facing: Int
    ): String? {
        return manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    fun rebindCamera() {
        val call = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val videoCall = call.videoCall ?: return
        if (!hasCameraPermission()) return

        currentCameraId?.let {
            videoCall.setCamera(it)
        }
    }


    private fun hasCameraPermission(): Boolean {
        val service = inCallService ?: return false
        return ContextCompat.checkSelfPermission(
            service, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    /* ------------------------------------------------
       CALL LIFECYCLE
    ------------------------------------------------ */

    fun onCallAdded(call: Call) {
        val id = System.identityHashCode(call)
        val phoneNumber = call.details.handle?.schemeSpecificPart ?: "Unknown"
        val isIncoming = call.state == Call.STATE_RINGING

        Log.d(TAG, "onCallAdded: $phoneNumber")

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
        sessions[id] = session

        scope.launch(Dispatchers.IO) {
            val contact = resolveContact(phoneNumber)
            if (contact != null) {
                sessions[id] = sessions[id]?.copy(
                    displayName = contact.displayName, profilePictureUrl = contact.profilePictureUrl
                ) ?: return@launch
                recompute()
            }
        }

        call.registerCallback(object : Call.Callback() {
            override fun onVideoCallChanged(call: Call?, videoCall: InCallService.VideoCall?) {
                Log.d(TAG, "onVideoCallChanged: ${videoCall != null}")
                val existing = sessions[id] ?: return
                sessions[id] = existing.copy(videoCall = videoCall)

                videoCall?.let { vc ->
                    vc.registerCallback(videoCallCallback)
                    vc.requestCameraCapabilities()
                    if (currentCameraId != null && hasCameraPermission()) {
                        Log.d(TAG, "Setting camera: $currentCameraId")
                        vc.setCamera(currentCameraId)
                    }
                }
                recompute()
            }

            override fun onDetailsChanged(call: Call?, details: Call.Details?) {
                val existing = sessions[id] ?: return

                val wasVideo = VideoProfile.isVideo(existing.call.details.videoState)
                val isNowVideo = VideoProfile.isVideo(details?.videoState ?: 0)

                val newState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    details?.state ?: existing.call.state
                } else {
                    existing.call.state
                }

                sessions[id] = existing.copy(
                    state = newState.toDomainState(), videoCall = call?.videoCall
                )

                if (isNowVideo && !wasVideo && currentCameraId != null && hasCameraPermission()) {
                    Log.d(TAG, "Upgraded to video, setting camera: $currentCameraId")
                    call?.videoCall?.setCamera(currentCameraId)
                }

                recompute()
            }
        })

        call.videoCall?.let { vc ->
            vc.registerCallback(videoCallCallback)
            if (currentCameraId != null && hasCameraPermission()) {
                vc.setCamera(currentCameraId)
            }
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
        val existing = sessions[id] ?: return

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

        if (callType == CallType.MISSED) {
            inCallService?.let {
                CallNotificationManager(it).showMissedCallNotification(
                    session.displayName, session.phoneNumber
                )
            }
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
            audioRoute = route, isMuted = audioState.isMuted
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
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "Phone:ProximityWakeLock"
            )
        }
    }

    private fun updateProximitySensor() {
        val shouldBeActive =
            sessions.values.any { it.state.isActive || it.state.isOutgoing } && _uiState.value.audioRoute != AudioRoute.SPEAKER && _uiState.value.audioRoute != AudioRoute.BLUETOOTH && !_uiState.value.isVideo

        if (shouldBeActive) {
            if (proximityWakeLock?.isHeld == false) {
                proximityWakeLock?.acquire(1 * 60 * 60 * 1000L)
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
            sessions.values.toList(), _uiState.value
        )
    }

    /* ------------------------------------------------
       USER ACTIONS
    ------------------------------------------------ */

    fun accept() {
        val incomingSession = sessions.values.firstOrNull { it.state.isIncoming } ?: return
        val incomingVideoState = incomingSession.call.details.videoState

        if (VideoProfile.isVideo(incomingVideoState)) {
            incomingSession.call.answer(VideoProfile.STATE_BIDIRECTIONAL)
        } else {
            incomingSession.call.answer(VideoProfile.STATE_AUDIO_ONLY)
        }
    }

    fun reject() {
        sessions.values.firstOrNull { it.state.isIncoming }?.call?.disconnect()
    }

    fun disconnectPrimary() {
        sessions.values.firstOrNull { it.state.isActive || it.state.isOutgoing }?.call?.disconnect()
    }

    fun hold() {
        sessions.values.firstOrNull { it.state.isActive }?.call?.hold()
    }

    fun unhold() {
        sessions.values.firstOrNull { it.state.isHolding }?.call?.unhold()
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
        val call = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val videoCall = call.videoCall ?: return

        val current = call.details.videoState
        val newState = if (VideoProfile.isVideo(current)) VideoProfile.STATE_AUDIO_ONLY
        else VideoProfile.STATE_BIDIRECTIONAL

        videoCall.sendSessionModifyRequest(VideoProfile(newState))
    }


    fun acceptVideoUpgrade() {
        val activeCall = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val profile = _uiState.value.incomingVideoUpgradeRequest ?: return

        activeCall.videoCall?.sendSessionModifyResponse(profile)
        _uiState.value = _uiState.value.copy(incomingVideoUpgradeRequest = null)
    }

    fun declineVideoUpgrade() {
        val activeCall = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        _uiState.value.incomingVideoUpgradeRequest ?: return

        // Respond with current video state (which should be audio only)
        val responseProfile = VideoProfile(VideoProfile.STATE_AUDIO_ONLY)
        activeCall.videoCall?.sendSessionModifyResponse(responseProfile)
        _uiState.value = _uiState.value.copy(incomingVideoUpgradeRequest = null)
    }

    fun flipCamera() {
        val service = inCallService ?: return
        val call = sessions.values.firstOrNull { it.state.isActive }?.call ?: return
        val videoCall = call.videoCall ?: return
        if (!hasCameraPermission()) return

        val manager = service.getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val targetFacing =
            if (currentFacing == CameraCharacteristics.LENS_FACING_FRONT) CameraCharacteristics.LENS_FACING_BACK
            else CameraCharacteristics.LENS_FACING_FRONT

        val targetCameraId = getCameraForFacing(manager, targetFacing)

        try {
            if (targetCameraId != null) {
                videoCall.setCamera(targetCameraId)
                currentCameraId = targetCameraId
                currentFacing = targetFacing
            } else {
                // 🔁 fallback to FRONT if BACK unavailable
                val frontId = getCameraForFacing(
                    manager, CameraCharacteristics.LENS_FACING_FRONT
                ) ?: return

                videoCall.setCamera(frontId)
                currentCameraId = frontId
                currentFacing = CameraCharacteristics.LENS_FACING_FRONT
            }
        } catch (e: Exception) {
            // 🛟 absolute safety fallback
            try {
                val frontId = getCameraForFacing(
                    manager, CameraCharacteristics.LENS_FACING_FRONT
                ) ?: return

                videoCall.setCamera(frontId)
                currentCameraId = frontId
                currentFacing = CameraCharacteristics.LENS_FACING_FRONT
            } catch (_: Exception) {
                // swallow – never crash in-call
            }
        }
    }


    private fun findCameraId(
        manager: CameraManager, facing: Int
    ): String? {
        return manager.cameraIdList.firstOrNull { id ->
            manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == facing
        }
    }

    private suspend fun resolveContact(number: String): Contact? {
        return try {
            contactRepository.getContactByNumber(number)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to resolve contact", e)
            null
        }
    }
}
