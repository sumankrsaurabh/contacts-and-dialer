package com.coderon.phone.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun DialPad(
    playTones: (Char) -> Unit,
    onDigitPress: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val digitLetters = mapOf(
        "1" to "",
        "2" to "ABC", "3" to "DEF",
        "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
        "0" to "+"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(22.dp),
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
                    if (digit.isEmpty()) {
                        Spacer(Modifier.size(76.dp))
                        return@forEach
                    }

                    val scale = remember { Animatable(1f) }
                    val scope = rememberCoroutineScope()

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                            .background(
                                colorScheme.surfaceVariant,
                                CircleShape
                            )
                            .clickable {
                                onDigitPress(digit)
                                playTones(digit.first())
                                scope.launch {
                                    scale.animateTo(0.94f, spring())
                                    scale.animateTo(1f, spring())
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = digit,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant
                            )

                            digitLetters[digit]?.takeIf { it.isNotEmpty() }?.let {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = it,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
