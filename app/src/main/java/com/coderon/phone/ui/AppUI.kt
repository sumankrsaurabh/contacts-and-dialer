package com.coderon.phone.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccessTime
import androidx.compose.material.icons.twotone.Contacts
import androidx.compose.material.icons.twotone.Dialpad
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.call.CallManager
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
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    object Keypad : Screen("keypad")
    object Recent : Screen("recent")
    object Contacts : Screen("contacts")
    object AddContact : Screen("add_contact")
    object CallDetails : Screen("contact_details/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "contact_details/$phoneNumber"
    }

    object IncomingCall : Screen("incoming_call")
    object OutgoingCall : Screen("outgoing_call/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "outgoing_call/$phoneNumber"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = koinViewModel()
    val callLogViewModel: CallLogViewModel = koinViewModel()
    val callManager: CallManager = koinInject()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Keypad.route,
            Modifier.padding(innerPadding),
        ) {
            composable(Screen.Keypad.route) {
                DialerScreen()
            }
            composable(Screen.Recent.route) {
                val callLogs = callLogViewModel.callLogs.collectAsStateWithLifecycle().value
                CallLogScreen(
                    callLog = callLogs,
                    filteredCallLogs = callLogViewModel::filteredCallLogs,
                    navController = navController,
                    onSearchContact = callLogViewModel::filteredCallLogs
                )
            }
            composable(Screen.Contacts.route) {
                val contacts = contactViewModel.contacts.collectAsStateWithLifecycle().value
                ContactsScreen(
                    contacts = contacts,
                    onAddContactClick = { navController.navigate(Screen.AddContact.route) },
                    onSearchContact = contactViewModel::filteredContacts
                )
            }
            composable(Screen.AddContact.route) {
                AddContactScreen(
                    onSaveContact = contactViewModel::saveContact
                )
            }
            composable(Screen.CallDetails.route) { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                CallLogDetailsScreen(
                    phoneNumber = phoneNumber,
                    getContact = contactViewModel::getContact,
                    getCallLogForPhoneNumber = callLogViewModel::getCallLogsForNumber,
                    onCallClick = {},
                    onMessageClick = { /* Handle messaging */ },
                    onBlockClick = { /* Handle blocking */ },
                    navController = navController
                )
            }
            composable(Screen.IncomingCall.route) {
                IncomingCallScreen(
                    onAnswer = { },
                    onDecline = { }
                )
            }
            composable(Screen.OutgoingCall.route) { backStackEntry ->
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                OutgoingCallScreen(
                    contactName = phoneNumber,
                    contactPhoneNumber = phoneNumber,
                    onEndCall = { }
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val fontFamily = FontFamily(Font(R.font.regular))

    NavigationBar {
        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                label = { Text(text = item.label, fontFamily = fontFamily) },
                selected = currentRoute == item.label,
                onClick = {
                    navController.navigate(item.label) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) }
            )
        }
    }
}

val bottomNavigationItems = listOf(
    BottomNavigationItem(Screen.Keypad, "Keypad", Icons.TwoTone.Dialpad),
    BottomNavigationItem(Screen.Recent, "Recent", Icons.TwoTone.AccessTime),
    BottomNavigationItem(Screen.Contacts, "Contacts", Icons.TwoTone.Contacts)
)

data class BottomNavigationItem(val screen: Screen, val label: String, val icon: ImageVector)
