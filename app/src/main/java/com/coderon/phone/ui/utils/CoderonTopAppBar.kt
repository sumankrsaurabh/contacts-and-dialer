package com.coderon.phone.ui.utils

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme

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
            LargeTopAppBar(
                title = {
                    Text(title)
                },
                actions = {
                    if (showActionsButton) {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Rounded.Search, contentDescription = "Search")
                        }
                        /*IconButton(onClick = onMenu) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                        }*/
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

@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun Test() {
    PhoneTheme {
        CoderonTopAppBar(
            onSearch = {},
            onMenu = {},
            showBackArrow = false,
            title = "Phone",
            onBack = {},
            isSearchExpanded = false,
            searchText = "",
            onSearchTextChanged = {},
            onDismissSearch = {},
            showActionsButton = true,
        )
    }
}
@Preview(showBackground = true)
@PreviewLightDark
@Composable
private fun TestSamsung() {
    PhoneTheme {
       SamsungStyleTopAppBar(
           title = "Phone",
           searchText = "",
           isSearchExpanded = false,
           onSearchTextChanged = {},
           onSearchToggle = {}
       )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SamsungStyleTopAppBar(
    title: String,
    searchText: String,
    isSearchExpanded: Boolean,
    onSearchTextChanged: (String) -> Unit,
    onSearchToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (!isSearchExpanded) {
            // **Collapsed Mode** (All elements in a row)
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* Handle menu click */ }) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = onSearchToggle) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        } else {
            // **Expanded Mode** (Title centered, search icon at the bottom)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = searchText,
                    onValueChange = onSearchTextChanged,
                    placeholder = { Text("Search...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                IconButton(onClick = onSearchToggle) {
                    Icon(Icons.Outlined.Search, contentDescription = "Search")
                }
            }
        }
    }
}
