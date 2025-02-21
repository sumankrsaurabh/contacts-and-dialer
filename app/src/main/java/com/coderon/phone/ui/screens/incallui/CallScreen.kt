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
import com.coderon.phone.call.services.CallManager.sendDtmfTone
import com.coderon.phone.call.services.CallManager.stopDtmfTone
import com.coderon.phone.call.services.NoCall
import com.coderon.phone.call.services.SingleCall
import com.coderon.phone.call.services.TwoCalls
import com.coderon.phone.ui.utils.extentions.AudioRoute
import com.coderon.phone.ui.utils.extentions.State
import com.coderon.phone.ui.utils.extentions.formatCallDuration
import com.coderon.phone.ui.utils.extentions.getCallerNumber
import com.coderon.phone.ui.utils.extentions.isBluetoothAvailable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CallScreen(
    navController: NavController, context: Context = LocalContext.current
) {
    val callState by CallManager.phoneState.collectAsStateWithLifecycle()
    val currentCallState by CallManager.callState.collectAsStateWithLifecycle()
    val currentAudioRoute by CallManager.currentAudioRoute.collectAsStateWithLifecycle()
    val isMuted by CallManager.isMuted.collectAsStateWithLifecycle()
    val callDuration by CallManager.callDuration.collectAsStateWithLifecycle()
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
                    profilePictureUrl = contact?.profilePictureUrl,
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
                    currentAudioRoute = currentAudioRoute,
                    isMuted = isMuted,
                    profilePictureUrl = contact?.profilePictureUrl,
                    callDuration = callDuration.formatCallDuration(),
                    bluetoothDeviceConnected = isBluetoothAvailable(context),
                    onEndCall = {
                        coroutineScope.launch {
                            Log.d("CallScreen", "Ending active call")
                            CallManager.rejectCall()
                        }
                    },
                    onToggleSpeaker = {
                        CallManager.switchAudioRoute(
                            if (currentAudioRoute == AudioRoute.SPEAKER.value) AudioRoute.EARPIECE
                            else AudioRoute.SPEAKER
                        )
                    },
                    onToggleHold = {
                        CallManager.toggleHold()
                    },
                    onToggleMute = {
                        CallManager.toggleMute()
                    },
                    onToggleBluetooth = {
                        CallManager.switchAudioRoute(
                            if (currentAudioRoute == AudioRoute.BLUETOOTH.value) AudioRoute.EARPIECE
                            else AudioRoute.BLUETOOTH
                        )
                    },
                    playDfmTones = {
                        coroutineScope.launch {
                            sendDtmfTone(it) // Send DTMF tone
                            delay(200) // Simulate keypress delay
                            stopDtmfTone() // Stop DTMF tone
                        }
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