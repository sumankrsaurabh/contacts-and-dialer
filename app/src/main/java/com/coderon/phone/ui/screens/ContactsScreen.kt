package com.coderon.phone.ui.screens

import android.content.Context
import android.telecom.TelecomManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.CoderonTopAppBar
import com.coderon.phone.ui.utils.SimSelectionDialog
import com.coderon.phone.utils.placeCall

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onAddContactClick: () -> Unit,
    onSearchContact: (String) -> Unit,
    navController: NavController
) {
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val filteredContacts = remember(searchText, contacts) {
        contacts.filter { it.name.contains(searchText, ignoreCase = true) }
    }
    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { it.name.first().uppercaseChar() }
    }
    val listState = rememberLazyListState()

    // Handle back press to close search bar
    BackHandler(enabled = isSearchExpanded) {
        isSearchExpanded = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .animateContentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .animateContentSize()
        ) {
            // Animated TopAppBar with Search Expansion
            CoderonTopAppBar(
                onSearch = { isSearchExpanded = true },
                onMenu = {},
                showBackArrow = false,
                title = "Contacts",
                isSearchExpanded = isSearchExpanded,
                searchText = searchText,
                onSearchTextChanged = { searchText = it },
                onDismissSearch = { isSearchExpanded = false })

            if (filteredContacts.isEmpty()) {
                NoContactsFound()
            } else {
                LazyColumn(state = listState) {
                    groupedContacts.forEach { (letter, contacts) ->
                        item { LetterHeader(letter) }
                        itemsIndexed(contacts) { index, contact ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 2 }) {
                                ContactItem(
                                    contact, navController
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun LetterHeader(letter: Char) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent, contentColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = letter.toString(),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    navController: NavController,
    shape: RoundedCornerShape = RoundedCornerShape(32.dp),
    context: Context = LocalContext.current
) {
    val telecomManager = context.getSystemService(TelecomManager::class.java)
    val showSimSelectDialog = remember { mutableStateOf(false) }
    val availableAccounts = telecomManager.callCapablePhoneAccounts
    if (showSimSelectDialog.value) {
        SimSelectionDialog(
            availableAccounts = availableAccounts,
            onDismiss = { showSimSelectDialog.value = false },
            onSimSelected = {
                placeCall(context, contact.phoneNumber, it)
            })
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize()
            .clickable(onClick = { showSimSelectDialog.value = true }), // Smooth resizing
        shape = shape, colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!contact.profilePictureUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current).data(contact.profilePictureUrl)
                            .crossfade(true).build()
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.first().toString(),
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = contact.phoneNumber,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = "Info",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.CallDetails.createRoute(phoneNumber = contact.phoneNumber))
                })
        }
    }
}

@Composable
fun NoContactsFound() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No contacts found", fontSize = 18.sp, color = Color.Gray
        )
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewContactsScreen() {
    val sampleContacts = listOf(
        Contact("1", "Alice Johnson", "1234567890", null),
        Contact("2", "Aaron Brown", "9876543210", null),
        Contact("3", "Alex Carter", "1112223333", "https://example.com/profile1.jpg"),
        Contact("4", "Brian Lee", "4445556666", null),
        Contact("5", "Bella Smith", "7778889999", "https://example.com/profile2.jpg"),
        Contact("6", "Charlie Adams", "0001112222", null),
        Contact("7", "David White", "3334445555", "https://example.com/profile3.jpg"),
        Contact("8", "Emma Davis", "6667778888", null)
    )
    ContactsScreen(sampleContacts, {}, {}, rememberNavController())
}
