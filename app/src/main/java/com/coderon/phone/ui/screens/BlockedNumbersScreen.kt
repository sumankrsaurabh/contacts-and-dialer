@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    val colorScheme = MaterialTheme.colorScheme
    val entryAlpha = remember { Animatable(0f) }
    val entryOffset = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        launch { entryAlpha.animateTo(1f, tween(600, easing = LinearEasing)) }
        launch { entryOffset.animateTo(0f, spring(stiffness = Spring.StiffnessLow)) }
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(20.dp)
                        .background(colorScheme.background.copy(alpha = 0.7f))
                )

                LargeTopAppBar(
                    title = {
                        Text(
                            "Blocked Numbers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navigator.goBack() }) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = colorScheme.surfaceContainer.copy(alpha = 0.8f)
                    )
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = colorScheme.primary,
                contentColor = colorScheme.onPrimary,
                shape = CircleShape
            ) {
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
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Block,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No blocked numbers",
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(blockedNumbers) { blocked ->
                        BlockedNumberItem(
                            number = blocked.phoneNumber,
                            onUnblock = { onUnblockNumber(blocked.phoneNumber) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Block a number", fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = numberToAdd,
                        onValueChange = { numberToAdd = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                        Text("Block", fontWeight = FontWeight.Bold, color = colorScheme.primary)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = colorScheme.onSurfaceVariant)
                    }
                },
                shape = RoundedCornerShape(28.dp),
                containerColor = colorScheme.surfaceContainerLow
            )
        }
    }
}

@Composable
private fun BlockedNumberItem(
    number: String,
    onUnblock: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colorScheme.error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Block,
                    contentDescription = null,
                    tint = colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Text(
                text = number,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            IconButton(
                onClick = onUnblock,
                modifier = Modifier.size(40.dp).background(Color.Red.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Unblock", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
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
