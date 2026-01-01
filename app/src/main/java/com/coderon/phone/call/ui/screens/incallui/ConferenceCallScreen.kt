package com.coderon.phone.call.ui.screens.incallui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.Text

/* ------------------------------------------------
   CONFERENCE CALL SCREEN (PIXEL STYLE)
------------------------------------------------ */

@Composable
fun ConferenceCallScreen(
    participants: List<String>,
    callDuration: String,
    profilePictureUrl: String? = null,
    onEndCall: () -> Unit,
    onMerge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Conference call",
                fontSize = 22.sp,
                color = Color.White
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = callDuration,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            participants.forEach {
                Text(
                    text = it,
                    fontSize = 18.sp,
                    color = Color.White
                )
                Spacer(Modifier.height(6.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            ConferenceActionButton(
                icon = R.drawable.plus,
                label = "Merge",
                color = Color(0xFF007AFF),
                onClick = onMerge
            )

            ConferenceActionButton(
                icon = R.drawable.end_call,
                label = "End",
                color = Color(0xFFFF3B30),
                onClick = onEndCall
            )
        }
    }
}

/* ------------------------------------------------
   BUTTON
------------------------------------------------ */

@Composable
private fun ConferenceActionButton(
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
private fun PreviewConferenceCallScreen() {
    ConferenceCallScreen(
        participants = listOf(
            "Alice Johnson",
            "Bob Williams",
            "+91 8887776665"
        ),
        callDuration = "00:12:45",
        onEndCall = {},
        onMerge = {}
    )
}
