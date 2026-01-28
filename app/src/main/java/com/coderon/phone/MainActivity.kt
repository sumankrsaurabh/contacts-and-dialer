package com.coderon.phone

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.screens.RequestDefaultDialerScreen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.getDefaultDialerIntent
import com.coderon.phone.utils.isDefaultDialer

class MainActivity : ComponentActivity() {

    private val intentState: MutableState<Intent?> = mutableStateOf(null)

    @SuppressLint("MissingPermission", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        intentState.value = intent

        setContent {
            PhoneTheme {

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
                        MyApp(intentState)
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentState.value = intent
    }
}
