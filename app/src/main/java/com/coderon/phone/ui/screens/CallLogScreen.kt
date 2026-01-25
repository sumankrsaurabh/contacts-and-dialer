package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.coderon.phone.ui.utils.ScaffoldScreen
import kotlinx.coroutines.flow.first

/* ------------------------------------------------ */
/* DATASTORE                                        */
/* ------------------------------------------------ */

private val Context.dataStore by preferencesDataStore("call_log_prefs")
private val FILTER_KEY = stringPreferencesKey("call_filter")

private enum class CallFilter {
    ALL, MISSED_TODAY, LAST_7_DAYS
}

/* ------------------------------------------------ */
/* MAIN SCREEN                                      */
/* ------------------------------------------------ */

@Composable
fun CallLogScreen(
    callLogs: List<CallLog>, navController: NavController
) {
    val context = LocalContext.current

    var filter by remember { mutableStateOf(CallFilter.ALL) }

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

            CallFilter.LAST_7_DAYS -> callLogs.filter {
                it.callTime >= System.currentTimeMillis() - 604_800_000
            }
        }
    }

    val callLogsByDate =
        filteredLogs.sortedByDescending { it.callTime }.groupBy { it.callTime.formatDate() }

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {

        /* ---------- HEADER ---------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Calls",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onBackground
            )

            Spacer(Modifier.height(8.dp))

            FilterChip(filter) { filter = it }
        }

        /* ---------- LIST ---------- */
        LazyColumn(
            contentPadding = PaddingValues(
                start = 12.dp, end = 12.dp, bottom = 120.dp
            )
        ) {
            callLogsByDate.forEach { (date, logs) ->

                item {
                    Text(
                        text = date,
                        fontSize = 14.sp,
                        color = colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                val groupedLogs = logs.groupBy { it.contact?.displayName ?: it.phoneNumber }
                    .map { GroupedCallLog(it.value) }

                items(groupedLogs, key = { it.log.id }) { group ->
                    SamsungPillRow(
                        group = group
                    ) {
                        navController.navigate(
                            Screen.CallDetails.createRoute(group.phoneNumber)
                        )
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* SAMSUNG PILL ROW                                 */
/* ------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun SamsungPillRow(
    group: GroupedCallLog,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    val icon = when (group.callType) {
        CallType.INCOMING -> R.drawable.ic_call_incoming
        CallType.OUTGOING -> R.drawable.ic_call_outgoing
        CallType.MISSED -> R.drawable.ic_call_missed
        else -> R.drawable.call
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(50))
            .background(colorScheme.surfaceContainer)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {

        ProfileAvatar(
            name = group.contact?.displayName ?: group.phoneNumber,
            photoUrl = group.contact?.profilePictureUrl
        )

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {

            Text(
                text = group.contact?.displayName ?: group.phoneNumber,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (group.callType == CallType.MISSED) colorScheme.error else colorScheme.onSurface
            )


            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = if (group.callType == CallType.MISSED) colorScheme.error else colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = group.callTime.formatTime(),
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "SIM" + group.simSlot.toString(),
                    fontSize = 13.sp,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* ------------------------------------------------ */
/* FILTER CHIP                                      */
/* ------------------------------------------------ */

@Composable
private fun FilterChip(
    filter: CallFilter, onChange: (CallFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = when (filter) {
                    CallFilter.ALL -> "All"
                    CallFilter.MISSED_TODAY -> "Missed today"
                    CallFilter.LAST_7_DAYS -> "Last 7 days"
                },
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariant
            )
        }

        DropdownMenu(expanded, { expanded = false }) {
            CallFilter.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.name.replace("_", " ")) },
                    onClick = {
                        onChange(it)
                        expanded = false
                    }
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
            .size(48.dp)
            .clip(CircleShape)
            .background(Brush.verticalGradient(
                listOf(
                    colorScheme.secondary.copy(.5f),
                    colorScheme.primary.copy(.5f)
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
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSecondary
            )
        }
    }
}

/* ------------------------------------------------ */
/* PREVIEW                                          */
/* ------------------------------------------------ */

@PreviewLightDark
@Composable
private fun PreviewSamsungPillCallLog() {
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
                profilePictureUrl = null
            )
        )
    }

    ScaffoldScreen(navController = rememberNavController()) {
        CallLogScreen(
            callLogs = mockCallLogs, navController = rememberNavController()
        )
    }
}
