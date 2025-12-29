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
import com.coderon.phone.data.model.PhoneNumber
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
    object CallDetails : Screen("contact_details/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "contact_details/$phoneNumber"
    }

    object CallScreen : Screen("call_screen")
}

@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun MyApp() {
    val navController = rememberNavController()

    val contactViewModel: ContactViewModel = koinViewModel()
    val callLogViewModel: CallLogViewModel = koinViewModel()

    val currentCallState = CallManager.phoneState.collectAsStateWithLifecycle().value

    val contactsGrouped =
        contactViewModel.groupedContacts.collectAsStateWithLifecycle().value

    val filteredContacts =
        contactViewModel.allContacts.collectAsStateWithLifecycle().value

    val callLogs =
        callLogViewModel.filteredCallLogs.collectAsStateWithLifecycle().value

    LaunchedEffect(currentCallState) {
        if (currentCallState != NoCall) {
            navController.navigate(Screen.CallScreen.route)
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Keypad.route
    ) {

        fun updateSearchQuery(query: String) {
            contactViewModel.onSearchQueryChanged(query)
            callLogViewModel.onSearchQueryChanged(query)
        }

        /* -------------------- KEYPAD -------------------- */
        composable(Screen.Keypad.route) {
            if (currentCallState == NoCall) {
                ScaffoldScreen(navController) {
                    DialerScreen(
                        navController = navController,
                        filterContact = contactViewModel.groupedContacts,
                        filterCallLog = callLogViewModel.filteredCallLogs,
                        updateSearchQuery = ::updateSearchQuery,
                        playTones = { playTones(it) }
                    )
                }
            }
        }

        /* -------------------- RECENT -------------------- */
        composable(Screen.Recent.route) {
            if (currentCallState == NoCall) {
                ScaffoldScreen(navController) {
                    CallLogScreen(
                        callLogs = callLogs,
                        navController = navController
                    )
                }
            }
        }

        /* -------------------- CONTACTS -------------------- */
        composable(Screen.Contacts.route) {
            if (currentCallState == NoCall) {
                ScaffoldScreen(navController) {
                    ContactsScreen(
                        contacts = contactsGrouped,
                        navController = navController
                    )
                }
            }
        }

        /* -------------------- SEARCH -------------------- */
        composable(Screen.Search.route) {
            SearchScreen(
                navController = navController,
                contacts = filteredContacts,
                logs = callLogs,
                onSearch = ::updateSearchQuery,
                onBack = { navController.popBackStack() }
            )
        }

        /* -------------------- ADD CONTACT -------------------- */
        composable(Screen.AddContact.route) {
            if (currentCallState == NoCall) {
                AddContactScreen(
//                    onSaveContact = contactViewModel::saveContact
                )
            }
        }

        /* -------------------- CALL DETAILS -------------------- */
        composable(Screen.CallDetails.route) { backStackEntry ->
            if (currentCallState == NoCall) {

                val routePhoneNumber =
                    backStackEntry.arguments?.getString("phoneNumber").orEmpty()

                val normalizedRouteNumber =
                    routePhoneNumber.replace(Regex("[^0-9+]"), "")

                val callLogsForNumber =
                    callLogViewModel
                        .getCallLogsForNumber(normalizedRouteNumber)
                        .collectAsStateWithLifecycle(emptyList())
                        .value

                val matchedContact = filteredContacts.firstOrNull { contact ->
                    contact.phoneNumbers.any { phone ->
                        normalize(phone.number) == normalizedRouteNumber
                    }
                }

                ContactDetailsScreen(
                    contact = matchedContact ?: Contact(
                        id = "",
                        displayName = normalizedRouteNumber,
                        phoneNumbers = listOf(
                            PhoneNumber(normalizedRouteNumber, isPrimary = true)
                        ),
                        profilePictureUrl = null
                    ),
                    callLogs = callLogsForNumber,
                    navController = navController,
                    onEditClick = { },
                    onDeleteClick = { }
                )
            }
        }

        /* -------------------- INCALL UI -------------------- */
        composable(Screen.CallScreen.route) {
            CallScreen(navController)
        }
    }
}

/* -------------------- HELPERS -------------------- */

private fun normalize(number: String): String {
    val clean = number.replace(Regex("[^0-9+]"), "")
    return when {
        clean.startsWith("+91") -> clean.substring(3)
        clean.startsWith("91") -> clean.substring(2)
        else -> clean
    }
}
