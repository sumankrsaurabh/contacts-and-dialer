@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.coderon.phone.R
import com.coderon.phone.data.helpers.formatDate
import com.coderon.phone.data.helpers.formatTime
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
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
    callLogs: List<CallLog>, navController: NavController
) {
    val context = LocalContext.current
    var filter by remember { mutableStateOf(CallFilter.ALL) }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        val savedFilterName = context.dataStore.data.first()[FILTER_KEY]
        filter = try {
            CallFilter.valueOf(savedFilterName ?: CallFilter.ALL.name)
        } catch (e: Exception) {
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

    val callLogsByDate = filteredLogs.sortedByDescending { it.callTime }.groupBy { it.callTime.formatDate() }

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

                LargeTopAppBar(
                    title = {
                        Text("Recents", fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    },
                    actions = {
                        IconButton(onClick = { /* More actions */ }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More", tint = colorScheme.primary)
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
                    selectedFilter = filter,
                    onFilterSelected = { filter = it }
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
                            modifier = Modifier.padding(start = 14.dp, top = 24.dp, bottom = 10.dp)
                        )
                    }

                    val groupedLogs = logs.groupBy { it.contact?.displayName ?: it.phoneNumber }
                        .map { GroupedCallLog(it.value) }

                    items(groupedLogs, key = { it.log.id }) { group ->
                        HybridCallLogPill(
                            group = group,
                            onRowClick = {
                                // iOS style: click row to call
                                // initiateCall(context, group.phoneNumber)
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
}

/* ------------------------------------------------ */
/* HYBRID SEGMENTED PICKER                         */
/* ------------------------------------------------ */

@Composable
private fun HybridSegmentedPicker(
    selectedFilter: CallFilter,
    onFilterSelected: (CallFilter) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(38.dp),
        shape = RoundedCornerShape(50),
        color = colorScheme.surfaceContainerHigh
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CallFilter.entries.forEach { filter ->
                val isSelected = selectedFilter == filter
                val label = if (filter == CallFilter.ALL) "All" else "Missed"
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) colorScheme.surface else Color.Transparent)
                        .clickable { onFilterSelected(filter) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* HYBRID CALL LOG PILL                            */
/* ------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun HybridCallLogPill(
    group: GroupedCallLog,
    onRowClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val icon = when (group.callType) {
        CallType.INCOMING -> R.drawable.ic_call_incoming
        CallType.OUTGOING -> R.drawable.ic_call_outgoing
        CallType.MISSED -> R.drawable.ic_call_missed
        else -> R.drawable.call
    }
    
    val isMissed = group.callType == CallType.MISSED

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(32.dp), // OneUI 8 Super Rounding
        color = colorScheme.surfaceContainerLow,
        onClick = onRowClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                name = group.contact?.displayName ?: group.phoneNumber,
                photoUrl = group.contact?.profilePictureUrl
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = group.contact?.displayName ?: group.phoneNumber,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isMissed) Color.Red else colorScheme.onSurface,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = if (isMissed) Color.Red else colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(Modifier.width(6.dp))

                    Text(
                        text = group.callTime.formatTime(),
                        fontSize = 13.sp,
                        color = colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.width(10.dp))

                    // SIM Badge (OneUI 8 / iOS Pill)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SIM ${group.simSlot}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // iOS style Info icon for details
            IconButton(
                onClick = onInfoClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Rounded.Info,
                    contentDescription = "Details",
                    tint = colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/* ------------------------------------------------ */
/* HELPERS                                          */
/* ------------------------------------------------ */

private data class GroupedCallLog(val logs: List<CallLog>) {
    val log = logs.first()
    val phoneNumber get() = log.phoneNumber
    val callType get() = log.callType
    val callTime get() = log.callTime
    val contact get() = log.contact
    val simSlot get() = log.simSlot
}

@Composable
fun ProfileAvatar(name: String, photoUrl: String?) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(
                listOf(
                    colorScheme.secondaryContainer,
                    colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            )),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer
            )
        }
    }
}

/* ------------------------------------------------ */
/* PREVIEW                                          */
/* ------------------------------------------------ */

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
