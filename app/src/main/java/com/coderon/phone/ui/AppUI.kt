package com.coderon.phone.ui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.services.NoCall
import com.coderon.phone.data.model.CallLog
import com.coderon.phone.data.model.CallType
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactDetailsScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.SearchScreen
import com.coderon.phone.ui.screens.incallui.CallScreen
import com.coderon.phone.ui.theme.PhoneTheme
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

    // Ensure the CallScreen is always shown when a call is active
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
                ScaffoldScreen(navController) {
                    DialerScreen(
                        navController,
                        contactViewModel::filteredContacts,
                        callLogViewModel::filteredCallLogs,
                        playTones = { playTones(it) }
                    )
                }
            }
        }
        composable(Screen.Recent.route) {
            if (currentCallState == NoCall) {
                val callLogs = callLogViewModel.callLogs.collectAsStateWithLifecycle().value
                ScaffoldScreen(navController) {
                    CallLogScreen(
                        callLog = callLogs,
                        navController = navController
                    )
                }
            }
        }
        composable(Screen.Contacts.route) {
            if (currentCallState == NoCall) {
                val contacts = contactViewModel.contacts.collectAsStateWithLifecycle().value
                ScaffoldScreen(navController) {
                    ContactsScreen(
                        contacts = contacts,
//                        onAddContactClick = { navController.navigate(Screen.AddContact.route) },
//                        onSearchContact = contactViewModel::filteredContacts,
                        navController
                    )
                }
            }
        }

        // Screens outside Scaffold (Call Screen, Add Contact, Contact Details)
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


/**
 * Wrapper to apply Scaffold only to certain screens
 */
@Composable
fun ScaffoldScreen(navController: NavController, content: @Composable () -> Unit) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
//                .clip(RoundedCornerShape(32.dp))
        ) {
            content()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val selectedColor = if (isDarkTheme) Color.White else Color.Black
    val unselectedColor = if (isDarkTheme) Color.LightGray else Color.Gray

    NavigationBar(containerColor = backgroundColor) {
        bottomNavigationItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            Column(
                Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (selected) item.selectedIcon else item.icon,
                    contentDescription = item.label,
                    tint = if (selected) selectedColor else unselectedColor
                )
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    color = if (selected) selectedColor else unselectedColor
                )
            }
        }
    }
}


val bottomNavigationItems = listOf(
    BottomNavigationItem(Screen.Keypad, "Keypad", Icons.Outlined.Dialpad, Icons.Filled.Dialpad),
    BottomNavigationItem(
        Screen.Recent,
        "Recent",
        Icons.Outlined.AccessTime,
        Icons.Filled.AccessTime
    ),
    BottomNavigationItem(Screen.Contacts, "Contacts", Icons.Outlined.People, Icons.Filled.People)
)

data class BottomNavigationItem(
    val screen: Screen, val label: String, val icon: ImageVector, val selectedIcon: ImageVector
)

@Preview
@PreviewLightDark
@Composable
private fun Test() {
    PhoneTheme {
        ScaffoldScreen(rememberNavController()) {
            CallLogScreen(
                callLog = listOf(
                    CallLog(
                        id = 100L,
                        callDuration = "30",
                        contact = Contact("1", "Suman Kumar Saurabh", "780840285", null),
                        callTime = System.currentTimeMillis() - 3600000, // 1 hour ago
                        callType = CallType.INCOMING,
                        phoneNumber = "7808140285"
                    ), CallLog(
                        id = 101L, callDuration = "45", contact = Contact(
                            "2", "Aarav Sharma", "9998887776", "https://example.com/profile1.jpg"
                        ), callTime = System.currentTimeMillis() - 86400000, // 1 day ago
                        callType = CallType.OUTGOING, phoneNumber = "9998887776"
                    ), CallLog(
                        id = 102L,
                        callDuration = "15",
                        contact = Contact("3", "Priya Singh", "9876543210", null),
                        callTime = System.currentTimeMillis() - 5400000, // 1.5 hours ago
                        callType = CallType.MISSED,
                        phoneNumber = "9876543210"
                    ), CallLog(
                        id = 103L,
                        callDuration = "120",
                        contact = Contact(
                            "4",
                            "Rohit Verma",
                            "8974561230",
                            "https://example.com/profile2.jpg"
                        ),
                        callTime = System.currentTimeMillis() - 172800000, // 2 days ago
                        callType = CallType.OUTGOING,
                        phoneNumber = "8974561230"
                    ), CallLog(
                        id = 104L,
                        callDuration = "60",
                        contact = Contact("5", "Anjali Kapoor", "7854123690", null),
                        callTime = System.currentTimeMillis() - 10800000, // 3 hours ago
                        callType = CallType.INCOMING,
                        phoneNumber = "7854123690"
                    ), CallLog(
                        id = 105L,
                        callDuration = "5",
                        contact = Contact("6", "Vikas Patel", "9638527410", null),
                        callTime = System.currentTimeMillis() - 259200000, // 3 days ago
                        callType = CallType.MISSED,
                        phoneNumber = "9638527410"
                    ), CallLog(
                        id = 106L,
                        callDuration = "20",
                        contact = Contact(
                            "7",
                            "Meera Joshi",
                            "8527419630",
                            "https://example.com/profile3.jpg"
                        ),
                        callTime = System.currentTimeMillis() - 432000000, // 5 days ago
                        callType = CallType.OUTGOING,
                        phoneNumber = "8527419630"
                    ), CallLog(
                        id = 107L,
                        callDuration = "90",
                        contact = Contact("8", "Raj Malhotra", "7896541230", null),
                        callTime = System.currentTimeMillis() - 7200000, // 2 hours ago
                        callType = CallType.INCOMING,
                        phoneNumber = "7896541230"
                    ), CallLog(
                        id = 108L, callDuration = "10", contact = Contact(
                            "9", "Kavita Sharma", "9517538520", "https://example.com/profile4.jpg"
                        ), callTime = System.currentTimeMillis() - 604800000, // 7 days ago
                        callType = CallType.MISSED, phoneNumber = "9517538520"
                    ), CallLog(
                        id = 109L,
                        callDuration = "25",
                        contact = Contact("10", "Sameer Khan", "7531598524", null),
                        callTime = System.currentTimeMillis() - 14400000, // 4 hours ago
                        callType = CallType.OUTGOING,
                        phoneNumber = "7531598524"
                    )
                ),
                navController = rememberNavController()
            )
        }
    }
}