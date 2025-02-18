package com.coderon.phone.ui.screens.incallui

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.services.NoCall
import com.coderon.phone.call.services.SingleCall
import com.coderon.phone.call.services.TwoCalls
import com.coderon.phone.ui.utils.extentions.State
import com.coderon.phone.ui.utils.extentions.audioManager
import com.coderon.phone.ui.utils.extentions.getCallerNumber
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CallScreen(
    navController: NavController, context: Context = LocalContext.current
) {
    val callState by CallManager.phoneState.collectAsStateWithLifecycle()
    val currentCallState by CallManager.callState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // Log phone state changes
    LaunchedEffect(callState) {
        Log.d("CallScreen", "Call state changed: $callState")
    }

    // Listen for call events (like call ended)
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            CallManager.callEvents.collectLatest { event ->
                Log.d("CallScreen", "Call event received: $event")
                if (event == "Call Rejected" || event == "Call Ended") {
                    navController.popBackStack()
                }
            }
        }
    }

    when (callState) {
        is NoCall -> {
            Log.d("CallScreen", "No active call, navigating back.")
            navController.popBackStack()
        }

        is SingleCall -> {
            val phoneNumber = (callState as SingleCall).call.getCallerNumber() ?: "Unknown"
            val contact = CallManager.getContactByPhoneNumber(phoneNumber, context)
            when (currentCallState) {
                State.RINGING -> IncomingCallScreen(
                    name = contact?.name,
                    phoneNumber = phoneNumber,
                    onAnswer = {
                        coroutineScope.launch {
                            Log.d("CallScreen", "Answering call")
                            CallManager.acceptCall()
                        }
                    },
                    onDecline = {
                        coroutineScope.launch {
                            Log.d("CallScreen", "Rejecting call")
                            CallManager.rejectCall()
                        }
                    })

                State.DISCONNECTING, State.ENDED -> {
                    Log.d("CallScreen", "Call disconnected, navigating back")
                    navController.popBackStack()
                }

                else -> OngoingCallScreen(
                    contactName = contact?.name.orEmpty(),
                    contactPhoneNumber = phoneNumber,
                    state = currentCallState,
                    profilePictureUrl = contact?.profilePictureUrl,
                    onEndCall = {
                        coroutineScope.launch {
                            Log.d("CallScreen", "Ending active call")
                            CallManager.rejectCall()
                        }
                    },
                    onToggleSpeaker = {
                       toggleSpeakerMode(context)
                    },
                    onToggleHold = {
                        CallManager.toggleHold()
                    },
                    onToggleMute = {
                        toggleMute(context)
                    })
            }
        }

        is TwoCalls -> {
            Log.d("CallScreen", "Two active calls detected")
            OutgoingCallScreen(
                contactName = "Caller Name",
                contactPhoneNumber = "1234567890",
                state = "On Hold",
                onEndCall = {
                    Log.d("CallScreen", "Swapping calls")
                    CallManager.swapCalls()
                })
        }
    }
}

/**
 * Toggles the speaker mode during a call.
 */
private fun toggleSpeakerMode(context: Context) {
    val audioManager = context.audioManager
    val isSpeakerOn = audioManager.isSpeakerphoneOn
    audioManager.isSpeakerphoneOn = !isSpeakerOn
    Log.d("CallScreen", "Speaker mode toggled: ${!isSpeakerOn}")
}

/**
 * Toggles the mute state during a call.
 */
private fun toggleMute(context: Context) {
    val audioManager = context.audioManager
    val isMuted = audioManager.isMicrophoneMute
    audioManager.isMicrophoneMute = !isMuted
    Log.d("CallScreen", "Mute state toggled: ${!isMuted}")
}