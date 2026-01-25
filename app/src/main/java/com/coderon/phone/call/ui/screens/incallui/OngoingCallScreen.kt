@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

import android.telecom.CallAudioState
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
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
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
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import com.coderon.phone.ui.utils.extentions.State

/* ------------------------------------------------
   ONGOING CALL – iOS + OneUI 8 + M3 HYBRID
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
                .padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- IDENTITY SECTION (OneUI 8 Reachability) ---------- */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Info Badge (M3 / OneUI 8)
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "$simInfo • $callType",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Avatar with Soft Glass Rim
                OngoingCallAvatar(
                    name = contactName.ifBlank { contactPhoneNumber },
                    photoUrl = profilePictureUrl
                )

                Spacer(Modifier.height(36.dp))

                Text(
                    text = contactName.ifBlank { contactPhoneNumber },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 40.sp
                )

                Spacer(Modifier.height(10.dp))

                // Time Duration Pill (iOS style)
                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = callDuration,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            /* ---------- CONTROLS (iOS Grid + OneUI 8 Shapes) ---------- */
            AnimatedVisibility(
                visible = !showKeypad,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    // Control Grid Row 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallActionCircle(
                            icon = Icons.Rounded.MicOff,
                            label = "Mute",
                            active = isMuted,
                            onClick = onToggleMute
                        )
                        InCallActionCircle(
                            icon = Icons.Rounded.Dialpad,
                            label = "Keypad",
                            active = false,
                            onClick = { showKeypad = true }
                        )
                        InCallActionCircle(
                            icon = Icons.AutoMirrored.Rounded.VolumeUp,
                            label = "Speaker",
                            active = currentAudioRoute == CallAudioState.ROUTE_SPEAKER,
                            onClick = onToggleSpeaker
                        )
                    }

                    // Control Grid Row 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InCallActionCircle(
                            icon = Icons.Rounded.Bluetooth,
                            label = "Bluetooth",
                            active = currentAudioRoute == CallAudioState.ROUTE_BLUETOOTH,
                            enabled = bluetoothDeviceConnected,
                            onClick = onToggleBluetooth
                        )
                        InCallActionCircle(
                            icon = Icons.Rounded.Pause,
                            label = "Hold",
                            active = state == State.HOLD,
                            onClick = onToggleHold
                        )
                        InCallActionCircle(
                            icon = Icons.Rounded.MoreVert,
                            label = "More",
                            active = false,
                            onClick = {}
                        )
                    }

                    Spacer(Modifier.height(40.dp))

                    // End Call (Prominent iOS Red)
                    EndCallButton(onClick = onEndCall)
                }
            }

            /* ---------- KEYPAD OVERLAY ---------- */
            AnimatedVisibility(
                visible = showKeypad,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OngoingDialPad { playDfmTones(it) }
                    Spacer(Modifier.height(48.dp))
                    // iOS-style text button to hide keypad
                    Text(
                        "Hide",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { showKeypad = false }
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OngoingCallAvatar(name: String, photoUrl: String?) {
    Box(contentAlignment = Alignment.Center) {
        // Glass Rim
        Surface(
            modifier = Modifier.size(136.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.08f)
        ) {}
        Surface(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            color = Color.White.copy(alpha = 0.12f)
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
                        fontSize = 56.sp,
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
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EndCallButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(80.dp),
        shape = CircleShape,
        color = Color(0xFFFF3B30), // iOS Red
        shadowElevation = 12.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(R.drawable.end_call),
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
    }
}

@Composable
private fun OngoingDialPad(onDigit: (Char) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
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
                            Text(digit.toString(), fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Normal)
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
