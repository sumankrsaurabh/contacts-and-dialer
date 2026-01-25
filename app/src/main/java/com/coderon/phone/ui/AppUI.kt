package com.coderon.phone.ui

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
       NAV HOST (DELEGATED)
    ------------------------------------------------ */
    AppNavHost(
        navController = navController,
        contactViewModel = contactViewModel,
        callLogViewModel = callLogViewModel
    )
}
