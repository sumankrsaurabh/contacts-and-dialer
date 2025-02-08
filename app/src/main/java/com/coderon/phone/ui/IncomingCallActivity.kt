package com.coderon.phone.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.viewmodel.CallViewModel

class IncomingCallActivity : ComponentActivity() {

    private val callViewModel: CallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up full-screen flags
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
        )

        // Get caller name or number
        val phoneNumber = intent.getStringExtra("CALLER_NUMBER") ?: "Unknown Caller"

        setContent {
            PhoneTheme {
                IncomingCallScreen(callerName = phoneNumber, onAccept = {
                    callViewModel.answerCall()
                    finish()
                }, onReject = {
                    callViewModel.rejectCall()
                    finish()
                })
            }
        }
    }
}

@Composable
private fun IncomingCallScreen(
    callerName: String, onAccept: () -> Unit, onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Caller Name
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp)
            ) {
                // Reject Call Button
                IconButton(
                    onClick = onReject,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Red, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.end_call),
                        contentDescription = "Reject Call",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Accept Call Button
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.Green, shape = CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.call),
                        contentDescription = "Accept Call",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingCallScreenPreview() {
    IncomingCallScreen(callerName = "John Doe", onAccept = {}, onReject = {})
}
