package com.coderon.phone.ui.utils

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text

/* -------------------- SCAFFOLD -------------------- */

@Composable
fun ScaffoldScreen(
    navController: NavController,
    content: @Composable () -> Unit
) {
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = { GlassBottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            content()
        }
    }
}

/* -------------------- GLASS BOTTOM NAV (BLUR FIXED) -------------------- */

@Composable
fun GlassBottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxWidth()) {

        // 🔹 BACKDROP BLUR LAYER (only this is blurred)
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(
                    radiusX = 35.dp,
                    radiusY = 35.dp,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
                .background(
                    Color.White.copy(
                        alpha = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) 0.08f else 0.12f
                    )
                )
        )

        // 🔹 CONTENT LAYER (NOT blurred)
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            bottomNavigationItems.forEach { item ->
                val selected = currentRoute == item.screen.route

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (selected)
                                Color.White.copy(alpha = 0.28f)
                            else
                                Color.Transparent
                        )
                        .clickable {
                            navController.navigate(item.screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = false
                                }
                                launchSingleTop = true
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) item.selectedIcon else item.icon,
                            contentDescription = item.label,
                            tint = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/* -------------------- NAV ITEMS -------------------- */

val bottomNavigationItems = listOf(
    BottomNavigationItem(
        Screen.Keypad,
        "KEYPAD",
        Icons.Outlined.Dialpad,
        Icons.Filled.Dialpad
    ),
    BottomNavigationItem(
        Screen.Recent,
        "RECENT",
        Icons.Outlined.AccessTime,
        Icons.Filled.AccessTime
    ),
    BottomNavigationItem(
        Screen.Contacts,
        "CONTACT",
        Icons.Outlined.People,
        Icons.Filled.People
    )
)

data class BottomNavigationItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
)

/* -------------------- PREVIEW -------------------- */

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun GlassBottomNavPreview() {
    GlassBottomNavigationBar(rememberNavController())
}
