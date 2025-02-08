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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AddIcCall
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.MainButton
import com.coderon.phone.ui.Text


@Composable
fun OutgoingCallScreen(
    contactName: String, contactPhoneNumber: String, onEndCall: () -> Unit
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
            .padding(vertical = 48.dp, horizontal = 24.dp)
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
                painter = painterResource(id = R.drawable.profile_picture_call),
                contentDescription = "Contact Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
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
            Text(text = "Calling...", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MainButton(
                        onClick = { },
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Record call"
                    )
                    MainButton(
                        onClick = { },
                        imageVector = Icons.Default.PauseCircle,
                        contentDescription = "Hold call"
                    )
                    MainButton(
                        onClick = { },
                        imageVector = Icons.Default.AddIcCall,
                        contentDescription = "Add call"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MainButton(
                        onClick = { },
                        imageVector = Icons.Default.KeyboardHide,
                        contentDescription = "Record call"
                    )
                    MainButton(
                        onClick = { },
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Hold call"
                    )
                    MainButton(
                        onClick = { },
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Add call"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MainButton(
                        onClick = { },
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Record call"
                    )
                    FilledTonalIconButton(
                        onClick = { },
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Red.copy(.8f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End call",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    MainButton(
                        onClick = { },
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = "Add call"
                    )
                }
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
        onEndCall = { /*TODO*/ })
}
