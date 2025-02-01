package com.coderon.phone.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.CallViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun MyApp() {
    val navController = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationBar(navController = navController)
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "keypad",
            Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it }, animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it }, animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it }, animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it }, animationSpec = tween(300)
                )
            }

        ) {
            composable("keypad") {
                val callLogViewModel = koinViewModel<CallViewModel>()
                DialerScreen(callLogViewModel::makeCall,{})
            }
            composable("recent") {
                val callLogViewModel: CallLogViewModel = koinViewModel()
                CallLogScreen(
                    callLog = callLogViewModel.callLogs.collectAsStateWithLifecycle().value,
                    filteredCallLogs = callLogViewModel::filteredCallLogs
                )
            }
            composable("contacts") {
                val contactViewModel: ContactViewModel = koinViewModel()
                ContactsScreen(
                    contacts = contactViewModel.contacts.collectAsStateWithLifecycle().value,
                    scrollToLetter = contactViewModel::scrollToLetter
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
    Keypad("keypad", "Keypad", Icons.TwoTone.KeyboardCommandKey), Recent(
        "recent",
        "Recent",
        Icons.TwoTone.AccessTime
    ),
    Contacts("contacts", "Contacts", Icons.TwoTone.Contacts)
}

