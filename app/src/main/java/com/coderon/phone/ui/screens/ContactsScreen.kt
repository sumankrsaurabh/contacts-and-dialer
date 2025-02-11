package com.coderon.phone.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.model.Contact
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.utils.SearchScreen

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onAddContactClick: () -> Unit,
    onSearchContact: (String) -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    val filteredContacts = remember(searchText, contacts) {
        contacts.filter { it.name.contains(searchText, ignoreCase = true) }
    }
    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { it.name.first().uppercaseChar() }
    }
    val listState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchScreen(
                query = searchText,
                onQueryChange = { searchText = it },
                onSearch = { onSearchContact(searchText) }
            )

            if (filteredContacts.isEmpty()) {
                NoContactsFound()
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.padding(bottom = 80.dp) // Prevent FAB from overlapping
                ) {
                    groupedContacts.forEach { (letter, contacts) ->
                        item { LetterHeader(letter) }
                        itemsIndexed(contacts) { index, contact ->
                            ContactItem(
                                contact,
                                shape = when {
                                    contacts.size == 1 -> RoundedCornerShape(24.dp) // Single contact
                                    index == 0 -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                                    index == contacts.lastIndex -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                                    else -> RoundedCornerShape(0.dp)
                                }
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddContactClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
        }
    }
}

@Composable
fun LetterHeader(letter: Char) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = letter.toString(),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    shape: RoundedCornerShape = RoundedCornerShape(2.dp)
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 1.dp),
        shape = shape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!contact.profilePictureUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(contact.profilePictureUrl)
                            .crossfade(true)
                            .build()
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

//            IconButton(onClick = { /* Navigate to contact details */ }) {
                Icon(Icons.Outlined.Info, contentDescription = "Info")
//            }
        }
    }
}

@Composable
fun NoContactsFound() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No contacts found",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
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
    ContactsScreen(sampleContacts, {}, {})
}
