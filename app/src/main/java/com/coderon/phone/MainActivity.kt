package com.coderon.phone

import android.Manifest.permission
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.coderon.phone.ui.MyApp
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.utils.DialerUtils.isDefaultDialer
import com.coderon.phone.utils.DialerUtils.setAsDefaultDialer

class MainActivity : ComponentActivity() {
    private val REQUEST_PERMISSIONS = 101
    private val REQUEST_CHANGE_DEFAULT_DIALER = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()  // Request permissions on start

        setContent {
            PhoneTheme {
                if (!isDefaultDialer(context = this)) {
                    setAsDefaultDialer(this)
                }
                enableEdgeToEdge()
                MyApp()
            }
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(
                permission.CALL_PHONE,
                permission.READ_PHONE_STATE,
                permission.ANSWER_PHONE_CALLS,
                permission.READ_CALL_LOG,
                permission.READ_CONTACTS,
                permission.WRITE_CONTACTS,
                permission.READ_PHONE_NUMBERS
            )
        } else {
            arrayOf(
                permission.CALL_PHONE,
                permission.READ_PHONE_STATE,
                permission.READ_CALL_LOG,
                permission.READ_CONTACTS,
                permission.WRITE_CONTACTS,
            )
        }

        ActivityCompat.requestPermissions(this, requiredPermissions, REQUEST_PERMISSIONS)
    }
}
