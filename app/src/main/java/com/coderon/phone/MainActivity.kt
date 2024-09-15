package com.coderon.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.coderon.phone.ui.ContactApp
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.viewmodal.ContactViewModal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            enableEdgeToEdge()
            PhoneTheme {
                val viewmodel: ContactViewModal = viewModel()
                val context = LocalContext.current
                RequestPermissions {
                    viewmodel.getContactList(context)
                    viewmodel.getRecentCalls(context)
                }
                LaunchedEffect(Unit) {
                    viewmodel.getContactList(context)
                    viewmodel.getRecentCalls(context)
                }
                ContactApp()
            }
        }
    }
}