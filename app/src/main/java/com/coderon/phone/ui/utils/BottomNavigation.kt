package com.coderon.phone.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text

/* ------------------------------------------------
   ROOT CONTAINER (DARK/LIGHT SAFE)
------------------------------------------------ */

@Composable
fun ScaffoldScreen(
    navController: NavController,
    showBottomBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(modifier = Modifier.fillMaxSize()) {

        /* ---------- MAIN CONTENT ---------- */
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .padding(bottom = if (showBottomBar) 96.dp else 0.dp)
        ) {
            content()
        }

        /* ---------- BOTTOM FADE ---------- */
        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isDark)
                                    Color.Black.copy(alpha = 0.9f)
                                else
                                    Color.White.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
        }

        /* ---------- NAV BAR ---------- */
        if (showBottomBar) {
            IosSegmentedBottomBar(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/* ------------------------------------------------
   iOS SEGMENTED NAV BAR (DARK/LIGHT)
------------------------------------------------ */

@Composable
fun IosSegmentedBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val haptic = LocalHapticFeedback.current

    val backStack = navController.currentBackStackEntryAsState().value
    val currentRoute = backStack?.destination?.route ?: Screen.Recent.route

    val iosBlue = Color(0xFF0A84FF)

    val inactive = if (isDark)
        Color.White.copy(alpha = 0.55f)
    else
        Color(0xFF8E8E93)

    val pillBg = if (isDark)
        Color(0xFF1C1C1E).copy(alpha = 0.75f)
    else
        Color.White.copy(alpha = 0.72f)

    val highlight = iosBlue.copy(alpha = if (isDark) 0.22f else 0.12f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            /* ---------- SEGMENTED PILL ---------- */
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(pillBg)
                    .padding(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        val tint = if (selected) iosBlue else inactive

                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) highlight else Color.Transparent)
                                .padding(vertical = 6.dp)
                                .noRippleClickable {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.TextHandleMove
                                    )
                                    navController.navigate(item.screen.route) {
                                        launchSingleTop = true
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    painter = painterResource(item.icon),
                                    contentDescription = item.label,
                                    tint = tint,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tint
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            /* ---------- SEARCH BUTTON ---------- */
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDark) Color(0xFF2C2C2E) else Color.White
                    )
                    .noRippleClickable {
                        haptic.performHapticFeedback(
                            HapticFeedbackType.TextHandleMove
                        )
                        navController.navigate(Screen.Search.route)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.search),
                    contentDescription = "Search",
                    tint = inactive,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/* ------------------------------------------------
   NAV CONFIG
------------------------------------------------ */

private val bottomNavItems = listOf(
    BottomNavigationItem(Screen.Keypad, "Keypad", R.drawable.ic_dialpad),
    BottomNavigationItem(Screen.Recent, "Calls", R.drawable.ic_recent),
    BottomNavigationItem(Screen.Contacts, "Lists", R.drawable.ic_contacts),
)

private data class BottomNavigationItem(
    val screen: Screen,
    val label: String,
    val icon: Int
)

/* ------------------------------------------------
   NO RIPPLE (iOS)
------------------------------------------------ */

private fun Modifier.noRippleClickable(onClick: () -> Unit) =
    clickable(
        interactionSource = MutableInteractionSource(),
        indication = null,
        onClick = onClick
    )

/* ------------------------------------------------
   PREVIEW
------------------------------------------------ */

@Preview(showBackground = true)
@Composable
private fun PerfectIosNavPreview() {
    ScaffoldScreen(
        navController = rememberNavController(),
        showBottomBar = true
    ) {
        LazyColumn {
            items(30) {
                Text(
                    text = "Scrollable content $it",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
