package com.coderon.phone.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.coderon.phone.R

@Composable
fun DialerScreen() {
    var phoneNumber by remember { mutableStateOf("") }
    val maxLength = 15

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = phoneNumber, fontSize = 32.sp, modifier = Modifier.padding(18.dp)
        )

        DialPad { digit ->
            if (phoneNumber.length < maxLength) {
                phoneNumber += digit
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Call action row with icons for video call, regular call, and delete
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { /*TODO*/ }) {
                Icon(
                    imageVector = Icons.Filled.VideoCall,
                    contentDescription = "video call",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            val context = LocalContext.current
            FilledIconButton(
                onClick = {
                    initiateCall(phoneNumber, context)
                },
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.call),
                    contentDescription = "call",
                    modifier = Modifier.size(32.dp)
                )
            }

            IconButton(onClick = {
                if (phoneNumber.isNotEmpty()) {
                    phoneNumber = phoneNumber.dropLast(1)  // Remove last digit
                }
            }) {
                Icon(
                    imageVector = Icons.Default.RemoveCircle,
                    contentDescription = "delete last digit",
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun initiateCall(phoneNumber: String, context: Context) {
    if (ActivityCompat.checkSelfPermission(
            context, Manifest.permission.CALL_PHONE
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    ) {
        val uri = Uri.fromParts("tel", phoneNumber, null)
        val intent = Intent(Intent.ACTION_CALL, uri)
        context.startActivity(intent)
    } else {
        // Request permission or show a message to the user
        Toast.makeText(context, "Permission to make calls is required", Toast.LENGTH_SHORT).show()
    }
}


@Composable
fun DialPad(onDigitPress: (String) -> Unit) {
    Column {
        // Loop through rows of digits for the dialer
        listOf("123", "456", "789", "*0#").forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Loop through each digit in the row
                row.forEach { digit ->
                    Column(modifier = Modifier
                        .padding(vertical = 8.dp)
                        .size(64.dp)
                        .clip(CircleShape)
                        .clickable { onDigitPress(digit.toString()) }
                        .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(digit.toString(), fontSize = 26.sp)
                    }
                }
            }
        }
    }
}
