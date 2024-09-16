package com.coderon.phone

import android.os.Bundle
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.ui.CallLogScreen
import com.coderon.phone.ui.ContactsScreen
import com.coderon.phone.ui.DialerScreen
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.viewmodel.CallLogViewModel
import com.coderon.phone.viewmodel.ContactViewModel
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PhoneTheme {
                enableEdgeToEdge()
                MyApp()
            }
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationBar(navController = navController)
    }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "keypad",
            Modifier.padding(innerPadding)
        ) {
            composable("keypad") {
                DialerScreen()
            }
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

@Composable
fun BottomNavigationBar(navController: NavController) {
    NavigationBar() {
        NavigationBarItem(label = { Text("Keypad", fontFamily = FontFamily(Font(R.font.regular))) },
            selected = false, // Update based on selected route
            onClick = { navController.navigate("keypad") },
            icon = {
                Icon(imageVector = Icons.TwoTone.KeyboardCommandKey, contentDescription = null)
            })
        NavigationBarItem(label = { Text("Recent", fontFamily = FontFamily(Font(R.font.regular))) },
            selected = false, // Update based on selected route
            onClick = { navController.navigate("recent") },
            icon = {
                Icon(imageVector = Icons.TwoTone.AccessTime, contentDescription = null)
            })
        NavigationBarItem(label = {
            Text(
                "Contacts", fontFamily = FontFamily(Font(R.font.regular))
            )
        }, selected = false, // Update based on selected route
            onClick = { navController.navigate("contacts") }, icon = {
                Icon(imageVector = Icons.TwoTone.Contacts, contentDescription = null)
            })
    }
}
