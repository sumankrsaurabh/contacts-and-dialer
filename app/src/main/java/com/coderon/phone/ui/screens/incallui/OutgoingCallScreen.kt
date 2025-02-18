package com.coderon.phone.ui.screens.incallui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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


@Composable
fun OutgoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    state: String = "Ringing",
    profilePictureUrl: String? = null,
    onEndCall: () -> Unit
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
            text = contactName, fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = contactPhoneNumber,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = state,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = { onEndCall.invoke() },
            modifier = Modifier.size(64.dp),
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = Color.Red,
                contentColor = Color.White
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
fun OutgoingCallScreenPreview() {
    OutgoingCallScreen(
        contactName = "Suman Kumar Saurabh",
        contactPhoneNumber = "+91 7808140285",
        onEndCall = { /*TODO*/ })
}
