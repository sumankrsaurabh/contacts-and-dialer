package com.coderon.phone

import android.Manifest
import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.coderon.phone.notifications.CallNotificationManager
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.screens.RequestDefaultDialerScreen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.getDefaultDialerIntent
import com.coderon.phone.utils.isDefaultDialer
import com.coderon.phone.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val intentState: MutableState<Intent?> = mutableStateOf(null)
    private var openedForCall = false

    @SuppressLint("MissingPermission", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        intentState.value = intent
        checkIntent(intent)

        showOnLockscreen()

        setContent {
            val settingsViewModel: SettingsViewModel = koinViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
            val dynamicColor by settingsViewModel.dynamicColor.collectAsStateWithLifecycle()

            PhoneTheme(
                themeMode = themeMode,
                dynamicColor = dynamicColor
            ) {

                val isDefaultDialerState =
                    remember { mutableStateOf(isDefaultDialer(this)) }

                val defaultDialerLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { _ ->
                    isDefaultDialerState.value = isDefaultDialer(this)
                }

                // Runtime Permissions for Notifications, Camera, and Audio
                val permissionsLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { _ ->
                    // Permissions handled
                }

                LaunchedEffect(Unit) {
                    val permissions = mutableListOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CALL_LOG,
                        Manifest.permission.WRITE_CALL_LOG
                    )
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    
                    permissionsLauncher.launch(permissions.toTypedArray())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!isDefaultDialerState.value) {
                        RequestDefaultDialerScreen(
                            onRequestDialerRole = {
                                getDefaultDialerIntent(this@MainActivity)?.let {
                                    defaultDialerLauncher.launch(it)
                                }
                            }
                        )
                    } else {
                        MyApp(
                            intentState = intentState,
                            onFinish = {
                                if (openedForCall) {
                                    finishAndRemoveTask()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentState.value = intent
        checkIntent(intent)
        showOnLockscreen()
    }

    private fun checkIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(CallNotificationManager.EXTRA_SHOW_CALL, false) == true) {
            openedForCall = true
        }
    }

    private fun showOnLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON
            )
        }
    }
}
