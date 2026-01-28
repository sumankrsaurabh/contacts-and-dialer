package com.coderon.phone.call.ui.screens.incallui

import android.telecom.InCallService
import android.telecom.VideoProfile
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.coderon.phone.call.domain.CallState
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.ui.components.HybridAlertDialog
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.utils.extentions.State
import com.coderon.phone.ui.utils.extentions.formatCallDuration
import com.coderon.phone.ui.utils.extentions.getCallType
import com.coderon.phone.ui.utils.extentions.getSimInfoForCall

@Composable
fun CallScreen(
    navController: NavController
) {
    val uiState by CallManager.uiState.collectAsState()
    val context = LocalContext.current

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

    // Use AnimatedContent for smoother transitions between call screens
    AnimatedContent(
        targetState = uiState.screen,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "call_screen_transition"
    ) { screenType ->
        when (screenType) {

            /* ---------------- INCOMING ---------------- */

            CallScreenType.INCOMING -> {
                val call = uiState.primaryCall ?: return@AnimatedContent

                IncomingCallScreen(
                    name = call.displayName,
                    phoneNumber = call.phoneNumber,
                    profilePictureUrl = call.profilePictureUrl,
                    simInfo = call.call.getSimInfoForCall(context),
                    onAnswer = { CallManager.accept() },
                    onDecline = { CallManager.reject() }
                )
            }

            /* ---------------- ONGOING ---------------- */

            CallScreenType.ONGOING -> {
                val call = uiState.primaryCall ?: return@AnimatedContent

                OngoingCallScreen(
                    contactName = call.displayName ?: call.phoneNumber,
                    contactPhoneNumber = call.phoneNumber,
                    profilePictureUrl = call.profilePictureUrl,
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
                    onEndCall = { CallManager.disconnectPrimary() },
                    onToggleSpeaker = { CallManager.toggleSpeaker() },
                    onToggleMute = { CallManager.toggleMute() },
                    onToggleHold = {
                        if (call.state == CallState.HOLDING) CallManager.unhold()
                        else CallManager.hold()
                    },
                    onToggleBluetooth = { CallManager.toggleBluetooth() },
                    onAddCall = {
                        navController.navigate(Screen.Keypad.route)
                    },
                    onVideoCall = { CallManager.toggleVideo() },
                    playDfmTones = { CallManager.playDtmfTone(it) }
                )
            }

            /* ---------------- CALL WAITING ---------------- */

            CallScreenType.CALL_WAITING -> {
                val active = uiState.primaryCall ?: return@AnimatedContent
                val waiting = uiState.secondaryCall ?: return@AnimatedContent

                CallWaitingScreen(
                    activeName = active.displayName ?: active.phoneNumber,
                    activeNumber = active.phoneNumber,
                    waitingName = waiting.displayName,
                    waitingNumber = waiting.phoneNumber,
                    profilePictureUrl = waiting.profilePictureUrl,
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
                val call = uiState.primaryCall ?: return@AnimatedContent

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
                val active = uiState.primaryCall ?: return@AnimatedContent
                val holding = uiState.secondaryCall ?: return@AnimatedContent
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
                // Should be handled by AppUI.kt auto-navigation
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
