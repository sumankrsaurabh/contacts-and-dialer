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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.Text
import com.coderon.phone.data.model.CallLog as CallLogEntry

/* ------------------------------------------------
   SCREEN
------------------------------------------------ */

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLogEntry>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {
    Scaffold(
        containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            ContactHeader(
                contact = contact,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick,
                navController = navController
            )

            CallLogList(callLogs)
        }
    }
}

/* ------------------------------------------------
   HEADER
------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun ContactHeader(
    contact: Contact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {
    var expanded by remember { mutableStateOf(false) }
    val primaryNumber = contact.phoneNumbers.firstOrNull()?.number.orEmpty()

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
            }
        }, title = {}, actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "More")
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { expanded = false; onEditClick() })
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { expanded = false; onDeleteClick() })
            }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        // Avatar
        if (!contact.profilePictureUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current).data(contact.profilePictureUrl)
                        .crossfade(true).placeholder(R.drawable.profile_picture_call).build()
                ), contentDescription = null, modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
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
                    text = contact.displayName.firstOrNull()?.toString() ?: "#",
                    fontSize = 48.sp,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = contact.displayName.ifBlank { primaryNumber }, fontSize = 24.sp
        )

        if (contact.displayName.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(text = primaryNumber, fontSize = 16.sp)
        }

        Spacer(Modifier.height(16.dp))

        // Action buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
//            callActionButtons.forEach { button ->
//                Box(
//                    modifier = Modifier
//                        .size(64.dp)
//                        .background(
//                            MaterialTheme.colorScheme.surfaceVariant,
//                            RoundedCornerShape(50)
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        painter = painterResource(button.icon),
//                        contentDescription = button.text,
//                        tint = if (isSystemInDarkTheme()) Color.White else Color.Black
//                    )
//                }
//            }
        }
    }
}

/* ------------------------------------------------
   CALL LOG LIST
------------------------------------------------ */

@Composable
private fun CallLogList(callLogs: List<CallLogEntry>) {
    if (callLogs.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
        ) {
            Text("No recent calls found")
        }
        return
    }

    LazyColumn {
        item {
            Text(
                "Recent calls", fontSize = 18.sp, modifier = Modifier.padding(8.dp)
            )
        }
        items(callLogs) { log ->
            CallLogItemDetails(log)
        }
    }
}

@Composable
private fun CallLogItemDetails(log: CallLogEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(50),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(
                    when (log.callType) {
                        CallType.INCOMING -> R.drawable.incoming_call
                        CallType.OUTGOING -> R.drawable.outgoing_call
                        CallType.MISSED -> R.drawable.missed_call
                        else -> R.drawable.call
                    }
                ),
                contentDescription = log.callType.name,
                tint = if (log.callType == CallType.MISSED) Color.Red
                else MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(text = log.phoneNumber, fontSize = 16.sp)
                Text(
                    text = log.callDurationSeconds.toString(), fontSize = 14.sp
                )
            }

            Text(
                text = log.callTime.formatTime(), fontSize = 14.sp
            )
        }
    }
}

/* ------------------------------------------------
   PREVIEW
------------------------------------------------ */

@Preview(showBackground = true)
@Composable
fun ContactDetailsScreenPreview() {
    ContactDetailsScreen(
        contact = Contact(
            id = "1", displayName = "John Doe", phoneNumbers = listOf(
                PhoneNumber("1234567890", isPrimary = true)
            ), profilePictureUrl = null
        ), callLogs = listOf(
            CallLogEntry(
                id = 1,
                phoneNumber = "1234567890",
                callType = CallType.OUTGOING,
                callDurationSeconds = 45,
                callTime = System.currentTimeMillis()
            ), CallLogEntry(
                id = 2,
                phoneNumber = "1234567890",
                callType = CallType.MISSED,
                callDurationSeconds = 0,
                callTime = System.currentTimeMillis()
            )
        ), onEditClick = {}, onDeleteClick = {}, navController = NavController(LocalContext.current)
    )
}
