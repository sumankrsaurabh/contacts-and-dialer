@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatDuration
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLog>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {
    Scaffold( // NOSONAR
        containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ContactDetails(
                contact = contact,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                navController = navController
            )
            CallLogList(callLogs)
        }
    }
}

@Composable
@SuppressLint("MissingPermission")
fun ContactDetails(
    contact: Contact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }

    Column { // NOSONAR
        TopAppBar(
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
            title = {},
            actions = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            expanded = false
                            onDeleteClick()
                        }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) { // NOSONAR
            if (contact.profilePictureUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(contact.profilePictureUrl)
                            .crossfade(true)
                            .placeholder(R.drawable.profile_picture_call)
                            .build()
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (contact.name.isEmpty()) contact.phoneNumber.firstOrNull()
                            .toString() else contact.name.firstOrNull().toString(),
                        fontSize = 48.sp,
                        color = Color.White
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = contact.name.ifBlank { contact.phoneNumber }, fontSize = 24.sp) // NOSONAR
                if (contact.name.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = contact.phoneNumber, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    callActionButtons.forEach { buttons ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(50)
                                )
                                .size(64.dp)
                        ) {
                            Icon(
                                painter = painterResource(buttons.icon),
                                contentDescription = buttons.text,
                                tint = if (isSystemInDarkTheme()) Color.White else Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CallLogList(callLogs: List<CallLog>) {
    LazyColumn { // NOSONAR
        item {
            Text(
                "Recent calls",
                fontSize = 18.sp,
                color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
        items(callLogs) { log ->
            CallLogItemDetails(log)
        }
    }
    if (callLogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text("No recent calls found")
        }
    }
}

@Composable
fun CallLogItemDetails(log: CallLog) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                when (log.callType) {
                    CallType.INCOMING -> painterResource(R.drawable.incoming_call)
                    CallType.OUTGOING -> painterResource(R.drawable.outgoing_call)
                    CallType.MISSED -> painterResource(R.drawable.incoming_call)
                    else -> painterResource(R.drawable.call)
                },
                contentDescription = log.callType.name,
                tint = if (log.callType == CallType.MISSED) Color.Red else Color.Black
            ) // NOSONAR

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = log.phoneNumber, fontSize = 16.sp) // NOSONAR
                Text(text = log.callDuration.toLong().formatDuration(), fontSize = 16.sp)
            }
            Text(text = log.callTime.formatTime(), fontSize = 16.sp) // NOSONAR
        }
    }
}

@PreviewLightDark
@Composable
private fun PreviewCallLogDetailsScreen() {
    val contact = Contact( // NOSONAR
        id = "1",
        name = "John Doe",
        phoneNumber = "+1234567890",
        profilePictureUrl = "https://example.com/profile.jpg"
    )

    val callLogs = listOf(
        CallLog( // NOSONAR
            id = 1,
            contact = contact,
            phoneNumber = "+1234567890",
            callType = CallType.INCOMING,
            callDuration = "120",
            callTime = 1700000000
        ),
        CallLog( // NOSONAR
            id = 2,
            contact = contact,
            phoneNumber = "+1234567890",
            callType = CallType.OUTGOING,
            callDuration = "60",
            callTime = 1700005000
        ),
        CallLog( // NOSONAR
            id = 3,
            contact = contact,
            phoneNumber = "+1234567890",
            callType = CallType.MISSED,
            callDuration = "0",
            callTime = 1700010000
        )
    )

    PhoneTheme {
        ContactDetailsScreen(
            contact = contact,
            callLogs = callLogs,
            onEditClick = {},
            onDeleteClick = {},
            navController = rememberNavController()
        )
    }
}

private data class CallActionButton(
    val icon: Int,
    val text: String
)

private val callActionButtons = listOf(
    CallActionButton(R.drawable.call, "Call"),
    CallActionButton(R.drawable.message, "Message"),
//    CallActionButton(R.drawable.whatsapp, "WhatsApp")
)