@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.coderon.phone.data.model.BlockedNumber
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.navigation.Navigator
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.navigation.rememberNavigationState
import com.coderon.phone.ui.theme.PhoneTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BlockedNumbersScreen(
    navigator: Navigator,
    blockedNumbers: List<BlockedNumber>,
    onBlockNumber: (String) -> Unit,
    onUnblockNumber: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var numberToAdd by remember { mutableStateOf("") }

    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Blocked Numbers", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navigator.goBack() }) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .alpha(entryAlpha.value)
                .offset { IntOffset(0, entryOffset.value.roundToInt()) }
        ) {
            if (blockedNumbers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No blocked numbers", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(blockedNumbers) { blocked ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(blocked.phoneNumber, fontSize = 18.sp)
                            IconButton(onClick = { onUnblockNumber(blocked.phoneNumber) }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Unblock")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Block a number") },
                text = {
                    OutlinedTextField(
                        value = numberToAdd,
                        onValueChange = { numberToAdd = it },
                        label = { Text("Phone Number") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (numberToAdd.isNotBlank()) {
                            onBlockNumber(numberToAdd)
                            numberToAdd = ""
                            showAddDialog = false
                        }
                    }) {
                        Text("Block")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBlockedNumbers() {
    val navState = rememberNavigationState(
        startRoute = Screen.BlockedNumbers,
        topLevelRoutes = setOf(Screen.Keypad, Screen.Recent, Screen.Contacts, Screen.Search)
    )
    val navigator = remember { Navigator(navState) }
    
    PhoneTheme {
        BlockedNumbersScreen(
            navigator = navigator,
            blockedNumbers = listOf(
                BlockedNumber("+1 234 567 8901"),
                BlockedNumber("+1 987 654 3210")
            ),
            onBlockNumber = {},
            onUnblockNumber = {}
        )
    }
}
