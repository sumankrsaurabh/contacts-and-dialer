@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.AudioRoute
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import com.coderon.phone.ui.utils.extentions.State

/* ------------------------------------------------
   ONGOING CALL – iOS Style Redesign
------------------------------------------------ */

@Composable
fun OngoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    state: State,
    currentAudioRoute: AudioRoute,
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
    onAddCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    bluetoothDeviceConnected: Boolean = true,
    playDfmTones: (Char) -> Unit
) {
    var showKeypad by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Shared Dynamic Background
        OneUi8DynamicBackground()

        /* ---------- MAIN CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- IDENTITY SECTION (iOS Style Top) ---------- */
            Spacer(Modifier.height(48.dp))
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = contactName.ifBlank { contactPhoneNumber },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = if (state == State.HOLD) "on hold" else callDuration,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                if (simInfo.isNotEmpty()) {
                    Text(
                        text = "$simInfo • $callType",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Dynamic Center Area: Avatar or Keypad
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!showKeypad) {
                    OngoingCallAvatar(
                        name = contactName.ifBlank { contactPhoneNumber },
                        photoUrl = profilePictureUrl
                    )
                } else {
                    OngoingDialPad { 
                        playDfmTones(it)
                        CallManager.playDtmfTone(it)
                    }
                }
            }

            /* ---------- CONTROLS (Always Showing End Call) ---------- */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Primary Action Grid (Visible when keypad is hidden)
                AnimatedVisibility(
                    visible = !showKeypad,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallActionCircle(
                                icon = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                label = "mute",
                                active = isMuted,
                                onClick = onToggleMute
                            )
                            InCallActionCircle(
                                icon = Icons.Rounded.Dialpad,
                                label = "keypad",
                                active = false,
                                onClick = { showKeypad = true }
                            )
                            InCallActionCircle(
                                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                                label = "speaker",
                                active = currentAudioRoute == AudioRoute.SPEAKER,
                                onClick = onToggleSpeaker
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InCallActionCircle(
                                icon = Icons.Rounded.Add,
                                label = "add call",
                                active = false,
                                onClick = onAddCall
                            )
                            InCallActionCircle(
                                icon = Icons.Rounded.VideoCall,
                                label = "video call",
                                active = false,
                                onClick = onVideoCall
                            )
                            InCallActionCircle(
                                icon = if (state == State.HOLD) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                label = if (state == State.HOLD) "resume" else "hold",
                                active = state == State.HOLD,
                                onClick = onToggleHold
                            )
                        }
                    }
                }

                // "Hide" button for keypad
                if (showKeypad) {
                    Text(
                        "Hide",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(vertical = 24.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { showKeypad = false }
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                } else {
                    // Audio route row (Bluetooth etc)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        InCallActionCircle(
                            icon = Icons.Rounded.Bluetooth,
                            label = "bluetooth",
                            active = currentAudioRoute == AudioRoute.BLUETOOTH,
                            enabled = bluetoothDeviceConnected,
                            onClick = onToggleBluetooth
                        )
                    }
                }

                // Always visible End Call Button
                EndCallButton(onClick = onEndCall)
            }
        }
    }
}

@Composable
private fun OngoingCallAvatar(name: String, photoUrl: String?) {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape),
            color = Color.White.copy(alpha = 0.1f)
        ) {
            if (!photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = name.firstOrNull()?.uppercase() ?: "?",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Light,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun InCallActionCircle(
    icon: ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(if (enabled) 1f else 0.4f)
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = if (active) Color.White else Color.White.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) Color.Black else Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
private fun EndCallButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(76.dp),
        shape = CircleShape,
        color = Color(0xFFFF3B30), // iOS Red
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.end_call),
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
            )
        }
    }
}

@Composable
private fun OngoingDialPad(onDigit: (Char) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 32.dp)
    ) {
        listOf("123", "456", "789", "*0#").forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    Surface(
                        onClick = { onDigit(digit) },
                        modifier = Modifier.size(76.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(digit.toString(), fontSize = 36.sp, color = Color.White, fontWeight = FontWeight.Normal)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OngoingCallScreenPreview() {
    PhoneTheme {
        OngoingCallScreen(
            contactName = "Sarah Johnson",
            contactPhoneNumber = "+1 234 567 8900",
            state = State.ACTIVE,
            currentAudioRoute = AudioRoute.EARPIECE,
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
