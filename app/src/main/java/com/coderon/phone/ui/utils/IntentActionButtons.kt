package com.coderon.phone.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.coderon.phone.R
import com.coderon.phone.ui.theme.PhoneTheme

@Composable
fun IntentActionButtons() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledIconButton(
            onClick = {},
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF34C759)) // Green
        ) {
            Icon(painter = painterResource(id = R.drawable.call), contentDescription = "Call", tint = Color.White)
        }
        FilledIconButton(
            onClick = {},
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF007AFF)) // Blue
        ) {
            Icon(painter = painterResource(id = R.drawable.message), contentDescription = "Message", tint = Color.White)
        }
        FilledIconButton(
            onClick = {},
            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF8E8E93)) // Gray
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = "Info", tint = Color.White)
        }
    }
}

@Preview
@Composable
private fun IntentActionButtonsPreview() {
    PhoneTheme {
        IntentActionButtons()
    }
}