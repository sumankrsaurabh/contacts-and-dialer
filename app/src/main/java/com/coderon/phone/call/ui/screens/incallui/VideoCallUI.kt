package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun VideoCallUI(
    contactName: String,
    callDuration: String,
    remoteVideoSurface: @Composable (() -> Unit)? = null,
    localVideoSurface: @Composable (() -> Unit)? = null,
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    isFrontCamera: Boolean = true,
    isActive: Boolean = true,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit
) {
    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(800, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // Remote Video (Full Screen)
        if (remoteVideoSurface != null && isVideoEnabled) {
            Box(modifier = Modifier.fillMaxSize()) {
                remoteVideoSurface()
            }
        } else {
            OneUi8DynamicBackground()
            // Show avatar or name if no remote video
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(entryAlpha.value)
                    .offset { IntOffset(0, entryOffset.value.roundToInt()) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = contactName.firstOrNull()?.uppercase() ?: "?",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White
                        )
                    }
                }
            }
        }

        /* ---------- TOP BAR (Flip + Info) ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) }
        ) {
            // Flip Camera Top Left
            Surface(
                onClick = onFlipCamera,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Cameraswitch,
                        contentDescription = "Flip",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Center Info
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = contactName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (isActive) callDuration else "Connecting...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Local Video Preview (Picture-in-Picture)
        AnimatedVisibility(
            visible = isVideoEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 24.dp)
        ) {
            Surface(
                modifier = Modifier
                    .size(110.dp, 160.dp)
                    .graphicsLayer(scaleX = if (isFrontCamera) -1f else 1f),
                shape = RoundedCornerShape(16.dp),
                color = Color.DarkGray,
                shadowElevation = 8.dp
            ) {
                if (localVideoSurface != null) {
                    localVideoSurface()
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) }
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute
                VideoActionCircle(
                    icon = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                    active = isMuted,
                    enabled = isActive,
                    onClick = onToggleMute
                )

                // End Call
                Surface(
                    onClick = onEndCall,
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF3B30),
                    shadowElevation = 12.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(R.drawable.end_call),
                            contentDescription = "End",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Video Toggle
                VideoActionCircle(
                    icon = if (isVideoEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                    active = !isVideoEnabled,
                    enabled = isActive,
                    onClick = onToggleVideo
                )
            }
        }
    }
}

@Composable
private fun VideoActionCircle(
    icon: ImageVector,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        onClick = { if (enabled) onClick() },
        modifier = Modifier.size(64.dp).alpha(if (enabled) 1f else 0.5f),
        shape = CircleShape,
        color = if (active) Color.White else Color.Black.copy(alpha = 0.3f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (active) Color.Black else Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VideoCallUIPreview() {
    PhoneTheme {
        VideoCallUI(
            contactName = "Sarah Johnson",
            callDuration = "05:24",
            isMuted = false,
            isVideoEnabled = true,
            isFrontCamera = true,
            isActive = true,
            onEndCall = {},
            onToggleMute = {},
            onToggleVideo = {},
            onFlipCamera = {}
        )
    }
}
