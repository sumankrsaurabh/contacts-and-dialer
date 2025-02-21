package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.telecom.TelecomManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.coderon.phone.ui.utils.CoderonTopAppBar
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.placeCall

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLog>,
    onMessageClick: () -> Unit,
    onBlockClick: () -> Unit,
    navController: NavController
) {
    Scaffold(
        bottomBar = { ActionButtons() },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(.25f)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            ContactDetails(
                contact, onMessageClick, onBlockClick, navController
            )
            CallLogList(callLogs)
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ContactDetails(
    contact: Contact,
    onMessageClick: () -> Unit,
    onBlockClick: () -> Unit,
    navController: NavController,
    context: Context = LocalContext.current
) {
    Column(
    ) {
        val telecomManager = context.getSystemService(TelecomManager::class.java)
        val showSimSelectDialog = remember { mutableStateOf(false) }
        val availableAccounts = telecomManager.callCapablePhoneAccounts
        if (showSimSelectDialog.value) {
            SimSelectionDialog(
                availableAccounts = availableAccounts,
                onDismiss = { showSimSelectDialog.value = false },
                onSimSelected = {
                    placeCall(context, contact.phoneNumber, it)
                })
        }
        CoderonTopAppBar(
            showBackArrow = true,
            onBack = { navController.popBackStack() },
            showActionsButton = false,
            title = ""
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (contact.profilePictureUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(contact.profilePictureUrl)
                            .crossfade(true).placeholder(R.drawable.profile_picture_call).build()
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

            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = contact.name.ifBlank { contact.phoneNumber }, fontSize = 24.sp)
                    if (contact.name.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = contact.phoneNumber, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                showSimSelectDialog.value = true
                            }, colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Green, contentColor = Color.White
                            )
                        ) {
                            Icon(painter = painterResource(R.drawable.call), "Call")
                        }
                        IconButton(
                            onClick = onMessageClick, colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Blue, contentColor = Color.White
                            )
                        ) {
                            Icon(painter = painterResource(R.drawable.message), "Call")
                        }
                        IconButton(
                            onClick = onBlockClick, colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Gray, contentColor = Color.White
                            )
                        ) {
                            Icon(painter = painterResource(R.drawable.video_call), "Call")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionButtons(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClick) {
                Icon(painter = painterResource(R.drawable.edit), contentDescription = "edit")
            }
            Text("Edit", fontSize = 16.sp)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(onClick = onClick) {
                Icon(painter = painterResource(R.drawable.delete), contentDescription = "delete")
            }
            Text("Delete", fontSize = 16.sp)
        }
    }
}

@Composable
fun CallLogList(callLogs: List<CallLog>) {

    LazyColumn() {
        item {
            Text(
                "Recent calls",
                fontSize = 16.sp,
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                log.callType.name,
                tint = if (log.callType == CallType.MISSED) Color.Red else Color.Black
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = log.phoneNumber,
                    fontSize = 16.sp,
                )
                Text(
                    text = log.callDuration.toLong().formatDuration(),
                    fontSize = 16.sp,
                )
            }
            Text(
                text = log.callTime.formatTime(),
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

    ContactDetailsScreen(
        contact = Contact(
            id = "", name = "Saurya", phoneNumber = "7808140285", profilePictureUrl = ""
        ),
        callLogs = callLogs,
        onMessageClick = {},
        onBlockClick = {},
        rememberNavController()
    )
}
