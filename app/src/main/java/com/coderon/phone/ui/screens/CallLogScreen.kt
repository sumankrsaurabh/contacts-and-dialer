@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.telecom.PhoneAccountHandle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.ui.components.HybridAlertDialog
import com.coderon.phone.ui.components.HybridCallLogPill
import com.coderon.phone.ui.components.HybridSegmentedPicker
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.LocalBottomNavVisible
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.placeCall
import com.coderon.phone.viewmodel.CallFilter
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.GroupedCallLog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CallLogScreen(
    callLogsByDate: Map<String, List<GroupedCallLog>>,
    filter: CallFilter,
    onFilterChanged: (CallFilter) -> Unit,
    onDeleteAllLogs: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    // SIM Selection State
    var showSimDialog by remember { mutableStateOf(false) }
    var availableSims by remember { mutableStateOf<List<PhoneAccountHandle>>(emptyList()) }
    var phoneNumberToDial by remember { mutableStateOf("") }

    // Bottom Nav Visibility
    val bottomNavVisible = LocalBottomNavVisible.current
    LaunchedEffect(showSimDialog) {
        bottomNavVisible.value = !showSimDialog
    }

    // Confirmation State
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = colorScheme.background,
            topBar = {
                Box {
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
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .alpha(entryAlpha.value)
                    .offset { IntOffset(0, entryOffset.value.roundToInt()) }
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    HybridSegmentedPicker(
                        options = CallFilter.entries.toTypedArray(),
                        selectedOption = filter,
                        onOptionSelected = onFilterChanged,
                        labelProvider = { if (it == CallFilter.ALL) "All" else "Missed" }
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
                ) {
                    callLogsByDate.forEach { (date, logs) ->
                        item(key = date) {
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

                        items(logs, key = { it.log.id }) { group ->
                            HybridCallLogPill(
                                name = group.contact?.displayName ?: group.phoneNumber,
                                phoneNumber = group.phoneNumber,
                                callType = group.callType,
                                callTime = group.callTime,
                                simSlot = group.simSlot,
                                contact = group.contact,
                                callCount = group.logs.size,
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

@Preview(showBackground = true)
@Composable
fun CallLogPreview() {
    PhoneTheme {
        CallLogScreen(
            callLogsByDate = mapOf(
                "Today" to listOf(
                    GroupedCallLog(listOf(CallLog(phoneNumber = "1234567890", callType = CallType.INCOMING)))
                )
            ),
            filter = CallFilter.ALL,
            onFilterChanged = {},
            onDeleteAllLogs = {},
            navController = rememberNavController()
        )
    }
}
