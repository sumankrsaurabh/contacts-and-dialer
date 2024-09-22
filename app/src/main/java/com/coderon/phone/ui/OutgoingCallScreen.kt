package com.coderon.phone.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

@Composable
fun OutgoingCallScreen(
    contactName: String,
    contactPhoneNumber: String,
    onEndCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Profile Image
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = "Contact Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Contact name and phone number
            Text(text = contactName, fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = contactPhoneNumber, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground)

            Spacer(modifier = Modifier.height(48.dp))

            // Call Status
            Text(text = "Calling...", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(48.dp))

            // End Call Button
            FilledIconButton(
                onClick = onEndCall,
                modifier = Modifier.size(64.dp),
                colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.CallEnd,
                    contentDescription = "End Call",
                    tint = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OutgoingCallScreenPreview() {
    OutgoingCallScreen(
        contactName = "Suman Kumar Saurabh",
        contactPhoneNumber = "+91 7808140285",
        onEndCall = { /*TODO*/ }
    )
}
