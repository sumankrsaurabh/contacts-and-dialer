package com.coderon.phone

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.PhoneAppManager.requestDefaultPhoneApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestDefaultPhoneApp(this)
        setContent {
            PhoneTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    val requiredPermissions = remember {
        mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                add(Manifest.permission.MANAGE_OWN_CALLS)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.all { it.value }
    }

    LaunchedEffect(Unit) {
        val notGrantedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }
        if (notGrantedPermissions.isEmpty()) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(notGrantedPermissions.toTypedArray())
        }
    }
    if (permissionsGranted) {
        MyApp()
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
