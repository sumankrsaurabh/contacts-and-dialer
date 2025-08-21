package com.coderon.phone.ui.screens

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import com.coderon.phone.ui.Text
import com.coderon.phone.ui.theme.PhoneTheme
import com.coderon.phone.ui.utils.ActionsMenuTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: Map<Char, List<Contact>>, navController: NavController
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Phone", fontSize = 24.sp)
            ActionsMenuTop(true, navController)
        }
        if (contacts.isEmpty()) {
            NoContactsFound()
        } else {
            LazyColumn {
                contacts.forEach { (letter, contacts) ->
                    item { LetterHeader(letter) }
                    itemsIndexed(contacts) { index, contact ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 2 }) {
                            ContactItem(
                                contact = contact
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}


@Composable
fun LetterHeader(letter: Char) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.dp, horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = letter.toString(),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(8.dp)
        )
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ContactItem(
    contact: Contact
) {
    Card(
        shape = RoundedCornerShape(50), modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                        fontSize = 24.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .animateContentSize()
            ) {
                Text(
                    text = contact.name,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = contact.phoneNumber, fontSize = 14.sp, color = Color.Gray
                )

            }
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
    PhoneTheme {
        ContactsScreen(sampleContacts.groupBy { it.name.first() }, rememberNavController())
    }
}