package com.coderon.phone.ui.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.coderon.phone.ui.Text

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun CoderonTopAppBar(
    onSearch: () -> Unit = {},
    onMenu: () -> Unit = {},
    showBackArrow: Boolean = false,
    title: String = "Phone",
    onBack: () -> Unit = {},
    isSearchExpanded: Boolean = false, // Control search state
    searchText: String = "",
    onSearchTextChanged: (String) -> Unit = {},
    onDismissSearch: () -> Unit = {},
    showActionsButton: Boolean = true,
) {
    AnimatedContent(
        targetState = isSearchExpanded,
//        transitionSpec = {
//            fadeIn(animationSpec = tween(300)) with slideInHorizontally { it / 2 }
//        },
        label = "TopAppBar Search Animation"
    ) { expanded ->
        if (expanded) {
            // Expanded Search Bar
            SearchScreen(
                query = searchText,
                onQueryChange = onSearchTextChanged,
                onSearch = { onSearch() },
                onDismiss = { onDismissSearch() }
            )
        } else {
            // Default App Bar with Search Icon
            TopAppBar(
                title = {
                    Text(title)
                },
                actions = {
                    if (showActionsButton) {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onMenu) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                        }
                    }
                },
                navigationIcon = {
                    if (showBackArrow) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
                windowInsets = WindowInsets(top = 0.dp)
            )
        }
    }
}
