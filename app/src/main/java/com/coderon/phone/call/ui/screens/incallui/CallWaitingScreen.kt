package com.coderon.phone.call.ui.screens.incallui

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.ui.Text

/* ------------------------------------------------
   CALL WAITING SCREEN (PIXEL STYLE)
------------------------------------------------ */

@Composable
fun CallWaitingScreen(
    activeName: String,
    activeNumber: String,
    waitingName: String?,
    waitingNumber: String,
    profilePictureUrl: String? = null,
    onAcceptWaiting: () -> Unit,
    onRejectWaiting: () -> Unit,
    onEndActiveAcceptWaiting: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Background blur
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl ?: R.drawable.background_incallui)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Waiting caller info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profilePictureUrl ?: R.drawable.profile_picture_call)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = waitingName ?: "Incoming call",
                    fontSize = 24.sp,
                    color = Color.White
                )

                Text(
                    text = waitingNumber,
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Call waiting",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Action buttons
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    CallActionButton(
                        icon = R.drawable.end_call,
                        label = "Reject",
                        color = Color(0xFFFF3B30),
                        onClick = onRejectWaiting
                    )

                    CallActionButton(
                        icon = R.drawable.call,
                        label = "Accept",
                        color = Color(0xFF34C759),
                        onClick = onAcceptWaiting
                    )
                }

                Spacer(Modifier.height(16.dp))

                IconButton(
                    onClick = onEndActiveAcceptWaiting,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Text(
                        text = "End current & accept",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------
   SHARED BUTTON
------------------------------------------------ */

@Composable
private fun CallActionButton(
    icon: Int,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(color)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(Modifier.height(6.dp))

        Text(label, color = Color.White)
    }
}

/* ------------------------------------------------
   PREVIEW
------------------------------------------------ */

@Preview(showBackground = true, device = "id:pixel_8")
@Composable
private fun PreviewCallWaitingScreen() {
    CallWaitingScreen(
        activeName = "Alice Johnson",
        activeNumber = "+91 9876543210",
        waitingName = "Bob Williams",
        waitingNumber = "+91 9998887776",
        onAcceptWaiting = {},
        onRejectWaiting = {},
        onEndActiveAcceptWaiting = {}
    )
}
