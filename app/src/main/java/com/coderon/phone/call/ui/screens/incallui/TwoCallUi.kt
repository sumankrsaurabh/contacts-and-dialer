@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Merge
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.SwapCalls
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import com.coderon.phone.ui.utils.extentions.AudioRoute

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
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            /* ---------- TOP SECTION: ACTIVE CALL ---------- */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Active Call",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = firstContactName.ifBlank { firstPhoneNumber },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = callDuration,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            /* ---------- MIDDLE SECTION: HELD CALL ---------- */
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "On Hold",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Text(
                            text = secondContactName.ifBlank { secondPhoneNumber },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Surface(
                        onClick = onSwapCalls,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapCalls,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text("Swap", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            /* ---------- BOTTOM SECTION: CONTROLS ---------- */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TwoCallActionCircle(
                        icon = Icons.Rounded.Merge,
                        label = "Merge",
                        active = false,
                        onClick = onMergeCalls
                    )
                    TwoCallActionCircle(
                        icon = Icons.Rounded.MicOff,
                        label = "Mute",
                        active = isMuted,
                        onClick = onToggleMute
                    )
                    TwoCallActionCircle(
                        icon = Icons.AutoMirrored.Rounded.VolumeUp,
                        label = "Speaker",
                        active = currentAudioRoute == AudioRoute.SPEAKER.name,
                        onClick = onToggleSpeaker
                    )
                    TwoCallActionCircle(
                        icon = Icons.Rounded.Bluetooth,
                        label = "Bluetooth",
                        active = currentAudioRoute == AudioRoute.BLUETOOTH.name,
                        onClick = onToggleBluetooth
                    )
                }

                Spacer(Modifier.height(16.dp))

                Surface(
                    onClick = onEndCall,
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF3B30)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TwoCallActionCircle(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(64.dp),
            shape = CircleShape,
            color = if (active) Color.White else Color.White.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (active) Color.Black else Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
    }
}
