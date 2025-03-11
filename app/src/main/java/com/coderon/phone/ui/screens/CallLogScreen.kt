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
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Phone") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            callLog.forEach { (date, logs) ->
                item { CallLogDateHeader(date) }
                itemsIndexed(logs) { index, log ->
                    CallLogItem(
                        log = log,
                        index = index,
                        lastIndex = logs.lastIndex,
                        size = logs.size
                    )
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
private fun CallLogItem(log: CallLog, size: Int, index: Int, lastIndex: Int) {
    val shape = when {
        size == 1 -> RoundedCornerShape(24.dp) // Fully rounded if it's the only item
        index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) // Round top corners
        index == lastIndex -> RoundedCornerShape(
            bottomStart = 24.dp,
            bottomEnd = 24.dp
        ) // Round bottom corners
        else -> RoundedCornerShape(0.dp) // No rounding for middle items
    }

    Card(
        shape = shape,
        modifier = Modifier
            .fillMaxWidth()
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
                ),
                contentDescription = "Call Type",
                tint = Color.DarkGray
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
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
                if (index != lastIndex) HorizontalDivider()
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
            id = 100L,
            callDuration = "30",
            contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
            callTime = System.currentTimeMillis() - 3600000, // 1 hour ago
            callType = CallType.INCOMING,
            phoneNumber = "7808140285"
        ),
        CallLog(
            id = 101L,
            callDuration = "45",
            contact = Contact(
                "2",
                "Aarav Sharma",
                "9998887776",
                "https://example.com/profile1.jpg"
            ),
            callTime = System.currentTimeMillis() - 86400000, // 1 day ago
            callType = CallType.OUTGOING,
            phoneNumber = "9998887776"
        ),
        CallLog(
            id = 102L,
            callDuration = "15",
            contact = Contact("3", "Priya Singh", "9876543210", null),
            callTime = System.currentTimeMillis() - 5400000, // 1.5 hours ago
            callType = CallType.MISSED,
            phoneNumber = "9876543210"
        ),
        CallLog(
            id = 103L,
            callDuration = "120",
            contact = Contact("4", "Rohit Verma", "8974561230", "https://example.com/profile2.jpg"),
            callTime = System.currentTimeMillis() - 172800000, // 2 days ago
            callType = CallType.OUTGOING,
            phoneNumber = "8974561230"
        ),
        CallLog(
            id = 104L,
            callDuration = "60",
            contact = Contact("5", "Anjali Kapoor", "7854123690", null),
            callTime = System.currentTimeMillis() - 10800000, // 3 hours ago
            callType = CallType.INCOMING,
            phoneNumber = "7854123690"
        ),
        CallLog(
            id = 105L,
            callDuration = "5",
            contact = Contact("6", "Vikas Patel", "9638527410", null),
            callTime = System.currentTimeMillis() - 259200000, // 3 days ago
            callType = CallType.MISSED,
            phoneNumber = "9638527410"
        ),
        CallLog(
            id = 106L,
            callDuration = "20",
            contact = Contact("7", "Meera Joshi", "8527419630", "https://example.com/profile3.jpg"),
            callTime = System.currentTimeMillis() - 432000000, // 5 days ago
            callType = CallType.OUTGOING,
            phoneNumber = "8527419630"
        ),
        CallLog(
            id = 107L,
            callDuration = "90",
            contact = Contact("8", "Raj Malhotra", "7896541230", null),
            callTime = System.currentTimeMillis() - 7200000, // 2 hours ago
            callType = CallType.INCOMING,
            phoneNumber = "7896541230"
        ),
        CallLog(
            id = 108L,
            callDuration = "10",
            contact = Contact(
                "9",
                "Kavita Sharma",
                "9517538520",
                "https://example.com/profile4.jpg"
            ),
            callTime = System.currentTimeMillis() - 604800000, // 7 days ago
            callType = CallType.MISSED,
            phoneNumber = "9517538520"
        ),
        CallLog(
            id = 109L,
            callDuration = "25",
            contact = Contact("10", "Sameer Khan", "7531598524", null),
            callTime = System.currentTimeMillis() - 14400000, // 4 hours ago
            callType = CallType.OUTGOING,
            phoneNumber = "7531598524"
        )
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
