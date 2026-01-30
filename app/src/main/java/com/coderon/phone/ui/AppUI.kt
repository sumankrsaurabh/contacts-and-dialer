package com.coderon.phone.ui

import android.Manifest
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coderon.phone.call.services.CallManager
import com.coderon.phone.call.ui.CallScreenType
import com.coderon.phone.call.ui.screens.incallui.CallNotificationContent
import com.coderon.phone.notifications.CallNotificationManager
import com.coderon.phone.ui.navigation.AppNavHost
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState

/* ------------------------------------------------
   ROOT APP
------------------------------------------------ */
@RequiresPermission(Manifest.permission.READ_PHONE_STATE)
@Composable
fun MyApp(
    intentState: State<Intent?>,
    onFinish: () -> Unit = {}
) {
    // Navigation 3 State
    val navigationState = rememberNavigationState(
        startRoute = Screen.Keypad,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navigationState) }

    val callUiState by CallManager.uiState.collectAsStateWithLifecycle()

    // Use the actual top of the current stack for route comparisons
    val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull()

    /* ------------------------------------------------
       INTENT HANDLING (NOTIFICATION / RECEIVER)
    ------------------------------------------------ */
    LaunchedEffect(intentState.value) {
        val intent = intentState.value
        if (intent?.getBooleanExtra(CallNotificationManager.EXTRA_SHOW_CALL, false) == true) {
            if (currentRoute != Screen.CallScreen) {
                navigator.navigate(Screen.CallScreen)
            }
        }
    }

    /* ------------------------------------------------
       AUTO NAVIGATION & POPUP LOGIC
    ------------------------------------------------ */
    val hasActiveCall = callUiState.primaryCall != null
    val isIncoming = callUiState.screen == CallScreenType.INCOMING

    LaunchedEffect(hasActiveCall, isIncoming, currentRoute) {
        if (hasActiveCall) {
            // Automatically navigate to CallScreen for non-incoming calls (outgoing/ongoing)
            // For incoming calls, we show a popup if the app is in the foreground
            if (!isIncoming && currentRoute != Screen.CallScreen) {
                navigator.navigate(Screen.CallScreen)
            }
        } else {
            // No active calls, ensure we aren't stuck on the CallScreen
            if (currentRoute == Screen.CallScreen) {
                navigator.goBack()
                onFinish()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        /* -------------------- MAIN NAV HOST -------------------- */
        AppNavHost(navigator = navigator)

        /* -------------------- IN-APP HEADS-UP POPUP -------------------- */
        // Show popup if there is an incoming call and we are NOT on the CallScreen
        AnimatedVisibility(
            visible = isIncoming && currentRoute != Screen.CallScreen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp, start = 8.dp, end = 8.dp)
        ) {
            val call = callUiState.primaryCall
            if (call != null) {
                CallNotificationContent(
                    name = call.displayName ?: "Unknown",
                    phoneNumber = call.phoneNumber,
                    profilePictureUrl = call.profilePictureUrl,
                    status = "Incoming Call",
                    onAccept = { CallManager.accept() },
                    onDecline = { CallManager.reject() },
                    onContentClick = { navigator.navigate(Screen.CallScreen) }
                )
            }
        }
    }
}
