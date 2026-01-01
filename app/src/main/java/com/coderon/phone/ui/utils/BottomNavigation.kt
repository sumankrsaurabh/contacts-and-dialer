package com.coderon.phone.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
   ROOT CONTAINER (NO SCAFFOLD = NO CRASH)
------------------------------------------------ */

@Composable
fun ScaffoldScreen(
    navController: NavController,
    showBottomBar: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {

        // Main content scrolls UNDER nav
        Box(
            modifier = Modifier
                .fillMaxSize()
//                .padding(bottom = if (showBottomBar) 32.dp else 0.dp)
        ) {
            content()
        }

        if (showBottomBar) {
            IosSegmentedBottomBar(
                navController = navController,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

/* ------------------------------------------------
   iOS SEGMENTED NAV BAR (FINAL)
------------------------------------------------ */

@Composable
fun IosSegmentedBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Screen.Recent.route

    bottomNavItems.indexOfFirst { it.screen.route == currentRoute }
        .coerceAtLeast(0)

    val iosBlue = Color(0xFF0A84FF)
    val inactive = Color(0xFF8E8E93)
    val highlight = iosBlue.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            /* ---------- SEGMENTED PILL ---------- */

            Box(
                modifier = Modifier
                    .height(56.dp)
                    .width(((bottomNavItems.size * 72) + ((bottomNavItems.size - 1) * 12)).dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.72f))
            ) {

//                /* ---- INDICATOR (BOTTOM LAYER) ---- */
//                Box(
//                    modifier = Modifier
//                        .offset(x = indicatorOffset)
//                        .padding(6.dp)
//                        .size(width = 60.dp, height = 44.dp)
//                        .clip(RoundedCornerShape(22.dp))
//                        .background(Color.White)
//                )

                /* ---- TABS (TOP LAYER) ---- */
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        val tint = if (selected) iosBlue else inactive

                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (selected) highlight else Color.Transparent
                                )
                                .padding(4.dp)
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
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
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

            /* ---------- FLOATING SEARCH ---------- */

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
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
    BottomNavigationItem(Screen.Recent, "Calls", R.drawable.ic_recent),
    BottomNavigationItem(Screen.Contacts, "Lists", R.drawable.ic_contacts),
    BottomNavigationItem(Screen.Keypad, "Keypad", R.drawable.ic_dialpad)
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
   PREVIEW (BULLETPROOF)
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
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
