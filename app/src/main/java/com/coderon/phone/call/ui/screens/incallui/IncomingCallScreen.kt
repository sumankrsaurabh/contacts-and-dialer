package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.ui.Text
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/* ------------------------------------------------
   INCOMING CALL – iOS EXACT STYLE
------------------------------------------------ */

@Composable
fun IncomingCallScreen(
    phoneNumber: String,
    name: String? = null,
    profilePictureUrl: String? = null,
    callType: String = "Spam Risk",
    simInfo: String = "",
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- BLURRED BACKGROUND ---------- */

//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(profilePictureUrl ?: R.drawable.background_incallui)
//                .crossfade(true)
//                .build(),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxSize()
//                .blur(22.dp),
//            contentScale = ContentScale.Crop
//        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
        )

        /* ---------- CONTENT ---------- */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            /* ---------- CALLER INFO ---------- */

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Text(
                    text = callType,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                Spacer(Modifier.height(12.dp))

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePictureUrl ?: R.drawable.profile_picture_call)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Caller photo",
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = name ?: phoneNumber,
                    fontSize = 30.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (!name.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = phoneNumber,
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }
            }

            /* ---------- SLIDE TO ANSWER ---------- */

            SlideToAnswerIOS(
                onAnswered = onAnswer,
                onDecline = onDecline
            )
        }
    }
}

/* ------------------------------------------------
   SLIDE TO ANSWER (NO SUSPEND ERROR)
------------------------------------------------ */

@Composable
private fun SlideToAnswerIOS(
    onAnswered: () -> Unit,
    onDecline: () -> Unit
) {
    val maxDragPx = 240f
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope() // ✅ REQUIRED

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .height(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {

        Text(
            text = "slide to answer",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 16.sp
        )

        /* ---------- SLIDER ---------- */

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFF34C759))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, drag ->
                            scope.launch {
                                offsetX.snapTo(
                                    (offsetX.value + drag)
                                        .coerceIn(0f, maxDragPx)
                                )
                            }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value > maxDragPx * 0.7f) {
                                    onAnswered()
                                } else {
                                    offsetX.animateTo(0f, tween(280))
                                }
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.call),
                contentDescription = "Answer",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        /* ---------- DECLINE BUTTON ---------- */

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF3B30))
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, _ -> }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.end_call),
                contentDescription = "Decline",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/* ------------------------------------------------
   PREVIEW
------------------------------------------------ */

@Preview(showBackground = true)
@Composable
fun PreviewIncomingCallIOSExact() {
    IncomingCallScreen(
        phoneNumber = "(405) 555-0145",
        callType = "Spam Risk",
        onAnswer = {},
        onDecline = {}
    )
}
