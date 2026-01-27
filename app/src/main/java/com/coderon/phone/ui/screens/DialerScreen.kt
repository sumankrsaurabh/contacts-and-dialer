package com.coderon.phone.ui.screens

import android.telecom.PhoneAccountHandle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.components.DialPad
import com.coderon.phone.ui.components.HybridCallLogPill
import com.coderon.phone.ui.components.HybridContactRow
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.placeCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.coderon.phone.data.model.CallLog as CallLogEntry

@Composable
fun DialerScreen(
    navController: NavController,
    filterContact: StateFlow<Map<Char, List<Contact>>>,
    filterCallLog: Flow<List<CallLogEntry>>,
    updateSearchQuery: (String) -> Unit = {},
    playTones: (Char) -> Unit
) {
    var dialedNumber by remember { mutableStateOf("") }
    val context = LocalContext.current

    // SIM Selection State
    var showSimDialog by remember { mutableStateOf(false) }
    var availableSims by remember { mutableStateOf<List<PhoneAccountHandle>>(emptyList()) }
    var phoneNumberToDial by remember { mutableStateOf("") }

    LaunchedEffect(dialedNumber) {
        updateSearchQuery(dialedNumber)
    }

    val contacts by filterContact.collectAsStateWithLifecycle()
    val callLogs by filterCallLog.collectAsStateWithLifecycle(emptyList())
    val colorScheme = MaterialTheme.colorScheme

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.background)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(bottom = 104.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            /* ---------- REDESIGNED DIALED TEXT (iOS / OneUI 8 Mix) ---------- */
            Text(
                text = dialedNumber.ifBlank { " " },
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                maxLines = 1
            )

            if (dialedNumber.isNotBlank()) {
                Text(
                    text = "Add to Contacts",
                    color = colorScheme.primary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            navController.navigate(Screen.AddContact.createRoute(dialedNumber))
                        }
                )
            } else {
                Spacer(Modifier.height(37.dp))
            }

            Spacer(Modifier.height(8.dp))

            val suggestions = remember(dialedNumber, contacts, callLogs) {
                if (dialedNumber.isBlank()) emptyList()
                else {
                    val map = linkedMapOf<String, Any>()

                    callLogs
                        .filter { it.phoneNumber.contains(dialedNumber) }
                        .groupBy { it.phoneNumber }
                        .mapNotNull { it.value.maxByOrNull { log -> log.callTime } }
                        .forEach { map[it.phoneNumber] = it }

                    contacts.values.flatten().forEach { contact ->
                        val number = contact.phoneNumbers.firstOrNull()?.number ?: return@forEach
                        if (number.contains(dialedNumber)) {
                            map.putIfAbsent(number, contact)
                        }
                    }

                    map.values.toList()
                }
            }

            /* ---------- REDESIGNED SUGGESTIONS ---------- */
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (suggestions.isNotEmpty()) {
                    item {
                        Text(
                            text = "SUGGESTIONS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(suggestions) { item ->
                        when (item) {
                            is CallLogEntry -> {
                                HybridCallLogPill(
                                    name = item.contact?.displayName ?: item.phoneNumber,
                                    phoneNumber = item.phoneNumber,
                                    callType = item.callType,
                                    callTime = item.callTime,
                                    simSlot = item.simSlot,
                                    contact = item.contact,
                                    onRowClick = {
                                        phoneNumberToDial = item.phoneNumber
                                        initiateCall(context, item.phoneNumber) { sims ->
                                            availableSims = sims
                                            showSimDialog = true
                                        }
                                    },
                                    onInfoClick = {
                                        navController.navigate(Screen.CallDetails.createRoute(item.phoneNumber))
                                    }
                                )
                            }

                            is Contact -> {
                                val contactNumber = item.phoneNumbers.firstOrNull()?.number ?: ""
                                HybridContactRow(
                                    name = item.displayName,
                                    subtitle = contactNumber,
                                    photoUrl = item.profilePictureUrl,
                                    onRowClick = {
                                        phoneNumberToDial = contactNumber
                                        initiateCall(context, contactNumber) { sims ->
                                            availableSims = sims
                                            showSimDialog = true
                                        }
                                    },
                                    onInfoClick = {
                                        navController.navigate(
                                            Screen.CallDetails.createRoute(
                                                contactNumber
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            DialPad(
                playTones = playTones,
                onDigitPress = {
                    if (dialedNumber.length < 15) {
                        dialedNumber += it
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.width(56.dp))

                FilledIconButton(
                    onClick = {
                        if (dialedNumber.isNotBlank()) {
                            phoneNumberToDial = dialedNumber
                            initiateCall(context, dialedNumber) { sims ->
                                availableSims = sims
                                showSimDialog = true
                            }
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        painter = painterResource(R.drawable.call),
                        contentDescription = "Call",
                        modifier = Modifier.size(34.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            colorScheme.surfaceVariant.copy(
                                alpha = if (dialedNumber.isEmpty()) 0.4f else 1f
                            ),
                            CircleShape
                        )
                        .combinedClickable(
                            enabled = dialedNumber.isNotEmpty(),
                            onClick = { dialedNumber = dialedNumber.dropLast(1) },
                            onLongClick = { dialedNumber = "" }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete),
                        contentDescription = "Delete",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        if (showSimDialog) {
            SimSelectionDialog(
                availableAccounts = availableSims,
                onSimSelected = { handle ->
                    showSimDialog = false
                    placeCall(context, phoneNumberToDial, handle)
                },
                onDismiss = { showSimDialog = false }
            )
        }
    }
}

/* ---------------- PREVIEW DATA ---------------- */

private fun previewContacts(): Map<Char, List<Contact>> {
    val contacts = (1..10).map {
        Contact(
            id = it.toString(),
            displayName = "Contact $it",
            phoneNumbers = listOf(
                PhoneNumber(
                    number = "98765432$it",
                )
            )
        )
    }
    return contacts.groupBy { it.displayName.first() }
}

private fun previewCallLogs(): List<CallLogEntry> {
    return (1..10).map {
        CallLogEntry(
            id = it.toLong(),
            phoneNumber = "98765432$it",
            callTime = System.currentTimeMillis() - it * 60_000L,
            contact = null,
            callType = CallType.INCOMING,
        )
    }
}

/* ---------------- PREVIEW ---------------- */

@Preview(showBackground = true)
@Composable
fun DialerPreview() {
    ScaffoldScreen(rememberNavController()) {
        DialerScreen(
            navController = rememberNavController(),
            filterContact = MutableStateFlow(previewContacts()),
            filterCallLog = MutableStateFlow(previewCallLogs()),
            updateSearchQuery = {},
            playTones = {}
        )
    }
}
