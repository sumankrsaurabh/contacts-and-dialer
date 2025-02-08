package com.coderon.phone.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CallScreen(
    callerName: String,
    phoneNumber: String,
    isIncoming: Boolean,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onMute: () -> Unit,
    onHold: () -> Unit,
    onSpeaker: () -> Unit,
    onRecord: () -> Unit
) {
    var isRecording by remember { mutableStateOf(false) }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = callerName, style = MaterialTheme.typography.headlineMedium)
        Text(text = phoneNumber, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            Button(
                onClick = onMute,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Mute")
            }

            Button(
                onClick = onHold,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Hold")
            }

            Button(
                onClick = onSpeaker,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Speaker")
            }

            Button(
                onClick = {
                    isRecording = !isRecording
                    onRecord()
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isRecording) Color.Red else Color.Gray)
            ) {
                Text(if (isRecording) "Stop Rec" else "Record")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isIncoming) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = onAnswer,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                ) {
                    Text("Answer")
                }

                Button(
                    onClick = onDecline,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Decline")
                }
            }
        } else {
            Button(
                onClick = onDecline,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("End Call")
            }
        }
    }
}
