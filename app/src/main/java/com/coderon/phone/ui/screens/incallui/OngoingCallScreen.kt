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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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

@Composable
fun OngoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    state: State,
    currentAudioRoute: Int,
    isMuted: Boolean,
    callDuration: String = "00:07:59",
    profilePictureUrl: String? = null,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleBluetooth: () -> Unit,
    bluetoothDeviceConnected: Boolean = true
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
            .data(profilePictureUrl.takeIf { !it.isNullOrEmpty() }
                ?: R.drawable.profile_picture_call).placeholder(R.drawable.profile_picture_call)
            .error(R.drawable.profile_picture_call).crossfade(true).build(),
            contentDescription = "Contact Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .blur(2.dp),
            contentScale = ContentScale.Crop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(.3f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = contactName, fontSize = 24.sp, color = Color.White

            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = contactPhoneNumber, fontSize = 18.sp, color = Color.White.copy(.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                callDuration, fontSize = 18.sp, color = Color.White.copy(.8f)
            )
            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = state == State.ACTIVE) {
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
                            isSelected = false,
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

            Spacer(modifier = Modifier.height(48.dp))

            // End Call Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                    }
                    Text(
                        "End", color = Color.White
                    )
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

@Preview(showBackground = true)
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
        isMuted = true
    )
}
