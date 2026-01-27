package com.coderon.phone.call.ui.screens.incallui

import android.view.SurfaceView
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
import androidx.compose.material.icons.rounded.FlipCameraAndroid
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground

@Composable
fun VideoCallUI(
    contactName: String,
    callDuration: String,
    remoteVideoSurface: @Composable (() -> Unit)? = null,
    localVideoSurface: @Composable (() -> Unit)? = null,
    isMuted: Boolean,
    isVideoEnabled: Boolean,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        
        // Remote Video (Full Screen)
        if (remoteVideoSurface != null) {
            Box(modifier = Modifier.fillMaxSize()) {
                remoteVideoSurface()
            }
        } else {
            OneUi8DynamicBackground()
        }

        /* ---------- TOP BAR (Flip + Info) ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Flip Camera Top Left (iOS/OneUI 8 style)
            Surface(
                onClick = onFlipCamera,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.FlipCameraAndroid,
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
                    color = Color.White
                )
                Text(
                    text = callDuration,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Local Video Preview (Picture-in-Picture)
        Surface(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 24.dp)
                .size(120.dp, 180.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.DarkGray,
            shadowElevation = 8.dp
        ) {
            if (isVideoEnabled && localVideoSurface != null) {
                localVideoSurface()
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.VideocamOff,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }

        // Bottom Controls (Always Showing End Call)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
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
                    onClick = onToggleMute
                )

                // End Call (Prominent iOS Red)
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

                // Camera Toggle
                VideoActionCircle(
                    icon = if (isVideoEnabled) Icons.Rounded.Videocam else Icons.Rounded.VideocamOff,
                    active = !isVideoEnabled,
                    onClick = onToggleVideo
                )
            }
        }
    }
}

@Composable
private fun VideoActionCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        shape = CircleShape,
        color = if (active) Color.White else Color.White.copy(alpha = 0.2f)
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
            onEndCall = {},
            onToggleMute = {},
            onToggleVideo = {},
            onFlipCamera = {},
            remoteVideoSurface = {
                Box(Modifier.fillMaxSize().background(Color.Gray), contentAlignment = Alignment.Center) {
                    Text("Remote Video Stream", color = Color.White)
                }
            },
            localVideoSurface = {
                Box(Modifier.fillMaxSize().background(Color.DarkGray), contentAlignment = Alignment.Center) {
                    Text("Local", color = Color.White, fontSize = 12.sp)
                }
            }
        )
    }
}
