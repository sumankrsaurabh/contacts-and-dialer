package com.coderon.phone.call.ui.screens.incallui

import android.telecom.CallAudioState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.extentions.State

/* ------------------------------------------------
   ANDROID ONGOING CALL · iOS STYLE
------------------------------------------------ */

@Composable
fun OngoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    state: State,
    currentAudioRoute: Int,
    callDuration: String = "00:00",
    isMuted: Boolean,
    callType: String = "HD",
    simInfo: String = "",
    profilePictureUrl: String? = null,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleBluetooth: () -> Unit,
    bluetoothDeviceConnected: Boolean = true,
    playDfmTones: (Char) -> Unit
) {
    var showKeypad by remember { mutableStateOf(false) }


    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- BACKGROUND ---------- */
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(profilePictureUrl ?: R.drawable.background_incallui)
//                .crossfade(true)
//                .build(),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxSize()
//                .blur(22.dp),
//            contentScale = ContentScale.Crop
//        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
        )

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- CALL INFO ---------- */


            Text(
                text = contactName.ifBlank { contactPhoneNumber },
                fontSize = 28.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (contactName.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = contactPhoneNumber,
                    fontSize = 18.sp,
                    color = Color.White.copy(0.75f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                callDuration, fontSize = 16.sp, color = Color.White.copy(0.85f),
                modifier = Modifier
                    .background(Color.White.copy(.25f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            Spacer(Modifier.weight(1f))

            /* ---------- BUTTON GRID ---------- */
            AnimatedVisibility(
                visible = !showKeypad,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = R.drawable.volume_high,
                            label = "Speaker",
                            active = currentAudioRoute == CallAudioState.ROUTE_SPEAKER,
                            onClick = onToggleSpeaker
                        )

                        ActionButton(
                            icon = R.drawable.bluetooth,
                            label = "Bluetooth",
                            active = currentAudioRoute == CallAudioState.ROUTE_BLUETOOTH,
                            enabled = bluetoothDeviceConnected,
                            onClick = onToggleBluetooth
                        )

                        ActionButton(
                            icon = R.drawable.mute,
                            label = "Mute",
                            active = isMuted,
                            onClick = onToggleMute
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ActionButton(
                            icon = R.drawable.plus,
                            label = "More",
                            active = false,
                            onClick = {}
                        )

                        EndCallButton(onClick = onEndCall)

                        ActionButton(
                            icon = R.drawable.keypad,
                            label = "Keypad",
                            active = showKeypad,
                            onClick = { showKeypad = true }
                        )
                    }
                }
            }

            /* ---------- KEYPAD ---------- */
            AnimatedVisibility(
                visible = showKeypad,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                DialPad { playDfmTones(it) }
            }
        }
    }
}

/* ------------------------------------------------
   ACTION BUTTON
------------------------------------------------ */

@Composable
private fun ActionButton(
    icon: Int,
    label: String,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            enabled = enabled,
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (active) Color.White else Color.White.copy(alpha = 0.18f)
                )
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = if (active) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 14.sp, color = Color.White)
    }
}

/* ------------------------------------------------
   END CALL
------------------------------------------------ */

@Composable
private fun EndCallButton(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF3B30))
        ) {
            Icon(
                painter = painterResource(R.drawable.end_call),
                contentDescription = "End",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text("End", fontSize = 14.sp, color = Color.White)
    }
}

/* ------------------------------------------------
   DIAL PAD (DTMF)
------------------------------------------------ */

@Composable
private fun DialPad(onDigit: (Char) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf("123", "456", "789", "*0#").forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    IconButton(
                        onClick = { onDigit(digit) },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                    ) {
                        Text(digit.toString(), fontSize = 24.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun OngoingCallScreenPreview() {
    PhoneTheme {
        OngoingCallScreen(
            contactName = "John Doe",
            contactPhoneNumber = "+1 234 567 8900",
            state = State.ACTIVE,
            currentAudioRoute = CallAudioState.ROUTE_EARPIECE,
            isMuted = false,
            callType = "HD",
            simInfo = "Sim 1",
            profilePictureUrl = null,
            onEndCall = {},
            onToggleSpeaker = {},
            onToggleMute = {},
            onToggleHold = {},
            onToggleBluetooth = {},
            bluetoothDeviceConnected = true,
            playDfmTones = {}
        )
    }
}
