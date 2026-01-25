@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.OneUi8DynamicBackground
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun IncomingCallScreen(
    phoneNumber: String,
    name: String? = null,
    profilePictureUrl: String? = null,
    callType: String = "Incoming Call",
    simInfo: String = "SIM 1",
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // REUSABLE BACKGROUND
        OneUi8DynamicBackground()

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- IDENTITY SECTION ---------- */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                // Info Badge
                Surface(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.padding(bottom = 36.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00D2D3))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "$simInfo • $callType",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Avatar
                IncomingCallAvatar(
                    name = name ?: phoneNumber,
                    photoUrl = profilePictureUrl
                )

                Spacer(Modifier.height(48.dp))

                Text(
                    text = name ?: phoneNumber,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )

                if (!name.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = phoneNumber,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.65f),
                        letterSpacing = 1.2.sp
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            /* ---------- ACTION SECTION ---------- */
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 64.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OneUiActionControl(Icons.Rounded.NotificationsActive, "Remind me")
                    OneUiActionControl(Icons.AutoMirrored.Rounded.Message, "Message")
                }

                Spacer(Modifier.height(72.dp))

                OneUi8BiDirectionalSlider(
                    onAnswer = onAnswer,
                    onDecline = onDecline
                )
            }
        }
    }
}

@Composable
private fun IncomingCallAvatar(name: String, photoUrl: String?) {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(176.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.06f)
        ) {}
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
private fun OneUi8BiDirectionalSlider(
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val maxDrag = 140f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp)
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Track
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp),
            shape = RoundedCornerShape(42.dp),
            color = Color.White.copy(alpha = 0.1f),
            border = null
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(R.drawable.end_call),
                    contentDescription = null,
                    tint = Color(0xFFFF4757).copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.call),
                    contentDescription = null,
                    tint = Color(0xFF2ECC71).copy(alpha = 0.4f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(80.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                offsetX.snapTo((offsetX.value + dragAmount).coerceIn(-maxDrag, maxDrag))
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                when {
                                    offsetX.value >= maxDrag * 0.8f -> {
                                        onAnswer()
                                        offsetX.animateTo(0f)
                                    }
                                    offsetX.value <= -maxDrag * 0.8f -> {
                                        onDecline()
                                        offsetX.animateTo(0f)
                                    }
                                    else -> {
                                        offsetX.animateTo(0f, spring())
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.12f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .background(Color.White.copy(alpha = 0.15f), CircleShape)
            )

            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = when {
                    offsetX.value > 20f -> Color(0xFF2ECC71) // Turn Green
                    offsetX.value < -20f -> Color(0xFFFF4757) // Turn Red
                    else -> Color.White.copy(alpha = 0.2f) // Clean Translucent
                },
                shadowElevation = if (offsetX.value > 20f || offsetX.value < -20f) 8.dp else 0.dp
            ) {}
        }
    }
}

@Composable
private fun OneUiActionControl(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.85f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOneUi8IncomingCall() {
    PhoneTheme {
        IncomingCallScreen(
            phoneNumber = "7808140285",
            name = "Sarah Johnson",
            onAnswer = {},
            onDecline = {}
        )
    }
}
