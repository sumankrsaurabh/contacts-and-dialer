package com.coderon.phone.ui.screens

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.coderon.phone.ui.utils.ScaffoldScreen

/* ------------------------------------------------ */
/* ---------------- SEARCH SCREEN ----------------- */
/* ------------------------------------------------ */

@Composable
fun SearchScreen(
    navController: NavController,
    contacts: List<Contact> = emptyList(),
    logs: List<CallLog> = emptyList(),
    onBack: () -> Unit = { navController.popBackStack() }
) {
    val isDark = isSystemInDarkTheme()
    val bg = if (isDark) Color.Black else Color.White
    val primary = if (isDark) Color.White else Color.Black
    val secondary = primary.copy(alpha = 0.6f)
    val searchBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)

    var query by remember { mutableStateOf("") }

    val filteredContacts = remember(query, contacts) {
        if (query.isBlank()) emptyList()
        else contacts.filter {
            it.displayName.contains(query, true) ||
                    it.phoneNumbers.any { p -> p.number.contains(query) }
        }
    }

    val filteredLogs = remember(query, logs) {
        if (query.isBlank()) emptyList()
        else logs.filter {
            it.phoneNumber.contains(query) ||
                    it.contact?.displayName?.contains(query, true) == true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 12.dp)
    ) {

        /* ---------- SEARCH BAR ---------- */

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clip(CircleShape)
                .background(searchBg)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = secondary
                )
            }

            Spacer(Modifier.width(6.dp))

            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 16.sp,
                    color = primary
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search",
                            fontSize = 16.sp,
                            color = secondary
                        )
                    }
                    inner()
                }
            )

            if (query.isNotEmpty()) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = null,
                    tint = secondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        /* ---------- RESULTS ---------- */

        if (query.isEmpty()) {
            EmptySearchState(primary)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {

                if (filteredContacts.isNotEmpty()) {
                    item {
                        SectionHeader("Contacts", secondary)
                    }
                    items(filteredContacts) { contact ->
                        SimpleRow(
                            title = contact.displayName,
                            subtitle = contact.phoneNumbers.firstOrNull()?.number,
                            primary = primary,
                            secondary = secondary
                        )
                    }
                }

                if (filteredLogs.isNotEmpty()) {
                    item {
                        SectionHeader("Recents", secondary)
                    }
                    items(filteredLogs) { log ->
                        SimpleRow(
                            title = log.contact?.displayName ?: log.phoneNumber,
                            subtitle = log.callType.name.lowercase()
                                .replaceFirstChar { it.uppercase() },
                            primary = primary,
                            secondary = secondary
                        )
                    }
                }

                if (filteredContacts.isEmpty() && filteredLogs.isEmpty()) {
                    item {
                        EmptySearchState(primary)
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------ */
/* ---------------- UI PARTS ---------------------- */
/* ------------------------------------------------ */

@Composable
private fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SimpleRow(
    title: String,
    subtitle: String?,
    primary: Color,
    secondary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = primary
        )
        subtitle?.let {
            Text(
                text = it,
                fontSize = 13.sp,
                color = secondary
            )
        }
    }
}

@Composable
private fun EmptySearchState(color: Color) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No Results",
            fontSize = 16.sp,
            color = color.copy(alpha = 0.6f)
        )
    }
}

/* ------------------------------------------------ */
/* ---------------- PREVIEW ----------------------- */
/* ------------------------------------------------ */

@Preview(showBackground = true)
@Composable
private fun PreviewSearchIOS() {
    ScaffoldScreen(rememberNavController()) {
    SearchScreen(
        navController = rememberNavController(),
        contacts = listOf(
            Contact("1", "Alice Johnson"),
            Contact("2", "Brian Lee")
        ),
        logs = listOf(
            CallLog(
                id = 1,
                phoneNumber = "9876543210",
                callType = CallType.INCOMING,
                callTime = System.currentTimeMillis(),
                contact = Contact("3", "John Appleseed")
            )
        )
    )}
}
