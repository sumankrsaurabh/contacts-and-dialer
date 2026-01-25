package com.coderon.phone.ui.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun OneUi8DynamicBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val animShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2400f,
        animationSpec = infiniteRepeatable(
            animation = tween(35000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shift"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Base Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1E2C),
                            Color(0xFF2D3436),
                            Color(0xFF1E1E2C)
                        )
                    )
                )
        )

        // Animated Mesh Blobs
        Box(
            modifier = Modifier
                .size(700.dp)
                .offset(y = (-180).dp, x = (-180).dp)
                .graphicsLayer { translationX = animShift / 10 }
                .blur(180.dp)
                .background(Color(0xFF0984E3).copy(alpha = 0.35f), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(600.dp)
                .align(Alignment.BottomEnd)
                .offset(y = 180.dp, x = 180.dp)
                .graphicsLayer { translationX = -animShift / 15 }
                .blur(160.dp)
                .background(Color(0xFF6C5CE7).copy(alpha = 0.3f), CircleShape)
        )
    }
}
