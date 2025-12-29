package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.ActionsMenuTop
import com.coderon.phone.data.model.CallLog as CallLogEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    callLogs: List<CallLogEntry>,
    navController: NavController,
    onSearchQueryChanged: ((String) -> Unit)? = null
) {
    val groupedLogs = remember(callLogs) {
        callLogs
            .sortedByDescending { it.callTime }
            .groupBy { it.callTime.formatDate() }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Phone",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            ActionsMenuTop(false, navController)
        }

        // Optional Search
        onSearchQueryChanged?.let {
            SearchBar(
                placeholder = "Search call logs",
                onQueryChanged = it
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            groupedLogs.forEach { (date, logs) ->
                item { DateHeader(date) }
                items(logs, key = { it.id }) { log ->
                    CallLogItem(log) { phoneNumber ->
                        navController.navigate(
                            Screen.CallDetails.createRoute(phoneNumber)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        color = Color.White.copy(alpha = 0.9f)
    )
}

@SuppressLint("MissingPermission")
@Composable
fun CallLogItem(
    log: CallLogEntry,
    onCallLogEntryClick: (String) -> Unit
) {
    Card(
        onClick = { onCallLogEntryClick(log.phoneNumber) },
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
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
                contentDescription = null,
                tint = if (log.callType == CallType.MISSED)
                    MaterialTheme.colorScheme.error
                else
                    Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(20.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = log.contact?.displayName ?: log.phoneNumber,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (log.callType == CallType.MISSED)
                        MaterialTheme.colorScheme.error
                    else
                        Color.White
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = log.phoneNumber,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Text(
                text = log.callTime.formatTime(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.9f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    placeholder: String,
    onQueryChanged: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    OutlinedTextField(
        value = query,
        onValueChange = {
            query = it
            onQueryChanged(it)
        },
        placeholder = {
            Text(placeholder, color = Color.White.copy(alpha = 0.7f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        shape = RoundedCornerShape(50.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color.White,
            focusedBorderColor = Color.White.copy(alpha = 0.7f),
            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewCallLogScreen() {
    val logs = listOf(
        CallLogEntry(
            id = 1L,
            phoneNumber = "780840285",
            callType = CallType.INCOMING,
            callDurationSeconds = 30,
            callTime = System.currentTimeMillis(),
            contact = Contact(
                id = "1",
                displayName = "Suman Kumar Saurabh",
                phoneNumbers = listOf(
                    PhoneNumber("780840285", isPrimary = true)
                )
            )
        ),
        CallLogEntry(
            id = 2L,
            phoneNumber = "9998887776",
            callType = CallType.OUTGOING,
            callDurationSeconds = 45,
            callTime = System.currentTimeMillis() - 3_600_000,
            contact = Contact(
                id = "2",
                displayName = "Aarav Sharma",
                phoneNumbers = listOf(
                    PhoneNumber("9998887776", isPrimary = true)
                )
            )
        )
    )

    Box(
        modifier = Modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    Color(0xFF6A82FB),
                    Color(0xFFFC5C7D)
                )
            )
        )
    ) {
        CallLogScreen(
            callLogs = logs,
            navController = rememberNavController()
        )
    }
}
