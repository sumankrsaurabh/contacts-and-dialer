package com.coderon.phone.ui.screens.incallui

import android.telecom.CallAudioState
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.extentions.State
import kotlinx.coroutines.delay

@Composable
fun OngoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    state: State,
    currentAudioRoute: Int,
    isMuted: Boolean,
    callType: String = "HD",
    simInfo: String = "SIM 1 - Jio",
    callDuration: String = "00:07:59",
    profilePictureUrl: String? = null,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleBluetooth: () -> Unit,
    bluetoothDeviceConnected: Boolean = true,
    playDfmTones: (Char) -> Unit
) {
    var isNumpadActive by remember { mutableStateOf(false) }
    var isButtonActive by remember { mutableStateOf(true) }
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl.takeIf { !it.isNullOrEmpty() }
                    ?: R.drawable.background_incallui).placeholder(R.drawable.background_incallui)
                .error(R.drawable.background_incallui).crossfade(true).build(),
            contentDescription = "Contact Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .blur(4.dp),
            contentScale = ContentScale.Crop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(.25f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(profilePictureUrl.takeIf { !it.isNullOrEmpty() }
                        ?: R.drawable.profile_picture_call)
                    .placeholder(R.drawable.profile_picture_call)
                    .error(R.drawable.profile_picture_call) // Ensures fallback if loading fails
                    .crossfade(true).build(),
                contentDescription = "Contact Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer))

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = contactName, fontSize = 24.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = contactPhoneNumber, fontSize = 18.sp, color = Color.White.copy(.8f))
            Spacer(modifier = Modifier.height(8.dp))
            // Call Type (HD, VoLTE, Wi-Fi) and SIM Info Display
            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = callType,  // e.g., "HD"
                    fontSize = 16.sp,
                    color = Color.White.copy(.8f)
                )
                Text(
                    text = simInfo,  // e.g., "SIM 1 - Jio"
                    fontSize = 16.sp,
                    color = Color.White.copy(.8f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(callDuration, fontSize = 16.sp, color = Color.White.copy(.8f))
            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(
                visible = state != State.CONNECTING && state != State.DIALING && isButtonActive && !isNumpadActive
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButtonWithSelection(
                            onClick = { /*TODO*/ },
                            isSelected = false,
                            iconRes = R.drawable.plus,
                            contentDescription = "Add Call",
                        )
                        IconButtonWithSelection(
                            onClick = { onToggleHold() },
                            isSelected = state == State.HOLD,
                            iconRes = R.drawable.pause,
                            contentDescription = "Hold Call",
                        )
                        IconButtonWithSelection(
                            onClick = { /*TODO*/ },
                            isSelected = false,
                            iconRes = R.drawable.record,
                            contentDescription = "Record Call",
                        )
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Speaker Button
                        AudioRouteButton(
                            route = CallAudioState.ROUTE_SPEAKER,
                            currentAudioRoute = currentAudioRoute,
                            iconRes = R.drawable.volume_high,
                            contentDescription = "Speaker",
                            onClick = onToggleSpeaker
                        )

                        // Mute Button
                        IconButtonWithSelection(
                            isSelected = isMuted,
                            iconRes = R.drawable.mute,
                            contentDescription = "mute",
                            onClick = { onToggleMute() })

                        // Bluetooth Button
                        AudioRouteButton(
                            route = CallAudioState.ROUTE_BLUETOOTH,
                            currentAudioRoute = currentAudioRoute,
                            iconRes = R.drawable.bluetooth,
                            contentDescription = "Bluetooth",
                            onClick = onToggleBluetooth,
                            enabled = bluetoothDeviceConnected
                        )
                    }
                }
            }
            
            AnimatedVisibility(state != State.CONNECTING && state != State.DIALING && isNumpadActive && !isButtonActive) {
                TextButtonsForDfmTones(onClick = { playDfmTones(it) })
            }
            Spacer(modifier = Modifier.height(48.dp))

            // End Call Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnimatedVisibility(state != State.CONNECTING && state != State.DIALING) {
                        IconButtonWithSelection(
                            isSelected = isNumpadActive,
                            iconRes = R.drawable.keypad,
                            contentDescription = "",
                            onClick = {
                                isNumpadActive = !isNumpadActive
                                isButtonActive = !isNumpadActive
                            })
                    }
                    IconButton(
                        onClick = { onEndCall.invoke() },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Red, contentColor = Color.White
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.end_call),
                            contentDescription = "End Call",
                            modifier = Modifier.size(32.dp)
                        )
                    }/* Text(
                         "End", color = Color.White
                     )*/
                    AnimatedVisibility(state != State.CONNECTING && state != State.DIALING) {
                        IconButtonWithSelection(
                            isSelected = false,
                            iconRes = if (!isButtonActive) R.drawable.arrow_up else R.drawable.arrow_down,
                            contentDescription = "",
                            onClick = {
                                isButtonActive = !isButtonActive
                                isNumpadActive = !isButtonActive
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun AudioRouteButton(
    route: Int,
    currentAudioRoute: Int,
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val isSelected = route == currentAudioRoute
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { onClick() }, colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (isSelected) Color.Black else Color.White,
                containerColor = if (isSelected) Color.White.copy(.9f) else Color.Black.copy(.25f)
            ), enabled = enabled, modifier = Modifier.size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            contentDescription, color = Color.White
        )
    }
}

@Composable
fun IconButtonWithSelection(
    isSelected: Boolean,
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = { onClick() }, colors = IconButtonDefaults.iconButtonColors(
                contentColor = if (isSelected) Color.Black else Color.White,
                containerColor = if (isSelected) Color.White.copy(.9f) else Color.Black.copy(.25f)
            ), enabled = enabled, modifier = Modifier.size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(contentDescription, color = Color.White)
    }
}

@Composable
fun TextButtonsForDfmTones(
    onClick: (Char) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        listOf("123", "456", "789", "*0#").forEach { digitRow ->
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                digitRow.forEach { digit ->
                    var isSelected by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = {
                            isSelected = true
                            onClick(digit)
                        }, colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (isSelected) Color.Black else Color.White,
                            containerColor = if (isSelected) Color.White.copy(.9f) else Color.Black.copy(
                                .25f
                            )
                        ), modifier = Modifier.size(64.dp)
                    ) {
                        Text(digit.toString(), fontSize = 24.sp)
                    }

                    LaunchedEffect(isSelected) {
                        if (isSelected) {
                            delay(300)
                            isSelected = false
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_9")
@Composable
private fun Test() {
    OngoingCallScreen(
        contactName = "Suman Kumar Saurabh",
        contactPhoneNumber = "+91 7808140285",
        onEndCall = { /*TODO*/ },
        onToggleSpeaker = { /*TODO*/ },
        onToggleMute = { /*TODO*/ },
        onToggleHold = { /*TODO*/ },
        state = State.ACTIVE,
        currentAudioRoute = CallAudioState.ROUTE_SPEAKER,
        onToggleBluetooth = {},
        isMuted = true,
        playDfmTones = {})
}
