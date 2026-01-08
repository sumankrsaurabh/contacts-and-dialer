package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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

/* ------------------------------------------------ */
/* ---------------- DATASTORE --------------------- */
/* ------------------------------------------------ */

private val Context.dataStore by preferencesDataStore("call_log_prefs")
private val FILTER_KEY = stringPreferencesKey("call_filter")

private enum class CallFilter {
    ALL, MISSED_TODAY, LAST_7_DAYS
}

/* ------------------------------------------------ */
/* ---------------- CALL LOG SCREEN --------------- */
/* ------------------------------------------------ */

@Composable
fun CallLogScreen(
    callLogs: List<CallLog>,
    navController: NavController,
    onDeleteCalls: (List<Long>) -> Unit = {}
) {
    val context = LocalContext.current
    rememberCoroutineScope()

    var filter by remember { mutableStateOf(CallFilter.ALL) }
    var editMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    /* ---------- Restore Filter ---------- */
    LaunchedEffect(Unit) {
        val saved = context.dataStore.data.first()[FILTER_KEY]
        filter = CallFilter.valueOf(saved ?: CallFilter.ALL.name)
    }

    /* ---------- Persist Filter ---------- */
    LaunchedEffect(filter) {
        context.dataStore.edit {
            it[FILTER_KEY] = filter.name
        }
    }

    val filteredLogs = remember(callLogs, filter) {
        when (filter) {
            CallFilter.ALL -> callLogs
            CallFilter.MISSED_TODAY ->
                callLogs.filter {
                    it.callType == CallType.MISSED &&
                            it.callTime >= System.currentTimeMillis() - 24 * 60 * 60 * 1000
                }

            CallFilter.LAST_7_DAYS ->
                callLogs.filter {
                    it.callTime >= System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                }
        }
    }

    val groupedByDate = filteredLogs
        .sortedByDescending { it.callTime }
        .groupBy { it.callTime.formatDate() }

    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0xFF121212) else Color(0xFFF5F5F5)
    val primary = if (isDark) Color.White else Color.Black
    val secondary = primary.copy(alpha = 0.6f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {

        Spacer(Modifier.height(8.dp))

        /* ---------- TOP BAR ---------- */
        TopBarIOS(
            filter = filter,
            editMode = editMode,
            selectedCount = selectedIds.size,
            onFilterChange = { filter = it },
            onEditToggle = {
                editMode = !editMode
                if (!editMode) selectedIds.clear()
            },
            onDelete = {
                onDeleteCalls(selectedIds.toList())
                selectedIds.clear()
                editMode = false
            }
        )

        /* ---------- LIST ---------- */
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            groupedByDate.forEach { (date, logs) ->

                item {
                    DateHeaderIOS(date, secondary)
                }

                val grouped = logs
                    .groupBy { it.contact?.displayName ?: it.phoneNumber }
                    .map { GroupedCallLog(it.value) }

                items(grouped, key = { it.primary.id }) { group ->
                    val selected = selectedIds.contains(group.primary.id)

                    CallLogRow(
                        log = group,
                        editMode = editMode,
                        selected = selected,
                        primaryText = primary,
                        secondaryText = secondary
                    ) {
                        if (editMode) {
                            if (selected)
                                selectedIds.remove(group.primary.id)
                            else
                                selectedIds.add(group.primary.id)
                        } else {
                            navController.navigate(
                                Screen.CallDetails.createRoute(group.phoneNumber)
                            )
                        }
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- TOP BAR ----------------------- */
/* ------------------------------------------------ */

@Composable
private fun TopBarIOS(
    filter: CallFilter,
    editMode: Boolean,
    selectedCount: Int,
    onFilterChange: (CallFilter) -> Unit,
    onEditToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (editMode) "$selectedCount Selected" else "Calls",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = if (editMode) "Done" else "Edit",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onEditToggle() },
            color = Color(0xFF007AFF),
            fontSize = 17.sp
        )

        if (!editMode) {
            FilterMenu(
                filter = filter,
                onFilterChange = onFilterChange,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        } else {
            Text(
                text = "Delete",
                color = Color.Red,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable(enabled = selectedCount > 0) { onDelete() }
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- FILTER MENU ------------------- */
/* ------------------------------------------------ */

@Composable
private fun FilterMenu(
    filter: CallFilter,
    onFilterChange: (CallFilter) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                painter = painterResource(R.drawable.ic_filter),
                contentDescription = "Filter"
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = { onFilterChange(CallFilter.ALL); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Missed Today") },
                onClick = { onFilterChange(CallFilter.MISSED_TODAY); expanded = false }
            )
            DropdownMenuItem(
                text = { Text("Last 7 Days") },
                onClick = { onFilterChange(CallFilter.LAST_7_DAYS); expanded = false }
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- ROW + MODELS ------------------ */
/* ------------------------------------------------ */

private data class GroupedCallLog(val logs: List<CallLog>) {
    val primary = logs.first()
    val count = logs.size
    val phoneNumber get() = primary.phoneNumber
    val callTime get() = primary.callTime
    val callType get() = primary.callType
    val contact get() = primary.contact
}

@Composable
private fun DateHeaderIOS(date: String, color: Color) {
    Text(
        text = date,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier.padding(16.dp)
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun CallLogRow(
    log: GroupedCallLog,
    editMode: Boolean,
    selected: Boolean,
    primaryText: Color,
    secondaryText: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        if (editMode) {
            SelectionDot(selected)
            Spacer(Modifier.width(12.dp))
        }

        ProfileAvatar(
            name = log.contact?.displayName ?: log.phoneNumber,
            photoUrl = log.contact?.profilePictureUrl
        )

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Row {
                Text(
                    text = log.contact?.displayName ?: log.phoneNumber,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = if (log.callType == CallType.MISSED)
                        Color.Red else primaryText
                )
                if (log.count > 1) {
                    Text(" (${log.count})", color = secondaryText)
                }
            }
            Text(
                text = log.callType.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                color = secondaryText
            )
        }

        Text(
            text = log.callTime.formatTime(),
            fontSize = 13.sp,
            color = secondaryText
        )
    }
}

/* ------------------------------------------------ */
/* ---------------- UI HELPERS -------------------- */
/* ------------------------------------------------ */

@Composable
private fun SelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(1.5.dp, Color.Gray, CircleShape)
            .background(if (selected) Color(0xFF007AFF) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ProfileAvatar(name: String, photoUrl: String?) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Gray.copy(alpha = 0.25f)),
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
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- PREVIEW ----------------------- */
/* ------------------------------------------------ */

@PreviewLightDark
@Composable
private fun PreviewCallLogIOS() {

    val names = listOf(
        "John Appleseed", "Alice Johnson", "Brian Lee", "Catherine Smith",
        "David Miller", "Emma Wilson", "Frank Thomas", "Grace Kim",
        "Henry Brown", "Ivy Anderson", "Jack White", "Katherine Moore",
        "Liam Harris", "Mia Clark", "Noah Lewis", "Olivia Walker",
        "Paul Young", "Quinn Scott", "Rachel Green", "Samuel King"
    )

    val logs = List(20) { i ->
        CallLog(
            id = i.toLong(),
            phoneNumber = "98${70 + i}54${100 + i}",
            callType = when (i % 3) {
                0 -> CallType.INCOMING
                1 -> CallType.OUTGOING
                else -> CallType.MISSED
            },
            callTime = System.currentTimeMillis() - i * 3_600_000L,
            contact = Contact(
                id = "$i",
                displayName = names[i],
                profilePictureUrl = null
            )
        )
    }

    ScaffoldScreen(
        navController = rememberNavController()
    ) {
        CallLogScreen(
            callLogs = logs,
            navController = rememberNavController()
        )
    }
}
