package com.coderon.phone.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.Message
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.helpers.formatDuration
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.modal.CallLog
import com.coderon.phone.data.modal.CallType
import com.coderon.phone.data.modal.Contact

@Composable
fun CallLogDetailsScreen(
    phoneNumber: String?,
    getContact:suspend (String) -> Contact?,
    getCallLogForPhoneNumber: (String) -> List<CallLog>,
    onCallClick: () -> Unit,
    onMessageClick: () -> Unit,
    onBlockClick: () -> Unit
) {
    val  contact = remember { mutableStateOf<Contact?>(null) }
    val  callLogs = remember { mutableStateOf<List<CallLog>>(emptyList()) }
    LaunchedEffect(phoneNumber) {
        if (phoneNumber != null) {
            contact.value = getContact(phoneNumber)
            callLogs.value = getCallLogForPhoneNumber(phoneNumber)
        }

    }
    Column(modifier = Modifier.fillMaxSize()) {
        contact.value?.let { ContactDetails(it, onCallClick, onMessageClick, onBlockClick) }
        CallLogList(callLogs.value)
    }
}

@Composable
fun ContactDetails(
    contact: Contact, onCallClick: () -> Unit, onMessageClick: () -> Unit, onBlockClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (contact.profilePictureUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(contact.profilePictureUrl)
                            .crossfade(true).build()
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.first().toString(),
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = contact.name, fontSize = 24.sp)
            Text(text = contact.phoneNumber, fontSize = 16.sp)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()
            ) {
                ActionButton(Icons.AutoMirrored.Rounded.CallMade, "Call", onCallClick)
                ActionButton(Icons.AutoMirrored.Rounded.Message, "Message", onMessageClick)
                ActionButton(Icons.Rounded.Block, "Block", onBlockClick)
            }
        }
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, modifier = Modifier.padding(horizontal = 8.dp)) {
        Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun CallLogList(callLogs: List<CallLog>) {
    LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
        items(callLogs) { log ->
            CallLogItemDetails(log)
            Spacer(modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}


@Composable
fun CallLogItemDetails(log: CallLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(1f)) { Text(text = log.callType.name, fontSize = 16.sp) }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = log.callTime.formatTime(), fontSize = 16.sp
            )
        }
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text(
                text = log.callDuration.toLong().formatDuration(),
                fontSize = 16.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCallLogDetailsScreen() {
    val contact = Contact(
        id = "1",
        name = "John Doe",
        phoneNumber = "+1234567890",
        profilePictureUrl = "https://example.com/profile.jpg"
    )

    val callLogs = listOf(
        CallLog(
            id = 1,
            contact = contact,
            phoneNumber = "+1234567890",
            callType = CallType.INCOMING,
            callDuration = "120",
            callTime = 1700000000
        ), CallLog(
            id = 2,
            contact = contact,
            phoneNumber = "+1234567890",
            callType = CallType.OUTGOING,
            callDuration = "60",
            callTime = 1700005000
        ), CallLog(
            id = 3,
            contact = contact,
            phoneNumber = "+1234567890",
            callType = CallType.MISSED,
            callDuration = "0",
            callTime = 1700010000
        )
    )
}
