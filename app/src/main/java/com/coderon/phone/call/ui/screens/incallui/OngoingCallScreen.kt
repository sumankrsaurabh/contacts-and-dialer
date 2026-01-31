package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.VideoCall
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.AudioRoute
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import com.coderon.phone.ui.utils.extentions.State
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    backgroundUri: String? = null,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
    onToggleBluetooth: () -> Unit,
    onAddCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onRecordCall: () -> Unit = {},
    onAddNote: () -> Unit = {},
    isRecording: Boolean = false,
    playDfmTones: (Char) -> Unit
) {
    var showKeypad by remember { mutableStateOf(false) }

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(800, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    // Logic for button states
    val isConnectingOrDialing = state == State.DIALING || state == State.CONNECTING
    val isActive = state == State.ACTIVE
    val isOnHold = state == State.HOLD

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!backgroundUri.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(backgroundUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay for readability
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        } else {
            OneUi8DynamicBackground()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 48.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- IDENTITY SECTION ---------- */
            Spacer(Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = contactName.ifBlank { contactPhoneNumber },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = (1).sp
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(Modifier.padding(horizontal = 4.dp))
                    }
                    Text(
                        text = when (state) {
                            State.HOLD -> "On Hold"
                            State.DIALING -> "Dialing..."
                            State.CONNECTING -> "Connecting..."
                            State.DISCONNECTING -> "Disconnecting..."
                            else -> callDuration
                        },
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isOnHold) Color(0xFFF1C40F) else Color.White.copy(alpha = 0.7f)
                    )
                }

                if (simInfo.isNotEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = "$simInfo • $callType",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            /* ---------- CENTER AREA ---------- */
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (!showKeypad) {
                    OngoingCallAvatar(
                        name = contactName.ifBlank { contactPhoneNumber },
                        photoUrl = profilePictureUrl,
                        isPulsing = isConnectingOrDialing
                    )
                } else {
                    OngoingDialPad { digit ->
                        playDfmTones(digit)
                        CallManager.playDtmfTone(digit)
                    }
                }
            }

            /* ---------- CONTROLS (3x3 Layout) ---------- */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = !showKeypad,
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(400))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Row 1
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ModernInCallAction(
                                imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                label = "Mute",
                                active = isMuted,
                                enabled = isActive,
                                onClick = onToggleMute
                            )
                            ModernInCallAction(
                                imageVector = Icons.Rounded.Bluetooth,
                                label = "Bluetooth",
                                active = currentAudioRoute == AudioRoute.BLUETOOTH,
                                onClick = onToggleBluetooth
                            )
                            ModernInCallAction(
                                imageVector = Icons.Rounded.VideoCall,
                                label = "Video call",
                                active = false,
                                enabled = isActive,
                                onClick = onVideoCall
                            )
                        }
                        // Row 2
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ModernInCallAction(
                                imageVector = Icons.Rounded.Add,
                                label = "Add call",
                                active = false,
                                enabled = isActive,
                                onClick = onAddCall
                            )
                            ModernInCallAction(
                                imageVector = if (isOnHold) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                                label = if (isOnHold) "Resume" else "Hold",
                                active = isOnHold,
                                enabled = isActive || isOnHold,
                                onClick = onToggleHold
                            )
                            ModernInCallAction(
                                imageVector = Icons.Rounded.RadioButtonChecked,
                                label = if (isRecording) "Stop" else "Record",
                                active = isRecording,
                                enabled = isActive,
                                activeColor = Color.Red,
                                onClick = onRecordCall
                            )
                        }
                        // Row 3: Speaker (bottomleft), End call (center), Keypad (bottomright)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ModernInCallAction(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                label = "Speaker",
                                active = currentAudioRoute == AudioRoute.SPEAKER,
                                onClick = onToggleSpeaker
                            )
                            
                            // Reusable End Call Button with Label
                            EndCallWithLabel(onClick = onEndCall)

                            ModernInCallAction(
                                imageVector = Icons.Rounded.Dialpad,
                                label = "Keypad",
                                active = false,
                                onClick = { showKeypad = true }
                            )
                        }
                    }
                }

                if (showKeypad) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Surface(
                            onClick = { showKeypad = false },
                            color = Color.White.copy(alpha = 0.15f),
                            shape = CircleShape
                        ) {
                            Text(
                                "Hide Keypad",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                            )
                        }

                        Spacer(Modifier.height(24.dp))

                        // End Call button visible when dialpad is shown - now identical to the one in grid
                        EndCallWithLabel(onClick = onEndCall)
                    }
                } else {
                    // Bottom spacer
                    Spacer(Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun EndCallWithLabel(onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = Color(0xFFFF3B30),
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painterResource(R.drawable.end_call),
                    contentDescription = "End",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "End",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OngoingCallAvatar(name: String, photoUrl: String?, isPulsing: Boolean = false) {
    // Pulse animation logic for dialing/connecting
    val pulseAlpha = remember { Animatable(0.2f) }
    if (isPulsing) {
        LaunchedEffect(Unit) {
            while (true) {
                pulseAlpha.animateTo(0.6f, tween(1000, easing = LinearEasing))
                pulseAlpha.animateTo(0.2f, tween(1000, easing = LinearEasing))
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        if (isPulsing) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(Color.White.copy(alpha = pulseAlpha.value), CircleShape)
            )
        }

        Surface(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            color = Color.White.copy(alpha = 0.08f),
            shadowElevation = 4.dp
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
                        fontSize = 84.sp,
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernInCallAction(
    imageVector: ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean = true,
    activeColor: Color = Color.White,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.alpha(alpha)
    ) {
        Surface(
            onClick = { if (enabled) onClick() },
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = if (active) activeColor else Color.White.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = label,
                    tint = if (active) (if (activeColor == Color.White) Color.Black else Color.White) else Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun OngoingDialPad(onDigit: (Char) -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf("123", "456", "789", "*0#").forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    Surface(
                        onClick = { onDigit(digit) },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                digit.toString(),
                                fontSize = 32.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Light
                            )
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
            playDfmTones = {}
        )
    }
}
