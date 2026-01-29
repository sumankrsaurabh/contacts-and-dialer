@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Merge
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

/* ------------------------------------------------
   CONFERENCE CALL SCREEN (ONE UI 8 STYLE)
------------------------------------------------ */

@Composable
fun ConferenceCallScreen(
    participants: List<String>,
    callDuration: String,
    onEndCall: () -> Unit,
    onMerge: () -> Unit
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = Color(0xFF5856D6).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "Conference",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD6D6FF)
                    )
                }

                Text(
                    text = "Active Group Call",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = callDuration,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(48.dp))

                participants.forEach {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        it.firstOrNull()?.toString() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(Modifier.padding(horizontal = 8.dp))
                            Text(
                                text = it,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ConferenceActionButton(
                        imageVector = Icons.Rounded.Merge,
                        label = "Merge",
                        color = Color.White.copy(alpha = 0.12f),
                        iconTint = Color.White,
                        onClick = onMerge
                    )

                    ConferenceActionButton(
                        painter = painterResource(R.drawable.end_call),
                        label = "End",
                        color = Color(0xFFFF3B30),
                        iconTint = Color.White,
                        onClick = onEndCall,
                        isLarge = true
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ConferenceActionButton(
    imageVector: ImageVector? = null,
    painter: androidx.compose.ui.graphics.painter.Painter? = null,
    label: String,
    color: Color,
    iconTint: Color,
    isLarge: Boolean = false,
    onClick: () -> Unit
) {
    val size = if (isLarge) 80.dp else 72.dp
    val iconSize = if (isLarge) 36.dp else 28.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(size),
            shape = CircleShape,
            color = color
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (imageVector != null) {
                    Icon(
                        imageVector = imageVector,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(iconSize)
                    )
                } else if (painter != null) {
                    Icon(
                        painter = painter,
                        contentDescription = label,
                        tint = iconTint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = label,
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewConferenceCallScreen() {
    PhoneTheme() {
        ConferenceCallScreen(
            participants = listOf(
                "Alice Johnson",
                "Bob Williams",
                "+1 888 777 6665"
            ),
            callDuration = "12:45",
            onEndCall = {},
            onMerge = {}
        )
    }
}
