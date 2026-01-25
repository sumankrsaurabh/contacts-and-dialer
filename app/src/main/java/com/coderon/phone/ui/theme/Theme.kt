package com.coderon.phone.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

fun oneUiLightScheme(
    accent: Color
): ColorScheme = lightColorScheme(
    background = OneUiColors.LightBackground,
    surface = OneUiColors.LightSurface,
    onSurface = OneUiColors.LightOnSurface,
    onSurfaceVariant = OneUiColors.LightOnSurfaceSubtle,
    primary = accent,
    onPrimary = Color.White,
    error = OneUiColors.Error
)

fun oneUiDarkScheme(
    accent: Color
): ColorScheme = darkColorScheme(
    background = OneUiColors.DarkBackground,
    surface = OneUiColors.DarkSurface,
    onSurface = OneUiColors.DarkOnSurface,
    onSurfaceVariant = OneUiColors.DarkOnSurfaceSubtle,
    primary = accent,
    onPrimary = Color.Black,
    error = OneUiColors.Error
)


@Composable
fun PhoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> oneUiDarkScheme(OneUiColors.Ancient)
        else -> oneUiLightScheme(OneUiColors.Ancient)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}