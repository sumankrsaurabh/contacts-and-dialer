package com.coderon.phone.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccessTime
import androidx.compose.material.icons.twotone.Contacts
import androidx.compose.material.icons.twotone.KeyboardCommandKey
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.CallLogDetailsScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.IncomingCallScreen
import com.coderon.phone.ui.screens.OutgoingCallScreen
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun MyApp() {
    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = koinViewModel()
    val callLogViewModel: CallLogViewModel = koinViewModel()
    Scaffold(bottomBar = {
        BottomNavigationBar(navController = navController)
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "keypad",
            Modifier.padding(innerPadding),
        ) {
            composable("keypad") {
                DialerScreen({ _, _ -> }) {}
            }
            composable("recent") {
                CallLogScreen(
                    callLog = callLogViewModel.callLogs.collectAsStateWithLifecycle().value,
                    filteredCallLogs = callLogViewModel::filteredCallLogs,
                    navController = navController,
                    onSearchContact = callLogViewModel::filteredCallLogs
                )
            }
            composable("contacts") {
                ContactsScreen(
                    contacts = contactViewModel.contacts.collectAsStateWithLifecycle().value,
                    onAddContactClick = { navController.navigate("add_contact") },
                    onSearchContact = contactViewModel::filteredContacts
                )
            }
            composable("add_contact") {
                AddContactScreen(
                    onSaveContact = contactViewModel::saveContact
                )
            }
            composable("contact_details/{phoneNumber}") {
                val phoneNumber = it.arguments?.getString("phoneNumber")
                CallLogDetailsScreen(
                    phoneNumber = phoneNumber,
                    getContact = contactViewModel::getContact,
                    getCallLogForPhoneNumber = callLogViewModel::getCallLogsForNumber,
                    onCallClick = {},
                    onMessageClick = {},
                    onBlockClick = {},
                    navController = navController
                )
            }

            composable("incoming_call") {
                IncomingCallScreen(
                    onAnswer = {},
                    onDecline = {}
                )
            }
            composable("outgoing_call/{phoneNumber}") {
                val phoneNumber = it.arguments?.getString("phoneNumber")
                OutgoingCallScreen(
                    contactName = phoneNumber ?: "",
                    contactPhoneNumber = phoneNumber ?: "",
                    onEndCall = {}
                )
            }
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val currentRoute = navController.currentDestination?.route
        val fontFamily = FontFamily(Font(R.font.regular))
        BottomNavigationItems.entries.forEach { item ->
            NavigationBarItem(
                label = { Text(text = item.label, fontFamily = fontFamily) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(route = item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon, contentDescription = item.label
                    )
                })
        }
    }
}

enum class BottomNavigationItems(val route: String, val label: String, val icon: ImageVector) {
    Keypad("keypad", "Keypad", Icons.TwoTone.KeyboardCommandKey),
    Recent("recent", "Recent", Icons.TwoTone.AccessTime),
    Contacts("contacts", "Contacts", Icons.TwoTone.Contacts)
}

