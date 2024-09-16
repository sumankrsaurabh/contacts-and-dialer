package com.coderon.phone.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.coderon.phone.data.modal.Contact

@Composable
fun CallerScreen(contact: Contact?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = rememberImagePainter(contact?.profilePictureUrl ?: "default_image_url"),
            contentDescription = "Contact Image",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(contact?.name ?: "Unknown")
        Text(contact?.phoneNumber ?: "")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* End Call Logic */ }) {
            Text("End Call")
        }
    }
}
