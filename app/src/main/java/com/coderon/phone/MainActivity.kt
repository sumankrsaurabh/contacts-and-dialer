package com.coderon.phone

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telecom.TelecomManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.isDefaultDialer
import com.coderon.phone.utils.requestDefaultDialerRole

class MainActivity : ComponentActivity() {
    private var callType: String? = null
    private val REQUEST_CODE_SET_DEFAULT_DIALER = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        // Ensure app is the default dialer
        if (!isDefaultDialer(this)) {
            requestDefaultDialerRole(this)
        }
//        checkDefaultDialer()
        // Handle call intent
        handleIntent(intent)

        setContent {
            PhoneTheme {
                MainScreen(callType)
            }
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SET_DEFAULT_DIALER -> checkSetDefaultDialerResult(resultCode)
        }
    }

    private fun checkDefaultDialer() {

        val telecomManager = getSystemService(TELECOM_SERVICE) as TelecomManager
        val isAlreadyDefaultDialer = packageName == telecomManager.defaultDialerPackage
        if (isAlreadyDefaultDialer) return

        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
        startActivityForResult(intent, REQUEST_CODE_SET_DEFAULT_DIALER)
    }

    private fun checkSetDefaultDialerResult(resultCode: Int) {
        val message = when (resultCode) {
            RESULT_OK -> "User accepted request to become default dialer"
            RESULT_CANCELED -> "User declined request to become default dialer"
            else -> "Unexpected result code $resultCode"
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setContent {
            PhoneTheme {
                MainScreen(callType)
            }
        }
    }

    private fun handleIntent(intent: Intent?) {
        callType = intent?.getStringExtra("CALL_TYPE")
    }
}

@Composable
fun MainScreen(callType: String?) {
    val context = LocalContext.current
    var permissionsGranted by remember { mutableStateOf(false) }

    val requiredPermissions = listOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.READ_CALL_LOG,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.MANAGE_OWN_CALLS,
        Manifest.permission.READ_PHONE_NUMBERS,
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.all { it.value }
    }

    LaunchedEffect(Unit) {
        val notGrantedPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (notGrantedPermissions.isEmpty()) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(notGrantedPermissions.toTypedArray())
        }
    }

    MyApp()
}
