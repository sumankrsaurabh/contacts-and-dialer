@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.R
import com.coderon.phone.R.drawable
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.components.HybridCallLogPill
import com.coderon.phone.ui.components.HybridContactRow
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SearchScreen(
    navigator: Navigator,
    contacts: List<Contact> = emptyList(),
    logs: List<CallLog> = emptyList(),
    onBack: () -> Unit = { navigator.goBack() }
) {
    val colorScheme = MaterialTheme.colorScheme
    var query by remember { mutableStateOf("") }

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

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
            .background(colorScheme.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .alpha(entryAlpha.value)
            .offset { IntOffset(0, entryOffset.value.roundToInt()) }
    ) {

        /* ---------- HYBRID SEARCH HEADER ---------- */
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = colorScheme.primary
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 17.sp,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                text = "Search name or number",
                                fontSize = 17.sp,
                                color = colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        inner()
                    }
                )

                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Icon(
                        painterResource(drawable.search),
                        contentDescription = null,
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(22.dp)
                    )
                }
            }
        }

        /* ---------- RESULTS ---------- */

        if (query.isEmpty()) {
            EmptySearchState("Search to find contacts or calls")
        } else if (filteredContacts.isEmpty() && filteredLogs.isEmpty()) {
            EmptySearchState("No results found for \"$query\"")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 48.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {

                if (filteredContacts.isNotEmpty()) {
                    item {
                        SectionHeader("CONTACTS")
                    }
                    items(filteredContacts) { contact ->
                        HybridContactRow(
                            name = contact.displayName,
                            subtitle = contact.phoneNumbers.firstOrNull()?.number,
                            photoUrl = contact.profilePictureUrl,
                            onRowClick = {
                                navigator.navigate(Screen.CallDetails(contact.phoneNumbers.firstOrNull()?.number ?: ""))
                            },
                            onInfoClick = {
                                navigator.navigate(Screen.CallDetails(contact.phoneNumbers.firstOrNull()?.number ?: ""))
                            },
                        )
                    }
                }

                if (filteredLogs.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(16.dp))
                        SectionHeader("RECENTS")
                    }
                    items(filteredLogs) { log ->
                        HybridCallLogPill(
                            name = log.contact?.displayName ?: log.phoneNumber,
                            phoneNumber = log.phoneNumber,
                            callType = log.callType,
                            callTime = log.callTime,
                            simSlot = log.simSlot,
                            contact = log.contact,
                            onRowClick = { navigator.navigate(Screen.CallDetails(log.phoneNumber)) },
                            onInfoClick = { navigator.navigate(Screen.CallDetails(log.phoneNumber)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        modifier = Modifier.padding(start = 12.dp, bottom = 8.dp, top = 8.dp)
    )
}

@Composable
private fun EmptySearchState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painterResource(drawable.search),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = message,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSearchHybrid() {
    val navState = rememberNavigationState(
        startRoute = Screen.Search,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }
    
    PhoneTheme {
        SearchScreen(
            navigator = navigator,
            contacts = listOf(
                Contact(
                    id = "1",
                    displayName = "Alice Smith",
                    phoneNumbers = listOf(PhoneNumber("123456"))
                ),
                Contact(
                    id = "2",
                    displayName = "Bob Johnson",
                    phoneNumbers = listOf(PhoneNumber("789012"))
                )
            )
        )
    }
}
