package com.coderon.phone.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.coderon.phone.call.ui.screens.incallui.CallScreen
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactDetailsScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.SearchScreen
import com.coderon.phone.ui.utils.ScaffoldScreen
import com.coderon.phone.utils.normalizePhoneNumber
import com.coderon.phone.utils.playTones
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    contactViewModel: ContactViewModel,
    callLogViewModel: CallLogViewModel
) {
    val groupedContacts = contactViewModel.groupedContacts.collectAsStateWithLifecycle().value
    val allContacts = contactViewModel.allContacts.collectAsStateWithLifecycle().value
    val filteredCallLogs = callLogViewModel.filteredCallLogs.collectAsStateWithLifecycle().value

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
                    navController = navController,
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

        /* -------------------- ADD/EDIT CONTACT -------------------- */
        composable(
            route = Screen.AddContact.route,
            arguments = listOf(
                navArgument("number") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("contactId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val number = backStackEntry.arguments?.getString("number")
            val contactId = backStackEntry.arguments?.getString("contactId")
            
            val existingContact = if (contactId != null) {
                allContacts.firstOrNull { it.id == contactId }
            } else null

            // Use key to force re-initialization when contact is loaded from the database
            key(existingContact?.id ?: "new_contact") {
                AddContactScreen(
                    navController = navController,
                    initialPhoneNumber = number,
                    existingContact = existingContact,
                    onSaveContact = { contact ->
                        if (contactId != null) {
                            contactViewModel.updateContact(
                                contactId = contactId,
                                firstName = contact.firstName,
                                lastName = contact.lastName,
                                displayName = contact.displayName,
                                phoneNumbers = contact.phoneNumbers,
                                emailAddresses = contact.emailAddresses,
                                profilePictureUri = contact.profilePictureUrl,
                                isFavorite = contact.isFavorite
                            )
                        } else {
                            contactViewModel.saveContact(
                                firstName = contact.firstName,
                                lastName = contact.lastName,
                                displayName = contact.displayName,
                                phoneNumbers = contact.phoneNumbers,
                                emailAddresses = contact.emailAddresses,
                                profilePictureUri = contact.profilePictureUrl,
                                isFavorite = contact.isFavorite
                            )
                        }
                    }
                )
            }
        }

        /* -------------------- CONTACT DETAILS -------------------- */
        composable(Screen.CallDetails.route) { backStackEntry ->
            val routePhoneNumber = backStackEntry.arguments?.getString("phoneNumber").orEmpty()
            val normalizedRouteNumber = normalizePhoneNumber(routePhoneNumber)

            val callLogsForNumber = callLogViewModel
                .getCallLogsForNumber(normalizedRouteNumber)
                .collectAsStateWithLifecycle(emptyList())
                .value

            val matchedContact = allContacts.firstOrNull { contact ->
                contact.phoneNumbers.any { phone ->
                    normalizePhoneNumber(phone.number) == normalizedRouteNumber
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
                onToggleFavorite = { contactViewModel.toggleFavorite(it) },
                onEditContact = { contact ->
                    navController.navigate(Screen.AddContact.createRoute(contactId = contact.id))
                }
            )
        }

        /* -------------------- INCALL UI -------------------- */
        composable(Screen.CallScreen.route) {
            CallScreen(navController)
        }
    }
}
