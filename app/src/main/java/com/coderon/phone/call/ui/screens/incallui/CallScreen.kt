package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.ui.utils.extentions.State

@Composable
fun CallScreen(
    navController: NavController
) {
    val uiState by CallManager.uiState.collectAsState()

    when (uiState.screen) {

        /* ---------------- INCOMING ---------------- */

        CallScreenType.INCOMING -> {
            val call = uiState.primaryCall ?: return

            IncomingCallScreen(
                name = call.displayName,
                phoneNumber = call.phoneNumber,
                profilePictureUrl = null, // later from contact resolver
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
                state = State.ACTIVE,
                currentAudioRoute = uiState.audioRoute.ordinal,
                isMuted = uiState.isMuted,
                callDuration = uiState.callDurationSeconds.toString(),
                onEndCall = { CallManager.reject() },
                onToggleSpeaker = { },
                onToggleMute = { },
                onToggleHold = { CallManager.hold() },
                onToggleBluetooth = { },
                playDfmTones = { }
            )
        }

        /* ---------------- CALL WAITING ---------------- */

        CallScreenType.CALL_WAITING -> {
            uiState.primaryCall ?: return
            uiState.secondaryCall ?: return

//            CallWaitingScreen(
//                active = active,
//                waiting = waiting,
//                onAcceptWaiting = { },
//                onRejectWaiting = { },
//                onSwap = { },
//                activeName = TODO(),
//                activeNumber = TODO(),
//                waitingName = TODO(),
//                waitingNumber = TODO(),
//                profilePictureUrl = TODO(),
//                onEndActiveAcceptWaiting = TODO()
        }

        /* ---------------- CONFERENCE ---------------- */

        CallScreenType.CONFERENCE -> {
//            ConferenceCallScreen(
//                uiState = uiState,
//                onMerge = { CallManager.mergeConference() },
//                onSwap = { CallManager.swapCalls() },
//                onEnd = { CallManager.reject() }
//            )
        }

        /* ---------------- NONE ---------------- */

        CallScreenType.NONE -> {
            navController.popBackStack()
        }
    }
}
