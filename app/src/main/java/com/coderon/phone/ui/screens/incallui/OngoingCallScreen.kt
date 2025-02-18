package com.coderon.phone.ui.screens.incallui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.extentions.State

@Composable
fun OngoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    state: State,
    profilePictureUrl: String? = null,
    onEndCall: () -> Unit,
    onToggleSpeaker: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleHold: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Profile Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(profilePictureUrl.takeIf { !it.isNullOrEmpty() }
                    ?: R.drawable.profile_picture_call).placeholder(R.drawable.profile_picture_call)
                .error(R.drawable.profile_picture_call) // Ensures fallback if loading fails
                .crossfade(true).build(),
            contentDescription = "Contact Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer))

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = contactName, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contactPhoneNumber,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state.toString(),
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        AnimatedVisibility(visible = state == State.ACTIVE) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painterResource(R.drawable.plus), "add call")
                    }
                    IconButton(onClick = { onToggleHold.invoke() }) {
                        Icon(painterResource(R.drawable.pause), "hold call")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painterResource(R.drawable.record), "record call")
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = { onToggleSpeaker.invoke("Speaker") }) {
                        Icon(painterResource(R.drawable.volume_high), "add call")
                    }
                    IconButton(onClick = { onToggleMute.invoke() }) {
                        Icon(painterResource(R.drawable.mic), "hold call")
                    }
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(painterResource(R.drawable.keypad_outline), "record call")
                    }
                }
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
        IconButton(
            onClick = { onEndCall.invoke() },
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Red, contentColor = Color.White
            )
        ) {
            Icon(
                painter = painterResource(id = R.drawable.end_call),
                contentDescription = "End Call",
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Test() {
    OngoingCallScreen(
        contactName = "Suman Kumar Saurabh",
        contactPhoneNumber = "+91 7808140285",
        onEndCall = { /*TODO*/ },
        onToggleSpeaker = { /*TODO*/ },
        onToggleMute = { /*TODO*/ },
        onToggleHold = { /*TODO*/ },
        state = State.IDLE
    )
}