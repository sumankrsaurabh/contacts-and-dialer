@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.data.model.PhoneNumberType
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme

/* ------------------------------------------------
   SCREEN
------------------------------------------------ */

@Composable
fun ContactDetailsScreen(
    contact: Contact,
    callLogs: List<CallLog>,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {
    Scaffold(
        containerColor =
            if (isSystemInDarkTheme()) Color.Black else Color.White
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                ContactHeader(
                    contact = contact,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    navController = navController
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
                PhoneNumbersSection(contact.phoneNumbers)
            }

            if (contact.emailAddresses.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(16.dp))
                    EmailSection(contact.emailAddresses)
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                StatusSection(contact)
            }

            if (callLogs.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(24.dp))
                    Text("Recent Calls", fontSize = 18.sp)
                }

                items(callLogs) { log ->
                    CallLogItem(log)
                }
            }
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

    TopAppBar(
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, null)
            }
        },
        title = {},
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Rounded.MoreVert, null)
            }
            DropdownMenu(expanded, { expanded = false }) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = { expanded = false; onEditClick() }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { expanded = false; onDeleteClick() }
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        if (!contact.profilePictureUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(contact.profilePictureUrl)
                        .crossfade(true)
                        .build()
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
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
            text = contact.displayName,
            fontSize = 24.sp
        )

        if (contact.firstName != null || contact.lastName != null) {
            Text(
                text = listOfNotNull(contact.firstName, contact.lastName).joinToString(" "),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/* ------------------------------------------------
   PHONE NUMBERS
------------------------------------------------ */

@Composable
private fun PhoneNumbersSection(numbers: List<PhoneNumber>) {
    SectionCard("Phone Numbers") {
        numbers.forEach { phone ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(phone.number)
                Text(
                    phone.type.name,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ------------------------------------------------
   EMAILS
------------------------------------------------ */

@Composable
private fun EmailSection(emails: List<String>) {
    SectionCard("Emails") {
        emails.forEach {
            Text(it, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

/* ------------------------------------------------
   STATUS
------------------------------------------------ */

@Composable
private fun StatusSection(contact: Contact) {
    SectionCard("Status") {
        if (contact.isFavorite) Text("⭐ Favorite")
        if (contact.isBlocked) Text("🚫 Blocked")
        if (!contact.isFavorite && !contact.isBlocked) {
            Text("Normal contact")
        }
    }
}

/* ------------------------------------------------
   CALL LOG ITEM
------------------------------------------------ */

@Composable
private fun CallLogItem(log: CallLog) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(
                    when (log.callType) {
                        CallType.INCOMING -> R.drawable.incoming_call
                        CallType.OUTGOING -> R.drawable.outgoing_call
                        CallType.MISSED -> R.drawable.missed_call
                        CallType.REJECTED -> R.drawable.call
                        CallType.BLOCKED -> R.drawable.call
                        CallType.VOICEMAIL -> R.drawable.call
                        else -> R.drawable.call
                    }
                ),
                contentDescription = null,
                tint = if (log.callType == CallType.MISSED) Color.Red
                else MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(log.phoneNumber)
                Text(
                    "${log.callType.name} • SIM ${log.simSlot}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = log.callTime.formatTime(),
                fontSize = 12.sp
            )
        }
    }
}

/* ------------------------------------------------
   SECTION CARD
------------------------------------------------ */

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }
}

@Preview
@Composable
private fun ContactDetailsScreenPreview() {
    PhoneTheme {
        ContactDetailsScreen(
            contact = Contact(
                id = "1",
                displayName = "John Doe",
                firstName = "John",
                lastName = "Doe",
                phoneNumbers = listOf(
                    PhoneNumber("123-456-7890", PhoneNumberType.MOBILE, true),
                    PhoneNumber("098-765-4321", PhoneNumberType.WORK, false)
                ),
                emailAddresses = listOf("john.doe@example.com"),
                isFavorite = true
            ),
            callLogs = listOf(
                CallLog(
                    id = 1,
                    phoneNumber = "123-456-7890",
                    callType = CallType.INCOMING,
                    callDurationSeconds = 60,
                    simSlot = 1
                ),
                CallLog(
                    id = 2,
                    phoneNumber = "123-456-7890",
                    callType = CallType.MISSED,
                    simSlot = 2
                )
            ),
            onEditClick = {},
            onDeleteClick = {},
            navController = rememberNavController()
        )
    }
}
