package com.coderon.phone.ui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.services.NoCall
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactDetailsScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.SearchScreen
import com.coderon.phone.ui.screens.incallui.CallScreen
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.utils.playTones
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Keypad : Screen("keypad")
    object Recent : Screen("recent")
    object Contacts : Screen("contacts")
    object Search : Screen("search")
    object AddContact : Screen("add_contact")
    object CallDetails : Screen("contact_details/{phoneNumber}")
    object CallScreen : Screen("call_screen")
}

@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun MyApp() {
    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = koinViewModel()
    val callLogViewModel: CallLogViewModel = koinViewModel()

    val currentCallState = CallManager.phoneState.collectAsStateWithLifecycle().value

    LaunchedEffect(currentCallState) {
        if (currentCallState != NoCall) {
            navController.navigate(Screen.CallScreen.route) {
                /* popUpTo(0) // Clear the back stack to prevent going back*/
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Keypad.route
    ) {
        // Screens inside Scaffold (Keypad, Recent, Contacts)
        composable(Screen.Keypad.route) {
            if (currentCallState == NoCall) {
                fun updateSearchQuery(query: String) {
                    contactViewModel.onSearchQueryChanged(query)
                    callLogViewModel.onSearchQueryChanged(query)
                }
                ScaffoldScreen(navController) {
                    DialerScreen(
                        navController,
                        contactViewModel.groupedContacts,
                        callLogViewModel.filteredCallLogs,
                        updateSearchQuery = { updateSearchQuery(it) },
                        playTones = { playTones(it) }
                    )
                }
            }
        }
        composable(Screen.Recent.route) {
            if (currentCallState == NoCall) {
                val callLogs = callLogViewModel.allCallLogs.collectAsStateWithLifecycle().value
                ScaffoldScreen(navController) {
                    CallLogScreen(
                        callLogs = callLogs,
                        navController = navController
                    )
                }
            }
        }
        composable(Screen.Contacts.route) {
            if (currentCallState == NoCall) {
                val contacts = contactViewModel.groupedContacts.collectAsStateWithLifecycle().value
                ScaffoldScreen(navController) {
                    ContactsScreen(contacts = contacts, navController)
                }
            }
        }

        composable(Screen.CallScreen.route) {
            CallScreen(navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.AddContact.route) {
            if (currentCallState == NoCall) {
                AddContactScreen(onSaveContact = contactViewModel::saveContact)
            }
        }
        composable(Screen.CallDetails.route) { backStackEntry ->
            if (currentCallState == NoCall) {
                val phoneNumber = backStackEntry.arguments?.getString("phoneNumber") ?: ""
                val contact = contactViewModel.getContact(phoneNumber)
                val callLogs = callLogViewModel.getCallLogsForNumber(phoneNumber)
                    .collectAsStateWithLifecycle(emptyList()).value
                ContactDetailsScreen(
                    contact = contact ?: Contact(
                        id = "",
                        name = "",
                        phoneNumber = phoneNumber,
                        profilePictureUrl = null
                    ),
                    callLogs = callLogs,
                    onMessageClick = {},
                    onBlockClick = {},
                    navController = navController
                )
            }
        }
    }
}