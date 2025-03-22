package com.coderon.phone.ui.screens.incallui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.extentions.AudioRoute

@Composable
fun TwoCallScreen(
    firstContactName: String,
    firstPhoneNumber: String,
    secondContactName: String,
    secondPhoneNumber: String,
    isMuted: Boolean,
    currentAudioRoute: String,
    callDuration: String,
    onSwapCalls: () -> Unit,
    onMergeCalls: () -> Unit,
    onEndCall: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleBluetooth: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Active Call: $firstContactName ($firstPhoneNumber)")
        Text(text = "On Hold: $secondContactName ($secondPhoneNumber)")
        Text(text = "Call Duration: $callDuration")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onSwapCalls) { Text("Swap Calls") }
            Button(onClick = onMergeCalls) { Text("Merge Calls") }
            Button(onClick = onEndCall) { Text("End Call") }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onToggleMute) { Text(if (isMuted) "Unmute" else "Mute") }
            Button(onClick = onToggleSpeaker) { Text(if (currentAudioRoute == AudioRoute.SPEAKER.name) "Earpiece" else "Speaker") }
            Button(onClick = onToggleBluetooth) { Text("Bluetooth") }
        }
    }
}
