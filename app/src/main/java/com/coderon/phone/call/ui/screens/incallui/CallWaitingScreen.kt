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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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

/* ------------------------------------------------
   CALL WAITING SCREEN (ONE UI 8 STYLE)
------------------------------------------------ */

@Composable
fun CallWaitingScreen(
    activeName: String,
    activeNumber: String,
    waitingName: String?,
    waitingNumber: String,
    profilePictureUrl: String? = null,
    onAcceptWaiting: () -> Unit,
    onRejectWaiting: () -> Unit,
    onEndActiveAcceptWaiting: () -> Unit
) {
    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(30f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(800, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        OneUi8DynamicBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Waiting caller info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape),
                    color = Color.White.copy(alpha = 0.1f)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(profilePictureUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = waitingName ?: "Incoming call",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = waitingNumber,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Call waiting while on call with $activeName",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
            }

            // Action buttons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    CallActionButton(
                        icon = R.drawable.end_call,
                        label = "Reject",
                        color = Color(0xFFFF3B30),
                        onClick = onRejectWaiting
                    )

                    CallActionButton(
                        icon = R.drawable.call,
                        label = "Accept",
                        color = Color(0xFF34C759),
                        onClick = onAcceptWaiting
                    )
                }

                Spacer(Modifier.height(48.dp))

                Surface(
                    onClick = onEndActiveAcceptWaiting,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "End current & accept",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------
   SHARED BUTTON
------------------------------------------------ */

@Composable
private fun CallActionButton(
    icon: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = label, 
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

/* ------------------------------------------------
   PREVIEW
------------------------------------------------ */

@Preview(showBackground = true)
@Composable
private fun PreviewCallWaitingScreen() {
    PhoneTheme {
        CallWaitingScreen(
            activeName = "Alice Johnson",
            activeNumber = "+1 987 654 3210",
            waitingName = "Bob Williams",
            waitingNumber = "+1 999 888 7776",
            onAcceptWaiting = {},
            onRejectWaiting = {},
            onEndActiveAcceptWaiting = {}
        )
    }
}
