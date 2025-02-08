package com.coderon.phone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.Text

@Composable
fun IncomingCallScreen(
    phoneNumber: String = "Unknown Caller",
    name: String = "Suman Kumar Saurabh",
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    listOf(
                        Color.Cyan, Color.Blue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.profile_picture_call),
                contentDescription = "Caller Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = name,
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = phoneNumber, fontSize = 16.sp, color = Color.White, letterSpacing = .1.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Buttons(
                    icon = painterResource(R.drawable.call),
                    contentDescription = "Answer Call",
                    onClick = onAnswer,
                    color = Color.Green
                )
                Buttons(
                    icon = painterResource(R.drawable.end_call),
                    contentDescription = "End Call",
                    onClick = onDecline,
                    color = Color.Red
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewIncomingCallScreen() {
    IncomingCallScreen(phoneNumber = "+912345678900", onAnswer = {}, onDecline = {})
//    Buttons(
//        iconVector = Icons.Default.Call, contentDescription = "Answer Call", onClick = {})
}

@Composable
fun Buttons(
    icon: Painter,
    contentDescription: String,
    onClick: () -> Unit,
    color: Color,
    size: Dp = 64.dp
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color.White)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = color
        )
    }
}