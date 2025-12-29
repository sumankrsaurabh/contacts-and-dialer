package com.coderon.phone.ui.screens.incallui

import android.annotation.SuppressLint
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
import com.coderon.phone.ui.utils.extentions.getCallType
import com.coderon.phone.ui.utils.extentions.getCallerNumber
import com.coderon.phone.ui.utils.extentions.isBluetoothAvailable
import com.coderon.phone.ui.utils.getSimInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
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
        is SingleCall -> {
            val singleCall = callState as SingleCall
            val phoneNumber = singleCall.call.getCallerNumber() ?: "Unknown"
            val contact = CallManager.getContactByPhoneNumber(phoneNumber, context)

            when (currentCallState) {
                State.RINGING -> IncomingCallScreen(
                    name = contact?.displayName,
                    phoneNumber = phoneNumber,
                    profilePictureUrl = contact?.profilePictureUrl,
                    onAnswer = {
                        coroutineScope.launch {
                            CallManager.acceptCall()
                        }
                    },
                    onDecline = {
                        coroutineScope.launch {
                            CallManager.rejectCall()
                        }
                    }
                )

                State.DISCONNECTING, State.ENDED -> {
                    navController.popBackStack()
                }

                else -> OngoingCallScreen(
                    contactName = contact?.displayName.orEmpty(),
                    contactPhoneNumber = phoneNumber,
                    state = currentCallState,
                    currentAudioRoute = currentAudioRoute,
                    isMuted = isMuted,
                    profilePictureUrl = contact?.profilePictureUrl,
                    callDuration = callDuration.formatCallDuration(),
                    bluetoothDeviceConnected = isBluetoothAvailable(context),
                    callType = getCallType(context),  // ✅ Pass Call Type (VoLTE, HD, Wi-Fi)
                    simInfo = getSimInfo(
                        context,
                        singleCall.call.details.accountHandle
                    ),    // ✅ Pass SIM Info (SIM 1 - Jio)
                    onEndCall = {
                        coroutineScope.launch {
                            CallManager.rejectCall()
                        }
                    },
                    onToggleSpeaker = {
                        CallManager.switchAudioRoute(
                            if (currentAudioRoute == AudioRoute.SPEAKER.value) AudioRoute.EARPIECE
                            else AudioRoute.SPEAKER
                        )
                    },
                    onToggleHold = { CallManager.toggleHold() },
                    onToggleMute = { CallManager.toggleMute() },
                    onToggleBluetooth = {
                        CallManager.switchAudioRoute(
                            if (currentAudioRoute == AudioRoute.BLUETOOTH.value) AudioRoute.EARPIECE
                            else AudioRoute.BLUETOOTH
                        )
                    },
                    playDfmTones = {
                        coroutineScope.launch {
                            sendDtmfTone(it)
                            delay(200)
                            stopDtmfTone()
                        }
                    }
                )
            }
        }

        is TwoCalls -> {
            Log.d("CallScreen", "TwoCalls state received")
        }

        NoCall -> {
            navController.popBackStack()
        }
    }

}