package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.coderon.phone.call.domain.CallState
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.CallScreenType
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

    when (uiState.screen) {

        /* ---------------- INCOMING ---------------- */

        CallScreenType.INCOMING -> {
            val call = uiState.primaryCall ?: return

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
            val call = uiState.primaryCall ?: return

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
             val active = uiState.primaryCall ?: return
             val waiting = uiState.secondaryCall ?: return
             
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
            val call = uiState.primaryCall ?: return
            VideoCallUI(
                contactName = call.displayName ?: call.phoneNumber,
                callDuration = uiState.callDurationSeconds.formatCallDuration(),
                isMuted = uiState.isMuted,
                isVideoEnabled = uiState.isVideo,
                onEndCall = { CallManager.disconnectPrimary() },
                onToggleMute = { CallManager.toggleMute() },
                onToggleVideo = { CallManager.toggleVideo() },
                onFlipCamera = { CallManager.flipCamera() }
            )
        }

        /* ---------------- TWO CALLS ---------------- */

        CallScreenType.TWO_CALLS -> {
            val active = uiState.primaryCall ?: return
            val holding = uiState.secondaryCall ?: return
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
             navController.popBackStack()
        }
    }
}
