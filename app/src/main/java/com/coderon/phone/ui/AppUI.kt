package com.coderon.phone.ui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.screens.incallui.CallScreen
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactDetailsScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.SearchScreen
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.utils.playTones
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.koinViewModel

/* ------------------------------------------------
   NAV ROUTES
------------------------------------------------ */

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

/* ------------------------------------------------
   ROOT APP
------------------------------------------------ */
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun MyApp() {

    val navController = rememberNavController()

    val contactViewModel: ContactViewModel = koinViewModel()
    val callLogViewModel: CallLogViewModel = koinViewModel()

    val callUiState =
        CallManager.uiState.collectAsStateWithLifecycle().value

    val groupedContacts =
        contactViewModel.groupedContacts.collectAsStateWithLifecycle().value

    val allContacts =
        contactViewModel.allContacts.collectAsStateWithLifecycle().value

    val filteredCallLogs =
        callLogViewModel.filteredCallLogs.collectAsStateWithLifecycle().value

    /* ------------------------------------------------
       AUTO NAVIGATION (SIMPLE & SAFE)
    ------------------------------------------------ */

    val activeCallId = callUiState.primaryCall?.id

    LaunchedEffect(activeCallId) {
        if (activeCallId != null) {
            navController.navigate(Screen.CallScreen.route) {
                launchSingleTop = true
            }
        } else {
            navController.popBackStack(
                route = Screen.Keypad.route,
                inclusive = false
            )
        }
    }

    /* ------------------------------------------------
       NAV HOST
    ------------------------------------------------ */

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

        /* -------------------- RECENT -------------------- */
        composable(Screen.Recent.route) {
            ScaffoldScreen(navController) {
                CallLogScreen(
                    callLogs = filteredCallLogs,
                    navController = navController
                )
            }
        }

        /* -------------------- CONTACTS -------------------- */
        composable(Screen.Contacts.route) {
            ScaffoldScreen(navController) {
                ContactsScreen(
                    contacts = groupedContacts,
                    navController = navController
                )
            }
        }

        /* -------------------- SEARCH -------------------- */
        composable(Screen.Search.route) {
            ScaffoldScreen(navController) {
                SearchScreen(
                    navController = navController,
                    contacts = allContacts,
                    logs = filteredCallLogs,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        /* -------------------- ADD CONTACT -------------------- */
        composable(Screen.AddContact.route) {
            AddContactScreen()
        }

        /* -------------------- CONTACT DETAILS -------------------- */
        composable(Screen.CallDetails.route) { backStackEntry ->

            val routePhoneNumber =
                backStackEntry.arguments?.getString("phoneNumber").orEmpty()

            val normalizedRouteNumber = normalize(routePhoneNumber)

            val callLogsForNumber =
                callLogViewModel
                    .getCallLogsForNumber(normalizedRouteNumber)
                    .collectAsStateWithLifecycle(emptyList())
                    .value

            val matchedContact = allContacts.firstOrNull { contact ->
                contact.phoneNumbers.any { phone ->
                    normalize(phone.number) == normalizedRouteNumber
                }
            }

            ContactDetailsScreen(
                contact = matchedContact ?: Contact(
                    displayName = normalizedRouteNumber,
                    phoneNumbers = listOf(
                        PhoneNumber(
                            number = normalizedRouteNumber,
                            isPrimary = true
                        )
                    )
                ),
                callLogs = callLogsForNumber,
                navController = navController,
//                onEditClick = {},
//                onDeleteClick = {}
            )
        }

        /* -------------------- INCALL UI -------------------- */
        composable(Screen.CallScreen.route) {
            CallScreen(navController)
        }
    }
}

/* ------------------------------------------------
   PHONE NORMALIZATION
------------------------------------------------ */

private fun normalize(number: String): String {
    val clean = number.replace(Regex("[^0-9+]"), "")
    return when {
        clean.startsWith("+91") -> clean.substring(3)
        clean.startsWith("91") -> clean.substring(2)
        else -> clean
    }
}

/* ------------------------------------------------ */
/* ---------------- PREVIEW DATA ------------------ */
/* ------------------------------------------------ */

private fun previewCallLogs(): List<CallLog> {
    val now = System.currentTimeMillis()

    return List(20) { index ->
        CallLog(
            id = index.toLong(),
            phoneNumber = "98765432${index}",
            callType = when (index % 3) {
                0 -> CallType.INCOMING
                1 -> CallType.OUTGOING
                else -> CallType.MISSED
            },
            callDurationSeconds = (10..300).random(),
            callTime = now - (index * 60 * 60 * 1000L),
            contact = if (index % 4 == 0) null else Contact(
                id = index.toString(),
                displayName = "Contact $index",
                phoneNumbers = listOf(
                    PhoneNumber(
                        number = "98765432$index",
                        isPrimary = true
                    )
                ),
                profilePictureUrl = null
            )
        )
    }
}

/* ------------------------------------------------ */
/* ---------------- PREVIEW ----------------------- */
/* ------------------------------------------------ */

@Preview
@Composable
private fun Test() {
    val navController = rememberNavController()

    ScaffoldScreen(navController = navController) {
        CallLogScreen(
            callLogs = previewCallLogs(),
            navController = navController
        )
    }
}

