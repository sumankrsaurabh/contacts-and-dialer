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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
   ROOT CONTAINER
------------------------------------------------ */

@Composable
fun ScaffoldScreen(
    navController: NavController,
    showBottomBar: Boolean = true,
    content: @Composable () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {

        Box(modifier = Modifier.fillMaxSize()) {
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
   iOS SEGMENTED NAV BAR (FIXED)
------------------------------------------------ */

@Composable
fun IosSegmentedBottomBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val backStack = navController.currentBackStackEntryAsState().value
    val currentRoute = backStack?.destination?.route ?: Screen.Recent.route


    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            /* ---------- SEGMENTED PILL ---------- */
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
            ) {

                // ✅ BACKGROUND BLUR & SURFACE COLOR
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f))
                )

                // ORIGINAL CONTENT (CLEAR)
                Row(
                    modifier = Modifier.padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        val tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        val itemBg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .clip(RoundedCornerShape(50))
                                .background(itemBg)
                                .padding(vertical = 8.dp)
                                .noRippleClickable {
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.TextHandleMove
                                    )
                                    navController.navigate(item.screen.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
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
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
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
                    .size(56.dp)
                    .clip(CircleShape)
            ) {

                // ✅ BLUR BACKGROUND & M3 CONTAINER COLOR
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                        )
                )

                // CLEAR ICON
                Box(
                    modifier = Modifier
                        .matchParentSize()
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
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
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
   NO RIPPLE CLICK
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

    }
}
