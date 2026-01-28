@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.call.ui.AudioRoute
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TwoCallScreen(
    firstContactName: String,
    firstPhoneNumber: String,
    secondContactName: String,
    secondPhoneNumber: String,
    isMuted: Boolean,
    currentAudioRoute: String,
    callDuration: String,
    onSwapCalls: () -> Unit,
    onMergeCalls: () -> Unit,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleBluetooth: () -> Unit
) {
    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(800, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        OneUi8DynamicBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = 48.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* ---------- TOP SECTION: ACTIVE CALL ---------- */
            Spacer(Modifier.height(48.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Surface(
                    color = Color(0xFF2ECC71).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Active Call",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2ECC71)
                    )
                }

                Text(
                    text = firstContactName.ifBlank { firstPhoneNumber },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = callDuration,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(Modifier.height(48.dp))

            /* ---------- MIDDLE SECTION: HELD CALL CAPSULE ---------- */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "On Hold",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF1C40F)
                        )
                        Text(
                            text = secondContactName.ifBlank { secondPhoneNumber },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        onClick = onSwapCalls,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.12f),
                        modifier = Modifier.size(height = 44.dp, width = 90.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapHoriz,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Swap", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            /* ---------- BOTTOM SECTION: CONTROLS ---------- */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ModernTwoCallAction(
                        imageVector = Icons.Rounded.Merge,
                        label = "Merge",
                        active = false,
                        onClick = onMergeCalls
                    )
                    ModernTwoCallAction(
                        imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                        label = "Mute",
                        active = isMuted,
                        onClick = onToggleMute
                    )
                    ModernTwoCallAction(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        label = "Speaker",
                        active = currentAudioRoute == AudioRoute.SPEAKER.name,
                        onClick = onToggleSpeaker
                    )
                    ModernTwoCallAction(
                        imageVector = Icons.Rounded.Bluetooth,
                        label = "Bluetooth",
                        active = currentAudioRoute == AudioRoute.BLUETOOTH.name,
                        onClick = onToggleBluetooth
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Modern End Call Button
                Surface(
                    onClick = onEndCall,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF3B30),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.end_call),
                            contentDescription = "End",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernTwoCallAction(
    imageVector: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = if (active) Color.White else Color.White.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = label,
                    tint = if (active) Color.Black else Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TwoCallScreenPreview() {
    PhoneTheme {
        TwoCallScreen(
            firstContactName = "John Doe",
            firstPhoneNumber = "1234567890",
            secondContactName = "Jane Smith",
            secondPhoneNumber = "0987654321",
            isMuted = false,
            currentAudioRoute = AudioRoute.EARPIECE.name,
            callDuration = "02:15",
            onSwapCalls = {},
            onMergeCalls = {},
            onEndCall = {},
            onToggleSpeaker = {},
            onToggleMute = {},
            onToggleBluetooth = {}
        )
    }
}
