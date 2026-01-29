package com.coderon.phone.ui.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.coderon.phone.R
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState

@Composable
fun ActionsMenuTop(
    isContactsScreen: Boolean = false, 
    navigator: Navigator
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (isContactsScreen) {
            GlassyIconButton(onClick = { navigator.navigate(Screen.AddContact()) }) {
                Icon(
                    painterResource(R.drawable.plus),
                    contentDescription = "Add Contact",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(16.dp))
        }
        GlassyIconButton(onClick = { navigator.navigate(Screen.Search) }) {
            Icon(
                painterResource(R.drawable.search),
                contentDescription = "Search",
                tint = Color.White
            )
        }
        Spacer(Modifier.width(16.dp))
        GlassyIconButton(onClick = { /*TODO*/ }) {
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassyIconButton(
    onClick: () -> Unit,
    cardModifier: Modifier = Modifier.size(48.dp),
    content: @Composable () -> Unit
) {

    Card(
        onClick = onClick,
        shape = CircleShape,
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

//@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun SearchBarPreview() {
    val navState = rememberNavigationState(
        startRoute = Screen.Keypad,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = Navigator(navState)
    Box {
        ActionsMenuTop(true, navigator)
    }
}
