package com.coderon.phone.call.ui.screens.incallui

import android.content.Intent
import android.provider.CalendarContract
import android.telecom.InCallService
import android.telecom.VideoProfile
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.coderon.phone.call.domain.CallState
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.services.CallRecorderManager
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.ui.components.HybridAlertDialog
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.extentions.State
import com.coderon.phone.ui.utils.extentions.formatCallDuration
import com.coderon.phone.ui.utils.extentions.getCallType
import com.coderon.phone.ui.utils.extentions.getSimInfoForCall

@Composable
fun CallScreen(
    navigator: Navigator,
    backgroundUri: String? = null,
    showContactPhoto: Boolean = true,
    fullScreenCallerPhoto: Boolean = false,
    keypadTonesEnabled: Boolean = true
) {
    val uiState by CallManager.uiState.collectAsState()
    val context = LocalContext.current
    
    // Call Recorder Backend Integration
    val recorderManager = remember { CallRecorderManager(context) }
    var isRecording by remember { mutableStateOf(recorderManager.isRecording()) }

    uiState.incomingVideoUpgradeRequest?.let {
        HybridAlertDialog(
            title = "Video Call Upgrade",
            message = "The other party wants to switch to a video call.",
            confirmText = "Accept",
            cancelText = "Decline",
            onConfirm = { CallManager.acceptVideoUpgrade() },
            onDismiss = { CallManager.declineVideoUpgrade() }
        )
    }

    AnimatedContent(
        targetState = uiState.screen,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "call_screen_transition",
        modifier = Modifier.fillMaxSize()
    ) { screenType ->
        when (screenType) {

            /* ---------------- INCOMING ---------------- */

            CallScreenType.INCOMING -> {
                val call = uiState.primaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())

                IncomingCallScreen(
                    name = call.displayName,
                    phoneNumber = call.phoneNumber,
                    profilePictureUrl = if (showContactPhoto) call.profilePictureUrl else null,
                    simInfo = call.call.getSimInfoForCall(context),
                    backgroundUri = backgroundUri,
                    fullScreenPhoto = fullScreenCallerPhoto,
                    onAnswer = { CallManager.accept() },
                    onDecline = { CallManager.reject() },
                    onSendMessage = { message ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = "smsto:${call.phoneNumber}".toUri()
                            putExtra("sms_body", message)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not send message", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onRemindMe = {
                        val intent = Intent(Intent.ACTION_INSERT).apply {
                            data = CalendarContract.Events.CONTENT_URI
                            putExtra(CalendarContract.Events.TITLE, "Call back ${call.displayName ?: call.phoneNumber}")
                            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis() + 3600000) // 1 hour later
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Calendar app not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            /* ---------------- ONGOING ---------------- */

            CallScreenType.ONGOING -> {
                val call = uiState.primaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())

                OngoingCallScreen(
                    contactName = call.displayName ?: call.phoneNumber,
                    contactPhoneNumber = call.phoneNumber,
                    profilePictureUrl = if (showContactPhoto) call.profilePictureUrl else null,
                    backgroundUri = backgroundUri,
                    state = when (call.state) {
                        CallState.ACTIVE -> State.ACTIVE
                        CallState.HOLDING -> State.HOLD
                        CallState.DIALING -> State.DIALING
                        CallState.CONNECTING -> State.CONNECTING
                        CallState.DISCONNECTING -> State.DISCONNECTING
                        else -> State.ACTIVE
                    },
                    currentAudioRoute = uiState.audioRoute,
                    isMuted = uiState.isMuted,
                    callDuration = uiState.callDurationSeconds.formatCallDuration(),
                    simInfo = call.call.getSimInfoForCall(context),
                    callType = getCallType(context),
                    onEndCall = { 
                        if (isRecording) {
                            recorderManager.stopRecording()
                            isRecording = false
                        }
                        CallManager.disconnectPrimary() 
                    },
                    onToggleSpeaker = { CallManager.toggleSpeaker() },
                    onToggleMute = { CallManager.toggleMute() },
                    onToggleHold = {
                        if (call.state == CallState.HOLDING) CallManager.unhold()
                        else CallManager.hold()
                    },
                    onToggleBluetooth = { CallManager.toggleBluetooth() },
                    onAddCall = {
                        navigator.navigate(Screen.Keypad)
                    },
                    onVideoCall = { CallManager.toggleVideo() },
                    onRecordCall = { 
                        if (isRecording) {
                            val path = recorderManager.stopRecording()
                            isRecording = false
                            if (path != null) {
                                Toast.makeText(context, "Recording saved: $path", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val success = recorderManager.startRecording(call.phoneNumber)
                            if (success) {
                                isRecording = true
                                Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Recording failed (Check permissions)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onAddNote = { 
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Call Note: ${call.displayName ?: call.phoneNumber}")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(Intent.createChooser(intent, "Save note to..."))
                    },
                    isRecording = isRecording,
                    playDfmTones = { if (keypadTonesEnabled) CallManager.playDtmfTone(it) }
                )
            }

            /* ---------------- CALL WAITING ---------------- */

            CallScreenType.CALL_WAITING -> {
                val active =
                    uiState.primaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())
                val waiting =
                    uiState.secondaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())

                CallWaitingScreen(
                    activeName = active.displayName ?: active.phoneNumber,
                    activeNumber = active.phoneNumber,
                    waitingName = waiting.displayName,
                    waitingNumber = waiting.phoneNumber,
                    profilePictureUrl = if (showContactPhoto) waiting.profilePictureUrl else null,
                    onAcceptWaiting = { CallManager.accept() },
                    onRejectWaiting = { waiting.call.disconnect() },
                    onEndActiveAcceptWaiting = {
                        active.call.disconnect()
                        CallManager.accept()
                    }
                )
            }

            /* ---------------- CONFERENCE ---------------- */

            CallScreenType.CONFERENCE -> {
                val participants = mutableListOf<String>()
                uiState.primaryCall?.displayName?.let { participants.add(it) }
                uiState.secondaryCall?.displayName?.let { participants.add(it) }

                ConferenceCallScreen(
                    participants = participants,
                    callDuration = uiState.callDurationSeconds.formatCallDuration(),
                    onEndCall = { CallManager.disconnectPrimary() },
                    onMerge = { CallManager.mergeConference() }
                )
            }

            /* ---------------- VIDEO ---------------- */

            CallScreenType.VIDEO -> {
                val call = uiState.primaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())

                val isVideoEnabled =
                    VideoProfile.isVideo(call.call.details.videoState)

                VideoCallUI(
                    contactName = call.displayName ?: call.phoneNumber,
                    callDuration = uiState.callDurationSeconds.formatCallDuration(),
                    isMuted = uiState.isMuted,
                    isVideoEnabled = isVideoEnabled,
                    isFrontCamera = uiState.isFrontCamera,
                    isActive = call.state == CallState.ACTIVE,
                    onEndCall = { CallManager.disconnectPrimary() },
                    onToggleMute = { CallManager.toggleMute() },
                    onToggleVideo = { CallManager.toggleVideo() },
                    onFlipCamera = { CallManager.flipCamera() },
                    remoteVideoSurface = {
                        key(uiState.cameraUpdateTick) {
                            VideoSurface(
                                videoCall = call.videoCall,
                                isPreview = false,
                                onSurfaceReady = {
                                    CallManager.rebindCamera()
                                }
                            )
                        }
                    },
                    localVideoSurface = {
                        key(uiState.cameraUpdateTick) {
                            VideoSurface(
                                videoCall = call.videoCall,
                                isPreview = true,
                                onSurfaceReady = {
                                    CallManager.rebindCamera()
                                }
                            )
                        }
                    }
                )
            }


            /* ---------------- TWO CALLS ---------------- */

            CallScreenType.TWO_CALLS -> {
                val active =
                    uiState.primaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())
                val holding =
                    uiState.secondaryCall ?: return@AnimatedContent Box(Modifier.fillMaxSize())
                TwoCallScreen(
                    firstContactName = active.displayName ?: active.phoneNumber,
                    firstPhoneNumber = active.phoneNumber,
                    secondContactName = holding.displayName ?: holding.phoneNumber,
                    secondPhoneNumber = holding.phoneNumber,
                    isMuted = uiState.isMuted,
                    currentAudioRoute = uiState.audioRoute.name,
                    callDuration = uiState.callDurationSeconds.formatCallDuration(),
                    onSwapCalls = { CallManager.swap() },
                    onMergeCalls = { CallManager.mergeConference() },
                    onEndCall = { CallManager.disconnectPrimary() },
                    onToggleSpeaker = { CallManager.toggleSpeaker() },
                    onToggleMute = { CallManager.toggleMute() },
                    onToggleBluetooth = { CallManager.toggleBluetooth() }
                )
            }

            /* ---------------- NONE ---------------- */
            CallScreenType.NONE -> {
                Box(Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun VideoSurface(
    videoCall: InCallService.VideoCall?,
    isPreview: Boolean,
    onSurfaceReady: (() -> Unit)? = null
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            SurfaceView(context).apply {
                if (isPreview) setZOrderMediaOverlay(true)

                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        if (isPreview) {
                            videoCall?.setPreviewSurface(holder.surface)
                        } else {
                            videoCall?.setDisplaySurface(holder.surface)
                        }
                        onSurfaceReady?.invoke()
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        if (holder.surface.isValid) {
                            if (isPreview) {
                                videoCall?.setPreviewSurface(holder.surface)
                            } else {
                                videoCall?.setDisplaySurface(holder.surface)
                            }
                        }
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        if (isPreview) {
                            videoCall?.setPreviewSurface(null)
                        } else {
                            videoCall?.setDisplaySurface(null)
                        }
                    }
                })
            }
        },
        update = { view ->
            if (view.holder.surface.isValid) {
                if (isPreview) {
                    videoCall?.setPreviewSurface(view.holder.surface)
                } else {
                    videoCall?.setDisplaySurface(view.holder.surface)
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CallScreenPreview() {
    val navState = rememberNavigationState(
        startRoute = Screen.Keypad,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = Navigator(navState)
    PhoneTheme {
        CallScreen(navigator)
    }
}
