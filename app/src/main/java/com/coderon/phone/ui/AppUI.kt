package com.coderon.phone.ui

import androidx.annotation.RequiresPermission
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.AccessTime
import androidx.compose.material.icons.twotone.Contacts
import androidx.compose.material.icons.twotone.Dialpad
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.services.NoCall
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.screens.AddContactScreen
import com.coderon.phone.ui.screens.CallLogScreen
import com.coderon.phone.ui.screens.ContactDetailsScreen
import com.coderon.phone.ui.screens.ContactsScreen
import com.coderon.phone.ui.screens.DialerScreen
import com.coderon.phone.ui.screens.incallui.CallScreen
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.koinViewModel

sealed class Screen(val route: String) {
    object Keypad : Screen("keypad")
    object Recent : Screen("recent")
    object Contacts : Screen("contacts")
    object AddContact : Screen("add_contact")
    object CallDetails : Screen("contact_details/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "contact_details/$phoneNumber"
    }

    object CallScreen : Screen("call_screen")
}

@RequiresPermission(android.Manifest.permission.READ_PHONE_STATE)
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
                ScaffoldScreen(navController) { DialerScreen() }
            }
        }
        composable(Screen.Recent.route) {
            if (currentCallState == NoCall) {
                val callLogs = callLogViewModel.callLogs.collectAsStateWithLifecycle().value
                ScaffoldScreen(navController) {
                    CallLogScreen(
                        callLog = callLogs,
                        filteredCallLogs = callLogViewModel::filteredCallLogs,
                        navController = navController,
                        onSearchContact = callLogViewModel::filteredCallLogs
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
                        onAddContactClick = { navController.navigate(Screen.AddContact.route) },
                        onSearchContact = contactViewModel::filteredContacts,
                        navController
                    )
                }
            }
        }

        // Screens outside Scaffold (Call Screen, Add Contact, Contact Details)
        composable(Screen.CallScreen.route) {
            CallScreen(navController)
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
        containerColor = if (isSystemInDarkTheme()) Color.Black.copy(.8f)else Color.White.copy(.8f),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(32.dp))
        ) {
            content()
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val fontFamily = FontFamily(Font(R.font.regular))

    NavigationBar(containerColor = Color.Transparent) {
        bottomNavigationItems.forEach { item ->
            NavigationBarItem(
                label = { Text(text = item.label, fontFamily = fontFamily) },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(imageVector = item.icon, contentDescription = item.label) })
        }
    }
}

val bottomNavigationItems = listOf(
    BottomNavigationItem(Screen.Keypad, "Keypad", Icons.TwoTone.Dialpad),
    BottomNavigationItem(Screen.Recent, "Recent", Icons.TwoTone.AccessTime),
    BottomNavigationItem(Screen.Contacts, "Contacts", Icons.TwoTone.Contacts)
)

data class BottomNavigationItem(
    val screen: Screen, val label: String, val icon: ImageVector
)
