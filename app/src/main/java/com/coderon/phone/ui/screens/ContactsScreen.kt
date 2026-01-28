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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
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
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.components.HybridContactRow
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.LocalBottomNavVisible
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.initiateCall
import com.coderon.phone.utils.placeCall
import kotlinx.coroutines.launch
import java.util.SortedMap
import java.util.TreeMap
import kotlin.math.roundToInt

@Composable
fun ContactsScreen(
    contactsGrouped: SortedMap<Char, List<Contact>>,
    navController: NavController
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
                            FilledTonalIconButton(onClick = { navController.navigate(Screen.AddContact.route) }) {
                                Icon(
                                    Icons.Rounded.Add,
                                    contentDescription = "Add",
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
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(list, key = { it.id }) { contact ->
                        val phoneNumber = contact.phoneNumbers.firstOrNull()?.number.orEmpty()
                        HybridContactRow(
                            name = contact.displayName,
                            subtitle = phoneNumber,
                            photoUrl = contact.profilePictureUrl,
                            onRowClick = {
                                if (phoneNumber.isNotBlank()) {
                                    phoneNumberToDial = phoneNumber
                                    initiateCall(context, phoneNumber) { sims ->
                                        availableSims = sims
                                        showSimDialog = true
                                    }
                                }
                            },
                            onInfoClick = {
                                navController.navigate(Screen.CallDetails.createRoute(phoneNumber))
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
                    placeCall(context, phoneNumberToDial, handle)
                },
                onDismiss = { showSimDialog = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHybridContacts() {
    PhoneTheme {
        ContactsScreen(
            contactsGrouped = TreeMap<Char, List<Contact>>().apply {
                put('A', listOf(Contact(id = "1", displayName = "Alice", phoneNumbers = listOf(PhoneNumber("123456")))))
                put('B', listOf(Contact(id = "2", displayName = "Bob", phoneNumbers = listOf(PhoneNumber("789012")))))
            },
            navController = rememberNavController()
        )
    }
}
