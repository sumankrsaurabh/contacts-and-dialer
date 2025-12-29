package com.coderon.phone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSearch: (String) -> Unit = {},
    contacts: List<Contact> = emptyList(),
    logs: List<CallLog> = emptyList(),
    navController: NavController,
    onBack: () -> Unit = {}
) {
    val (filteredContacts, filteredCallLogs) = remember(logs, contacts) {
        val latestCallLogsByNumber = logs.groupBy { it.phoneNumber }.mapValues { (_, logs) ->
            logs.maxByOrNull { it.callTime }
        }.filterValues { it != null }

        val filteredContactsList = mutableListOf<Contact>()
        val filteredCallLogsList = mutableListOf<CallLog>()

        contacts.forEach { contact ->
            filteredContactsList.add(contact)
        }
        latestCallLogsByNumber.values.forEach { log ->
            log?.let { filteredCallLogsList.add(it) }
        }
        Pair(filteredContactsList, filteredCallLogsList)
    }
    var text by remember { mutableStateOf(TextFieldValue("")) }
    LaunchedEffect(text.text) {
        onSearch(text.text)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(top = 64.dp, bottom = 16.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { text = it },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = Transparent,
                unfocusedIndicatorColor = Transparent,
            ),
            shape = CircleShape,
            placeholder = { Text("Search", fontSize = 16.sp) },
            singleLine = true,
            maxLines = 1,
            leadingIcon = {
                // Add your search icon here
                IconButton(onClick = { onBack() }) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                }
            },
            trailingIcon = {
                // Add your clear search icon here
                IconButton(onClick = { onSearch(text.text) }) {
                    Icon(painter = painterResource(R.drawable.search), "Search")
                }
            })
        when (text.text.isEmpty()) {
            true -> NoItem()
            false -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        if (filteredContacts.isNotEmpty()) Text(
                            "Contacts",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filteredContacts) { contact ->
                        ContactItemGlass(contact, navController)
                    }

                    item {
                        if (filteredCallLogs.isNotEmpty()) Text(
                            "Call Logs",
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(filteredCallLogs) { callLog ->
                        CallLogItem(
                            callLog,
                            onCallLogEntryClick = { TODO() }
                        )
                    }


                }
            }
        }
    }
}

@Composable
private fun NoItem() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text("No results", fontSize = 16.sp)
    }
}

@Preview
@Composable
private fun Test() {
    SearchScreen(
        contacts = listOf(
            Contact("1", "Alice Johnson", "1234567890", null),
            Contact("2", "Aaron Brown", "9876543210", null),
            Contact("3", "Alex Carter", "1112223333", "https://example.com/profile1.jpg"),
            Contact("4", "Brian Lee", "4445556666", null),
            Contact("5", "Bella Smith", "7778889999", "https://example.com/profile2.jpg"),
            Contact("6", "Charlie Adams", "0001112222", null),
            Contact("7", "David White", "3334445555", "https://example.com/profile3.jpg"),
            Contact("8", "Emma Davis", "6667778888", null)
        ), navController = rememberNavController(), logs = listOf(
            CallLog(
                id = 0L,
                contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
                callTime = System.currentTimeMillis() - 3600000,
                callType = CallType.INCOMING,
                phoneNumber = "7808140285"
            ), CallLog(
                id = 1L,
                contact = Contact(
                    "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
                ),
                callTime = System.currentTimeMillis() - 86400000,
                callType = CallType.OUTGOING,
                phoneNumber = "9998887776"
            )
        )
    )
}