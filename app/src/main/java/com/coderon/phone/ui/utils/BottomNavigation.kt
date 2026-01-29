package com.coderon.phone.ui.utils

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.coderon.phone.R
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState

/* ------------------------------------------------
   BOTTOM NAV VISIBILITY CONTROL
------------------------------------------------ */

val LocalBottomNavVisible = compositionLocalOf<MutableState<Boolean>> {
    mutableStateOf(true)
}

/* ------------------------------------------------
   ROOT CONTAINER
------------------------------------------------ */

@Composable
fun ScaffoldScreen(
    navigator: Navigator,
    showBottomBar: Boolean = true,
    content: @Composable () -> Unit
) {
    val bottomNavVisible = remember { mutableStateOf(true) }

    CompositionLocalProvider(LocalBottomNavVisible provides bottomNavVisible) {
        Box(modifier = Modifier.fillMaxSize()) {

            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }

            if (showBottomBar && bottomNavVisible.value) {
                // Gradient fade to soften the area behind the floating dock
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                IosSegmentedBottomBar(
                    navigator = navigator,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}

/* ------------------------------------------------
   HYBRID BOTTOM DOCK (iOS + OneUI 8 + M3)
------------------------------------------------ */

@Composable
fun IosSegmentedBottomBar(
    navigator: Navigator,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val currentRoute = navigator.state.topLevelRoute

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .height(72.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(36.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.92f),
            shadowElevation = 12.dp,
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.screen
                    val tint = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }

                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(26.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.35f
                                )
                                else Color.Transparent
                            )
                            .noRippleClickable {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navigator.navigate(item.screen)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier.size(24.dp)
                            )
                            if (isSelected) {
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tint
                                )
                            }
                        }
                    }
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
    BottomNavigationItem(Screen.Recent, "Recents", R.drawable.ic_recent),
    BottomNavigationItem(Screen.Contacts, "Contacts", R.drawable.ic_contacts),
    BottomNavigationItem(Screen.Search, "Search", R.drawable.search),
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
    val navState = rememberNavigationState(
        startRoute = Screen.Keypad,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }
    
    ScaffoldScreen(
        navigator = navigator,
        showBottomBar = true
    ) {

    }
}
