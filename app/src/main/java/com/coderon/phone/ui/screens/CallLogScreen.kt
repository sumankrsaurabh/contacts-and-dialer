package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
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
import com.coderon.phone.ui.BottomNavigationBar
import com.coderon.phone.ui.theme.PhoneTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    callLog: Map<String, List<CallLog>>, navController: NavController
) {
    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                com.coderon.phone.ui.Text("Phone")
            },
            expandedHeight = TopAppBarDefaults.LargeAppBarExpandedHeight
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            callLog.forEach { (date, logs) ->
                item { CallLogDateHeader(date) }
                itemsIndexed(logs) { index, log ->
                    CallLogItem(log, index, logs.lastIndex)
                }
            }
        }
    }
}

@Composable
fun CallLogDateHeader(date: String) {
    Text(
        text = date,
        fontSize = 18.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.onBackground
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun CallLogItem(log: CallLog, index: Int, lastIndex: Int) {
    Card(
        shape = when (index) {
            0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            lastIndex -> RoundedCornerShape(
                bottomStart = 24.dp,
                bottomEnd = 24.dp
            )

            else -> RoundedCornerShape(0.dp)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp),
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
                ), contentDescription = "Call Type", tint = Color.DarkGray
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = log.contact?.name ?: log.phoneNumber,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.weight(1f))
                    Text(text = log.callTime.formatTime(), fontSize = 14.sp, color = Color.DarkGray)
                }
                if (index != lastIndex)
                    HorizontalDivider()
            }
        }
    }
}

@Composable
fun ContactProfileImage(contact: Contact?) {
    val profilePictureUrl = contact?.profilePictureUrl
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (profilePictureUrl != null) {
            AsyncImage(
                model = profilePictureUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier.size(48.dp)
            )
        } else {
            Text(
                text = contact?.name?.firstOrNull()?.toString() ?: "?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@PreviewLightDark
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
        ),
        CallLog(
            id = 1L,
            callDuration = "45",
            contact = Contact(
                "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
            ),
            callTime = System.currentTimeMillis() - 86400000,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        ),
        CallLog(
            id = 0L,
            callDuration = "30",
            contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
            callTime = System.currentTimeMillis() - 3600000,
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        ),
        CallLog(
            id = 1L,
            callDuration = "45",
            contact = Contact(
                "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
            ),
            callTime = System.currentTimeMillis() - 86400000,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        ),
        CallLog(
            id = 0L,
            callDuration = "30",
            contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
            callTime = System.currentTimeMillis() - 3600000,
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        ),
        CallLog(
            id = 1L,
            callDuration = "45",
            contact = Contact(
                "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
            ),
            callTime = System.currentTimeMillis() - 86400000,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        ),
        CallLog(
            id = 0L,
            callDuration = "30",
            contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
            callTime = System.currentTimeMillis() - 3600000,
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        ),
        CallLog(
            id = 1L,
            callDuration = "45",
            contact = Contact(
                "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
            ),
            callTime = System.currentTimeMillis() - 86400000,
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        ),
    )
    PhoneTheme {
        Scaffold(
            bottomBar = { BottomNavigationBar(rememberNavController()) },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                CallLogScreen(
                    callLog = sampleLogs.groupBy { it.callTime.formatDate() },
                    navController = rememberNavController()
                )
            }
        }
    }
}
