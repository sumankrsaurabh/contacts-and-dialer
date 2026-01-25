@file:OptIn(ExperimentalMaterial3Api::class)

package com.coderon.phone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.navigation.Screen
import com.coderon.phone.ui.components.HybridContactRow
import com.coderon.phone.ui.components.Text
import com.coderon.phone.ui.theme.PhoneTheme

@Composable
fun ContactsScreen(
    contacts: Map<Char, List<Contact>>,
    navController: NavController
) {
    val colorScheme = MaterialTheme.colorScheme

    val allContacts = remember(contacts) { contacts.values.flatten() }

    val grouped = remember(allContacts) {
        allContacts
            .sortedBy { it.displayName.lowercase() }
            .groupBy { it.displayName.firstOrNull()?.uppercaseChar() ?: '#' }
            .toSortedMap()
    }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            Box {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .blur(24.dp)
                        .background(colorScheme.background.copy(alpha = 0.65f))
                )

                LargeTopAppBar(
                    title = {
                        Text(text = "Contacts", fontWeight = FontWeight.Bold, fontSize = 32.sp)
                    },
                    navigationIcon = {
                        TextButton(onClick = { /* iOS Groups */ }) {
                            Text(
                                text = "Groups",
                                color = colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.AddContact.route) }) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = "Add",
                                tint = colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = colorScheme.surfaceContainer.copy(alpha = 0.9f)
                    )
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = innerPadding.calculateTopPadding(),
                bottom = 100.dp,
                start = 16.dp,
                end = 16.dp
            )
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                HybridContactRow(
                    name = "Set up My Card",
                    subtitle = "Personal info",
                    isMyCard = true
                ) {}
                Spacer(Modifier.height(24.dp))
            }

            grouped.forEach { (letter, list) ->
                item {
                    Text(
                        text = letter.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )
                }

                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = colorScheme.surfaceContainerLow
                    ) {
                        Column {
                            list.forEachIndexed { index, contact ->
                                HybridContactRow(
                                    name = contact.displayName,
                                    subtitle = contact.phoneNumbers.firstOrNull()?.number,
                                    photoUrl = contact.profilePictureUrl
                                ) {
                                    navController.navigate(
                                        Screen.CallDetails.createRoute(
                                            contact.phoneNumbers.firstOrNull()?.number.orEmpty()
                                        )
                                    )
                                }
                                if (index < list.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 74.dp),
                                        thickness = 0.5.dp,
                                        color = colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHybridContacts() {
    val contacts = listOf(
        Contact("1", "Simple Alpaca", phoneNumbers = listOf(PhoneNumber("123"))),
        Contact("2", "Alice Smith", phoneNumbers = listOf(PhoneNumber("456"))),
        Contact("3", "John Doe", phoneNumbers = listOf(PhoneNumber("789")))
    )

    PhoneTheme {
        ContactsScreen(
            contacts = contacts.groupBy {
                it.displayName.first().uppercaseChar()
            },
            navController = rememberNavController()
        )
    }
}
