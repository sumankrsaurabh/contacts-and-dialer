@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.coderon.phone.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@PreviewLightDark
@Composable
fun DialPad(
    playTones: (Char) -> Unit = {},
    onDigitPress: (String) -> Unit = {},
    onDigitLongPress: (String) -> Unit = {},
    hapticFeedbackEnabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val digitLetters = mapOf(
        "1" to "",
        "2" to "ABC", "3" to "DEF",
        "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
        "0" to "+"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { digit ->
                    val scale = remember { Animatable(1f) }
                    val scope = rememberCoroutineScope()

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .background(
                                colorScheme.surfaceContainerHigh,
                                CircleShape
                            )
                            .combinedClickable(
                                onClick = {
                                    onDigitPress(digit)
                                    playTones(digit.first())
                                    if (hapticFeedbackEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    scope.launch {
                                        scale.animateTo(0.92f, spring())
                                        scale.animateTo(1f, spring())
                                    }
                                },
                                onLongClick = {
                                    onDigitLongPress(digit)
                                    if (hapticFeedbackEnabled) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    scope.launch {
                                        scale.animateTo(0.85f, spring())
                                        scale.animateTo(1f, spring())
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = digit,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colorScheme.onSurface
                            )

                            digitLetters[digit]?.takeIf { it.isNotEmpty() }?.let {
                                Text(
                                    text = it,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
