@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.content.Context
import android.telecom.PhoneAccountHandle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.components.HybridAlertDialog
import com.coderon.phone.ui.components.HybridCallLogPill
import com.coderon.phone.ui.components.HybridSegmentedPicker
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.placeCall
import kotlinx.coroutines.flow.first

/* ------------------------------------------------ */
/* DATASTORE                                        */
/* ------------------------------------------------ */

private val Context.dataStore by preferencesDataStore("call_log_prefs")
private val FILTER_KEY = stringPreferencesKey("call_filter")

private enum class CallFilter {
    ALL, MISSED_TODAY
}

/* ------------------------------------------------ */
/* MAIN SCREEN (iOS + OneUI 8 + M3)                */
/* ------------------------------------------------ */

@Composable
fun CallLogScreen(
    callLogs: List<CallLog>,
    navController: NavController,
    onDeleteAllLogs: () -> Unit = {}
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(CallFilter.ALL) }
    val colorScheme = MaterialTheme.colorScheme

    // SIM Selection State
    var showSimDialog by remember { mutableStateOf(false) }
    var availableSims by remember { mutableStateOf<List<PhoneAccountHandle>>(emptyList()) }
    var phoneNumberToDial by remember { mutableStateOf("") }

    // Confirmation State
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val savedFilterName = context.dataStore.data.first()[FILTER_KEY]
        filter = try {
            CallFilter.valueOf(savedFilterName ?: CallFilter.ALL.name)
        } catch (ignored: Exception) {
            CallFilter.ALL
        }
    }

    LaunchedEffect(filter) {
        context.dataStore.edit { it[FILTER_KEY] = filter.name }
    }

    val filteredLogs = remember(callLogs, filter) {
        when (filter) {
            CallFilter.ALL -> callLogs
            CallFilter.MISSED_TODAY -> callLogs.filter {
                it.callType == CallType.MISSED && it.callTime >= System.currentTimeMillis() - 86_400_000
            }
        }
    }

    val callLogsByDate =
        filteredLogs.sortedByDescending { it.callTime }.groupBy { it.callTime.formatDate() }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colorScheme.background,
            topBar = {
                Box {
                    // iOS Blur Background Effect
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .blur(24.dp)
                            .background(colorScheme.background.copy(alpha = 0.65f))
                    )

                    TopAppBar(
                        title = {
                            Text("Recents", fontWeight = FontWeight.Bold, fontSize = 32.sp)
                        },
                        actions = {
                            IconButton(onClick = { showDeleteConfirmation = true }) {
                                Icon(
                                    Icons.Rounded.DeleteSweep,
                                    contentDescription = "Clear All",
                                    tint = colorScheme.primary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = colorScheme.surfaceContainer.copy(alpha = 0.9f)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {

                // iOS/OneUI 8 Segmented Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HybridSegmentedPicker(
                        options = CallFilter.entries.toTypedArray(),
                        selectedOption = filter,
                        onOptionSelected = { filter = it },
                        labelProvider = { if (it == CallFilter.ALL) "All" else "Missed" }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
                ) {
                    callLogsByDate.forEach { (date, logs) ->
                        item {
                            Text(
                                text = date.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.padding(
                                    start = 14.dp,
                                    top = 24.dp,
                                    bottom = 10.dp
                                )
                            )
                        }

                        val groupedLogs = logs.groupBy { it.id } // Using id as unique key for now
                            .map { GroupedCallLog(it.value) }

                        items(groupedLogs, key = { it.log.id }) { group ->
                            HybridCallLogPill(
                                name = group.contact?.displayName ?: group.phoneNumber,
                                phoneNumber = group.phoneNumber,
                                callType = group.callType,
                                callTime = group.callTime,
                                simSlot = group.simSlot,
                                contact = group.contact,
                                onRowClick = {
                                    initiateCall(context, group.phoneNumber) { sims ->
                                        availableSims = sims
                                        phoneNumberToDial = group.phoneNumber
                                        showSimDialog = true
                                    }
                                },
                                onInfoClick = {
                                    navController.navigate(
                                        Screen.CallDetails.createRoute(group.phoneNumber)
                                    )
                                }
                            )
                        }
                    }
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

        if (showDeleteConfirmation) {
            HybridAlertDialog(
                title = "Clear All Recents?",
                message = "Are you sure you want to delete all call logs? This action cannot be undone.",
                confirmText = "Clear All",
                confirmColor = Color.Red,
                onConfirm = {
                    onDeleteAllLogs()
                    showDeleteConfirmation = false
                },
                onDismiss = { showDeleteConfirmation = false }
            )
        }
    }
}

private data class GroupedCallLog(val logs: List<CallLog>) {
    val log = logs.first()
    val phoneNumber get() = log.phoneNumber
    val callType get() = log.callType
    val callTime get() = log.callTime
    val contact get() = log.contact
    val simSlot get() = log.simSlot
}

@PreviewLightDark
@Composable
private fun PreviewHybridCallLog() {
    val mockContactNames = listOf(
        "John Appleseed",
        "Alice Johnson",
        "Brian Lee",
        "Catherine Smith",
        "David Miller",
        "Emma Wilson"
    )

    val mockCallLogs = List(12) { logIndex ->
        CallLog(
            id = logIndex.toLong(),
            phoneNumber = "98765432$logIndex",
            callType = when (logIndex % 3) {
                0 -> CallType.INCOMING
                1 -> CallType.OUTGOING
                else -> CallType.MISSED
            },
            callTime = System.currentTimeMillis() - logIndex * 3_600_000L,
            contact = Contact(
                id = "$logIndex",
                displayName = mockContactNames[logIndex % mockContactNames.size],
                phoneNumbers = listOf(com.coderon.phone.data.model.PhoneNumber("98765432$logIndex")),
                profilePictureUrl = null
            )
        )
    }

    PhoneTheme {
        CallLogScreen(
            callLogs = mockCallLogs, navController = rememberNavController()
        )
    }
}
