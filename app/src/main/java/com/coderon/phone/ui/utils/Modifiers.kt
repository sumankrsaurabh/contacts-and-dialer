package com.coderon.phone.ui.utils

import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Standard gradient background for the app's surface areas.
 */
fun Modifier.surfaceBackground(): Modifier {
    return this.background(
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFFBBD2C5),
                Color(0xFF536976),
                Color(0xFF292E49),
            )
        )
    )
}
