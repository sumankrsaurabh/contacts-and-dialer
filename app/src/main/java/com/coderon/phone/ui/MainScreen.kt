package com.coderon.phone.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.viewmodal.ContactViewModal
import kotlinx.coroutines.launch

@Composable
fun ContactApp() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val viewModal: ContactViewModal = viewModel()

    Scaffold(containerColor = Color(0xff12151D), bottomBar = {
        BottomNavigationBar(items = listOf(Screen.Keypad, Screen.Recent, Screen.Contacts),
            navController = navController,
            onTabSelected = { route ->
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                if (currentRoute != route) {  // Prevent redundant navigation
                    coroutineScope.launch {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            })
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Keypad.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Keypad.route) {
                KeypadScreen()
            }
            composable(Screen.Recent.route) {
                DialerScreen(recentCalls = viewModal.recentCallList.collectAsState().value,
                    onRecentCallClick = {

                    })
            }
            composable(Screen.Contacts.route) {
                ContactList(contacts = viewModal.contactList.collectAsState().value,
                    onContactClick = { contact ->
//                        onCallClick(contact.phoneNumber)
                    })
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<Screen>, navController: NavHostController, onTabSelected: (String) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.Transparent,
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route
            NavigationBarItem(
                selected = false,
                onClick = { onTabSelected(screen.route) },
                icon = {
                    Icon(
                        painter = when (screen) {
                            Screen.Keypad -> if (isSelected) painterResource(id = R.drawable.keypad_icon) else painterResource(
                                id = R.drawable.keypad_outline
                            )

                            Screen.Recent -> if (isSelected) painterResource(id = R.drawable.recent) else painterResource(
                                id = R.drawable.recent_outline
                            )

                            Screen.Contacts -> if (isSelected) painterResource(id = R.drawable.contscts) else painterResource(
                                id = R.drawable.contacts_outline
                            )
                        },
                        contentDescription = screen.title,
                        tint = if (isSelected) Color(0xff44AEF9) else Color(0xff8993A2)
                    )
                },
                label = {
                    Text(
                        text = screen.title,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color(0xff44AEF9) else Color(0xff8993A2)
                    )
                },
                alwaysShowLabel = true, // Show the label even when unselected
            )
        }
    }
}

sealed class Screen(val route: String, val title: String) {
    data object Keypad : Screen("keypad", "Keypad")
    data object Recent : Screen("recent", "Recent")
    data object Contacts : Screen("contacts", "Contacts")
}
