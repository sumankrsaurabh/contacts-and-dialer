package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.coderon.phone.ui.utils.ScaffoldScreen
import kotlinx.coroutines.flow.first

/* ------------------------------------------------ *//* DATASTORE                                        *//* ------------------------------------------------ */

private val Context.dataStore by preferencesDataStore("call_log_prefs")
private val FILTER_KEY = stringPreferencesKey("call_filter")

private enum class CallFilter {
    ALL, MISSED_TODAY, LAST_7_DAYS
}

/* ------------------------------------------------ *//* MAIN SCREEN                                      *//* ------------------------------------------------ */

@Composable
fun CallLogScreen(
    callLogs: List<CallLog>, navController: NavController
) {
    val context = LocalContext.current

    var filter by remember { mutableStateOf(CallFilter.ALL) }

    LaunchedEffect(Unit) {
        val savedFilterName = context.dataStore.data.first()[FILTER_KEY]
        filter = CallFilter.valueOf(savedFilterName ?: CallFilter.ALL.name)
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

    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (isDark) Color(0xFF0E0E0E) else Color(0xFFF4F4F4)
    val rowBackgroundColor = if (isDark) Color(0xFF1F1F1F) else Color.White
    val contentColor = if (isDark) Color.White else Color.Black
    val subContentColor = contentColor.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {

        /* ---------- HEADER ---------- */
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Calls", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = contentColor
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
                        color = subContentColor,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                val groupedLogs = logs.groupBy { it.contact?.displayName ?: it.phoneNumber }
                    .map { GroupedCallLog(it.value) }

                items(groupedLogs, key = { it.contentColor.id }) { group ->
                    SamsungPillRow(
                        group = group,
                        contentColor = contentColor,
                        subContentColor = subContentColor,
                        pillColor = rowBackgroundColor
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

/* ------------------------------------------------ *//* SAMSUNG PILL ROW                                 *//* ------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun SamsungPillRow(
    group: GroupedCallLog,
    contentColor: Color,
    subContentColor: Color,
    pillColor: Color,
    onClick: () -> Unit
) {
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
            .background(pillColor)
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
                color = if (group.callType == CallType.MISSED) Color(0xFFD32F2F) else contentColor
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = group.callTime.formatTime(), fontSize = 13.sp, color = subContentColor
                )

                Spacer(Modifier.width(6.dp))

                Text(
                    text = "SIM" + group.simSlot.toString(),
                    fontSize = 13.sp,
                    color = subContentColor
                )


            }
        }
    }
}

/* ------------------------------------------------ *//* FILTER CHIP                                      *//* ------------------------------------------------ */

@Composable
private fun FilterChip(
    filter: CallFilter, onChange: (CallFilter) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFFE0E0E0))
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = when (filter) {
                    CallFilter.ALL -> "All"
                    CallFilter.MISSED_TODAY -> "Missed today"
                    CallFilter.LAST_7_DAYS -> "Last 7 days"
                }, fontSize = 14.sp
            )
        }

        DropdownMenu(expanded, { expanded = false }) {
            CallFilter.values().forEach {
                DropdownMenuItem(text = { Text(it.name.replace("_", " ")) }, onClick = {
                    onChange(it)
                    expanded = false
                })
            }
        }
    }
}

/* ------------------------------------------------ *//* HELPERS                                          *//* ------------------------------------------------ */

private data class GroupedCallLog(val mockCallLogs: List<CallLog>) {
    val contentColor = mockCallLogs.first()
    val phoneNumber get() = contentColor.phoneNumber
    val callType get() = contentColor.callType
    val callTime get() = contentColor.callTime
    val contact get() = contentColor.contact
    val simSlot get() = contentColor.simSlot
}

@Composable
private fun ProfileAvatar(name: String, photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.25f)), contentAlignment = Alignment.Center
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

/* ------------------------------------------------ *//* PREVIEW                                          *//* ------------------------------------------------ */

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
