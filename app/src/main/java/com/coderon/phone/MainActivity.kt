package com.coderon.phone

import android.Manifest
import android.app.Activity
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.coderon.phone.call.CallManager
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.isDefaultDialer
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneTheme {
                MainScreen(this@MainActivity)
            }
        }

        val dialerRoleRequest = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            Log.d(
                "Change Default Dialer Request",
                "dialerRoleRequest succeeded: ${it.resultCode == Activity.RESULT_OK}"
            )
        }
        val roleManager = getSystemService(RoleManager::class.java)

        if (roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        )
            dialerRoleRequest.launch(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
        else Log.d("Change Default Dialer Request", "dialerRoleRequest failed")
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(activity: MainActivity) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }
    var isDefaultDialer by remember { mutableStateOf(context.isDefaultDialer()) }

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
        // Ensure the app is the default dialer
        if (!isDefaultDialer) {
//            activity.launchSetDefaultDialerIntent()

            isDefaultDialer = context.isDefaultDialer()
        }

        // Check and request permissions
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

    MyApp()
}
