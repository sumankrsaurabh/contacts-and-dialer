package com.coderon.phone.ui.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text


/**
 * Wrapper to apply Scaffold only to certain screens
 */
@Composable
fun ScaffoldScreen(navController: NavController, content: @Composable () -> Unit) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = if (isSystemInDarkTheme()) Color.Black else Color.White,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
//                .clip(RoundedCornerShape(32.dp))
        ) {
            content()
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route
    val isDarkTheme = isSystemInDarkTheme()

    val backgroundColor = if (isDarkTheme) Color.Black else Color.White
    val selectedColor = if (isDarkTheme) Color.White else Color.Black
    val unselectedColor = if (isDarkTheme) Color.LightGray else Color.Gray

    NavigationBar(containerColor = backgroundColor) {
        bottomNavigationItems.forEach { item ->
            val selected = currentRoute == item.screen.route
            Column(
                Modifier
                    .weight(1f)
                    .clickable {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (selected) item.selectedIcon else item.icon,
                    contentDescription = item.label,
                    tint = if (selected) selectedColor else unselectedColor
                )
                Text(
                    text = item.label,
                    fontSize = 10.sp,
                    color = if (selected) selectedColor else unselectedColor
                )
            }
        }
    }
}


val bottomNavigationItems = listOf(
    BottomNavigationItem(Screen.Keypad, "Keypad", Icons.Outlined.Dialpad, Icons.Filled.Dialpad),
    BottomNavigationItem(
        Screen.Recent,
        "Recent",
        Icons.Outlined.AccessTime,
        Icons.Filled.AccessTime
    ),
    BottomNavigationItem(Screen.Contacts, "Contacts", Icons.Outlined.People, Icons.Filled.People)
)

data class BottomNavigationItem(
    val screen: Screen, val label: String, val icon: ImageVector, val selectedIcon: ImageVector
)


@Preview
@Composable
private fun TestOnly() {
    BottomNavigationBar(rememberNavController())
}