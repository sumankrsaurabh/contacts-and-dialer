package com.coderon.phone.ui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.ui.navigation.AppNavHost
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.koinViewModel

/* ------------------------------------------------
   ROOT APP
------------------------------------------------ */
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun MyApp() {

    val navController = rememberNavController()
    val contactViewModel: ContactViewModel = koinViewModel()
    val callLogViewModel: CallLogViewModel = koinViewModel()

    val callUiState = CallManager.uiState.collectAsStateWithLifecycle().value
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    /* ------------------------------------------------
       AUTO NAVIGATION (SMART & FLEXIBLE)
    ------------------------------------------------ */
    val activeCallId = callUiState.primaryCall?.id

    LaunchedEffect(activeCallId) {
        if (activeCallId != null) {
            // Automatically navigate to CallScreen when a new call starts or changes
            // but ONLY if we aren't already there.
            if (currentRoute != Screen.CallScreen.route) {
                navController.navigate(Screen.CallScreen.route) {
                    launchSingleTop = true
                }
            }
        } else {
            // No active calls, ensure we aren't stuck on the CallScreen
            if (currentRoute == Screen.CallScreen.route) {
                navController.popBackStack(
                    route = Screen.Keypad.route,
                    inclusive = false
                )
            }
        }
    }

    /* ------------------------------------------------
       NAV HOST (DELEGATED)
    ------------------------------------------------ */
    AppNavHost(
        navController = navController,
        contactViewModel = contactViewModel,
        callLogViewModel = callLogViewModel
    )
}
