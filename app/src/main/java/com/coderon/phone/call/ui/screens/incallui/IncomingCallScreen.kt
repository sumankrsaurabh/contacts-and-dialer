@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.coderon.phone.ui.components.Text
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
    backgroundUri: String? = null,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(40f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(1000, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

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

        /* ---------- CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(vertical = 48.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            /* ---------- IDENTITY SECTION ---------- */
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text(
                    text = name ?: phoneNumber,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = if (name != null) phoneNumber else callType,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                if (simInfo.isNotEmpty()) {
                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Text(
                            text = simInfo,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(64.dp))

            // Avatar with Pulse Animation
            PulseAvatar(
                name = name ?: phoneNumber,
                photoUrl = profilePictureUrl
            )

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
                    IncomingActionIcon(Icons.Rounded.NotificationsActive, "remind me")
                    IncomingActionIcon(Icons.AutoMirrored.Rounded.Message, "message")
                }

                Spacer(Modifier.height(64.dp))

                // New Modern Call Slider
                ModernCallSlider(
                    onAnswer = onAnswer,
                    onDecline = onDecline
                )
            }
        }
    }
}

@Composable
private fun PulseAvatar(name: String, photoUrl: String?) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(160.dp)
                .scale(pulseScale)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
        )

        Surface(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape),
            color = Color.White.copy(alpha = 0.15f),
            shadowElevation = 8.dp
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
                        fontWeight = FontWeight.ExtraLight,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernCallSlider(
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val maxDrag = 150f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp)
            .height(110.dp),
        contentAlignment = Alignment.Center
    ) {
        // Background Track with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(88.dp)
                .clip(RoundedCornerShape(44.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF4757).copy(alpha = 0.15f * ((-offsetX.value / maxDrag).coerceIn(0f, 1f) + 0.2f)),
                            Color.White.copy(alpha = 0.1f),
                            Color(0xFF2ECC71).copy(alpha = 0.15f * ((offsetX.value / maxDrag).coerceIn(0f, 1f) + 0.2f))
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    painter = painterResource(R.drawable.end_call),
                    contentDescription = null,
                    tint = Color(0xFFFF4757).copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
                Icon(
                    painter = painterResource(R.drawable.call),
                    contentDescription = null,
                    tint = Color(0xFF2ECC71).copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Draggable Center Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(84.dp)
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
                                    offsetX.value >= maxDrag * 0.75f -> {
                                        onAnswer()
                                        offsetX.animateTo(0f, spring())
                                    }
                                    offsetX.value <= -maxDrag * 0.75f -> {
                                        onDecline()
                                        offsetX.animateTo(0f, spring())
                                    }
                                    else -> {
                                        offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = CircleShape,
                color = when {
                    offsetX.value > 10f -> Color(0xFF2ECC71)
                    offsetX.value < -10f -> Color(0xFFFF4757)
                    else -> Color.White
                },
                shadowElevation = 12.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val iconScale = 1f + (kotlin.math.abs(offsetX.value) / maxDrag) * 0.2f
                    Icon(
                        painter = painterResource(if (offsetX.value < 0) R.drawable.end_call else R.drawable.call),
                        contentDescription = null,
                        tint = if (offsetX.value == 0f) Color.Black else Color.White,
                        modifier = Modifier.size(34.dp).graphicsLayer(scaleX = iconScale, scaleY = iconScale)
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomingActionIcon(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { }
    ) {
        Surface(
            modifier = Modifier.size(54.dp),
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
        Spacer(Modifier.height(10.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIncomingCallRedesign() {
    PhoneTheme {
        IncomingCallScreen(
            phoneNumber = "+1 234 567 890",
            name = "Sarah Johnson",
            onAnswer = {},
            onDecline = {}
        )
    }
}
