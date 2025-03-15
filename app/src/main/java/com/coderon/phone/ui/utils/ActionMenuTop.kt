package com.coderon.phone.ui.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.R
import com.coderon.phone.ui.Screen

@Composable
fun ActionsMenuTop(
    isContactsScreen: Boolean = false, navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        if (isContactsScreen) {
            IconButton(onClick = { navController.navigate(Screen.AddContact.route) }) {
                Icon(painterResource(R.drawable.plus), "search")
            }
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
            Icon(painterResource(R.drawable.search), "search")
        }
        Spacer(Modifier.width(8.dp))
        IconButton(onClick = {}) {
            Icon(Icons.Default.MoreVert, "search")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    ActionsMenuTop(true, rememberNavController())
}
