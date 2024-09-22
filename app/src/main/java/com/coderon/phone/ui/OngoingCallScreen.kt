package com.coderon.phone.ui

import android.telecom.Call
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
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.viewmodel.CallViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun OngoingCallScreen(
    call: Call, // Pass Call object from InCallService
    callViewModel: CallViewModel = getViewModel() // Use ViewModel to manage call state
) {
    var speakerEnabled by remember { mutableStateOf(false) }

    val callerPhoneNumber by remember { mutableStateOf(call.details.handle.schemeSpecificPart) }
    val isMuted by callViewModel.isMuted.collectAsState()
    val callDuration by callViewModel.callDuration.collectAsState()

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
            Text(
                text = callViewModel.getCallerName(call),
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = callerPhoneNumber,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Call duration
            Text(text = callDuration, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(48.dp))

            // Controls: Mute, Speaker, End Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Mute/Unmute Button
                FilledIconButton(
                    onClick = { callViewModel.toggleMute() },
                    modifier = Modifier.size(64.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isMuted) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                        contentDescription = if (isMuted) "Unmute" else "Mute",
                        tint = Color.White
                    )
                }

                // Speaker Button
                FilledIconButton(
                    onClick = {
                        speakerEnabled = !speakerEnabled
                        callViewModel.toggleSpeaker()
                    },
                    modifier = Modifier.size(64.dp),
                    colors = androidx.compose.material3.IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (speakerEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VolumeUp,
                        contentDescription = "Speaker",
                        tint = Color.White
                    )
                }

                // End Call Button
                FilledIconButton(
                    onClick = { callViewModel.endCall(call) },
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
}
