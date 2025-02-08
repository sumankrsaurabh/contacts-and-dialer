package com.coderon.phone.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import com.coderon.phone.data.helpers.formatDuration
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.SearchScreen
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

    Column {
        val filteredContacts = filteredCallLogs(searchText).collectAsStateWithLifecycle(
            initialValue = callLog
        ).value
        SearchScreen(query = searchText, onQueryChange = { searchText = it }, onSearch = {
            onSearchContact(searchText)
        })
        LazyColumn {
            items(filteredContacts) { log -> CallLogItem(log, navController) }
        }
    }
}

@Composable
fun CallLogItem(log: CallLog, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactProfileImage(log.contact)

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = log.contact?.name ?: log.phoneNumber,
                    fontSize = 18.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (log.callType == CallType.MISSED) {
                        Text(
                            log.callType.name, fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Text(log.callDuration.toLong().formatDuration(), fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(log.callTime.formatTime(), fontSize = 14.sp)
                }
            }

            FilledTonalIconButton(
                onClick = {
                    navController.navigate("contact_details/${log.contact?.phoneNumber}")
                    Log.d("Contact ID:", log.contact?.id.toString())
                },
                modifier = Modifier.size(32.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "expand"
                )
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
                ImageRequest.Builder(LocalContext.current)
                    .data(profilePictureUrl)
                    .crossfade(true)
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
            callTime = 12,
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        ),
        CallLog(
            id = 1L,
            callDuration = "45",
            contact = Contact(
                "2",
                "Aarav Sharma",
                "9998887776",
                "https://example.com/profile1.jpg"
            ),
            callTime = 14,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        )
    )
    CallLogScreen(
        callLog = sampleLogs, navController = rememberNavController(),
        filteredCallLogs = { _ -> emptyFlow() },
        onSearchContact = {}
    )
}
