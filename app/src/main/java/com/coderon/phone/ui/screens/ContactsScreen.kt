package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.ui.Screen
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.ActionsMenuTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: Map<Char, List<Contact>>,
    navController: NavController
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF6A82FB),
            Color(0xFFFC5C7D)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Phone",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                ActionsMenuTop(true, navController)
            }

            if (contacts.isEmpty()) {
                NoContactsFoundGlass()
            } else {
                LazyColumn {
                    contacts.forEach { (letter, list) ->
                        item { LetterHeaderGlass(letter) }

                        itemsIndexed(list) { _, contact ->
                            ContactItemGlass(
                                contact = contact,
                                navController = navController
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LetterHeaderGlass(letter: Char) {
    Text(
        text = letter.toString(),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        color = Color.White.copy(alpha = 0.9f)
    )
}

@SuppressLint("MissingPermission")
@Composable
fun ContactItemGlass(
    contact: Contact,
    navController: NavController
) {
    val primaryNumber = contact.phoneNumbers.firstOrNull()?.number ?: ""

    Card(
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.12f)
        ),
        border = BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.25f)
        ),
        onClick = {
            navController.navigate(
                Screen.CallDetails.createRoute(primaryNumber)
            )
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Avatar
            if (!contact.profilePictureUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(contact.profilePictureUrl)
                            .crossfade(true)
                            .build()
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.displayName.firstOrNull()?.toString() ?: "#",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = contact.displayName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = primaryNumber,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun NoContactsFoundGlass() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No contacts found",
            fontSize = 18.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewContactsScreen() {
    val sampleContacts = listOf(
        Contact(
            id = "1",
            displayName = "Alice Johnson",
            phoneNumbers = listOf(PhoneNumber("1234567890", isPrimary = true)),
            profilePictureUrl = null
        ),
        Contact(
            id = "2",
            displayName = "Brian Lee",
            phoneNumbers = listOf(PhoneNumber("9876543210", isPrimary = true)),
            profilePictureUrl = null
        ),
        Contact(
            id = "3",
            displayName = "Charlie Adams",
            phoneNumbers = listOf(PhoneNumber("1112223333", isPrimary = true)),
            profilePictureUrl = "https://example.com/profile.jpg"
        )
    )

    PhoneTheme {
        ContactsScreen(
            contacts = sampleContacts.groupBy {
                it.displayName.first().uppercaseChar()
            },
            navController = rememberNavController()
        )
    }
}
