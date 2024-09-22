package com.coderon.phone

import android.Manifest.permission
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.ui.CallLogScreen
import com.coderon.phone.ui.ContactsScreen
import com.coderon.phone.ui.DialerScreen
import com.coderon.phone.ui.IncomingCallScreen
import com.coderon.phone.ui.OngoingCallScreen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.CallState
import com.coderon.phone.viewmodel.CallViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {
    private val REQUEST_PERMISSIONS = 101
    private val REQUEST_CHANGE_DEFAULT_DIALER = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()  // Request permissions on start

        setContent {
            PhoneTheme {
                enableEdgeToEdge()
                checkDefaultDialer()
                MyApp()
            }
        }
    }

    private fun checkDefaultDialer() {
        val telecomManager = getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        if (telecomManager.defaultDialerPackage != packageName) {
            // Explain the benefits of setting the app as default dialer (e.g., using a dialog)

            val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
            startActivityForResult(intent, REQUEST_CHANGE_DEFAULT_DIALER) // Use startActivityForResult to handle user response
        }
    }

    // Handle the result in onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHANGE_DEFAULT_DIALER) {
            if (resultCode == Activity.RESULT_OK) {
                // User set the app as default
            } else {
                // User declined - maybe offer to change later in settings
            }
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(
                permission.CALL_PHONE,
                permission.READ_PHONE_STATE,
                permission.ANSWER_PHONE_CALLS
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            // Check if permissions were granted and handle accordingly
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted
            } else {
                // Handle permission denial
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    val callViewModel: CallViewModel = getViewModel()
    val callState by callViewModel.callState.collectAsState()

    Scaffold(bottomBar = {
        BottomNavigationBar(navController = navController)
    }) { innerPadding ->
        when (callState) {
            is CallState.Ringing -> {
                IncomingCallScreen((callState as CallState.Ringing).call, {}, {})
            }
            is CallState.Active -> {
                OngoingCallScreen((callState as CallState.Active).call)
            }
            else -> {
                NavHost(
                    navController = navController,
                    startDestination = "keypad",
                    Modifier.padding(innerPadding)
                ) {
                    composable("keypad") { DialerScreen() }
                    composable("recent") {
                        val callLogViewModel: CallLogViewModel = getViewModel()
                        CallLogScreen(callLogViewModel)
                    }
                    composable("contacts") {
                        val contactViewModel: ContactViewModel = getViewModel()
                        ContactsScreen(contactViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        NavigationBarItem(
            label = { Text("Keypad", fontFamily = FontFamily(Font(R.font.regular))) },
            selected = currentRoute == "keypad",
            onClick = { navController.navigate("keypad") },
            icon = { Icon(imageVector = Icons.TwoTone.KeyboardCommandKey, contentDescription = null) }
        )
        NavigationBarItem(
            label = { Text("Recent", fontFamily = FontFamily(Font(R.font.regular))) },
            selected = currentRoute == "recent",
            onClick = { navController.navigate("recent") },
            icon = { Icon(imageVector = Icons.TwoTone.AccessTime, contentDescription = null) }
        )
        NavigationBarItem(
            label = { Text("Contacts", fontFamily = FontFamily(Font(R.font.regular))) },
            selected = currentRoute == "contacts",
            onClick = { navController.navigate("contacts") },
            icon = { Icon(imageVector = Icons.TwoTone.Contacts, contentDescription = null) }
        )
    }
}
