package com.coderon.phone

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.screens.RequestDefaultDialerScreen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.getDefaultDialerIntent
import com.coderon.phone.utils.isDefaultDialer

class MainActivity : ComponentActivity() {

    @SuppressLint("MissingPermission", "SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            PhoneTheme {

                val isDefaultDialerState =
                    remember { mutableStateOf(isDefaultDialer(this)) }

                val defaultDialerLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.StartActivityForResult()
                ) { _ ->
                    isDefaultDialerState.value = isDefaultDialer(this)
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
                        MyApp()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
