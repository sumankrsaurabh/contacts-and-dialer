package com.coderon.phone.ui.screens.incallui

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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.ui.Text

@Composable
fun IncomingCallScreen(
    phoneNumber: String = "Unknown Caller",
    name: String?,
    profilePictureUrl: String? = null,
    callType: String = "",
    simInfo: String = "",
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl.takeIf { !it.isNullOrEmpty() }
                    ?: R.drawable.background_incallui).placeholder(R.drawable.background_incallui)
                .error(R.drawable.background_incallui).crossfade(true).build(),
            contentDescription = "Contact Profile Picture",
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .blur(4.dp),
            contentScale = ContentScale.Crop)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(.5f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {// Profile Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePictureUrl.takeIf { !it.isNullOrEmpty() }
                            ?: R.drawable.profile_picture_call)
                        .placeholder(R.drawable.profile_picture_call)
                        .error(R.drawable.profile_picture_call) // Ensures fallback if loading fails
                        .crossfade(true).build(),
                    contentDescription = "Contact Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer))

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = name ?: phoneNumber, fontSize = 24.sp, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = phoneNumber, fontSize = 18.sp, color = Color.White.copy(.8f))
                Spacer(modifier = Modifier.height(8.dp))
                // Call Type (HD, VoLTE, Wi-Fi) and SIM Info Display
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = callType,  // e.g., "HD"
                        fontSize = 16.sp, color = Color.White.copy(.8f)
                    )
                    Text(
                        text = simInfo,  // e.g., "SIM 1 - Jio"
                        fontSize = 16.sp, color = Color.White.copy(.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Buttons(
                        icon = painterResource(R.drawable.call),
                        contentDescription = "Answer Call",
                        onClick = onAnswer,
                        color = Color(0xFF34C759)
                    )
                    Text(
                        "Accept", color = Color.White
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Buttons(
                        icon = painterResource(R.drawable.end_call),
                        contentDescription = "End Call",
                        onClick = onDecline,
                        color = Color.Red
                    )
                    Text(
                        "Decline", color = Color.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewIncomingCallScreen() {
    IncomingCallScreen(name = null, phoneNumber = "+912345678900", onAnswer = {}, onDecline = {})
//    Buttons(
//        iconVector = Icons.Default.Call, contentDescription = "Answer Call", onClick = {})
}

@Composable
fun Buttons(
    icon: Painter, contentDescription: String, onClick: () -> Unit, color: Color, size: Dp = 64.dp
) {
    IconButton(
        onClick = onClick, modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}