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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.components.HybridContactRow
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.LocalBottomNavVisible
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.getAvailableSims
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.placeCall
import kotlinx.coroutines.launch
import java.util.SortedMap
import java.util.TreeMap
import kotlin.math.roundToInt

@Composable
fun ContactsScreen(
    contactsGrouped: SortedMap<Char, List<Contact>>,
    navigator: Navigator,
    defaultSimId: String? = null,
    showContactPhoto: Boolean = true,
    displayNameFormat: Int = 0, // 0: First Last, 1: Last First
    swipeEnabled: Boolean = true
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

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

    fun onCallClick(number: String) {
        phoneNumberToDial = number
        val sims = getAvailableSims(context)
        val defaultSim = sims.find { it.id == defaultSimId }
        
        if (defaultSim != null) {
            placeCall(context, number, defaultSim)
        } else {
            initiateCall(context, number) { available ->
                availableSims = available
                showSimDialog = true
            }
        }
    }

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
                            Text(
                                "Contacts",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        },
                        actions = {
                            FilledTonalIconButton(onClick = { navigator.navigate(Screen.AddContact()) }) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Add",
                                    tint = colorScheme.primary
                                )
                            }
                            IconButton(onClick = { navigator.navigate(Screen.Settings) }) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = "Settings",
                                    tint = colorScheme.onSurfaceVariant
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .alpha(entryAlpha.value)
                    .offset { IntOffset(0, entryOffset.value.roundToInt()) },
                contentPadding = PaddingValues(
                    top = 16.dp,
                    bottom = 100.dp,
                    start = 16.dp,
                    end = 16.dp
                )
            ) {
                contactsGrouped.forEach { (letter, list) ->
                    item(key = letter) {
                        Text(
                            text = letter.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorScheme.primary.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 28.dp, bottom = 12.dp, top = 16.dp),
                            letterSpacing = 0.8.sp
                        )
                    }

                    items(list, key = { it.id }) { contact ->
                        val phoneNumber = contact.phoneNumbers.firstOrNull()?.number.orEmpty()
                        val displayName = if (displayNameFormat == 0) {
                            contact.displayName
                        } else {
                            if (!contact.lastName.isNullOrBlank() && !contact.firstName.isNullOrBlank()) {
                                "${contact.lastName} ${contact.firstName}"
                            } else {
                                contact.displayName
                            }
                        }
                        
                        HybridContactRow(
                            name = displayName,
                            subtitle = phoneNumber,
                            photoUrl = if (showContactPhoto) contact.profilePictureUrl else null,
                            onRowClick = {
                                if (phoneNumber.isNotBlank()) {
                                    onCallClick(phoneNumber)
                                }
                            },
                            onInfoClick = {
                                navigator.navigate(Screen.CallDetails(phoneNumber))
                            }
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        if (showSimDialog) {
            SimSelectionDialog(
                availableAccounts = availableSims,
                onSimSelected = { handle ->
                    showSimDialog = false
                    handle?.let { placeCall(context, phoneNumberToDial, handle)}
                },
                onDismiss = { showSimDialog = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHybridContacts() {
    val navState = rememberNavigationState(
        startRoute = Screen.Contacts,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }
    
    PhoneTheme {
        ContactsScreen(
            contactsGrouped = TreeMap<Char, List<Contact>>().apply {
                put('A', listOf(Contact(id = "1", displayName = "Alice", phoneNumbers = listOf(PhoneNumber("123456")))))
                put('B', listOf(Contact(id = "2", displayName = "Bob", phoneNumbers = listOf(PhoneNumber("789012")))))
            },
            navigator = navigator
        )
    }
}
