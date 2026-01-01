package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.ScaffoldScreen

/* ------------------------------------------------ */
/* ---------------- CALL LOG SCREEN --------------- */
/* ------------------------------------------------ */

@Composable
fun CallLogScreen(
    callLogs: List<CallLog>,
    navController: NavController,
    onDeleteCalls: (List<Long>) -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color(0xFF121212) else Color(0xFFF5F5F5)
    val primary = if (isDark) Color.White else Color.Black
    val secondary = primary.copy(alpha = 0.6f)
    val divider = primary.copy(alpha = 0.06f)

    var editMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }

    val groupedByDate = remember(callLogs) {
        callLogs
            .sortedByDescending { it.callTime }
            .groupBy { it.callTime.formatDate() }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(bg)
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {

            Spacer(Modifier.height(8.dp))

            /* ---------- TOP BAR ---------- */

            TopBarIOS(
                editMode = editMode,
                selectedCount = selectedIds.size,
                onEditToggle = {
                    editMode = !editMode
                    if (!editMode) selectedIds.clear()
                }
            )

            /* ---------- CALL LIST ---------- */

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = if (editMode) 160.dp else 100.dp
                )
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
                            secondaryText = secondary,
                            divider = divider
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

        /* ---------- EDIT TOOLBAR ---------- */

        if (editMode) {
            EditActionBar(
                enabled = selectedIds.isNotEmpty(),
                onDelete = {
                    onDeleteCalls(selectedIds.toList())
                    selectedIds.clear()
                    editMode = false
                }
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- TOP BAR ----------------------- */
/* ------------------------------------------------ */

@Composable
private fun TopBarIOS(
    editMode: Boolean,
    selectedCount: Int,
    onEditToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        val onBg = if (isSystemInDarkTheme()) Color.Black.copy(.7f) else Color.White.copy(.7f)
        val onBgItem = if (isSystemInDarkTheme()) Color.White.copy(.7f) else Color.Black.copy(.7f)

        Text(
            text = if (editMode) "$selectedCount Selected" else "Calls",
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            color = onBgItem,
            modifier = Modifier.align(Alignment.Center)
        )

        Text(
            text = if (editMode) "Done" else "Edit",
            fontSize = 17.sp,
            color = onBgItem,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onEditToggle() }
                .background(onBg, CircleShape)
                .padding(12.dp, 8.dp)
        )

        if (!editMode) {
            IconButton(
                {},
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = onBg,
                    contentColor = onBgItem
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_filter),
                    contentDescription = "Filter",
                    tint = onBgItem,
                    modifier = Modifier
//                        .align(Alignment.CenterEnd)
                        .size(22.dp)
                )
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- EDIT TOOLBAR ------------------ */
/* ------------------------------------------------ */

@Composable
private fun EditActionBar(
    enabled: Boolean,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        val onBg = if (isSystemInDarkTheme()) Color.Black.copy(.7f) else Color.White.copy(.7f)


        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            Text(
                text = "Delete",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) Color.Red else Color.Red.copy(alpha = 0.4f),
                modifier = Modifier
                    .clickable(enabled = enabled) { onDelete() }
                    .background(onBg, CircleShape)
                    .padding(12.dp, 8.dp)
            )
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- GROUP MODEL ------------------- */
/* ------------------------------------------------ */

private data class GroupedCallLog(val logs: List<CallLog>) {
    val primary = logs.first()
    val count = logs.size
    val phoneNumber get() = primary.phoneNumber
    val callTime get() = primary.callTime
    val callType get() = primary.callType
    val contact get() = primary.contact
}

/* ------------------------------------------------ */
/* ---------------- DATE HEADER ------------------- */
/* ------------------------------------------------ */

@Composable
private fun DateHeaderIOS(date: String, color: Color) {
    Text(
        text = date,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

/* ------------------------------------------------ */
/* ---------------- CALL ROW ---------------------- */
/* ------------------------------------------------ */

@SuppressLint("MissingPermission")
@Composable
private fun CallLogRow(
    log: GroupedCallLog,
    editMode: Boolean,
    selected: Boolean,
    primaryText: Color,
    secondaryText: Color,
    divider: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
    ) {

        Row(
            modifier = Modifier.padding(vertical = 16.dp),
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (log.callType == CallType.MISSED)
                            Color.Red else primaryText
                    )

                    if (log.count > 1) {
                        Text(
                            text = " (${log.count})",
                            fontSize = 14.sp,
                            color = secondaryText
                        )
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

        HorizontalDivider(Modifier, DividerDefaults.Thickness, color = divider)
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
            .background(if (selected) Color(0xFF007AFF) else Color.Transparent)
            .border(1.5.dp, Color.Gray, CircleShape),
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

    val sampleNames = listOf(
        "John Appleseed",
        "Alice Johnson",
        "Brian Lee",
        "Catherine Smith",
        "David Miller",
        "Emma Wilson",
        "Frank Thomas",
        "Grace Kim",
        "Henry Brown",
        "Ivy Anderson",
        "Jack White",
        "Katherine Moore",
        "Liam Harris",
        "Mia Clark",
        "Noah Lewis",
        "Olivia Walker",
        "Paul Young",
        "Quinn Scott",
        "Rachel Green",
        "Samuel King"
    )

    val logs = List(20) { index ->
        CallLog(
            id = index.toLong(),
            phoneNumber = "98${70 + index}54${100 + index}",
            callType = when {
                index % 3 == 0 -> CallType.INCOMING
                index % 3 == 1 -> CallType.OUTGOING
                else -> CallType.MISSED
            },
            callTime = System.currentTimeMillis() - index * 60 * 60 * 1000L,
            contact = Contact(
                id = index.toString(),
                displayName = sampleNames[index],
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
