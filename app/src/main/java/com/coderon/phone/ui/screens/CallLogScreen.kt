package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.ActionsMenuTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    callLogs: List<CallLog>,
    navController: NavController,
    onSearchQueryChanged: ((String) -> Unit)? = null // Optional
) {
    val groupedLogs = remember(callLogs) {
        callLogs.sortedByDescending { it.callTime }
            .groupBy { it.callTime.formatDate() }
    }
    val expandedId = remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Phone", fontSize = 24.sp)
            ActionsMenuTop(false, navController)
        }

        // Optional search bar
        onSearchQueryChanged?.let { onSearch ->
            SearchBar(
                placeholder = "Search call logs",
                onQueryChanged = onSearch
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groupedLogs.forEach { (date, logs) ->
                item { DateHeader(date) }
                items(logs) { log ->
                    CallLogItem(log) { id ->
                        expandedId.value = if (expandedId.value == id) null else id
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) = Text(
    text = date,
    fontSize = 18.sp,
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
)

@SuppressLint("MissingPermission")
@Composable
fun CallLogItem(log: CallLog, onExpand: (String) -> Unit) {
    Card(
        onClick = { onExpand(log.id.toString()) },
        shape = RoundedCornerShape(50),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            Modifier.padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(
                    when (log.callType) {
                        CallType.INCOMING -> R.drawable.incoming_call
                        CallType.OUTGOING -> R.drawable.outgoing_call
                        CallType.MISSED -> R.drawable.missed_call
                        else -> R.drawable.call
                    }
                ),
                contentDescription = null,
                tint = if (log.callType == CallType.MISSED || isSystemInDarkTheme()) Color.Gray else Color.DarkGray.copy(
                    alpha = 0.8f
                ),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = log.contact?.name ?: log.phoneNumber,
                    fontSize = 16.sp,
                    color = if (log.callType == CallType.MISSED) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = log.phoneNumber,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            Text(
                log.callTime.formatTime(),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ContactProfileImage(contact: Contact?) {
    val profileUrl = contact?.profilePictureUrl
    Box(
        Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        profileUrl?.let {
            AsyncImage(model = it, contentDescription = null, modifier = Modifier.size(40.dp))
        } ?: Text(
            text = contact?.name?.firstOrNull()?.uppercase() ?: "",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun SearchBar(
    placeholder: String = "Search...",
    onQueryChanged: (String) -> Unit
) {
    val query = remember { mutableStateOf("") }

    androidx.compose.material3.OutlinedTextField(
        value = query.value,
        onValueChange = {
            query.value = it
            onQueryChanged(it)
        },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCallLogScreen() {
    // Sample data for preview
    val logs = listOf(
        CallLog(
            id = 1L,
            callDuration = "30",
            contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
            callTime = System.currentTimeMillis(),
            callType = CallType.INCOMING,
            phoneNumber = "780840285"
        ),
        CallLog(
            id = 2L,
            callDuration = "45",
            contact = Contact("2", "Aarav Sharma", "9998887776", null),
            callTime = System.currentTimeMillis() - 3600000L,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        )
    )

    // Provide a dummy NavController for preview
    CallLogScreen(callLogs = logs, navController = rememberNavController())
}
