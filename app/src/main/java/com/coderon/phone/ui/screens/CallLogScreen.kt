package com.coderon.phone.ui.screens

import android.content.Context
import android.telecom.TelecomManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.helpers.formatDuration
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.CoderonTopAppBar
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.placeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun CallLogScreen(
    callLog: List<CallLog>,
    filteredCallLogs: (String) -> Flow<List<CallLog>>,
    navController: NavController,
    onSearchContact: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    val filteredLogs =
        filteredCallLogs(searchText).collectAsStateWithLifecycle(initialValue = callLog).value
    val groupedLogs = remember(filteredLogs) { filteredLogs.groupBy { it.callTime.formatDate() } }
    // Handle back press to close search bar
    BackHandler(enabled = isSearchExpanded) {
        isSearchExpanded = false
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CoderonTopAppBar(
            onSearch = { isSearchExpanded = true },
            onMenu = {},
            showBackArrow = false,
            title = "Contacts",
            isSearchExpanded = isSearchExpanded,
            searchText = searchText,
            onSearchTextChanged = { searchText = it },
            onDismissSearch = { isSearchExpanded = false }
        )


        LazyColumn {
            groupedLogs.forEach { (date, logs) ->
                item { CallLogDateHeader(date) }
                itemsIndexed(logs) { index, log ->
                    CallLogItem(
                        log, navController
                    )
                }
            }
        }
    }
}

@Composable
fun CallLogDateHeader(date: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = date,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun CallLogItem(
    log: CallLog,
    navController: NavController,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    context: Context =  LocalContext.current
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val showSimSelectDialog = remember { mutableStateOf(false) }
    val availableAccounts = telecomManager.callCapablePhoneAccounts
    if (showSimSelectDialog.value) {
        SimSelectionDialog(
            availableAccounts = availableAccounts,
            onDismiss = { showSimSelectDialog.value = false },
            onSimSelected = {
                placeCall(context, log.phoneNumber, it)
            })
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = { showSimSelectDialog.value = true }),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface, // White in light mode, dark gray in dark mode
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactProfileImage(log.contact)

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.contact?.name ?: log.phoneNumber, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.callType == CallType.MISSED) {
                        Text(
                            text = log.callType.name,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = log.callDuration.toLong().formatDuration(), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = log.callTime.formatTime(), fontSize = 14.sp)
                }
            }

            IconButton(
                onClick = { navController.navigate("contact_details/${log.contact?.phoneNumber}") },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Outlined.Info, contentDescription = "Expand")
            }
        }
    }
}

@Composable
fun ContactProfileImage(contact: Contact?) {
    val profilePictureUrl = contact?.profilePictureUrl
    if (profilePictureUrl != null) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(profilePictureUrl).crossfade(true)
                    .build()
            ),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        )
    } else {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact?.name?.firstOrNull()?.toString() ?: "?",
                fontSize = 20.sp,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCallLogScreen() {
    val sampleLogs = listOf(
        CallLog(
            id = 0L,
            callDuration = "30",
            contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
            callTime = System.currentTimeMillis() - 3600000,
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        ), CallLog(
            id = 1L,
            callDuration = "45",
            contact = Contact(
                "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
            ),
            callTime = System.currentTimeMillis() - 86400000,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        )
    )
    CallLogScreen(
        callLog = sampleLogs,
        navController = rememberNavController(),
        filteredCallLogs = { _ -> emptyFlow() },
        onSearchContact = {})
}
